# REST 接口参考

`PicaClient` 的全部接口。所有函数**同步阻塞**，返回 `PicaResult<T>`。

- `auth` 列：`是` 表示需要已登录（会自动带 `authorization` 头）。
- `路径`：相对于 `PicaConfig.baseUrl`。
- 返回类型中的 `XxxResponse` 若无特别说明都在 `com.picacomic.fregata.objects.responses`，
  分页类在 `com.picacomic.fregata.objects.responses.DataClass.*`（见文末）。
- `page` 默认为 `1`。

---

## 登录鉴权

| 函数 | HTTP | 路径 | auth | 返回 |
| --- | --- | --- | --- | --- |
| `signIn(body: SignInBody)` | POST | `auth/sign-in` | 否 | `PicaResult<SignInResponse>` |
| `login(email, password)` | POST | `auth/sign-in` | 否 | `PicaResult<SignInResponse>` |
| `register(body: RegisterBody)` | POST | `auth/register` | 否 | `PicaResult<RegisterResponse>` |
| `forgotPassword(body: ForgotPasswordBody)` | POST | `auth/forgot-password` | 否 | `PicaResult<ForgotPasswordResponse>` |
| `resetPassword(body: ResetPasswordBody)` | POST | `auth/reset-password` | 否 | `PicaResult<PasswordResponse>` |

`login` 是 `signIn` 的便捷版：内部构造 `SignInBody`，成功后自动调用 `updateAuthorization`。

```kotlin
pica.login("a@b.com", "pw").onSuccess { pica.updateAuthorization(it.token) }
```

---

## 初始化与元数据

| 函数 | HTTP | 路径 | 返回 |
| --- | --- | --- | --- |
| `init()` | GET | `init?platform=android` | `PicaResult<InitialResponse>` |
| `getCategories()` | GET | `categories` | `PicaResult<CategoryResponse>` |
| `getBanners()` | GET | `banners` | `PicaResult<BannersResponse>` |
| `getKeywords()` | GET | `keywords` | `PicaResult<KeywordsResponse>` |
| `getCollections()` | GET | `collections` | `PicaResult<CollectionsResponse>` |
| `getChatroomList()` | GET | `chat` | `PicaResult<ChatroomListResponse>` |
| `getPicaApps()` | GET | `pica-apps` | `PicaResult<PicaAppsResponse>` |
| `getAnnouncements(page = 1)` | GET | `announcements` | `PicaResult<AnnouncementsResponse>` |
| `getApplications(page = 1)` | GET | `applications?platform=android` | `PicaResult<ApplicationsResponse>` |

---

## 漫画

| 函数 | HTTP | 路径 | 说明 |
| --- | --- | --- | --- |
| `getComics(page, category, tag, author, finished, sort, categoryType, categoryArea)` | GET | `comics` | 列表/搜索 |
| `advancedSearchComics(page, body: SortingBody)` | POST | `comics/advanced-search` | 高级搜索 |
| `getRandomComics()` | GET | `comics/random` | 随机 |
| `getLeaderboard(timeType, category)` | GET | `comics/leaderboard?tt=&ct=` | 排行榜 |
| `getKnightLeaderboard()` | GET | `comics/knight-leaderboard` | 骑士榜 |
| `getComicDetail(comicId)` | GET | `comics/{comicId}` | 详情 |
| `getComicEpisodes(comicId, page = 1)` | GET | `comics/{comicId}/eps` | 章节 |
| `getComicPagesByOrder(comicId, order, page = 1)` | GET | `comics/{comicId}/order/{order}/pages` | 按序取页 |
| `getEpisodePages(episodeId, page = 1)` | GET | `eps/{epsId}/pages` | 按 ID 取页 |
| `getComicRecommendation(comicId)` | GET | `comics/{comicId}/recommendation` | 推荐 |
| `likeComic(comicId)` | POST | `comics/{comicId}/like` | 点赞/取消 |
| `favouriteComic(comicId)` | POST | `comics/{comicId}/favourite` | 收藏/取消 |
| `getFavouriteComics(sort, page = 1)` | GET | `users/favourite` | 我的收藏 |

返回类型：

| 函数 | 返回 |
| --- | --- |
| `getComics` / `advancedSearchComics` / `getFavouriteComics` | `PicaResult<ComicListResponse>` |
| `getRandomComics` / `getComicRecommendation` | `PicaResult<ComicRandomListResponse>` |
| `getLeaderboard` | `PicaResult<LeaderboardResponse>` |
| `getKnightLeaderboard` | `PicaResult<LeaderboardKnightResponse>` |
| `getComicDetail` | `PicaResult<ComicDetailResponse>` |
| `getComicEpisodes` | `PicaResult<ComicEpisodeResponse>` |
| `getComicPagesByOrder` / `getEpisodePages` | `PicaResult<ComicPagesResponse>` |
| `likeComic` / `favouriteComic` | `PicaResult<ActionResponse>` |

### `getComics` 参数

| 参数 | 类型 | 对应 query | 说明 |
| --- | --- | --- | --- |
| `page` | `Int` | `page` | 页码 |
| `category` | `String?` | `c` | 分类 |
| `tag` | `String?` | `t` | 标签 |
| `author` | `String?` | `a` | 作者 |
| `finished` | `String?` | `f` | 完结状态 |
| `sort` | `String?` | `s` | 排序，如 `dd`、`da`、`ld` |
| `categoryType` | `String?` | `ct` | 分类类型 |
| `categoryArea` | `String?` | `ca` | 分类地区 |

---

## 评论

| 函数 | HTTP | 路径 | 返回 |
| --- | --- | --- | --- |
| `getComicComments(comicId, page = 1)` | GET | `comics/{comicId}/comments` | `PicaResult<CommentsResponse>` |
| `postComicComment(comicId, body: CommentBody)` | POST | `comics/{comicId}/comments` | `PicaResult<PostCommentResponse>` |
| `getGameComments(gameId, page = 1)` | GET | `games/{gameId}/comments` | `PicaResult<CommentsResponse>` |
| `postGameComment(gameId, body: CommentBody)` | POST | `games/{gameId}/comments` | `PicaResult<PostCommentResponse>` |
| `replyComment(commentId, body: CommentBody)` | POST | `comments/{commentId}` | `PicaResult<PostCommentResponse>` |
| `getCommentChildren(commentId, page = 1)` | GET | `comments/{commentId}/childrens` | `PicaResult<CommentsResponse>` |
| `likeComment(commentId)` | POST | `comments/{commentId}/like` | `PicaResult<ActionResponse>` |
| `hideComment(commentId)` | POST | `comments/{commentId}/hide` | `PicaResult<MessageResponse>` |
| `reportComment(commentId)` | POST | `comments/{commentId}/report` | `PicaResult<MessageResponse>` |
| `topComment(commentId)` | POST | `comments/{commentId}/top` | `PicaResult<CommentPostToTopResponse>` |
| `getMyComments(page = 1)` | GET | `users/my-comments` | `PicaResult<ProfileCommentsResponse>` |

---

## 游戏

| 函数 | HTTP | 路径 | 返回 |
| --- | --- | --- | --- |
| `getGames(page = 1)` | GET | `games` | `PicaResult<GameListResponse>` |
| `getGameDetail(gameId)` | GET | `games/{gameId}` | `PicaResult<GameDetailResponse>` |
| `likeGame(gameId)` | POST | `games/{gameId}/like` | `PicaResult<ActionResponse>` |

---

## 用户

| 函数 | HTTP | 路径 | 返回 |
| --- | --- | --- | --- |
| `getUserProfile()` | GET | `users/profile` | `PicaResult<UserProfileResponse>` |
| `getUserProfileById(userId)` | GET | `users/{userId}/profile` | `PicaResult<UserProfileResponse>` |
| `getUserProfileDirty(userId)` | POST | `users/{userId}/dirty` | `PicaResult<UserProfileDirtyResponse>` |
| `punchIn()` | POST | `users/punch-in` | `PicaResult<PunchInResponse>` |
| `updateProfile(body: UpdateProfileBody)` | PUT | `users/profile` | `PicaResult<RegisterResponse>` |
| `updateAvatar(body: AvatarBody)` | PUT | `users/avatar` | `PicaResult<PutAvatarResponse>` |
| `changePassword(body: ChangePasswordBody)` | PUT | `users/password` | `PicaResult<RegisterResponse>` |
| `updatePicaId(body: UpdatePicaIdBody)` | PUT | `users/update-id` | `PicaResult<Unit>` |
| `updateQandA(body: UpdateQandABody)` | PUT | `users/update-qa` | `PicaResult<Unit>` |
| `updateUserTitle(userId, body: UpdateUserTitleBody)` | PUT | `users/{userId}/title` | `PicaResult<RegisterResponse>` |
| `adjustExp(body: AdjustExpBody)` | POST | `utils/adjust-exp` | `PicaResult<RegisterResponse>` |
| `blockUser(body: UserIdBody)` | POST | `utils/block-user` | `PicaResult<Unit>` |
| `removeComment(body: UserIdBody)` | POST | `utils/remove-comment` | `PicaResult<Unit>` |
| `getNotifications(page = 1)` | GET | `users/notifications` | `PicaResult<NotificationsResponse>` |

---

## 请求体类型

都在 `com.picacomic.fregata.objects.requests`：

| 类型 | 字段 |
| --- | --- |
| `SignInBody(email, password)` | email, password |
| `RegisterBody(...)` | 注册信息 |
| `ForgotPasswordBody(...)` | email |
| `ResetPasswordBody(...)` | email, code, password |
| `ChangePasswordBody(...)` | oldPassword, newPassword |
| `UpdateProfileBody(...)` | 昵称、生日、性別等 |
| `AvatarBody(...)` | 头像 |
| `UpdatePicaIdBody(...)` | picaId |
| `UpdateQandABody(...)` | 密保问题 |
| `UpdateUserTitleBody(title)` | title |
| `AdjustExpBody(...)` | userId, exp |
| `UserIdBody(userId)` | userId |
| `CommentBody(...)` | content, toCommentId 等 |
| `SortingBody(...)` | 高级搜索排序条件 |

具体字段请查看对应源文件（已带中英双语注释）。

---

## 返回数据说明

### 通用信封

除登录/注册/找回/重置密码外，其余接口的 HTTP 响应形如：

```json
{ "code": 200, "message": "success", "data": { ... } }
```

`PicaResult.Success.data` 已经是解包后的 `data`，即上表返回类型。

### 响应包裹字段

部分返回类型本身只是信封，真正的数据在下列字段中：

| 返回类型 | 字段（Kotlin 属性 / Java getter） | 类型 |
| --- | --- | --- |
| `ComicDetailResponse` | `comic` / `getComic()` | `ComicDetailObject` |
| `ComicListResponse` | `comics` / `getComics()` | `ComicListData` |
| `ComicEpisodeResponse` | `eps` / `getEps()` | `ComicEpisodeData` |
| `ComicPagesResponse` | `pages` / `getPages()`，`ep` / `getEp()` | `ComicPageData`、`ComicEpisodeObject` |
| `ComicRandomListResponse` | `comics` | `List<ComicListObject>` |
| `CommentsResponse` | `comments`，`topComments` | `CommentsData`、`List<CommentObject>` |
| `ProfileCommentsResponse` | `comments` | `ProfileCommentsData` |
| `GameListResponse` | `games` | `GameListData` |
| `GameDetailResponse` | `game` | `GameDetailObject` |
| `NotificationsResponse` | `notifications` | `NotificationsData` |
| `AnnouncementsResponse` | `announcements` | `AnnouncementsData` |
| `ApplicationsResponse` | `applications` | `ApplicationsData` |
| `UserProfileResponse` | `user` | `UserProfileObject` |
| `ChatroomListResponse` | `chatList` | `List<ChatroomListObject>` |
| `InitialResponse` | `categories`、`imageServer`、`latestApplication`、`notification`、`isPunched`、`isIdUpdated` | 混合 |
| `CategoryResponse` | `categories` | `List<CategoryObject>` |
| `BannersResponse` | `banners` | `List<BannerObject>` |
| `KeywordsResponse` | `keywords` | `List<String>` |
| `CollectionsResponse` | `collections` | `List<CollectionObject>` |
| `PicaAppsResponse` | `apps` | `List<PicaAppObject>` |
| `LeaderboardResponse` | `comics` | `List<LeaderboardComicListObject>` |
| `LeaderboardKnightResponse` | `users` | `List<LeaderboardKnightObject>` |
| `PunchInResponse` | `res` | `PunchInObject` |
| `PutAvatarResponse` | `avatar` | `ThumbnailObject` |
| `ActionResponse` | `action` | `String` |
| `MessageResponse` | `message` | `String` |
| `CommentPostToTopResponse` | `isTop` | `boolean` |
| `UserProfileDirtyResponse` | `dirty` | `boolean` |
| `ForgotPasswordResponse` | `question1` / `question2` / `question3` | `String` |
| `PasswordResponse` | `password` | `String` |
| `SignInResponse` | `token` | `String` |
| `RegisterResponse` | 公共字段 `code` / `message` | `int` / `String` |
| `PostCommentResponse` | `comment`、`comicId`、`commentId`、`parentId`、`createdAt`、`likesCount`、`childsCount` | 混合 |

### 分页数据

`ComicListData` / `ComicEpisodeData` / `ComicPageData` / `CommentsData` /
`NotificationsData` / `GameListData` / `PostCommentData` / `ProfileCommentsData` /
`AnnouncementsData` / `ApplicationsData` 统一含：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `total` | `int` | 总数 |
| `limit` | `int` | 每页数量 |
| `page` | `int` | 当前页 |
| `pages` | `int` | 总页数 |
| `docs` | `List<T>` | 条目列表 |

### 常用条目模型

| 类型 | 说明 |
| --- | --- |
| `ComicListObject` | 漫画列表项（`_id`→comicId, `epsCount`→episodeCount） |
| `ComicDetailObject` | 漫画详情 |
| `ComicEpisodeObject` | 章节 |
| `ComicPageObject` | 漫画图片页（`media: ThumbnailObject`） |
| `CommentObject` / `CommentWithReplyObject` | 评论 |
| `UserProfileObject` | 用户资料 |
| `ThumbnailObject` | 图片资源（`fileServer`, `path`, `originalName`） |

---

## 调用示例

### Kotlin 示例

```kotlin
import com.picaapi.*

val pica = PicaClient {
    imageQuality = PicaImageQuality.HIGH
}

// 1. 登录
pica.login("user@example.com", "password").getOrThrow()

// 2. 漫画搜索 / 检索
val comics = pica.searchComics("naruto", sort = PicaSort.NEWEST).getOrThrow()
comics.comics.docs?.forEach { println("${it.title}: ${it.thumb.toImageUrl()}") }

// 3. 详情与按章节取页
val comicId = comics.comics.docs!!.first().comicId
val detail = pica.getComicDetail(comicId).getOrThrow()
val pages = pica.getComicPagesByOrder(comicId, order = 1).getOrThrow()
val urls = pages.pages.docs?.map { it.toImageUrl() }

// 4. 评论与互动
pica.postComicComment(comicId, "神作！")
pica.favouriteComic(comicId)

// 5. 异步调用 (CompletableFuture)
pica.async.getComicDetail(comicId).thenAccept { res ->
    res.onSuccess { println(it.comic.title) }
}
```

### Java 示例

```java
import com.picaapi.*;
import com.picacomic.fregata.objects.responses.DataClass.ComicListResponse.ComicListResponse;

// 1. 初始化客户端
PicaClient pica = PicaClient.create(PicaConfig.builder()
    .imageQuality(PicaImageQuality.HIGH)
    .build());

// 2. 登录
pica.login("user@example.com", "password")
    .onSuccess(res -> System.out.println("登录成功: " + res.getToken()))
    .onFailure(err -> System.err.println("登录失败: " + err.getMessage()));

// 3. 使用 ComicQuery 或 @JvmOverloads 查询
PicaResult<ComicListResponse> result = pica.getComics(ComicQuery.builder()
    .category("Cosplay")
    .sort(PicaSort.NEWEST)
    .build());

// 4. 便捷方法
pica.searchComics("naruto", PicaSort.NEWEST);
pica.postComicComment("comicId", "神作！");

// 5. 异步非阻塞调用 (CompletableFuture)
pica.async().getComicDetail("comicId").thenAccept(res -> {
    res.onSuccess(detail -> System.out.println(detail.getComic().getTitle()));
});
```

