package com.picaapi

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
import java.io.File
import java.util.concurrent.CompletableFuture

/**
 * [PicaClient] 的异步非阻塞客户端封装。所有方法均返回 [CompletableFuture]，
 * 适用于 Java 8+ / 17 异步调用以及 Android 避免在主线程发起同步网络请求。
 *
 * Asynchronous non-blocking wrapper for [PicaClient]. All methods return [CompletableFuture].
 */
class PicaAsyncClient internal constructor(private val client: PicaClient) {

    // region 登录鉴权 / auth ------------------------------------------------------------------------

    fun signIn(body: SignInBody): CompletableFuture<PicaResult<SignInResponse>> =
        client.callDataAsync("POST", "auth/sign-in", SignInResponse::class.java, body = body, authorized = false)

    fun login(email: String, password: String): CompletableFuture<PicaResult<SignInResponse>> =
        signIn(SignInBody(email, password)).thenApply { result ->
            if (result is PicaResult.Success) {
                client.updateAuthorization(result.data.token)
            }
            result
        }

    fun register(body: RegisterBody): CompletableFuture<PicaResult<RegisterResponse>> =
        client.callRawAsync("POST", "auth/register", RegisterResponse::class.java, body = body, authorized = false)

    fun forgotPassword(body: ForgotPasswordBody): CompletableFuture<PicaResult<ForgotPasswordResponse>> =
        client.callDataAsync(
            "POST",
            "auth/forgot-password",
            ForgotPasswordResponse::class.java,
            body = body,
            authorized = false,
        )

    fun forgotPassword(email: String): CompletableFuture<PicaResult<ForgotPasswordResponse>> =
        forgotPassword(ForgotPasswordBody(email))

    fun resetPassword(body: ResetPasswordBody): CompletableFuture<PicaResult<PasswordResponse>> =
        client.callDataAsync(
            "POST",
            "auth/reset-password",
            PasswordResponse::class.java,
            body = body,
            authorized = false,
        )

    fun resetPassword(email: String, questionNo: Int, answer: String): CompletableFuture<PicaResult<PasswordResponse>> =
        resetPassword(ResetPasswordBody(email, questionNo, answer))

    // endregion

    // region 初始化与元数据 / init & meta -----------------------------------------------------------------

    fun init(): CompletableFuture<PicaResult<InitialResponse>> =
        client.callDataAsync("GET", "init", InitialResponse::class.java, query = mapOf("platform" to "android"))

    fun getCategories(): CompletableFuture<PicaResult<CategoryResponse>> =
        client.callDataAsync("GET", "categories", CategoryResponse::class.java)

    fun getBanners(): CompletableFuture<PicaResult<BannersResponse>> =
        client.callDataAsync("GET", "banners", BannersResponse::class.java)

    fun getKeywords(): CompletableFuture<PicaResult<KeywordsResponse>> =
        client.callDataAsync("GET", "keywords", KeywordsResponse::class.java)

    fun getCollections(): CompletableFuture<PicaResult<CollectionsResponse>> =
        client.callDataAsync("GET", "collections", CollectionsResponse::class.java)

    fun getChatroomList(): CompletableFuture<PicaResult<ChatroomListResponse>> =
        client.callDataAsync("GET", "chat", ChatroomListResponse::class.java)

    fun getPicaApps(): CompletableFuture<PicaResult<PicaAppsResponse>> =
        client.callDataAsync("GET", "pica-apps", PicaAppsResponse::class.java)

    @JvmOverloads
    fun getAnnouncements(page: Int = 1): CompletableFuture<PicaResult<AnnouncementsResponse>> =
        client.callDataAsync("GET", "announcements", AnnouncementsResponse::class.java, query = mapOf("page" to page))

    @JvmOverloads
    fun getApplications(page: Int = 1): CompletableFuture<PicaResult<ApplicationsResponse>> =
        client.callDataAsync(
            "GET",
            "applications",
            ApplicationsResponse::class.java,
            query = mapOf("platform" to "android", "page" to page),
        )

    // endregion

    // region 漫画 / comics ----------------------------------------------------------------------

    fun getComics(query: ComicQuery): CompletableFuture<PicaResult<ComicListResponse>> =
        client.callDataAsync("GET", "comics", ComicListResponse::class.java, query = query.toQueryMap())

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
    ): CompletableFuture<PicaResult<ComicListResponse>> = getComics(
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

    @JvmOverloads
    fun searchComics(
        keyword: String,
        sort: String? = null,
        categories: List<String>? = null,
        page: Int = 1,
    ): CompletableFuture<PicaResult<ComicListResponse>> =
        advancedSearchComics(
            page = page,
            body = SortingBody(keyword, sort, if (categories != null) ArrayList(categories) else null),
        )

    @JvmOverloads
    fun advancedSearchComics(
        page: Int = 1,
        body: SortingBody,
    ): CompletableFuture<PicaResult<ComicListResponse>> =
        client.callDataAsync("POST", "comics/advanced-search", ComicListResponse::class.java, body = body, query = mapOf("page" to page))

    fun getRandomComics(): CompletableFuture<PicaResult<ComicRandomListResponse>> =
        client.callDataAsync("GET", "comics/random", ComicRandomListResponse::class.java)

    fun getLeaderboard(timeType: String, category: String): CompletableFuture<PicaResult<LeaderboardResponse>> =
        client.callDataAsync("GET", "comics/leaderboard", LeaderboardResponse::class.java, query = mapOf("tt" to timeType, "ct" to category))

    fun getKnightLeaderboard(): CompletableFuture<PicaResult<LeaderboardKnightResponse>> =
        client.callDataAsync("GET", "comics/knight-leaderboard", LeaderboardKnightResponse::class.java)

    fun getComicDetail(comicId: String): CompletableFuture<PicaResult<ComicDetailResponse>> =
        client.callDataAsync("GET", "comics/$comicId", ComicDetailResponse::class.java)

    @JvmOverloads
    fun getComicEpisodes(comicId: String, page: Int = 1): CompletableFuture<PicaResult<ComicEpisodeResponse>> =
        client.callDataAsync("GET", "comics/$comicId/eps", ComicEpisodeResponse::class.java, query = mapOf("page" to page))

    @JvmOverloads
    fun getComicPagesByOrder(comicId: String, order: Int, page: Int = 1): CompletableFuture<PicaResult<ComicPagesResponse>> =
        client.callDataAsync("GET", "comics/$comicId/order/$order/pages", ComicPagesResponse::class.java, query = mapOf("page" to page))

    @JvmOverloads
    fun getEpisodePages(episodeId: String, page: Int = 1): CompletableFuture<PicaResult<ComicPagesResponse>> =
        client.callDataAsync("GET", "eps/$episodeId/pages", ComicPagesResponse::class.java, query = mapOf("page" to page))

    fun getComicRecommendation(comicId: String): CompletableFuture<PicaResult<ComicRandomListResponse>> =
        client.callDataAsync("GET", "comics/$comicId/recommendation", ComicRandomListResponse::class.java)

    fun likeComic(comicId: String): CompletableFuture<PicaResult<ActionResponse>> =
        client.callDataAsync("POST", "comics/$comicId/like", ActionResponse::class.java)

    fun favouriteComic(comicId: String): CompletableFuture<PicaResult<ActionResponse>> =
        client.callDataAsync("POST", "comics/$comicId/favourite", ActionResponse::class.java)

    @JvmOverloads
    fun getFavouriteComics(sort: String = PicaSort.NEWEST, page: Int = 1): CompletableFuture<PicaResult<ComicListResponse>> =
        client.callDataAsync("GET", "users/favourite", ComicListResponse::class.java, query = mapOf("s" to sort, "page" to page))

    // endregion

    // region 评论 / comments --------------------------------------------------------------------

    @JvmOverloads
    fun getComicComments(comicId: String, page: Int = 1): CompletableFuture<PicaResult<CommentsResponse>> =
        client.callDataAsync("GET", "comics/$comicId/comments", CommentsResponse::class.java, query = mapOf("page" to page))

    fun postComicComment(comicId: String, body: CommentBody): CompletableFuture<PicaResult<PostCommentResponse>> =
        client.callDataAsync("POST", "comics/$comicId/comments", PostCommentResponse::class.java, body = body)

    fun postComicComment(comicId: String, content: String): CompletableFuture<PicaResult<PostCommentResponse>> =
        postComicComment(comicId, CommentBody(content))

    @JvmOverloads
    fun getGameComments(gameId: String, page: Int = 1): CompletableFuture<PicaResult<CommentsResponse>> =
        client.callDataAsync("GET", "games/$gameId/comments", CommentsResponse::class.java, query = mapOf("page" to page))

    fun postGameComment(gameId: String, body: CommentBody): CompletableFuture<PicaResult<PostCommentResponse>> =
        client.callDataAsync("POST", "games/$gameId/comments", PostCommentResponse::class.java, body = body)

    fun postGameComment(gameId: String, content: String): CompletableFuture<PicaResult<PostCommentResponse>> =
        postGameComment(gameId, CommentBody(content))

    fun replyComment(commentId: String, body: CommentBody): CompletableFuture<PicaResult<PostCommentResponse>> =
        client.callDataAsync("POST", "comments/$commentId", PostCommentResponse::class.java, body = body)

    fun replyComment(commentId: String, content: String): CompletableFuture<PicaResult<PostCommentResponse>> =
        replyComment(commentId, CommentBody(content))

    @JvmOverloads
    fun getCommentChildren(commentId: String, page: Int = 1): CompletableFuture<PicaResult<CommentsResponse>> =
        client.callDataAsync("GET", "comments/$commentId/childrens", CommentsResponse::class.java, query = mapOf("page" to page))

    fun likeComment(commentId: String): CompletableFuture<PicaResult<ActionResponse>> =
        client.callDataAsync("POST", "comments/$commentId/like", ActionResponse::class.java)

    fun hideComment(commentId: String): CompletableFuture<PicaResult<MessageResponse>> =
        client.callDataAsync("POST", "comments/$commentId/hide", MessageResponse::class.java)

    fun reportComment(commentId: String): CompletableFuture<PicaResult<MessageResponse>> =
        client.callDataAsync("POST", "comments/$commentId/report", MessageResponse::class.java)

    fun topComment(commentId: String): CompletableFuture<PicaResult<CommentPostToTopResponse>> =
        client.callDataAsync("POST", "comments/$commentId/top", CommentPostToTopResponse::class.java)

    @JvmOverloads
    fun getMyComments(page: Int = 1): CompletableFuture<PicaResult<ProfileCommentsResponse>> =
        client.callDataAsync("GET", "users/my-comments", ProfileCommentsResponse::class.java, query = mapOf("page" to page))

    // endregion

    // region 游戏 / games -----------------------------------------------------------------------

    @JvmOverloads
    fun getGames(page: Int = 1): CompletableFuture<PicaResult<GameListResponse>> =
        client.callDataAsync("GET", "games", GameListResponse::class.java, query = mapOf("page" to page))

    fun getGameDetail(gameId: String): CompletableFuture<PicaResult<GameDetailResponse>> =
        client.callDataAsync("GET", "games/$gameId", GameDetailResponse::class.java)

    fun likeGame(gameId: String): CompletableFuture<PicaResult<ActionResponse>> =
        client.callDataAsync("POST", "games/$gameId/like", ActionResponse::class.java)

    // endregion

    // region 用户 / users -----------------------------------------------------------------------

    fun getUserProfile(): CompletableFuture<PicaResult<UserProfileResponse>> =
        client.callDataAsync("GET", "users/profile", UserProfileResponse::class.java)

    fun getUserProfileById(userId: String): CompletableFuture<PicaResult<UserProfileResponse>> =
        client.callDataAsync("GET", "users/$userId/profile", UserProfileResponse::class.java)

    fun getUserProfileDirty(userId: String): CompletableFuture<PicaResult<UserProfileDirtyResponse>> =
        client.callDataAsync("POST", "users/$userId/dirty", UserProfileDirtyResponse::class.java)

    fun punchIn(): CompletableFuture<PicaResult<PunchInResponse>> =
        client.callDataAsync("POST", "users/punch-in", PunchInResponse::class.java)

    fun updateProfile(body: UpdateProfileBody): CompletableFuture<PicaResult<RegisterResponse>> =
        client.callRawAsync("PUT", "users/profile", RegisterResponse::class.java, body = body)

    fun updateAvatar(body: AvatarBody): CompletableFuture<PicaResult<PutAvatarResponse>> =
        client.callDataAsync("PUT", "users/avatar", PutAvatarResponse::class.java, body = body)

    fun updateAvatar(base64Image: String): CompletableFuture<PicaResult<PutAvatarResponse>> {
        val dataUri = if (base64Image.startsWith("data:image")) base64Image else "data:image/jpeg;base64,$base64Image"
        return updateAvatar(AvatarBody(dataUri))
    }

    fun updateAvatar(imageFile: File): CompletableFuture<PicaResult<PutAvatarResponse>> =
        CompletableFuture.supplyAsync {
            val bytes = imageFile.readBytes()
            java.util.Base64.getEncoder().encodeToString(bytes)
        }.thenCompose { updateAvatar(it) }

    fun changePassword(body: ChangePasswordBody): CompletableFuture<PicaResult<RegisterResponse>> =
        client.callRawAsync("PUT", "users/password", RegisterResponse::class.java, body = body)

    fun changePassword(oldPassword: String, newPassword: String): CompletableFuture<PicaResult<RegisterResponse>> =
        changePassword(ChangePasswordBody(oldPassword, newPassword))

    fun updatePicaId(body: UpdatePicaIdBody): CompletableFuture<PicaResult<Unit>> =
        client.callDataAsync("PUT", "users/update-id", Unit::class.java, body = body)

    fun updatePicaId(email: String, newName: String): CompletableFuture<PicaResult<Unit>> =
        updatePicaId(UpdatePicaIdBody(email, newName))

    fun updateQandA(body: UpdateQandABody): CompletableFuture<PicaResult<Unit>> =
        client.callDataAsync("PUT", "users/update-qa", Unit::class.java, body = body)

    fun updateUserTitle(userId: String, body: UpdateUserTitleBody): CompletableFuture<PicaResult<RegisterResponse>> =
        client.callRawAsync("PUT", "users/$userId/title", RegisterResponse::class.java, body = body)

    fun updateUserTitle(userId: String, title: String): CompletableFuture<PicaResult<RegisterResponse>> =
        updateUserTitle(userId, UpdateUserTitleBody(title))

    fun adjustExp(body: AdjustExpBody): CompletableFuture<PicaResult<RegisterResponse>> =
        client.callRawAsync("POST", "utils/adjust-exp", RegisterResponse::class.java, body = body)

    fun blockUser(body: UserIdBody): CompletableFuture<PicaResult<Unit>> =
        client.callDataAsync("POST", "utils/block-user", Unit::class.java, body = body)

    fun blockUser(userId: String): CompletableFuture<PicaResult<Unit>> =
        blockUser(UserIdBody(userId))

    fun removeComment(body: UserIdBody): CompletableFuture<PicaResult<Unit>> =
        client.callDataAsync("POST", "utils/remove-comment", Unit::class.java, body = body)

    fun removeComment(commentId: String): CompletableFuture<PicaResult<Unit>> =
        removeComment(UserIdBody(commentId))

    @JvmOverloads
    fun getNotifications(page: Int = 1): CompletableFuture<PicaResult<NotificationsResponse>> =
        client.callDataAsync("GET", "users/notifications", NotificationsResponse::class.java, query = mapOf("page" to page))

    // endregion
}
