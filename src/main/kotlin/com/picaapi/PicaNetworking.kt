package com.picaapi

import okhttp3.Dns
import okhttp3.OkHttpClient
import java.net.InetAddress
import java.net.Socket
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * 复刻原项目的网络参数：可选 DNS、系统 TLS（强制 TLSv1.1/1.2）以及可选的信任所有证书。
 * Mirrors the original project's networking parameters: optional DNS, system TLS
 * (forcing TLSv1.1/1.2) and an optional trust-all policy.
 *
 * 原项目未设置任何超时，因此这里同样使用 OkHttp 默认超时。
 * The original set no timeouts, so OkHttp defaults are kept.
 */
object PicaNetworking {

    /**
     * 构造自定义 DNS：IP 列表非空时把主机名解析为这些 IP，否则回退系统 DNS。
     * Builds a custom DNS: when [ips] is non-empty every hostname resolves to
     * those addresses, otherwise the system resolver is used.
     */
    @JvmStatic
    fun dns(ips: List<String>): Dns = Dns { hostname ->
        if (ips.isEmpty()) {
            Dns.SYSTEM.lookup(hostname)
        } else {
            ips.map { InetAddress.getByName(it) }
        }
    }

    /**
     * 应用 SSL 策略，对应原 `NetworkSecurityHelper.applySslPolicy`。
     * Applies the SSL policy, matching `NetworkSecurityHelper.applySslPolicy`.
     *
     * @param trustAll true 时跳过证书与主机名校验（原 `KEY_DISABLE_SSL_VERIFICATION`）。
     *                 when true, certificate and hostname verification are skipped
     *                 (the original `KEY_DISABLE_SSL_VERIFICATION`).
     */
    @JvmStatic
    fun applySslPolicy(builder: OkHttpClient.Builder, trustAll: Boolean): OkHttpClient.Builder {
        return if (trustAll) applyTrustAllSsl(builder) else applySystemTls(builder)
    }

    /** 系统信任链 + 强制 TLSv1.1/1.2，对应原 `TLSSocketFactory`。 / System trust + forced TLSv1.1/1.2, matching the original `TLSSocketFactory`. */
    @JvmStatic
    fun applySystemTls(builder: OkHttpClient.Builder): OkHttpClient.Builder {
        val factory = TlsSocketFactory()
        builder.sslSocketFactory(factory, factory.systemDefaultTrustManager())
        return builder
    }

    /** 信任所有证书与主机名，对应原 `applyTrustAllSsl`。 / Trusts all certificates and hostnames, matching the original `applyTrustAllSsl`. */
    @JvmStatic
    fun applyTrustAllSsl(builder: OkHttpClient.Builder): OkHttpClient.Builder {
        val trustAll = TrustAllManager()
        val context = SSLContext.getInstance("TLS")
        context.init(null, arrayOf<TrustManager>(trustAll), SecureRandom())
        builder.sslSocketFactory(context.socketFactory, trustAll)
        builder.hostnameVerifier { _, _ -> true }
        return builder
    }

    /**
     * 与原 `TLSSocketFactory` 等价的实现：启用系统信任链，并在支持的情况下启用 TLSv1.1/1.2。
     * Equivalent of the original `TLSSocketFactory`: uses the system trust chain
     * and enables TLSv1.1/1.2 when the runtime supports them.
     */
    private class TlsSocketFactory : SSLSocketFactory() {
        private val delegate: SSLSocketFactory

        init {
            val context = SSLContext.getInstance("TLS")
            context.init(null, arrayOf<TrustManager>(systemDefaultTrustManager()), null)
            delegate = context.socketFactory
        }

        fun systemDefaultTrustManager(): X509TrustManager {
            val factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            factory.init(null as KeyStore?)
            val managers = factory.trustManagers
            if (managers.size != 1 || managers[0] !is X509TrustManager) {
                error("Unexpected default trust managers: ${managers.contentToString()}")
            }
            return managers[0] as X509TrustManager
        }

        private fun configure(socket: Socket): Socket {
            if (socket is SSLSocket) {
                val supported = socket.supportedProtocols.toSet()
                val enabled = listOf("TLSv1.2", "TLSv1.1").filter { it in supported }
                if (enabled.isNotEmpty()) socket.enabledProtocols = enabled.toTypedArray()
            }
            return socket
        }

        override fun getDefaultCipherSuites(): Array<String> = delegate.defaultCipherSuites
        override fun getSupportedCipherSuites(): Array<String> = delegate.supportedCipherSuites
        override fun createSocket(s: Socket?, host: String?, port: Int, autoClose: Boolean): Socket =
            configure(delegate.createSocket(s, host, port, autoClose))
        override fun createSocket(host: String?, port: Int): Socket =
            configure(delegate.createSocket(host, port))
        override fun createSocket(host: String?, port: Int, localHost: InetAddress?, localPort: Int): Socket =
            configure(delegate.createSocket(host, port, localHost, localPort))
        override fun createSocket(host: InetAddress?, port: Int): Socket =
            configure(delegate.createSocket(host, port))
        override fun createSocket(
            address: InetAddress?,
            port: Int,
            localAddress: InetAddress?,
            localPort: Int,
        ): Socket = configure(delegate.createSocket(address, port, localAddress, localPort))
    }

    /** 信任所有证书的 [X509TrustManager]。 / An [X509TrustManager] that trusts every certificate. */
    private class TrustAllManager : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
    }
}
