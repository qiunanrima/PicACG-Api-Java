package com.picaapi

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.picacomic.fregata.objects.ComicPageObject
import com.picacomic.fregata.objects.NetworkErrorObject
import com.picacomic.fregata.objects.requests.AdjustExpBody
import com.picacomic.fregata.objects.requests.AvatarBody
import com.picacomic.fregata.objects.requests.ChangePasswordBody
import com.picacomic.fregata.objects.requests.CommentBody
import com.picacomic.fregata.objects.requests.ForgotPasswordBody
import com.picacomic.fregata.objects.requests.RegisterBody
import com.picacomic.fregata.objects.requests.ResetPasswordBody
import com.picacomic.fregata.objects.requests.SignInBody
import com.picacomic.fregata.objects.requests.SortingBody
import com.picacomic.fregata.objects.requests.UpdatePicaIdBody
import com.picacomic.fregata.objects.requests.UpdateProfileBody
import com.picacomic.fregata.objects.requests.UpdateQandABody
import com.picacomic.fregata.objects.requests.UpdateUserTitleBody
import com.picacomic.fregata.objects.requests.UserIdBody
import com.picacomic.fregata.objects.responses.ActionResponse
import com.picacomic.fregata.objects.responses.BannersResponse
import com.picacomic.fregata.objects.responses.CategoryResponse
import com.picacomic.fregata.objects.responses.ChatroomListResponse
import com.picacomic.fregata.objects.responses.ComicDetailResponse
import com.picacomic.fregata.objects.responses.ComicRandomListResponse
import com.picacomic.fregata.objects.responses.DataClass.ComicPageResponse.ComicPagesResponse
import com.picacomic.fregata.objects.responses.CommentPostToTopResponse
import com.picacomic.fregata.objects.responses.ForgotPasswordResponse
import com.picacomic.fregata.objects.responses.GeneralResponse
import com.picacomic.fregata.objects.responses.InitialResponse
import com.picacomic.fregata.objects.responses.KeywordsResponse
import com.picacomic.fregata.objects.responses.LeaderboardKnightResponse
import com.picacomic.fregata.objects.responses.LeaderboardResponse
import com.picacomic.fregata.objects.responses.MessageResponse
import com.picacomic.fregata.objects.responses.PasswordResponse
import com.picacomic.fregata.objects.responses.PunchInResponse
import com.picacomic.fregata.objects.responses.PutAvatarResponse
import com.picacomic.fregata.objects.responses.RegisterResponse
import com.picacomic.fregata.objects.responses.SignInResponse
import com.picacomic.fregata.objects.responses.UserProfileDirtyResponse
import com.picacomic.fregata.objects.responses.UserProfileResponse
import okhttp3.Call
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.lang.reflect.Type
import java.util.Base64
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicLong

/**
 * PicACG API 的线程安全 REST 客户端。
 * Thread-safe REST client for the PicACG API.
 *
 * 支持 Java 与 Kotlin 无缝调用，提供完整的 `@JvmOverloads`、实体查询对象、便捷重载、
 * 函数式回调以及基于 [CompletableFuture] 的异步调用 [async]。
 */
class PicaClient @JvmOverloads constructor(config: PicaConfig = PicaConfig()) {

    /** 当前配置（可运行时替换）。 / Current configuration (replaceable at runtime). */
    @Volatile
    var config: PicaConfig = config
        private set

    /** 鉴权令牌，会作为 `authorization` 头附加到需要登录的请求。 / Bearer token sent as the `authorization` header on authenticated calls. */
    @Volatile
    var authorization: String? = config.authorization

    private val gson = Gson()
    private val serverTimeOffset = AtomicLong(config.timeOffsetSeconds)
    @Volatile
    private var httpClient: OkHttpClient = buildHttpClient(config)

    /** 获取底层的 OkHttpClient 实例。 / Raw OkHttpClient instance. */
    val rawHttpClient: OkHttpClient
        get() = httpClient

    /** 内置的图片下载器。 / Built-in image and episode downloader. */
    val downloader: PicaDownloader
        get() = PicaDownloader

    /** 异步调用客户端，所有方法返回 [CompletableFuture]。 / Async client returning [CompletableFuture]. */
    val async: PicaAsyncClient by lazy { PicaAsyncClient(this) }

    /** 供 Java 友好调用的异步客户端获取方法。 / Java-friendly getter for async client. */
    @JvmName("async")
    fun async(): PicaAsyncClient = async

    /** 最近一次已知的 `Server-Time - 本地时间`（秒）。 / Last known `Server-Time - localTime` in seconds. */
    val serverTimeOffsetSeconds: Long
        get() = serverTimeOffset.get()

    /** 替换当前配置（鉴权令牌单独维护）。 / Replaces the active configuration (authorization is kept separate). */
    fun updateConfig(newConfig: PicaConfig) {
        config = newConfig
        serverTimeOffset.set(newConfig.timeOffsetSeconds)
        httpClient = buildHttpClient(newConfig)
    }

    /** 更新鉴权令牌。 / Updates the authorization token. */
    fun updateAuthorization(token: String?) {
        authorization = token
    }

    // region 传输层 / transport -------------------------------------------------------------------

    private fun buildHttpClient(cfg: PicaConfig): OkHttpClient {
        val builder = OkHttpClient.Builder()
        if (cfg.dnsIps.isNotEmpty()) {
            builder.dns(PicaNetworking.dns(cfg.dnsIps))
        }
        PicaNetworking.applySslPolicy(builder, cfg.disableSslVerification)
        if (cfg.enableLogging) {
            builder.addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY },
            )
        }
        builder.addInterceptor { chain -> signAndProceed(chain) }
        return builder.build()
    }

    /** 为单个请求计算签名并补齐所有协议头。 / Signs a single request and attaches every protocol header. */
    private fun signAndProceed(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        val cfg = config
        val request = chain.request()
        val requestUrl = request.url.toString()
        val baseUrl = cfg.normalizedBaseUrl()
        // 若为图片 CDN 或外部资源，跳过 API 签名与协议头注入
        if (!requestUrl.startsWith(baseUrl)) {
            return chain.proceed(request)
        }
        val nonce = UUID.randomUUID().toString().replace("-", "")
        val path = requestUrl.replace(baseUrl, "")
        val time = (System.currentTimeMillis() / 1000L + serverTimeOffset.get()).toString()
        val signature = PicaSignature.sign(
            path = path,
            time = time,
            nonce = nonce,
            method = request.method,
            apiKey = cfg.apiKey,
            hmacKey = cfg.hmacKey,
        )
        val signed = request.newBuilder()
            .header("api-key", cfg.apiKey)
            .header("accept", "application/vnd.picacomic.com.v1+json")
            .header("app-channel", cfg.appChannel.toString())
            .header("time", time)
            .header("nonce", nonce)
            .header("signature", signature)
            .header("app-version", cfg.appVersion)
            .header("app-uuid", cfg.appUuid)
            .header("image-quality", cfg.imageQuality)
            .header("app-platform", "android")
            .header("app-build-version", cfg.appBuildVersion)
            .header("User-Agent", cfg.userAgent)
            .build()

        val response = chain.proceed(signed)
        response.header("Server-Time")?.trim()?.toLongOrNull()?.let { serverTime ->
            serverTimeOffset.set(serverTime - System.currentTimeMillis() / 1000L)
        }
        return response
    }

    /** 构造带查询参数的完整 URL。 / Builds the full URL including query parameters. */
    private fun buildUrl(path: String, query: Map<String, Any?>): HttpUrl {
        val url = (config.normalizedBaseUrl() + path).toHttpUrlOrNull()
            ?: error("Invalid URL: ${config.normalizedBaseUrl()}$path")
        val builder = url.newBuilder()
        query.forEach { (key, value) -> if (value != null) builder.addQueryParameter(key, value.toString()) }
        return builder.build()
    }

    /** 构造一个 OkHttp 请求。 / Builds a single OkHttp request. */
    private fun buildRequest(
        method: String,
        path: String,
        body: Any?,
        query: Map<String, Any?>,
        authorized: Boolean,
    ): Request {
        val requestBody: RequestBody? = when {
            body != null -> gson.toJson(body).toRequestBody(JSON_MEDIA_TYPE)
            method == "POST" || method == "PUT" || method == "PATCH" -> EMPTY_REQUEST_BODY
            else -> null
        }
        val builder = Request.Builder()
            .url(buildUrl(path, query))
            .method(method, requestBody)
        if (authorized) {
            authorization?.takeIf { it.isNotBlank() }?.let { builder.header("authorization", it) }
        }
        return builder.build()
    }

    /** 执行一个载荷包裹在 [GeneralResponse] 中的同步请求。 */
    internal fun <T> callData(
        method: String,
        path: String,
        dataType: Type,
        body: Any? = null,
        query: Map<String, Any?> = emptyMap(),
        authorized: Boolean = true,
    ): PicaResult<T> = executeData(
        httpClient.newCall(buildRequest(method, path, body, query, authorized)),
        dataType,
    )

    /** 执行一个响应体即载荷的同步请求。 */
    internal fun <T> callRaw(
        method: String,
        path: String,
        type: Type,
        body: Any? = null,
        query: Map<String, Any?> = emptyMap(),
        authorized: Boolean = true,
    ): PicaResult<T> = executeRaw(
        httpClient.newCall(buildRequest(method, path, body, query, authorized)),
        type,
    )

    /** 执行一个载荷包裹在 [GeneralResponse] 中的异步请求。 */
    internal fun <T> callDataAsync(
        method: String,
        path: String,
        dataType: Type,
        body: Any? = null,
        query: Map<String, Any?> = emptyMap(),
        authorized: Boolean = true,
    ): CompletableFuture<PicaResult<T>> = executeDataAsync(
        httpClient.newCall(buildRequest(method, path, body, query, authorized)),
        dataType,
    )

    /** 执行一个响应体即载荷的异步请求。 */
    internal fun <T> callRawAsync(
        method: String,
        path: String,
        type: Type,
        body: Any? = null,
        query: Map<String, Any?> = emptyMap(),
        authorized: Boolean = true,
    ): CompletableFuture<PicaResult<T>> = executeRawAsync(
        httpClient.newCall(buildRequest(method, path, body, query, authorized)),
        type,
    )

    /** 解析 `{ code, message, data }` 信封并返回 `data`。 */
    @Suppress("UNCHECKED_CAST")
    private fun <T> executeData(call: Call, dataType: Type): PicaResult<T> {
        return try {
            call.execute().use { response ->
                val text = response.body?.string()
                if (!response.isSuccessful) return failure(response.code, text)
                if (dataType == Unit::class.java) return PicaResult.Success(Unit as T, response.code)
                if (text.isNullOrBlank()) return PicaResult.Success(null as T, response.code)
                val envelopeType = TypeToken.getParameterized(GeneralResponse::class.java, dataType).type
                val envelope: GeneralResponse<*> = gson.fromJson(text, envelopeType)
                val data = (envelope.data ?: if (dataType == Unit::class.java) Unit else null) as T
                PicaResult.Success(data, response.code)
            }
        } catch (t: Throwable) {
            PicaResult.Failure(httpCode = null, message = t.message, cause = t)
        }
    }

    /** 直接解析响应体为 [type]。 */
    @Suppress("UNCHECKED_CAST")
    private fun <T> executeRaw(call: Call, type: Type): PicaResult<T> {
        return try {
            call.execute().use { response ->
                val text = response.body?.string()
                if (!response.isSuccessful) return failure(response.code, text)
                if (type == Unit::class.java) return PicaResult.Success(Unit as T, response.code)
                if (text.isNullOrBlank()) return PicaResult.Success(null as T, response.code)
                PicaResult.Success(gson.fromJson<T>(text, type), response.code)
            }
        } catch (t: Throwable) {
            PicaResult.Failure(httpCode = null, message = t.message, cause = t)
        }
    }

    private fun <T> executeDataAsync(call: Call, dataType: Type): CompletableFuture<PicaResult<T>> {
        val future = CompletableFuture<PicaResult<T>>()
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                future.complete(PicaResult.Failure(httpCode = null, message = e.message, cause = e))
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                try {
                    response.use { res ->
                        val text = res.body?.string()
                        if (!res.isSuccessful) {
                            future.complete(failure(res.code, text))
                            return
                        }
                        if (dataType == Unit::class.java) {
                            @Suppress("UNCHECKED_CAST")
                            future.complete(PicaResult.Success(Unit as T, res.code))
                            return
                        }
                        if (text.isNullOrBlank()) {
                            @Suppress("UNCHECKED_CAST")
                            future.complete(PicaResult.Success(null as T, res.code))
                            return
                        }
                        val envelopeType = TypeToken.getParameterized(GeneralResponse::class.java, dataType).type
                        val envelope: GeneralResponse<*> = gson.fromJson(text, envelopeType)
                        @Suppress("UNCHECKED_CAST")
                        val data = (envelope.data ?: if (dataType == Unit::class.java) Unit else null) as T
                        future.complete(PicaResult.Success(data, res.code))
                    }
                } catch (t: Throwable) {
                    future.complete(PicaResult.Failure(httpCode = null, message = t.message, cause = t))
                }
            }
        })
        return future
    }

    private fun <T> executeRawAsync(call: Call, type: Type): CompletableFuture<PicaResult<T>> {
        val future = CompletableFuture<PicaResult<T>>()
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                future.complete(PicaResult.Failure(httpCode = null, message = e.message, cause = e))
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                try {
                    response.use { res ->
                        val text = res.body?.string()
                        if (!res.isSuccessful) {
                            future.complete(failure(res.code, text))
                            return
                        }
                        if (type == Unit::class.java) {
                            @Suppress("UNCHECKED_CAST")
                            future.complete(PicaResult.Success(Unit as T, res.code))
                            return
                        }
                        if (text.isNullOrBlank()) {
                            @Suppress("UNCHECKED_CAST")
                            future.complete(PicaResult.Success(null as T, res.code))
                            return
                        }
                        @Suppress("UNCHECKED_CAST")
                        future.complete(PicaResult.Success(gson.fromJson<T>(text, type), res.code))
                    }
                } catch (t: Throwable) {
                    future.complete(PicaResult.Failure(httpCode = null, message = t.message, cause = t))
                }
            }
        })
        return future
    }

    /** 将非 2xx 响应转换为 [PicaResult.Failure]，并尽量解析出业务错误码。 */
    private fun failure(httpCode: Int, rawBody: String?): PicaResult.Failure {
        val error = rawBody?.let { runCatching { gson.fromJson(it, NetworkErrorObject::class.java) }.getOrNull() }
        return PicaResult.Failure(
            httpCode = httpCode,
            errorCode = error?.error,
            message = error?.message ?: error?.detail,
            rawBody = rawBody,
        )
    }

    // endregion

    // region 登录鉴权 / auth ------------------------------------------------------------------------

    /** `POST auth/sign-in` — 用账号密码换取令牌。 / exchange credentials for a bearer token. */
    fun signIn(body: SignInBody): PicaResult<SignInResponse> =
        callData("POST", "auth/sign-in", SignInResponse::class.java, body = body, authorized = false)

    /** 登录成功时自动保存令牌，之后无需手动调用 [updateAuthorization]。 / Signs in and stores the token automatically. */
    fun login(email: String, password: String): PicaResult<SignInResponse> {
        val result = signIn(SignInBody(email, password))
        if (result is PicaResult.Success) updateAuthorization(result.data.token)
        return result
    }

    /** `POST auth/register` — 注册新账号。 / create a new account. */
    fun register(body: RegisterBody): PicaResult<RegisterResponse> =
        callRaw("POST", "auth/register", RegisterResponse::class.java, body = body, authorized = false)

    /** `POST auth/forgot-password` — 请求找回密码验证码。 / request a password reset code. */
    fun forgotPassword(body: ForgotPasswordBody): PicaResult<ForgotPasswordResponse> =
        callData(
            "POST",
            "auth/forgot-password",
            ForgotPasswordResponse::class.java,
            body = body,
            authorized = false,
        )

    /** `POST auth/forgot-password` 便捷重载。 / convenient overload for forgotPassword. */
    fun forgotPassword(email: String): PicaResult<ForgotPasswordResponse> =
        forgotPassword(ForgotPasswordBody(email))

    /** `POST auth/reset-password` — 完成重置密码流程。 / finish the password reset flow. */
    fun resetPassword(body: ResetPasswordBody): PicaResult<PasswordResponse> =
        callData(
            "POST",
            "auth/reset-password",
            PasswordResponse::class.java,
            body = body,
            authorized = false,
        )

    /** `POST auth/reset-password` 便捷重载。 / convenient overload for resetPassword. */
    fun resetPassword(email: String, questionNo: Int, answer: String): PicaResult<PasswordResponse> =
        resetPassword(ResetPasswordBody(email, questionNo, answer))

    // endregion

    // region 初始化与元数据 / init & meta -----------------------------------------------------------------

    /** `GET init?platform=android` — 拉取已登录会话的启动数据。 / bootstrap data for the authenticated session. */
    fun init(): PicaResult<InitialResponse> =
        callData("GET", "init", InitialResponse::class.java, query = mapOf("platform" to "android"))

    /** `GET categories` — 全部分区。 / all comic categories. */
    fun getCategories(): PicaResult<CategoryResponse> =
        callData("GET", "categories", CategoryResponse::class.java)

    /** `GET banners` — 首页轮播图。 / home banner list. */
    fun getBanners(): PicaResult<BannersResponse> =
        callData("GET", "banners", BannersResponse::class.java)

    /** `GET keywords` — 搜索关键词建议。 / search keyword suggestions. */
    fun getKeywords(): PicaResult<KeywordsResponse> =
        callData("GET", "keywords", KeywordsResponse::class.java)

    /** `GET collections` — 服务端定义的合集。 / server-defined collections. */
    fun getCollections(): PicaResult<CollectionsResponse> =
        callData("GET", "collections", CollectionsResponse::class.java)

    /** `GET chat` — 可用聊天室列表。 / available chatrooms. */
    fun getChatroomList(): PicaResult<ChatroomListResponse> =
        callData("GET", "chat", ChatroomListResponse::class.java)

    /** `GET pica-apps` — 第三方 Pica 应用目录。 / third-party Pica app catalogue. */
    fun getPicaApps(): PicaResult<PicaAppsResponse> =
        callData("GET", "pica-apps", PicaAppsResponse::class.java)

    /** `GET announcements?page=` — 公告列表。 / announcement list. */
    @JvmOverloads
    fun getAnnouncements(page: Int = 1): PicaResult<AnnouncementsResponse> =
        callData("GET", "announcements", AnnouncementsResponse::class.java, query = mapOf("page" to page))

    /** `GET applications?platform=android&page=` — 应用列表。 / application list. */
    @JvmOverloads
    fun getApplications(page: Int = 1): PicaResult<ApplicationsResponse> =
        callData(
            "GET",
            "applications",
            ApplicationsResponse::class.java,
            query = mapOf("platform" to "android", "page" to page),
        )

    // endregion

    // region 漫画 / comics ----------------------------------------------------------------------

    /** 使用 [ComicQuery] 检索漫画列表。 / Query comics using [ComicQuery]. */
    fun getComics(query: ComicQuery): PicaResult<ComicListResponse> =
        callData("GET", "comics", ComicListResponse::class.java, query = query.toQueryMap())

    /**
     * `GET comics` — 漫画列表/搜索。
     * `GET comics` — comic listing/search.
     */
    @JvmOverloads
    fun getComics(
        page: Int = 1,
        category: String? = null,
        tag: String? = null,
        author: String? = null,
        finished: String? = null,
        sort: String? = null,
        categoryType: String? = null,
        categoryArea: String? = null,
    ): PicaResult<ComicListResponse> = getComics(
        ComicQuery(
            page = page,
            category = category,
            tag = tag,
            author = author,
            finished = finished,
            sort = sort,
            categoryType = categoryType,
            categoryArea = categoryArea,
        ),
    )

    /** 按分类快捷获取漫画。 / Convenient helper for getting comics by category. */
    @JvmOverloads
    fun getComicsByCategory(
        category: String,
        page: Int = 1,
        sort: String? = null,
    ): PicaResult<ComicListResponse> =
        getComics(ComicQuery(page = page, category = category, sort = sort))

    /** 按标签快捷获取漫画。 / Convenient helper for getting comics by tag. */
    @JvmOverloads
    fun getComicsByTag(
        tag: String,
        page: Int = 1,
        sort: String? = null,
    ): PicaResult<ComicListResponse> =
        getComics(ComicQuery(page = page, tag = tag, sort = sort))

    /** 按作者快捷获取漫画。 / Convenient helper for getting comics by author. */
    @JvmOverloads
    fun getComicsByAuthor(
        author: String,
        page: Int = 1,
        sort: String? = null,
    ): PicaResult<ComicListResponse> =
        getComics(ComicQuery(page = page, author = author, sort = sort))

    /**
     * 漫画关键词搜索便捷方法。
     * Convenient comic search by keyword.
     */
    @JvmOverloads
    fun searchComics(
        keyword: String,
        sort: String? = null,
        categories: List<String>? = null,
        page: Int = 1,
    ): PicaResult<ComicListResponse> =
        advancedSearchComics(
            page = page,
            body = SortingBody(keyword, sort, if (categories != null) ArrayList(categories) else null),
        )

    /** `POST comics/advanced-search` — 高级多条件搜索。 / advanced multi-filter search. */
    @JvmOverloads
    fun advancedSearchComics(
        page: Int = 1,
        body: SortingBody,
    ): PicaResult<ComicListResponse> =
        callData(
            "POST",
            "comics/advanced-search",
            ComicListResponse::class.java,
            body = body,
            query = mapOf("page" to page),
        )

    /** `GET comics/random` — 随机漫画。 / random comics. */
    fun getRandomComics(): PicaResult<ComicRandomListResponse> =
        callData("GET", "comics/random", ComicRandomListResponse::class.java)

    /** `GET comics/leaderboard?tt=&ct=` — 排行榜。 / leaderboard. */
    fun getLeaderboard(
        timeType: String,
        category: String,
    ): PicaResult<LeaderboardResponse> =
        callData(
            "GET",
            "comics/leaderboard",
            LeaderboardResponse::class.java,
            query = mapOf("tt" to timeType, "ct" to category),
        )

    /** `GET comics/knight-leaderboard` — 骑士榜。 / knight leaderboard. */
    fun getKnightLeaderboard(): PicaResult<LeaderboardKnightResponse> =
        callData("GET", "comics/knight-leaderboard", LeaderboardKnightResponse::class.java)

    /** `GET comics/{comicId}` — 漫画详情。 / comic detail. */
    fun getComicDetail(comicId: String): PicaResult<ComicDetailResponse> =
        callData("GET", "comics/$comicId", ComicDetailResponse::class.java)

    /** `GET comics/{comicId}/eps?page=` — 章节列表。 / episode list. */
    @JvmOverloads
    fun getComicEpisodes(
        comicId: String,
        page: Int = 1,
    ): PicaResult<ComicEpisodeResponse> =
        callData(
            "GET",
            "comics/$comicId/eps",
            ComicEpisodeResponse::class.java,
            query = mapOf("page" to page),
        )

    /** `GET comics/{comicId}/order/{order}/pages?page=` — 按章节序号取页。 / pages by episode order. */
    @JvmOverloads
    fun getComicPagesByOrder(
        comicId: String,
        order: Int,
        page: Int = 1,
    ): PicaResult<ComicPagesResponse> =
        callData(
            "GET",
            "comics/$comicId/order/$order/pages",
            ComicPagesResponse::class.java,
            query = mapOf("page" to page),
        )

    /** `GET eps/{epsId}/pages?page=` — 按章节 ID 取页。 / pages by episode ID. */
    @JvmOverloads
    fun getEpisodePages(
        episodeId: String,
        page: Int = 1,
    ): PicaResult<ComicPagesResponse> =
        callData(
            "GET",
            "eps/$episodeId/pages",
            ComicPagesResponse::class.java,
            query = mapOf("page" to page),
        )

    /** 获取某章节的所有图片（自动循环翻页聚合）。 / Fetches all pages for a comic episode by paging until end. */
    fun getAllComicPagesByOrder(comicId: String, order: Int): PicaResult<List<ComicPageObject>> {
        val allPages = mutableListOf<ComicPageObject>()
        var currentPage = 1
        while (true) {
            when (val res = getComicPagesByOrder(comicId, order, currentPage)) {
                is PicaResult.Success -> {
                    val pageData = res.data.pages ?: break
                    val docs = pageData.docs ?: emptyList()
                    allPages.addAll(docs)
                    if (currentPage >= pageData.pages || docs.isEmpty()) {
                        break
                    }
                    currentPage++
                }
                is PicaResult.Failure -> return res
            }
        }
        return PicaResult.Success(allPages)
    }

    /**
     * 下载单张图片为原始二进制字节数组。
     *
     * @param imageUrl 图片完整下载地址
     * @return 字节数组结果
     */
    fun fetchImageBytes(imageUrl: String): PicaResult<ByteArray> =
        PicaDownloader.fetchImageBytes(imageUrl, rawHttpClient)

    /**
     * 下载图片并安全原子写入目标文件。
     *
     * @param imageUrl 图片完整下载地址
     * @param targetFile 保存的目标文件
     * @param overwrite 是否覆盖已有文件，默认为 false
     * @return 目标文件结果
     */
    @JvmOverloads
    fun downloadImage(imageUrl: String, targetFile: File, overwrite: Boolean = false): PicaResult<File> =
        PicaDownloader.downloadImage(imageUrl, targetFile, overwrite, rawHttpClient)

    /**
     * 并发下载单话/单章节的全部图片。
     *
     * @param comicId 漫画 ID
     * @param episodeOrder 章节序号（从 1 开始）
     * @param targetDir 本地保存目录
     * @param concurrency 并发线程数，默认为 4
     * @param overwrite 是否覆盖已有文件，默认为 false
     * @param listener 进度监听器
     * @return 按页面顺序排列的文件列表
     */
    @JvmOverloads
    fun downloadEpisode(
        comicId: String,
        episodeOrder: Int,
        targetDir: File,
        concurrency: Int = 4,
        overwrite: Boolean = false,
        listener: DownloadProgressListener? = null
    ): PicaResult<List<File>> =
        PicaDownloader.downloadEpisode(this, comicId, episodeOrder, targetDir, concurrency, overwrite, listener)

    /** `GET comics/{comicId}/recommendation` — 相关推荐。 / related recommendations. */
    fun getComicRecommendation(comicId: String): PicaResult<ComicRandomListResponse> =
        callData("GET", "comics/$comicId/recommendation", ComicRandomListResponse::class.java)

    /** `POST comics/{comicId}/like` — 点赞/取消点赞。 / like or unlike. */
    fun likeComic(comicId: String): PicaResult<ActionResponse> =
        callData("POST", "comics/$comicId/like", ActionResponse::class.java)

    /** `POST comics/{comicId}/favourite` — 收藏/取消收藏。 / favourite or unfavourite. */
    fun favouriteComic(comicId: String): PicaResult<ActionResponse> =
        callData("POST", "comics/$comicId/favourite", ActionResponse::class.java)

    /** `GET users/favourite?s=&page=` — 我的收藏。 / my favourites. */
    @JvmOverloads
    fun getFavouriteComics(
        sort: String = PicaSort.NEWEST,
        page: Int = 1,
    ): PicaResult<ComicListResponse> =
        callData(
            "GET",
            "users/favourite",
            ComicListResponse::class.java,
            query = mapOf("s" to sort, "page" to page),
        )

    // endregion

    // region 评论 / comments --------------------------------------------------------------------

    /** `GET comics/{comicId}/comments?page=` — 漫画评论。 / comic comments. */
    @JvmOverloads
    fun getComicComments(
        comicId: String,
        page: Int = 1,
    ): PicaResult<CommentsResponse> =
        callData(
            "GET",
            "comics/$comicId/comments",
            CommentsResponse::class.java,
            query = mapOf("page" to page),
        )

    /** `POST comics/{comicId}/comments` — 发表漫画评论。 / post a comic comment. */
    fun postComicComment(
        comicId: String,
        body: CommentBody,
    ): PicaResult<PostCommentResponse> =
        callData(
            "POST",
            "comics/$comicId/comments",
            PostCommentResponse::class.java,
            body = body,
        )

    /** `POST comics/{comicId}/comments` 便捷重载。 / convenient overload for postComicComment. */
    fun postComicComment(comicId: String, content: String): PicaResult<PostCommentResponse> =
        postComicComment(comicId, CommentBody(content))

    /** `GET games/{gameId}/comments?page=` — 游戏评论。 / game comments. */
    @JvmOverloads
    fun getGameComments(
        gameId: String,
        page: Int = 1,
    ): PicaResult<CommentsResponse> =
        callData(
            "GET",
            "games/$gameId/comments",
            CommentsResponse::class.java,
            query = mapOf("page" to page),
        )

    /** `POST games/{gameId}/comments` — 发表游戏评论。 / post a game comment. */
    fun postGameComment(
        gameId: String,
        body: CommentBody,
    ): PicaResult<PostCommentResponse> =
        callData(
            "POST",
            "games/$gameId/comments",
            PostCommentResponse::class.java,
            body = body,
        )

    /** `POST games/{gameId}/comments` 便捷重载。 / convenient overload for postGameComment. */
    fun postGameComment(gameId: String, content: String): PicaResult<PostCommentResponse> =
        postGameComment(gameId, CommentBody(content))

    /** `POST comments/{commentId}` — 回复评论。 / reply to a comment. */
    fun replyComment(
        commentId: String,
        body: CommentBody,
    ): PicaResult<PostCommentResponse> =
        callData(
            "POST",
            "comments/$commentId",
            PostCommentResponse::class.java,
            body = body,
        )

    /** `POST comments/{commentId}` 便捷重载。 / convenient overload for replyComment. */
    fun replyComment(commentId: String, content: String): PicaResult<PostCommentResponse> =
        replyComment(commentId, CommentBody(content))

    /** `GET comments/{commentId}/childrens?page=` — 子评论。 / child comments. */
    @JvmOverloads
    fun getCommentChildren(
        commentId: String,
        page: Int = 1,
    ): PicaResult<CommentsResponse> =
        callData(
            "GET",
            "comments/$commentId/childrens",
            CommentsResponse::class.java,
            query = mapOf("page" to page),
        )

    /** `POST comments/{commentId}/like` — 评论点赞。 / like a comment. */
    fun likeComment(commentId: String): PicaResult<ActionResponse> =
        callData("POST", "comments/$commentId/like", ActionResponse::class.java)

    /** `POST comments/{commentId}/hide` — 隐藏评论。 / hide a comment. */
    fun hideComment(commentId: String): PicaResult<MessageResponse> =
        callData("POST", "comments/$commentId/hide", MessageResponse::class.java)

    /** `POST comments/{commentId}/report` — 举报评论。 / report a comment. */
    fun reportComment(commentId: String): PicaResult<MessageResponse> =
        callData("POST", "comments/$commentId/report", MessageResponse::class.java)

    /** `POST comments/{commentId}/top` — 置顶评论（管理端）。 / pin a comment (admin only). */
    fun topComment(commentId: String): PicaResult<CommentPostToTopResponse> =
        callData("POST", "comments/$commentId/top", CommentPostToTopResponse::class.java)

    /** `GET users/my-comments?page=` — 我的评论。 / my comments. */
    @JvmOverloads
    fun getMyComments(
        page: Int = 1,
    ): PicaResult<ProfileCommentsResponse> =
        callData(
            "GET",
            "users/my-comments",
            ProfileCommentsResponse::class.java,
            query = mapOf("page" to page),
        )

    // endregion

    // region 游戏 / games -----------------------------------------------------------------------

    /** `GET games?page=` — 游戏列表。 / game list. */
    @JvmOverloads
    fun getGames(
        page: Int = 1,
    ): PicaResult<GameListResponse> =
        callData(
            "GET",
            "games",
            GameListResponse::class.java,
            query = mapOf("page" to page),
        )

    /** `GET games/{gameId}` — 游戏详情。 / game detail. */
    fun getGameDetail(
        gameId: String,
    ): PicaResult<GameDetailResponse> =
        callData(
            "GET",
            "games/$gameId",
            GameDetailResponse::class.java,
        )

    /** `POST games/{gameId}/like` — 游戏点赞。 / like a game. */
    fun likeGame(gameId: String): PicaResult<ActionResponse> =
        callData("POST", "games/$gameId/like", ActionResponse::class.java)

    // endregion

    // region 用户 / users -----------------------------------------------------------------------

    /** `GET users/profile` — 当前登录用户资料。 / profile of the authenticated user. */
    fun getUserProfile(): PicaResult<UserProfileResponse> =
        callData("GET", "users/profile", UserProfileResponse::class.java)

    /** `GET users/{userId}/profile` — 指定用户资料。 / profile of another user. */
    fun getUserProfileById(userId: String): PicaResult<UserProfileResponse> =
        callData("GET", "users/$userId/profile", UserProfileResponse::class.java)

    /** `POST users/{userId}/dirty` — 仅拉取已变化的资料字段。 / fetch only the changed profile fields. */
    fun getUserProfileDirty(userId: String): PicaResult<UserProfileDirtyResponse> =
        callData("POST", "users/$userId/dirty", UserProfileDirtyResponse::class.java)

    /** `POST users/punch-in` — 每日签到。 / daily check-in. */
    fun punchIn(): PicaResult<PunchInResponse> =
        callData("POST", "users/punch-in", PunchInResponse::class.java)

    /** `PUT users/profile` — 更新资料。 / update the profile. */
    fun updateProfile(body: UpdateProfileBody): PicaResult<RegisterResponse> =
        callRaw("PUT", "users/profile", RegisterResponse::class.java, body = body)

    /** `PUT users/avatar` — 更新头像。 / update the avatar. */
    fun updateAvatar(body: AvatarBody): PicaResult<PutAvatarResponse> =
        callData("PUT", "users/avatar", PutAvatarResponse::class.java, body = body)

    /** `PUT users/avatar` 便捷重载（Base64 字符串）。 */
    fun updateAvatar(base64Image: String): PicaResult<PutAvatarResponse> {
        val dataUri = if (base64Image.startsWith("data:image")) base64Image else "data:image/jpeg;base64,$base64Image"
        return updateAvatar(AvatarBody(dataUri))
    }

    /** `PUT users/avatar` 便捷重载（图片文件）。 */
    fun updateAvatar(imageFile: File): PicaResult<PutAvatarResponse> {
        val bytes = imageFile.readBytes()
        return updateAvatar(bytes)
    }

    /** `PUT users/avatar` 便捷重载（图片字节流）。 */
    fun updateAvatar(imageBytes: ByteArray): PicaResult<PutAvatarResponse> {
        val base64 = Base64.getEncoder().encodeToString(imageBytes)
        return updateAvatar(base64)
    }

    /** `PUT users/password` — 修改密码。 / change the password. */
    fun changePassword(body: ChangePasswordBody): PicaResult<RegisterResponse> =
        callRaw("PUT", "users/password", RegisterResponse::class.java, body = body)

    /** `PUT users/password` 便捷重载。 / convenient overload for changePassword. */
    fun changePassword(oldPassword: String, newPassword: String): PicaResult<RegisterResponse> =
        changePassword(ChangePasswordBody(oldPassword, newPassword))

    /** `PUT users/update-id` — 修改 Pica ID。 / change the Pica ID. */
    fun updatePicaId(body: UpdatePicaIdBody): PicaResult<Unit> =
        callData("PUT", "users/update-id", Unit::class.java, body = body)

    /** `PUT users/update-id` 便捷重载。 / convenient overload for updatePicaId. */
    fun updatePicaId(email: String, newName: String): PicaResult<Unit> =
        updatePicaId(UpdatePicaIdBody(email, newName))

    /** `PUT users/update-qa` — 更新密保问题。 / update security questions. */
    fun updateQandA(body: UpdateQandABody): PicaResult<Unit> =
        callData("PUT", "users/update-qa", Unit::class.java, body = body)

    /** `PUT users/{userId}/title` — 修改用户头衔。 / update a user's title. */
    fun updateUserTitle(
        userId: String,
        body: UpdateUserTitleBody,
    ): PicaResult<RegisterResponse> =
        callRaw("PUT", "users/$userId/title", RegisterResponse::class.java, body = body)

    /** `PUT users/{userId}/title` 便捷重载。 / convenient overload for updateUserTitle. */
    fun updateUserTitle(userId: String, title: String): PicaResult<RegisterResponse> =
        updateUserTitle(userId, UpdateUserTitleBody(title))

    /** `POST utils/adjust-exp` — 调整经验值（管理端）。 / grant experience to a user (admin only). */
    fun adjustExp(body: AdjustExpBody): PicaResult<RegisterResponse> =
        callRaw("POST", "utils/adjust-exp", RegisterResponse::class.java, body = body)

    /** `POST utils/block-user` — 拉黑用户。 / block a user. */
    fun blockUser(body: UserIdBody): PicaResult<Unit> =
        callData("POST", "utils/block-user", Unit::class.java, body = body)

    /** `POST utils/block-user` 便捷重载。 / convenient overload for blockUser. */
    fun blockUser(userId: String): PicaResult<Unit> =
        blockUser(UserIdBody(userId))

    /** `POST utils/remove-comment` — 删除评论。 / remove a comment. */
    fun removeComment(body: UserIdBody): PicaResult<Unit> =
        callData("POST", "utils/remove-comment", Unit::class.java, body = body)

    /** `POST utils/remove-comment` 便捷重载。 / convenient overload for removeComment. */
    fun removeComment(commentId: String): PicaResult<Unit> =
        removeComment(UserIdBody(commentId))

    /** `GET users/notifications?page=` — 通知列表。 / notification list. */
    @JvmOverloads
    fun getNotifications(
        page: Int = 1,
    ): PicaResult<NotificationsResponse> =
        callData(
            "GET",
            "users/notifications",
            NotificationsResponse::class.java,
            query = mapOf("page" to page),
        )

    // endregion

    companion object {
        /** JSON 请求体媒体类型。 / Media type used for JSON request bodies. */
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        /** 空请求体，用于不需要载荷的 POST/PUT 请求。 / Empty request body for parameter-less POST/PUT calls. */
        val EMPTY_REQUEST_BODY = byteArrayOf().toRequestBody(null)

        /** 创建默认配置的 [PicaClient]。 / Creates a [PicaClient] with default config. */
        @JvmStatic
        fun create(): PicaClient = PicaClient()

        /** 使用指定配置创建 [PicaClient]。 / Creates a [PicaClient] with custom config. */
        @JvmStatic
        fun create(config: PicaConfig): PicaClient = PicaClient(config)
    }
}

/** Kotlin DSL: 便捷配置并初始化 [PicaClient]。 / Kotlin DSL for constructing [PicaClient]. */
inline fun PicaClient(builderAction: PicaConfig.Builder.() -> Unit): PicaClient =
    PicaClient(PicaConfig.build(builderAction))
