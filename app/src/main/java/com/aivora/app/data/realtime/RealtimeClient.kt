package com.aivora.app.data.realtime

import com.aivora.app.BuildConfig
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.call.ActiveCallSession
import com.aivora.app.data.call.CallLog
import com.aivora.app.data.call.LiveKitCallController
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.local.MessageDeliveryStatus
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.CallDto
import com.aivora.app.data.model.MessageDto
import com.aivora.app.data.toLocalMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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
    /** When true, onClosed must not auto-reconnect (app backgrounded). */
    @Volatile private var suppressReconnect = false
    /** Token used for the open (or in-flight) socket — avoid thrashing reconnects on cold start. */
    @Volatile private var socketToken: String? = null

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    private val _typing = MutableSharedFlow<TypingData>(extraBufferCapacity = 8)
    val typing: SharedFlow<TypingData> = _typing.asSharedFlow()

    private val _presence = MutableSharedFlow<PresenceData>(extraBufferCapacity = 8)
    val presence: SharedFlow<PresenceData> = _presence.asSharedFlow()

    /** userId → online in any currently tracked chat room. */
    private val _onlineUsers = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val onlineUsers: StateFlow<Map<String, Boolean>> = _onlineUsers.asStateFlow()

    /** conversationId → set of online peer user ids (chat open in that room). */
    private val _roomOnline = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val roomOnline: StateFlow<Map<String, Set<String>>> = _roomOnline.asStateFlow()

    private val joinedRooms = mutableSetOf<String>()
    private val client: OkHttpClient = OkHttpClient.Builder()
        .pingInterval(25, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    fun start(tokenProvider: () -> String?, myUserIdProvider: () -> String?) {
        this.tokenProvider = tokenProvider
        this.myUserIdProvider = myUserIdProvider
        suppressReconnect = false
        connectIfNeeded()
    }

    fun startFromSession(context: android.content.Context) {
        val app = context.applicationContext
        start(
            tokenProvider = { SessionStore.accessToken(app) },
            myUserIdProvider = { SessionStore.userId(app) },
        )
    }

    fun stop() {
        suppressReconnect = true
        reconnectJob?.cancel()
        reconnectJob = null
        socketToken = null
        socketRef.getAndSet(null)?.close(1000, "stop")
        _connected.value = false
        // Keep joinedRooms so we re-join after returning to foreground.
        _onlineUsers.value = emptyMap()
        _roomOnline.value = emptyMap()
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
        // Leaving the screen does not clear app-level onlineUsers.
        _roomOnline.value = _roomOnline.value.toMutableMap().also { it.remove(conversationId) }
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

    /**
     * Idempotent connect: Home + PresenceLifecycle + Chat all call start on cold open.
     * Previously each call closed the socket mid-handshake → missed call.incoming until chat open.
     */
    private fun connectIfNeeded() {
        val token = tokenProvider?.invoke() ?: return
        if (token.isBlank()) return
        // Same token already connected or still handshaking — keep it.
        if (socketRef.get() != null && socketToken == token) return
        // Token refreshed or no socket — (re)connect.
        connect(token)
    }

    private fun connect(token: String) {
        if (token.isBlank()) return
        socketToken = token
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
                        if (socketRef.get() !== webSocket) return
                        socketToken = token
                        _connected.value = true
                        // Re-join rooms after reconnect
                        val rooms = synchronized(joinedRooms) { joinedRooms.toList() }
                        rooms.forEach { id ->
                            send("join", buildJsonObject { put("conversation_id", id) })
                        }
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
                        if (socketRef.get() !== webSocket) return
                        handleFrame(text)
                    }

                    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                        webSocket.close(code, reason)
                    }

                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                        if (socketRef.compareAndSet(webSocket, null)) {
                            if (socketToken == token) socketToken = null
                            _connected.value = false
                            if (!suppressReconnect) scheduleReconnect()
                        }
                    }

                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        if (socketRef.compareAndSet(webSocket, null)) {
                            if (socketToken == token) socketToken = null
                            _connected.value = false
                            if (!suppressReconnect) scheduleReconnect()
                        }
                    }
                },
            ),
        )
    }

    private fun scheduleReconnect() {
        if (suppressReconnect) return
        if (reconnectJob?.isActive == true) return
        reconnectJob = scope.launch {
            delay(1_000)
            if (isActive && !suppressReconnect && tokenProvider?.invoke()?.isNotBlank() == true) {
                connectIfNeeded()
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
            "message", "message_deleted" -> {
                val data = frame.data ?: return
                val dto = runCatching {
                    NetworkModule.json.decodeFromJsonElement(MessageDto.serializer(), data)
                }.getOrNull() ?: return
                val chatId = dto.conversationId ?: return
                val uid = myUserIdProvider?.invoke() ?: return
                val local = dto.toLocalMessage(uid)
                InMemoryChatStore.upsertMessage(chatId, local)
                val app = runCatching { com.aivora.app.AivoraApp.instance }.getOrNull()
                // Debounced IO persist — never block the WS reader on Mate-class devices.
                if (app != null) InMemoryChatStore.persistMessages(app, chatId)
                if (!local.mine && !local.deletedForEveryone) {
                    ackDelivered(listOfNotNull(dto.id))
                    if (!InMemoryChatStore.isViewing(chatId) && app != null) {
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
            "receipt" -> {
                val obj = frame.data?.jsonObject ?: return
                val chatId = obj.string("conversation_id") ?: return
                val status = when (obj.string("status")?.lowercase()) {
                    "read" -> MessageDeliveryStatus.Read
                    "delivered" -> MessageDeliveryStatus.Delivered
                    else -> return
                }
                val ids = mutableListOf<String>()
                obj["message_ids"]?.jsonArray?.forEach { el ->
                    el.jsonPrimitive.contentOrNull?.let { ids.add(it) }
                }
                obj.string("message_id")?.let { if (it.isNotBlank()) ids.add(it) }
                InMemoryChatStore.applyReceipt(chatId, ids.distinct(), status)
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
                val userId = obj.string("user_id") ?: return
                val online = obj.boolean("online") ?: false
                val conversationId = obj.string("conversation_id")
                val event = PresenceData(
                    userId = userId,
                    conversationId = conversationId,
                    online = online,
                    lastSeenAt = obj.string("last_seen_at"),
                )
                _presence.tryEmit(event)
                _onlineUsers.value = _onlineUsers.value.toMutableMap().also { it[userId] = online }
                if (!conversationId.isNullOrBlank()) {
                    _roomOnline.value = _roomOnline.value.toMutableMap().also { rooms ->
                        val cur = rooms[conversationId].orEmpty().toMutableSet()
                        if (online) cur.add(userId) else cur.remove(userId)
                        rooms[conversationId] = cur
                    }
                }
            }
            "presence_snapshot" -> {
                val obj = frame.data?.jsonObject ?: return
                val conversationId = obj.string("conversation_id") ?: return
                val ids = obj["online_user_ids"]?.jsonArray
                    ?.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
                    ?.toSet()
                    .orEmpty()
                // Snapshot is app-level online members of this chat (not "viewing this screen").
                _roomOnline.value = _roomOnline.value.toMutableMap().also { it[conversationId] = ids }
                _onlineUsers.value = _onlineUsers.value.toMutableMap().also { map ->
                    // Mark listed users online; do not wipe others (other chats may still track them).
                    ids.forEach { map[it] = true }
                }
            }
            "call.incoming" -> {
                val data = frame.data ?: return
                val call = runCatching {
                    NetworkModule.json.decodeFromJsonElement(CallDto.serializer(), data)
                }.onFailure { CallLog.w("decode call.incoming failed", it) }.getOrNull() ?: return
                // Never auto-join — only show Accept / Decline.
                ActiveCallSession.setIncoming(call)
            }
            "call.answered" -> {
                val data = frame.data ?: return
                val call = runCatching {
                    NetworkModule.json.decodeFromJsonElement(CallDto.serializer(), data)
                }.getOrNull()
                val activeId = ActiveCallSession.session.value?.callId
                CallLog.i("ws call.answered id=${call?.id} active=$activeId")
                if (call?.id != null && call.id == activeId) {
                    ActiveCallSession.markAnswered()
                    val media = ActiveCallSession.session.value?.media == "video"
                    LiveKitCallController.enableMedia(media)
                }
            }
            "call.rejected", "call.ended" -> {
                val data = frame.data
                val call = data?.let {
                    runCatching {
                        NetworkModule.json.decodeFromJsonElement(CallDto.serializer(), it)
                    }.getOrNull()
                }
                val reason = when {
                    frame.type == "call.rejected" -> "rejected"
                    call?.endReason.equals("rejected", true) -> "rejected"
                    call?.endReason.equals("cancelled", true) -> "cancelled"
                    call?.endReason.equals("timeout", true) -> "timeout"
                    call?.status.equals("missed", true) -> "missed"
                    else -> "ended"
                }
                val activeId = ActiveCallSession.session.value?.callId
                val incomingId = ActiveCallSession.incoming.value?.id
                val matchesIncoming = incomingId != null && (call?.id == null || call.id == incomingId)
                val matchesActive = activeId != null && (call?.id == null || call.id == activeId)
                CallLog.i(
                    "ws ${frame.type} id=${call?.id} reason=$reason " +
                        "matchIncoming=$matchesIncoming matchActive=$matchesActive",
                )
                // Always tear down local UI so one side never stays on the call screen alone.
                if (matchesIncoming) {
                    ActiveCallSession.clearIncoming()
                }
                if (matchesActive) {
                    LiveKitCallController.disconnect()
                    ActiveCallSession.markEnded(reason)
                } else if (matchesIncoming && frame.type == "call.ended") {
                    // Incoming dismissed (peer cancelled / timeout) — no in-call session.
                }
            }
            "status.new" -> {
                val data = frame.data ?: return
                val row = runCatching {
                    NetworkModule.json.decodeFromJsonElement(
                        com.aivora.app.data.model.StatusRowDto.serializer(),
                        data,
                    )
                }.getOrNull() ?: return
                val uid = myUserIdProvider?.invoke()
                com.aivora.app.data.local.InMemoryStatusStore.mergeRow(row, uid)
                val app = runCatching { com.aivora.app.AivoraApp.instance }.getOrNull()
                if (app != null) {
                    com.aivora.app.data.local.StatusCache.save(
                        app,
                        com.aivora.app.data.local.InMemoryStatusStore.statuses.value,
                    )
                }
            }
        }
    }

    fun isUserOnline(userId: String?): Boolean =
        !userId.isNullOrBlank() && (_onlineUsers.value[userId] == true)

    fun onlineCountInChat(conversationId: String, excludeUserId: String?): Int {
        val set = _roomOnline.value[conversationId].orEmpty()
        return if (excludeUserId.isNullOrBlank()) set.size else set.count { it != excludeUserId }
    }

    /** Presence for one peer — skips recomposition when unrelated users go online. */
    fun observeUserOnline(userId: String?): Flow<Boolean> =
        _onlineUsers
            .map { map -> !userId.isNullOrBlank() && map[userId] == true }
            .distinctUntilChanged()

    fun observeRoomOnlineCount(conversationId: String, excludeUserId: String?): Flow<Int> =
        _roomOnline
            .map { rooms ->
                val set = rooms[conversationId].orEmpty()
                if (excludeUserId.isNullOrBlank()) set.size else set.count { it != excludeUserId }
            }
            .distinctUntilChanged()

    private fun ackDelivered(messageIds: List<String>) {
        if (messageIds.isEmpty()) return
        val token = tokenProvider?.invoke() ?: return
        scope.launch {
            runCatching {
                val api = NetworkModule.api(NetworkModule.okHttp { token })
                api.postMessagesDelivered(mapOf("message_ids" to messageIds))
            }
        }
    }

    private fun JsonObject.string(key: String): String? =
        (this[key] as? JsonPrimitive)?.contentOrNull

    private fun JsonObject.boolean(key: String): Boolean? =
        (this[key] as? JsonPrimitive)?.booleanOrNull
}
