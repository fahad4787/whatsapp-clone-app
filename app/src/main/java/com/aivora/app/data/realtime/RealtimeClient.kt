package com.aivora.app.data.realtime

import com.aivora.app.BuildConfig
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.MessageDto
import com.aivora.app.data.toLocalMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Shared OkHttp WebSocket client for Wave 5 realtime.
 * Connect: wss://…/ws?token=…
 */
object RealtimeClient {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val socketRef = AtomicReference<WebSocket?>(null)
    private var reconnectJob: Job? = null
    private var tokenProvider: (() -> String?)? = null
    private var myUserIdProvider: (() -> String?)? = null

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    private val _typing = MutableSharedFlow<TypingData>(extraBufferCapacity = 8)
    val typing: SharedFlow<TypingData> = _typing.asSharedFlow()

    private val _presence = MutableSharedFlow<PresenceData>(extraBufferCapacity = 8)
    val presence: SharedFlow<PresenceData> = _presence.asSharedFlow()

    private val joinedRooms = mutableSetOf<String>()
    private val client: OkHttpClient = OkHttpClient.Builder()
        .pingInterval(25, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    fun start(tokenProvider: () -> String?, myUserIdProvider: () -> String?) {
        this.tokenProvider = tokenProvider
        this.myUserIdProvider = myUserIdProvider
        connect()
    }

    fun startFromSession(context: android.content.Context) {
        start(
            tokenProvider = { SessionStore.accessToken(context) },
            myUserIdProvider = { SessionStore.userId(context) },
        )
    }

    fun stop() {
        reconnectJob?.cancel()
        reconnectJob = null
        socketRef.getAndSet(null)?.close(1000, "stop")
        _connected.value = false
        joinedRooms.clear()
    }

    fun join(conversationId: String) {
        if (conversationId.isBlank()) return
        synchronized(joinedRooms) { joinedRooms.add(conversationId) }
        send("join", buildJsonObject { put("conversation_id", conversationId) })
    }

    fun leave(conversationId: String) {
        if (conversationId.isBlank()) return
        synchronized(joinedRooms) { joinedRooms.remove(conversationId) }
        send("leave", buildJsonObject { put("conversation_id", conversationId) })
    }

    fun setTyping(conversationId: String, typing: Boolean) {
        send(
            "typing",
            buildJsonObject {
                put("conversation_id", conversationId)
                put("typing", typing)
            },
        )
    }

    private fun connect() {
        val token = tokenProvider?.invoke() ?: return
        if (token.isBlank()) return
        socketRef.getAndSet(null)?.close(1000, "reconnect")

        val base = BuildConfig.API_BASE_URL.trimEnd('/')
        val wsBase = when {
            base.startsWith("https://") -> "wss://" + base.removePrefix("https://")
            base.startsWith("http://") -> "ws://" + base.removePrefix("http://")
            else -> base
        }
        val url = "$wsBase/ws?token=${java.net.URLEncoder.encode(token, Charsets.UTF_8.name())}"
        val req = Request.Builder().url(url).build()
        socketRef.set(
            client.newWebSocket(
                req,
                object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        _connected.value = true
                        // Re-join rooms after reconnect
                        val rooms = synchronized(joinedRooms) { joinedRooms.toList() }
                        rooms.forEach { id ->
                            send("join", buildJsonObject { put("conversation_id", id) })
                        }
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
                        handleFrame(text)
                    }

                    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                        webSocket.close(code, reason)
                    }

                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                        _connected.value = false
                        scheduleReconnect()
                    }

                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        _connected.value = false
                        scheduleReconnect()
                    }
                },
            ),
        )
    }

    private fun scheduleReconnect() {
        if (reconnectJob?.isActive == true) return
        reconnectJob = scope.launch {
            delay(2_000)
            if (isActive && tokenProvider?.invoke()?.isNotBlank() == true) {
                connect()
            }
        }
    }

    private fun send(type: String, data: JsonObject?) {
        val ws = socketRef.get() ?: return
        val frame = ClientFrame(type = type, data = data)
        val json = NetworkModule.json.encodeToString(ClientFrame.serializer(), frame)
        ws.send(json)
    }

    private fun handleFrame(text: String) {
        val frame = runCatching {
            NetworkModule.json.decodeFromString(ServerFrame.serializer(), text)
        }.getOrNull() ?: return

        when (frame.type) {
            "message" -> {
                val data = frame.data ?: return
                val dto = runCatching {
                    NetworkModule.json.decodeFromJsonElement(MessageDto.serializer(), data)
                }.getOrNull() ?: return
                val chatId = dto.conversationId ?: return
                val uid = myUserIdProvider?.invoke() ?: return
                val local = dto.toLocalMessage(uid)
                InMemoryChatStore.upsertMessage(chatId, local)
                // Heads-up while app is open on home (FCM alone often won't show in foreground).
                if (!local.mine && !InMemoryChatStore.isViewing(chatId)) {
                    val app = runCatching { com.aivora.app.AivoraApp.instance }.getOrNull()
                    if (app != null) {
                        val preview = when {
                            !local.mediaUri.isNullOrBlank() -> "Photo"
                            local.body.isNotBlank() -> local.body
                            else -> "New message"
                        }
                        val title = InMemoryChatStore.chat(chatId).title.ifBlank { "Aivora" }
                        com.aivora.app.data.push.MessageNotifier.show(app, title, preview, chatId)
                    }
                }
            }
            "typing" -> {
                val obj = frame.data?.jsonObject ?: return
                _typing.tryEmit(
                    TypingData(
                        conversationId = obj.string("conversation_id"),
                        typing = obj.boolean("typing") ?: false,
                    ),
                )
            }
            "presence" -> {
                val obj = frame.data?.jsonObject ?: return
                _presence.tryEmit(
                    PresenceData(
                        userId = obj.string("user_id"),
                        online = obj.boolean("online") ?: false,
                        lastSeenAt = obj.string("last_seen_at"),
                    ),
                )
            }
        }
    }

    private fun JsonObject.string(key: String): String? =
        (this[key] as? JsonPrimitive)?.contentOrNull

    private fun JsonObject.boolean(key: String): Boolean? =
        (this[key] as? JsonPrimitive)?.booleanOrNull
}
