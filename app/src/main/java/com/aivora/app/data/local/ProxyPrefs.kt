package com.aivora.app.data.local

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.ProxySelector
import java.net.SocketAddress
import java.net.URI

/** SOCKS / HTTP proxy for OkHttp (Wave 43). */
object ProxyPrefs {
    private const val PREFS = "aivora_proxy"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_HOST = "host"
    private const val KEY_PORT = "port"
    private const val KEY_TYPE = "type"

    const val TYPE_SOCKS = "socks5"
    const val TYPE_HTTP = "http"

    data class Config(
        val enabled: Boolean = false,
        val host: String = "",
        val port: Int = 1080,
        val type: String = TYPE_SOCKS,
    )

    private val _config = MutableStateFlow(Config())
    val flow: StateFlow<Config> = _config.asStateFlow()

    @Volatile
    private var cached = Config()

    fun hydrate(context: Context) {
        cached = load(context)
        _config.value = cached
    }

    fun load(context: Context): Config {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return Config(
            enabled = p.getBoolean(KEY_ENABLED, false),
            host = p.getString(KEY_HOST, "").orEmpty(),
            port = p.getInt(KEY_PORT, 1080).coerceIn(1, 65535),
            type = p.getString(KEY_TYPE, TYPE_SOCKS)?.takeIf { it == TYPE_HTTP || it == TYPE_SOCKS }
                ?: TYPE_SOCKS,
        )
    }

    fun save(context: Context, config: Config) {
        val safe = config.copy(
            host = config.host.trim(),
            port = config.port.coerceIn(1, 65535),
            type = if (config.type == TYPE_HTTP) TYPE_HTTP else TYPE_SOCKS,
        )
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_ENABLED, safe.enabled)
            .putString(KEY_HOST, safe.host)
            .putInt(KEY_PORT, safe.port)
            .putString(KEY_TYPE, safe.type)
            .apply()
        cached = safe
        _config.value = safe
    }

    fun trailingLabel(config: Config = cached): String =
        if (config.enabled && config.host.isNotBlank()) "On" else "Off"

    fun currentProxy(): Proxy? {
        val c = cached
        if (!c.enabled || c.host.isBlank()) return null
        val type = if (c.type == TYPE_HTTP) Proxy.Type.HTTP else Proxy.Type.SOCKS
        return Proxy(type, InetSocketAddress(c.host, c.port))
    }

    /** OkHttp-compatible selector that re-reads prefs on each connection. */
    val selector: ProxySelector = object : ProxySelector() {
        override fun select(uri: URI?): List<Proxy> {
            val proxy = currentProxy()
            return if (proxy != null) listOf(proxy) else listOf(Proxy.NO_PROXY)
        }

        override fun connectFailed(uri: URI?, sa: SocketAddress?, ioe: java.io.IOException?) {
            // Keep prefs; user can fix host/port in Settings.
        }
    }
}
