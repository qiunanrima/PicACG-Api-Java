package com.picaapi

import java.util.UUID

/**
 * [PicaClient] 的不可变配置。
 * Immutable configuration for [PicaClient].
 *
 * 默认值取自原 Android 客户端，因此使用默认配置即可被 PicACG 服务端接受。
 * Defaults are copied from the original Android client, so an out-of-the-box
 * config is accepted by the PicACG API.
 *
 * Java 开发者推荐使用 [builder] 进行链式构建：
 * Java callers are recommended to use the fluent [builder]:
 * ```java
 * PicaConfig config = PicaConfig.builder()
 *     .imageQuality("high")
 *     .enableLogging(true)
 *     .build();
 * ```
 */
data class PicaConfig @JvmOverloads constructor(
    /** API 主机地址，必须以斜杠结尾。 / API host, always ending with a slash. */
    val baseUrl: String = DEFAULT_BASE_URL,
    /** 公共 API Key，用于 `api-key` 头并参与签名。 / Public API key sent in the `api-key` header and mixed into the signature. */
    val apiKey: String = DEFAULT_API_KEY,
    /** 用于签名的 HMAC-SHA256 密钥（Key2）。 / HMAC-SHA256 secret used to sign every request (`Key2`). */
    val hmacKey: String = DEFAULT_HMAC_KEY,
    /** `app-version` 请求头。 / `app-version` header. */
    val appVersion: String = DEFAULT_APP_VERSION,
    /** `app-build-version` 请求头。 / `app-build-version` header. */
    val appBuildVersion: String = DEFAULT_APP_BUILD_VERSION,
    /** `app-uuid` 请求头，应保证同一安装稳定不变。 / `app-uuid` header, should be stable per installation. */
    val appUuid: String = DEFAULT_APP_UUID,
    /** `app-channel` 请求头。 / `app-channel` header. */
    val appChannel: Int = DEFAULT_APP_CHANNEL,
    /** `image-quality` 请求头，取值 `original`/`low`/`medium`/`high`。 / `image-quality` header, one of `original`, `low`, `medium`, `high`. */
    val imageQuality: String = DEFAULT_IMAGE_QUALITY,
    /** `User-Agent` 请求头。 / `User-Agent` header. */
    val userAgent: String = DEFAULT_USER_AGENT,
    /** 可选的 `authorization` 鉴权令牌，用于需要登录的接口。 / Optional `authorization` token used for authenticated calls. */
    val authorization: String? = null,
    /** 签名时对本地时钟施加的初始偏移（秒）。 / Initial offset (seconds) applied to the local clock when signing. */
    val timeOffsetSeconds: Long = 0L,
    /** 是否通过 [okhttp3.logging.HttpLoggingInterceptor] 打印完整请求/响应体。 / Log full request/response bodies through [okhttp3.logging.HttpLoggingInterceptor]. */
    val enableLogging: Boolean = false,
    /** 是否跳过 SSL 证书/主机名校验（对应原 `KEY_DISABLE_SSL_VERIFICATION`）。 / Skip SSL certificate and hostname verification (original `KEY_DISABLE_SSL_VERIFICATION`). */
    val disableSslVerification: Boolean = false,
    /** 自定义 DNS IP 列表；非空时把主机名强制解析到这些地址（对应原 `HttpDns`）。 / Custom DNS IP list; when non-empty every hostname resolves to these addresses (original `HttpDns`). */
    val dnsIps: List<String> = emptyList(),
    /** 可选的自定义图片 CDN 前缀（例如 "https://img.picacomic.com"）。 / Optional custom image CDN prefix. */
    val imageServer: String? = null,
) {
    /** 规范化 baseUrl，确保以斜杠结尾。 / Normalizes [baseUrl] so it always ends with a slash. */
    fun normalizedBaseUrl(): String = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

    /** 基于当前配置创建 Builder。 / Creates a [Builder] pre-populated with current settings. */
    fun toBuilder(): Builder = Builder()
        .baseUrl(baseUrl)
        .apiKey(apiKey)
        .hmacKey(hmacKey)
        .appVersion(appVersion)
        .appBuildVersion(appBuildVersion)
        .appUuid(appUuid)
        .appChannel(appChannel)
        .imageQuality(imageQuality)
        .userAgent(userAgent)
        .authorization(authorization)
        .timeOffsetSeconds(timeOffsetSeconds)
        .enableLogging(enableLogging)
        .disableSslVerification(disableSslVerification)
        .dnsIps(dnsIps)
        .imageServer(imageServer)

    /** 供 Java 和 Kotlin 使用的流式配置构建器。 / Fluent builder for Java and Kotlin callers. */
    class Builder {
        var baseUrl: String = DEFAULT_BASE_URL
        var apiKey: String = DEFAULT_API_KEY
        var hmacKey: String = DEFAULT_HMAC_KEY
        var appVersion: String = DEFAULT_APP_VERSION
        var appBuildVersion: String = DEFAULT_APP_BUILD_VERSION
        var appUuid: String = DEFAULT_APP_UUID
        var appChannel: Int = DEFAULT_APP_CHANNEL
        var imageQuality: String = DEFAULT_IMAGE_QUALITY
        var userAgent: String = DEFAULT_USER_AGENT
        var authorization: String? = null
        var timeOffsetSeconds: Long = 0L
        var enableLogging: Boolean = false
        var disableSslVerification: Boolean = false
        var dnsIps: List<String> = emptyList()
        var imageServer: String? = null

        fun baseUrl(baseUrl: String) = apply { this.baseUrl = baseUrl }
        fun apiKey(apiKey: String) = apply { this.apiKey = apiKey }
        fun hmacKey(hmacKey: String) = apply { this.hmacKey = hmacKey }
        fun appVersion(appVersion: String) = apply { this.appVersion = appVersion }
        fun appBuildVersion(appBuildVersion: String) = apply { this.appBuildVersion = appBuildVersion }
        fun appUuid(appUuid: String) = apply { this.appUuid = appUuid }
        fun appChannel(appChannel: Int) = apply { this.appChannel = appChannel }
        fun imageQuality(imageQuality: String) = apply { this.imageQuality = imageQuality }
        fun userAgent(userAgent: String) = apply { this.userAgent = userAgent }
        fun authorization(authorization: String?) = apply { this.authorization = authorization }
        fun timeOffsetSeconds(timeOffsetSeconds: Long) = apply { this.timeOffsetSeconds = timeOffsetSeconds }
        fun enableLogging(enableLogging: Boolean) = apply { this.enableLogging = enableLogging }
        fun disableSslVerification(disableSslVerification: Boolean) = apply { this.disableSslVerification = disableSslVerification }
        fun dnsIps(dnsIps: List<String>) = apply { this.dnsIps = dnsIps }
        fun dnsIps(vararg dnsIps: String) = apply { this.dnsIps = dnsIps.toList() }
        fun imageServer(imageServer: String?) = apply { this.imageServer = imageServer }

        fun build(): PicaConfig = PicaConfig(
            baseUrl = baseUrl,
            apiKey = apiKey,
            hmacKey = hmacKey,
            appVersion = appVersion,
            appBuildVersion = appBuildVersion,
            appUuid = appUuid,
            appChannel = appChannel,
            imageQuality = imageQuality,
            userAgent = userAgent,
            authorization = authorization,
            timeOffsetSeconds = timeOffsetSeconds,
            enableLogging = enableLogging,
            disableSslVerification = disableSslVerification,
            dnsIps = dnsIps,
            imageServer = imageServer,
        )
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://picaapi.picacomic.com/"
        const val DEFAULT_API_KEY = "C69BAF41DA5ABD1FFEDC6D2FEA56B"

        // 与原生 getStringSigFromNative() 完全一致的 Key2。 / Key2, identical to the native getStringSigFromNative() implementation.
        const val DEFAULT_HMAC_KEY =
            "~d}\$Q7\$eIni=V)9\\RK/P.RM4;9[7|@/CA}b~OW!3?EV`:<>M7pddUBL5n|0/*Cn"

        const val DEFAULT_APP_VERSION = "2.2.1.3.3.4"
        const val DEFAULT_APP_BUILD_VERSION = "45"
        const val DEFAULT_APP_CHANNEL = 1
        const val DEFAULT_IMAGE_QUALITY = "medium"
        const val DEFAULT_USER_AGENT = "okhttp/3.8.1"

        /**
         * 与原版 APK 一致的固定 app-uuid（StaticData.tu）。
         * Fixed app-uuid matching the original APK (StaticData.tu).
         * 如需模拟不同设备可替换为 [randomUuid]。/ Replace with [randomUuid] to simulate a different device.
         */
        const val DEFAULT_APP_UUID = "defaultUuid"

        /** 生成不带连字符的随机 UUID。 / Generates a random UUID without dashes. */
        @JvmStatic
        fun randomUuid(): String = UUID.randomUUID().toString().replace("-", "")

        /** 创建 Builder。 / Creates a [Builder]. */
        @JvmStatic
        fun builder(): Builder = Builder()

        /** 获取默认配置。 / Creates a default [PicaConfig]. */
        @JvmStatic
        fun create(): PicaConfig = PicaConfig()

        /** Kotlin DSL 构建配置。 / Kotlin DSL configuration builder. */
        inline fun build(block: Builder.() -> Unit): PicaConfig = Builder().apply(block).build()
    }
}

/** Kotlin DSL 便捷函数 / Convenient Kotlin DSL top-level builder. */
inline fun picaConfig(block: PicaConfig.Builder.() -> Unit): PicaConfig = PicaConfig.build(block)
