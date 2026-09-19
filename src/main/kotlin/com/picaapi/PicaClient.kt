package com.picaapi

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
import java.lang.reflect.Type
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

/**
 * PicACG API 的线程安全 REST 客户端。
 * Thread-safe REST client for the PicACG API.
 *
 * ### 签名 / Signing
 * 所有请求都由 [okhttp3.Interceptor] 透明签名：
 * `signature = HMAC_SHA256((path + time + nonce + method + apiKey).lowercase(), key2)`。
 * 服务端时间偏移从响应头 `Server-Time` 学习并用于后续请求，与原客户端行为一致。
 *
 * Every request is transparently signed by an [okhttp3.Interceptor]:
 * `signature = HMAC_SHA256((path + time + nonce + method + apiKey).lowercase(), key2)`.
 * The server clock offset is learned from the `Server-Time` response header and
 * applied to later requests, matching the original client behaviour.
 *
 * ### 线程安全 / Threading
 * [OkHttpClient] 被共享且本身线程安全；所有可变状态（鉴权令牌、服务器时间偏移、配置）
 * 均通过 `@Volatile` / [AtomicLong] 发布。同步接口可从任意线程调用，每次调用都会新建
 * 独立的 [Call]，请求之间不共享任何可变数据。
 *
 * [OkHttpClient] is shared and thread-safe; all mutable state (authorization
 * token, server time offset, config) is published through `@Volatile` / [AtomicLong].
 * Synchronous endpoint functions may be called from any thread. Each call creates
 * its own [Call]; nothing is shared across in-flight requests.
 *
 * ### 用法 / Usage
 * ```java
 * PicaClient client = new PicaClient(new PicaConfig());
 * PicaResult<SignInResponse> result = client.signIn(new SignInBody("a@b.c", "pw"));
 * if (result instanceof PicaResult.Success) {
 *     client.updateAuthorization(((PicaResult.Success<SignInResponse>) result).getData().getToken());
 * }
 * ```
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
    private val httpClient: OkHttpClient = buildHttpClient(config)

    /** 最近一次已知的 `Server-Time - 本地时间`（秒）。 / Last known `Server-Time - localTime` in seconds. */
    val serverTimeOffsetSeconds: Long
        get() = serverTimeOffset.get()

    /** 替换当前配置（鉴权令牌单独维护）。 / Replaces the active configuration (authorization is kept separate). */
    fun updateConfig(newConfig: PicaConfig) {
        config = newConfig
        serverTimeOffset.set(newConfig.timeOffsetSeconds)
    }

    /** 更新鉴权令牌。 / Updates the authorization token. */
    fun updateAuthorization(token: String?) {
        authorization = token
    }

    // region 传输层 / transport -------------------------------------------------------------------

    private fun buildHttpClient(cfg: PicaConfig): OkHttpClient {
        val builder = OkHttpClient.Builder()
        // 与原项目一致：未设置任何超时，使用 OkHttp 默认值。 / Like the original: no explicit timeouts, OkHttp defaults are used.
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
        val nonce = UUID.randomUUID().toString().replace("-", "")
        val path = request.url.toString().replace(cfg.normalizedBaseUrl(), "")
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
        // 从响应头学习服务端时间，修正本地时钟偏移。 / Learn server time from the header and correct the local clock offset.
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
        val requestBody: RequestBody? = if (body != null) {
            gson.toJson(body).toRequestBody(JSON_MEDIA_TYPE)
        } else {
            null
        }
        val builder = Request.Builder()
            .url(buildUrl(path, query))
            .method(method, requestBody)
        if (authorized) {
            authorization?.takeIf { it.isNotBlank() }?.let { builder.header("authorization", it) }
        }
        return builder.build()
    }

    /** 执行一个载荷包裹在 [GeneralResponse] 中的请求。 / Executes a call whose payload is wrapped in [GeneralResponse]. */
    private fun <T> callData(
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

    /** 执行一个响应体即载荷的请求。 / Executes a call whose payload is the response body itself. */
    private fun <T> callRaw(
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

    /** 解析 `{ code, message, data }` 信封并返回 `data`。 / Parses the `{ code, message, data }` envelope and returns `data`. */
    @Suppress("UNCHECKED_CAST")
    private fun <T> executeData(call: Call, dataType: Type): PicaResult<T> {
        return try {
            call.execute().use { response ->
                val text = response.body?.string()
                if (!response.isSuccessful) return failure(response.code, text)
                if (text.isNullOrBlank()) return PicaResult.Success(null as T, response.code)
                val envelopeType = TypeToken.getParameterized(GeneralResponse::class.java, dataType).type
                val envelope: GeneralResponse<*> = gson.fromJson(text, envelopeType)
                @Suppress("UNCHECKED_CAST")
                PicaResult.Success(envelope.data as T, response.code)
            }
        } catch (t: Throwable) {
            PicaResult.Failure(httpCode = null, message = t.message, cause = t)
        }
    }

    /** 直接解析响应体为 [type]。 / Parses the response body directly into [type]. */
    @Suppress("UNCHECKED_CAST")
    private fun <T> executeRaw(call: Call, type: Type): PicaResult<T> {
        return try {
            call.execute().use { response ->
                val text = response.body?.string()
                if (!response.isSuccessful) return failure(response.code, text)
                if (text.isNullOrBlank()) return PicaResult.Success(null as T, response.code)
                PicaResult.Success(gson.fromJson<T>(text, type), response.code)
            }
        } catch (t: Throwable) {
            PicaResult.Failure(httpCode = null, message = t.message, cause = t)
        }
    }

    /** 将非 2xx 响应转换为 [PicaResult.Failure]，并尽量解析出业务错误码。 / Converts a non-2xx response into [PicaResult.Failure], extracting the business error when possible. */
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
        callRaw("POST", "auth/sign-in", SignInResponse::class.java, body = body, authorized = false)

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

    /** `POST auth/reset-password` — 完成重置密码流程。 / finish the password reset flow. */
    fun resetPassword(body: ResetPasswordBody): PicaResult<PasswordResponse> =
        callData(
            "POST",
            "auth/reset-password",
            PasswordResponse::class.java,
            body = body,
            authorized = false,
        )

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
    fun getCollections(): PicaResult<com.picacomic.fregata.objects.responses.DataClass.CollectionsResponse> =
        callData(
            "GET",
            "collections",
            com.picacomic.fregata.objects.responses.DataClass.CollectionsResponse::class.java,
        )

    /** `GET chat` — 可用聊天室列表。 / available chatrooms. */
    fun getChatroomList(): PicaResult<ChatroomListResponse> =
        callData("GET", "chat", ChatroomListResponse::class.java)

    /** `GET pica-apps` — 第三方 Pica 应用目录。 / third-party Pica app catalogue. */
    fun getPicaApps(): PicaResult<com.picacomic.fregata.objects.responses.DataClass.PicaAppsResponse> =
        callData(
            "GET",
            "pica-apps",
            com.picacomic.fregata.objects.responses.DataClass.PicaAppsResponse::class.java,
        )

    /** `GET announcements?page=` — 公告列表。 / announcement list. */
    fun getAnnouncements(
        page: Int = 1,
    ): PicaResult<com.picacomic.fregata.objects.responses.DataClass.AnnouncementsResponse.AnnouncementsResponse> =
        callData(
            "GET",
            "announcements",
            com.picacomic.fregata.objects.responses.DataClass.AnnouncementsResponse.AnnouncementsResponse::class.java,
            query = mapOf("page" to page),
        )

    /** `GET applications?platform=android&page=` — 应用列表。 / application list. */
    fun getApplications(
        page: Int = 1,
    ): PicaResult<com.picacomic.fregata.objects.responses.DataClass.ApplicationsResponse.ApplicationsResponse> =
        callData(
            "GET",
            "applications",
            com.picacomic.fregata.objects.responses.DataClass.ApplicationsResponse.ApplicationsResponse::class.java,
            query = mapOf("platform" to "android", "page" to page),
        )

    // endregion

    // region 漫画 / comics ----------------------------------------------------------------------

    /**
     * `GET comics` — 漫画列表/搜索。
     * `GET comics` — comic listing/search.
     *
     * @param category 分类过滤 / category filter
     * @param tag 标签过滤 / tag filter
     * @param author 作者过滤 / author filter
     * @param finished 是否完结 / finished flag
     * @param sort 排序方式 / sort order
     * @param categoryType 分类类型 / category type
     * @param categoryArea 分类地区 / category area
     */
    fun getComics(
        page: Int = 1,
        category: String? = null,
        tag: String? = null,
        author: String? = null,
        finished: String? = null,
        sort: String? = null,
        categoryType: String? = null,
        categoryArea: String? = null,
    ): PicaResult<com.picacomic.fregata.objects.responses.DataClass.ComicListResponse.ComicListResponse> =
        callData(
            "GET",
            "comics",
            com.picacomic.fregata.objects.responses.DataClass.ComicListResponse.ComicListResponse::class.java,
            query = mapOf(
                "page" to page,
                "c" to category,
                "t" to tag,
                "a" to author,
                "f" to finished,
                "s" to sort,
                "ct" to categoryType,
                "ca" to categoryArea,
            ),
        )

    /** `POST comics/advanced-search` — 高级多条件搜索。 / advanced multi-filter search. */
    fun advancedSearchComics(
        page: Int,
        body: SortingBody,
    ): PicaResult<com.picacomic.fregata.objects.responses.DataClass.ComicListResponse.ComicListResponse> =
        callData(
            "POST",
            "comics/advanced-search",
            com.picacomic.fregata.objects.responses.DataClass.ComicListResponse.ComicListResponse::class.java,
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
    fun getComicEpisodes(
        comicId: String,
        page: Int = 1,
    ): PicaResult<com.picacomic.fregata.objects.responses.DataClass.ComicEpisodeResponse.ComicEpisodeResponse> =
        callData(
            "GET",
            "comics/$comicId/eps",
            com.picacomic.fregata.objects.responses.DataClass.ComicEpisodeResponse.ComicEpisodeResponse::class.java,
            query = mapOf("page" to page),
        )

    /** `GET comics/{comicId}/order/{order}/pages?page=` — 按章节序号取页。 / pages by episode order. */
    fun getComicPagesByOrder(
        comicId: String,
        order: Int,
        page: Int = 1,
    ): PicaResult<com.picacomic.fregata.objects.responses.DataClass.ComicPageResponse.ComicPagesResponse> =
        callData(
            "GET",
            "comics/$comicId/order/$order/pages",
            com.picacomic.fregata.objects.responses.DataClass.ComicPageResponse.ComicPagesResponse::class.java,
            query = mapOf("page" to page),
        )

    /** `GET eps/{epsId}/pages?page=` — 按章节 ID 取页。 / pages by episode ID. */
    fun getEpisodePages(
        episodeId: String,
        page: Int = 1,
    ): PicaResult<com.picacomic.fregata.objects.responses.DataClass.ComicPageResponse.ComicPagesResponse> =
        callData(
            "GET",
            "eps/$episodeId/pages",
            com.picacomic.fregata.objects.responses.DataClass.ComicPageResponse.ComicPagesResponse::class.java,
            query = mapOf("page" to page),
        )

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
    fun getFavouriteComics(
        sort: String,
        page: Int = 1,
    ): PicaResult<com.picacomic.fregata.objects.responses.DataClass.ComicListResponse.ComicListResponse> =
        callData(
            "GET",
            "users/favourite",
            com.picacomic.fregata.objects.responses.DataClass.ComicListResponse.ComicListResponse::class.java,
            query = mapOf("s" to sort, "page" to page),
        )

    // endregion

    // region 评论 / comments --------------------------------------------------------------------

    /** `GET comics/{comicId}/comments?page=` — 漫画评论。 / comic comments. */
    fun getComicComments(
        comicId: String,
        page: Int = 1,
    ): PicaResult<com.picacomic.fregata.objects.responses.DataClass.CommentsResponse.CommentsResponse> =
        callData(
            "GET",
            "comics/$comicId/comments",
            com.picacomic.fregata.objects.responses.DataClass.CommentsResponse.CommentsResponse::class.java,
            query = mapOf("page" to page),
        )

    /** `POST comics/{comicId}/comments` — 发表漫画评论。 / post a comic comment. */
    fun postComicComment(
        comicId: String,
        body: CommentBody,
    ): PicaResult<com.picacomic.fregata.objects.responses.DataClass.PostCommentResponse.PostCommentResponse> =
        callData(
            "POST",
            "comics/$comicId/comments",
            com.picacomic.fregata.objects.responses.DataClass.PostCommentResponse.PostCommentResponse::class.java,
            body = body,
        )

    /** `GET games/{gameId}/comments?page=` — 游戏评论。 / game comments. */
    fun getGameComments(
        gameId: String,
        page: Int = 1,
    ): PicaResult<com.picacomic.fregata.objects.responses.DataClass.CommentsResponse.CommentsResponse> =
        callData(
            "GET",
            "games/$gameId/comments",
            com.picacomic.fregata.objects.responses.DataClass.CommentsResponse.CommentsResponse::class.java,
            query = mapOf("page" to page),
        )

    /** `POST games/{gameId}/comments` — 发表游戏评论。 / post a game comment. */
    fun postGameComment(
        gameId: String,
        body: CommentBody,
    ): PicaResult<com.picacomic.fregata.objects.responses.DataClass.PostCommentResponse.PostCommentResponse> =
        callData(
            "POST",
            "games/$gameId/comments",
            com.picacomic.fregata.objects.responses.DataClass.PostCommentResponse.PostCommentResponse::class.java,
            body = body,
        )

    /** `POST comments/{commentId}` — 回复评论。 / reply to a comment. */
    fun replyComment(
        commentId: String,
        body: CommentBody,
    ): PicaResult<com.picacomic.fregata.objects.responses.DataClass.PostCommentResponse.PostCommentResponse> =
        callData(
            "POST",
            "comments/$commentId",
            com.picacomic.fregata.objects.responses.DataClass.PostCommentResponse.PostCommentResponse::class.java,
            body = body,
        )

    /** `GET comments/{commentId}/childrens?page=` — 子评论。 / child comments. */
    fun getCommentChildren(
        commentId: String,
        page: Int = 1,
    ): PicaResult<com.picacomic.fregata.objects.responses.DataClass.CommentsResponse.CommentsResponse> =
        callData(
            "GET",
            "comments/$commentId/childrens",
            com.picacomic.fregata.objects.responses.DataClass.CommentsResponse.CommentsResponse::class.java,
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
    fun getMyComments(
        page: Int = 1,
    ): PicaResult<com.picacomic.fregata.objects.responses.DataClass.ProfileCommentsResponse.ProfileCommentsResponse> =
        callData(
            "GET",
            "users/my-comments",
            com.picacomic.fregata.objects.responses.DataClass.ProfileCommentsResponse.ProfileCommentsResponse::class.java,
            query = mapOf("page" to page),
        )

    // endregion

    // region 游戏 / games -----------------------------------------------------------------------

    /** `GET games?page=` — 游戏列表。 / game list. */
    fun getGames(
        page: Int = 1,
    ): PicaResult<com.picacomic.fregata.objects.responses.DataClass.GameListResponse.GameListResponse> =
        callData(
            "GET",
            "games",
            com.picacomic.fregata.objects.responses.DataClass.GameListResponse.GameListResponse::class.java,
            query = mapOf("page" to page),
        )

    /** `GET games/{gameId}` — 游戏详情。 / game detail. */
    fun getGameDetail(
        gameId: String,
    ): PicaResult<com.picacomic.fregata.objects.responses.DataClass.GameDetailResponse.GameDetailResponse> =
        callData(
            "GET",
            "games/$gameId",
            com.picacomic.fregata.objects.responses.DataClass.GameDetailResponse.GameDetailResponse::class.java,
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

    /** `PUT users/password` — 修改密码。 / change the password. */
    fun changePassword(body: ChangePasswordBody): PicaResult<RegisterResponse> =
        callRaw("PUT", "users/password", RegisterResponse::class.java, body = body)

    /** `PUT users/update-id` — 修改 Pica ID。 / change the Pica ID. */
    fun updatePicaId(body: UpdatePicaIdBody): PicaResult<Unit> =
        callData("PUT", "users/update-id", Any::class.java, body = body)

    /** `PUT users/update-qa` — 更新密保问题。 / update security questions. */
    fun updateQandA(body: UpdateQandABody): PicaResult<Unit> =
        callData("PUT", "users/update-qa", Any::class.java, body = body)

    /** `PUT users/{userId}/title` — 修改用户头衔。 / update a user's title. */
    fun updateUserTitle(
        userId: String,
        body: UpdateUserTitleBody,
    ): PicaResult<RegisterResponse> =
        callRaw("PUT", "users/$userId/title", RegisterResponse::class.java, body = body)

    /** `POST utils/adjust-exp` — 调整经验值（管理端）。 / grant experience to a user (admin only). */
    fun adjustExp(body: AdjustExpBody): PicaResult<RegisterResponse> =
        callRaw("POST", "utils/adjust-exp", RegisterResponse::class.java, body = body)

    /** `POST utils/block-user` — 拉黑用户。 / block a user. */
    fun blockUser(body: UserIdBody): PicaResult<Unit> =
        callData("POST", "utils/block-user", Any::class.java, body = body)

    /** `POST utils/remove-comment` — 删除评论。 / remove a comment. */
    fun removeComment(body: UserIdBody): PicaResult<Unit> =
        callData("POST", "utils/remove-comment", Any::class.java, body = body)

    /** `GET users/notifications?page=` — 通知列表。 / notification list. */
    fun getNotifications(
        page: Int = 1,
    ): PicaResult<com.picacomic.fregata.objects.responses.DataClass.NotificationsResponse.NotificationsResponse> =
        callData(
            "GET",
            "users/notifications",
            com.picacomic.fregata.objects.responses.DataClass.NotificationsResponse.NotificationsResponse::class.java,
            query = mapOf("page" to page),
        )

    // endregion

    private companion object {
        /** JSON 请求体媒体类型。 / Media type used for JSON request bodies. */
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
