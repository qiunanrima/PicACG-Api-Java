package com.picaapi

import com.picacomic.fregata.objects.responses.SignInResponse

/**
 * 全局单例管理器，跨页面共享同一个 [PicaClient] 与登录令牌。
 * Global singleton manager that shares one [PicaClient] and the login token
 * across screens.
 *
 * ### 用法 / Usage
 * ```kotlin
 * // 启动时初始化一次 / initialize once at startup
 * Pica.init(PicaConfig(imageQuality = "high"), FilePicaTokenStore(File("token")))
 *
 * // 登录页 / login screen（成功后自动持久化令牌）
 * Pica.login("user@example.com", "password")
 *
 * // 其它页面 / other screens
 * Pica.client.getComicDetail(id)
 *
 * // 退出登录 / logout
 * Pica.logout()
 * ```
 *
 * 所有方法线程安全；未初始化时访问 [client] 会抛出 [IllegalStateException]。
 * All methods are thread-safe; accessing [client] before [init] throws
 * [IllegalStateException].
 *
 * Java 可直接调用静态方法：`Pica.init(...)`、`Pica.getClient()`、`Pica.login(...)`。
 * Java callers use the generated statics: `Pica.init(...)`, `Pica.getClient()`, ...
 */
object Pica {

    @Volatile
    private var _client: PicaClient? = null

    @Volatile
    private var tokenStore: PicaTokenStore = MemoryPicaTokenStore

    /** 是否已初始化。 / Whether [init] has been called. */
    @JvmStatic
    val isInitialized: Boolean
        get() = _client != null

    /** 是否已经具备登录令牌。 / Whether a non-blank authorization token exists. */
    @JvmStatic
    val isLoggedIn: Boolean
        get() = isInitialized && !_client?.authorization.isNullOrBlank()

    /** 全局客户端；未初始化时抛异常。 / The global client; throws when not initialized. */
    @JvmStatic
    val client: PicaClient
        get() = _client ?: error("Pica is not initialized. Call Pica.init(...) first. / Pica 未初始化，请先调用 Pica.init(...)")

    /** 全局异步客户端；未初始化时抛异常。 / The global async client; throws when not initialized. */
    @JvmStatic
    val async: PicaAsyncClient
        get() = client.async

    /** 全局下载器。 / Global downloader utility. */
    @JvmStatic
    val downloader: PicaDownloader
        get() = PicaDownloader

    /**
     * 初始化全局单例。会读取 [tokenStore] 中已保存的令牌并自动登录态恢复。
     * Initializes the singleton; any token already saved in [tokenStore] is
     * restored automatically. Re-calling replaces the previous instance.
     *
     * @return 新建的全局客户端 / the created global client
     */
    @JvmStatic
    @JvmOverloads
    fun init(
        config: PicaConfig = PicaConfig(),
        tokenStore: PicaTokenStore = MemoryPicaTokenStore,
    ): PicaClient {
        synchronized(this) {
            this.tokenStore = tokenStore
            val newClient = PicaClient(config).apply {
                updateAuthorization(tokenStore.loadToken())
            }
            _client = newClient
            return newClient
        }
    }

    /** Kotlin DSL: 使用配置块快速初始化单例。 */
    inline fun init(
        tokenStore: PicaTokenStore = MemoryPicaTokenStore,
        configBlock: PicaConfig.Builder.() -> Unit,
    ): PicaClient = init(PicaConfig.build(configBlock), tokenStore)

    /**
     * 登录并在成功时持久化令牌。等价于 `client.login(...)` + [saveToken]。
     * Logs in and persists the token on success. Equivalent to
     * `client.login(...)` + [saveToken].
     *
     * @param persist 是否自动持久化，默认 true。 / whether to persist automatically, default true.
     */
    @JvmStatic
    @JvmOverloads
    fun login(email: String, password: String, persist: Boolean = true): PicaResult<SignInResponse> {
        val result = client.login(email, password)
        if (persist && result is PicaResult.Success) {
            saveToken(result.data.token)
        }
        return result
    }

    /** 保存令牌到 [tokenStore] 并更新内存中的客户端。 / Saves the token to [tokenStore] and updates the in-memory client. */
    @JvmStatic
    fun saveToken(token: String?) {
        tokenStore.saveToken(token)
        _client?.updateAuthorization(token)
    }

    /** 读取当前令牌。 / Returns the current token. */
    @JvmStatic
    fun token(): String? = _client?.authorization

    /** 退出登录：清除令牌。 / Logs out by clearing the token. */
    @JvmStatic
    fun logout() {
        saveToken(null)
    }

    /** 清除令牌同义方法。 / Clears token synonym. */
    @JvmStatic
    fun clearToken() {
        logout()
    }
}
