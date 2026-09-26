# picapi

从 [PicACG / 嗶咔漫画] 客户端中提取出来的独立 **Kotlin / JVM API 库**，不含任何 Android 依赖。

- **Gradle** 构建，纯 JVM（Java 17+）
- **OkHttp 5.x** 直连 REST，无 Retrofit
- **完整 Socket.IO 聊天室**（内置 `socket.io-client` 与 `engine.io-client` 源码）
- 复刻原客户端的 **请求签名、TLS、DNS、image-quality** 等网络参数
- **线程安全**，接口全部封装为函数
- 模型类已反混淆，带中英双语注释

> 本项目仅用于学习与研究网络协议，请遵守相关服务条款与当地法律法规。

---

## 目录

- [功能特性](#功能特性)
- [环境与构建](#环境与构建)
- [引入项目](#引入项目)
- [快速开始](#快速开始)
- [全局单例](#全局单例)
- [结果处理 `PicaResult`](#结果处理-picaresult)
- [配置 `PicaConfig`](#配置-picaconfig)
- [REST 接口](#rest-接口)
- [聊天室](#聊天室)
- [图片地址](#图片地址)
- [旧版 Waka 服务](#旧版-waka-服务)
- [线程安全](#线程安全)
- [签名与协议](#签名与协议)
- [错误处理](#错误处理)
- [目录结构](#目录结构)
- [FAQ](#faq)

更多细节见：

- [docs/API.md](docs/API.md) —— 全部 REST 接口签名与说明
- [docs/CHATROOM.md](docs/CHATROOM.md) —— 聊天室事件与用法
- [docs/CONFIG.md](docs/CONFIG.md) —— 配置项完整参考

---

## 功能特性

| 模块 | 说明 |
| --- | --- |
| 登录鉴权 | 登录 / 注册 / 找回密码 / 重置密码 |
| 初始化 | `init`、分类、轮播图、关键词、合集、Pica 小应用、公告、应用列表 |
| 漫画 | 列表、高级搜索、随机、排行榜、骑士榜、详情、章节、分页、推荐、点赞、收藏 |
| 评论 | 漫画/游戏评论、发表、回复、子评论、点赞、隐藏、举报、置顶、我的评论 |
| 游戏 | 列表、详情、点赞 |
| 用户 | 资料、他人资料、增量资料、签到、改资料/头像/密码/PicaID/密保/头衔、拉黑、删除评论、通知 |
| 聊天室 | Socket.IO 连接、收发文本/图片/语音/私聊、系统动作、游戏动作 |

---

## 环境与构建

要求：

- JDK 17+（`JAVA_HOME` 指向 JDK 17）
- 无需 Android SDK

```bash
./gradlew build            # 编译 + 打包
./gradlew jar              # 只打 jar
```

产物：

```
build/libs/picapi-1.0.0.jar
build/libs/picapi-1.0.0-sources.jar
```

---

## 引入项目

### 方式一：复合构建（推荐，直接用源码）

```groovy
// settings.gradle
includeBuild('../picapi')

// build.gradle
dependencies {
    implementation 'com.picaapi:picapi:1.0.0'
}
```

### 方式二：本地 jar

```groovy
dependencies {
    implementation files('libs/picapi-1.0.0.jar')
    // 传递依赖需自行声明
    implementation 'com.squareup.okhttp3:okhttp:5.3.2'
    implementation 'com.google.code.gson:gson:2.10.1'
    implementation 'org.jetbrains.kotlin:kotlin-stdlib:2.2.21'
    implementation 'org.json:json:20231013'
}
```

> 会用到 Kotlin `PicaResult` 扩展函数/JVM 默认方法，建议直接用 Gradle 依赖而非裸 jar。

---

## 快速开始

### Kotlin

```kotlin
import com.picaapi.PicaClient
import com.picaapi.PicaSort
import com.picaapi.toImageUrl

val pica = PicaClient {
    imageQuality = "high"
}

// 登录（成功后自动保存 token）
pica.login("user@example.com", "password")
    .onSuccess { println("token = ${it.token}") }
    .onFailure { println("登录失败: ${it.httpCode} ${it.message}") }

// 便捷搜索或分类获取，直接 getOrThrow()
val comics = pica.searchComics("naruto", sort = PicaSort.NEWEST).getOrThrow()
comics.comics.docs?.forEach { println("${it.title}: ${it.thumb.toImageUrl()}") }

// 异步调用 (返回 CompletableFuture)
pica.async.getComicDetail("comicId").thenAccept { res ->
    res.onSuccess { println(it.comic.title) }
}
```

### Java

```java
import com.picaapi.*;
import com.picacomic.fregata.objects.responses.SignInResponse;

// 支持 fluent Builder 构造配置
PicaClient pica = PicaClient.create(PicaConfig.builder()
    .imageQuality(PicaImageQuality.HIGH)
    .enableLogging(true)
    .build());

// 函数式链式回调，无需繁杂类型强转与 Unit 返回值
pica.login("user@example.com", "password")
    .onSuccess(data -> System.out.println("token = " + data.getToken()))
    .onFailure(failure -> System.out.println("error = " + failure.getMessage()));

// 1. 无需填满 8 个参数，全接口均提供 @JvmOverloads 便捷重载
PicaResult<ComicListResponse> comics = pica.getComics();

// 2. 或使用清晰的 ComicQuery 构造查询
PicaResult<ComicListResponse> filtered = pica.getComics(
    ComicQuery.builder().category("Cosplay").sort(PicaSort.NEWEST).build()
);

// 3. 关键字搜索与直接评论重载
pica.searchComics("naruto", PicaSort.NEWEST);
pica.postComicComment("comicId", "好看！");

// 4. 原生 CompletableFuture 异步支持，避免主线程网络阻塞
pica.async().getComicDetail("comicId").thenAccept(result -> {
    result.onSuccess(detail -> System.out.println(detail.getComic().getTitle()));
});
```

---

## 全局单例

`Pica` 提供跨页面共享的 `PicaClient` 与登录令牌持久化，避免每个页面各建实例导致 token 丢失。

```kotlin
import com.picaapi.Pica
import com.picaapi.FilePicaTokenStore
import java.io.File

// Kotlin DSL 快速初始化单例
Pica.init {
    imageQuality = "high"
}

// 登录页：成功后自动持久化 token
Pica.login("user@example.com", "password")

// 其他页面：直接复用，token 自动带上
Pica.client.getComicDetail(id)

// 退出登录
Pica.logout()
```

| 成员 | 说明 |
| --- | --- |
| `Pica.init(config?, tokenStore?)` | 初始化单例，并恢复 `tokenStore` 中已保存的 token |
| `Pica.client` | 全局 `PicaClient`（未初始化时抛 `IllegalStateException`） |
| `Pica.async` / `Pica.getAsync()` | 全局异步客户端 `PicaAsyncClient` |
| `Pica.isInitialized` | 是否已初始化 |
| `Pica.isLoggedIn` | 是否已持有有效令牌 |
| `Pica.login(email, password, persist = true)` | 登录并持久化 token |
| `Pica.saveToken(token)` / `Pica.token()` | 保存 / 读取当前 token |
| `Pica.logout()` / `Pica.clearToken()` | 清除 token |

---

## 结果处理 `PicaResult`

所有接口统一返回 `PicaResult<T>`，在 Java 与 Kotlin 下均具备一流的使用体验：

```kotlin
sealed class PicaResult<out T> {
    data class Success<out T>(val data: T, val httpCode: Int = 200) : PicaResult<T>()
    data class Failure(
        val httpCode: Int?,      // 请求未到达服务器时为 null
        val errorCode: String?,  // 业务错误码
        val message: String?,
        val rawBody: String?,
        val cause: Throwable?,
    ) : PicaResult<Nothing>()
}
```

通用方法（Java / Kotlin 均可直接调用）：

| 方法 / 属性 | 说明 |
| --- | --- |
| `isSuccess` / `isSuccess()` | 是否成功 |
| `isFailure` / `isFailure()` | 是否失败 |
| `getOrNull()` | 成功返回数据，失败返回 `null` |
| `getFailureOrNull()` | 失败返回 `Failure` 结构，成功返回 `null`（Java 免强转） |
| `getOrDefault(default)` | 成功返回数据，失败返回默认值 |
| `getOrElse(fallback)` | 成功返回数据，失败通过 lambda / Function 计算备选值 |
| `getOrThrow()` | 成功返回数据，失败直接抛出 `PicaException` |
| `toOptional()` | 转换为 Java 8 `Optional<T>` |
| `onSuccess(Consumer / Block)` | 成功回调，支持链式操作 |
| `onFailure(Consumer / Block)` | 失败回调，支持链式操作 |
| `map(Function / Block)` | 转换成功数据并保持封装 |
| `flatMap(Function / Block)` | 平铺转换 |
| `fold(onSuccess, onFailure)` | 双分支折叠为目标类型 |

---

## 配置 `PicaConfig`

```kotlin
val config = PicaConfig(
    baseUrl = "https://picaapi.picacomic.com/",
    apiKey = PicaConfig.DEFAULT_API_KEY,
    hmacKey = PicaConfig.DEFAULT_HMAC_KEY,
    appVersion = "2.2.1.3.3.4",
    appBuildVersion = "45",
    appUuid = "your-stable-uuid",
    appChannel = 1,
    imageQuality = "medium",          // original / low / medium / high
    userAgent = "okhttp/3.8.1",
    authorization = null,
    timeOffsetSeconds = 0L,
    enableLogging = false,
    disableSslVerification = false,
    dnsIps = emptyList(),
)
val pica = PicaClient(config)
```

运行时更新：

```kotlin
pica.updateConfig(config)
pica.updateAuthorization("token")     // pica.authorization 也可直接读写
pica.serverTimeOffsetSeconds          // 只读，最近一次服务端时间偏移
```

完整字段说明见 [docs/CONFIG.md](docs/CONFIG.md)。

---

## REST 接口

共 **55** 个接口，全部为同步阻塞函数（请在子线程调用）。完整签名见 [docs/API.md](docs/API.md)。

快速索引：

```kotlin
// 登录鉴权
pica.signIn(SignInBody(email, password))
pica.login(email, password)                 // 自动保存 token
pica.register(RegisterBody(...))
pica.forgotPassword(ForgotPasswordBody(...))
pica.resetPassword(ResetPasswordBody(...))

// 初始化与元数据
pica.init(); pica.getCategories(); pica.getBanners(); pica.getKeywords()
pica.getCollections(); pica.getChatroomList(); pica.getPicaApps()
pica.getAnnouncements(page); pica.getApplications(page)

// 漫画
pica.getComics(page, category, tag, author, finished, sort, categoryType, categoryArea)
pica.advancedSearchComics(page, SortingBody(...))
pica.getRandomComics(); pica.getLeaderboard(timeType, category); pica.getKnightLeaderboard()
pica.getComicDetail(comicId); pica.getComicEpisodes(comicId, page)
pica.getComicPagesByOrder(comicId, order, page); pica.getEpisodePages(episodeId, page)
pica.getComicRecommendation(comicId)
pica.likeComic(comicId); pica.favouriteComic(comicId); pica.getFavouriteComics(sort, page)

// 评论
pica.getComicComments(comicId, page); pica.postComicComment(comicId, CommentBody(...))
pica.getGameComments(gameId, page);   pica.postGameComment(gameId, CommentBody(...))
pica.replyComment(commentId, CommentBody(...)); pica.getCommentChildren(commentId, page)
pica.likeComment(commentId); pica.hideComment(commentId); pica.reportComment(commentId)
pica.topComment(commentId); pica.getMyComments(page)

// 游戏
pica.getGames(page); pica.getGameDetail(gameId); pica.likeGame(gameId)

// 用户
pica.getUserProfile(); pica.getUserProfileById(userId); pica.getUserProfileDirty(userId)
pica.punchIn(); pica.updateProfile(UpdateProfileBody(...)); pica.updateAvatar(AvatarBody(...))
pica.changePassword(ChangePasswordBody(...)); pica.updatePicaId(UpdatePicaIdBody(...))
pica.updateQandA(UpdateQandABody(...)); pica.updateUserTitle(userId, UpdateUserTitleBody(...))
pica.adjustExp(AdjustExpBody(...)); pica.blockUser(UserIdBody(...))
pica.removeComment(UserIdBody(...)); pica.getNotifications(page)
```

---

## 聊天室

完整用法见 [docs/CHATROOM.md](docs/CHATROOM.md)。

```kotlin
import com.picaapi.ChatroomClient
import com.picaapi.ChatroomCallbacks

val chat = ChatroomClient()                       // 默认 https://chat.picacomic.com
chat.listener = ChatroomCallbacks(
    connected = { println("已连接") },
    disconnected = { println("已断开: $it") },
    message = { println("${it.name}: ${it.message}") },
    privateMessage = { println("私聊: ${it.message}") },
    kicked = { println("被踢: $it") },
)

chat.setProfile(userProfile)                      // UserProfileObject
chat.connect()                                    // 或 chat.connect("https://other.room")

chat.sendText("hello")
chat.sendImageBase64(base64Image)
chat.sendAudioBase64(base64Audio)
chat.sendPrivateText("hi", ChatroomToObject(name, uniqueId, userId))
chat.mute(email, name, userId, minutes = 10)      // 禁言
chat.toggleTimestamp(email, true)
chat.setAvatar(email, name, userId, no = 1)
chat.changeTitle(email, name, userId, "新头衔")

chat.disconnect()
```

Java 端可只重写关心的方法（已启用 JVM 默认方法）：

```java
chat.setListener(new ChatroomListener() {
    @Override public void onMessage(ChatMessageObject message) {
        System.out.println(message.getName() + ": " + message.getMessage());
    }
});
```

---

## 图片地址

API 返回的图片是 `ThumbnailObject`，用 `PicaImages` 拼装成绝对地址：

```kotlin
val url = PicaImages.thumbnailUrl(comic.thumb, imageServer = "https://cdn.example.com")
```

规则（与原 `utils.g.b()` 一致）：

1. `fileServer == http://lorempixel.com` → `fileServer + path`
2. 配置了 `imageServer` → `imageServer + path`
3. 否则 → `fileServer + "/static/" + path`

---

## 旧版 Waka 服务

```kotlin
val waka = PicaWakaClient()          // 默认 http://68.183.234.72/
val init = waka.init().getOrThrow()
```

该服务不使用签名协议，且原客户端始终使用「信任所有证书」策略（`trustAllSsl = true`）。

---

## 线程安全

- `OkHttpClient` 在实例内共享，本身线程安全，且每个请求创建独立的 `Call`。
- 可变状态发布方式：`config`、`authorization` 使用 `@Volatile`，服务端时间偏移使用 `AtomicLong`。
- 所有 REST 接口均为**同步阻塞**调用，请在 `Dispatchers.IO` / 子线程中执行。
- 聊天室 `emit` 使用锁保护；**`ChatroomListener` 回调运行在 Socket.IO 网络线程**，更新 UI 前请自行切换线程。

```kotlin
// 协程示例
suspend fun load() = withContext(Dispatchers.IO) {
    pica.getComics(page = 1).getOrThrow()
}
```

---

## 签名与协议

每个请求都会由 OkHttp 拦截器自动签名：

```
raw       = (path + time + nonce + method + apiKey).lowercase()
signature = hex(HMAC_SHA256(raw, key2))
```

发送的请求头：

| Header | 值 |
| --- | --- |
| `api-key` | `PicaConfig.apiKey` |
| `accept` | `application/vnd.picacomic.com.v1+json` |
| `app-channel` | `appChannel` |
| `time` | 秒级时间戳（已加服务端偏移） |
| `nonce` | 32 位随机串 |
| `signature` | 上述 HMAC |
| `app-version` | `appVersion` |
| `app-uuid` | `appUuid` |
| `image-quality` | `imageQuality` |
| `app-platform` | `android` |
| `app-build-version` | `appBuildVersion` |
| `User-Agent` | `userAgent` |
| `authorization` | 需要登录的接口才带 |

响应头 `Server-Time` 会被读取并换算成本地时钟偏移，用于后续签名。

---

## 错误处理

```kotlin
val r = pica.getComicDetail("bad-id")
when (r) {
    is PicaResult.Success -> println(r.data.comic.title)
    is PicaResult.Failure -> {
        if (r.isNetworkError) {
            println("网络异常: ${r.cause?.message}")
        } else {
            println("HTTP=${r.httpCode} code=${r.errorCode} msg=${r.message}")
            println("原始响应: ${r.rawBody}")
        }
    }
}
```

或统一转异常：

```kotlin
try {
    val detail = pica.getComicDetail(id).getOrThrow()
} catch (e: PicaException) {
    println("${e.httpCode} / ${e.errorCode} / ${e.message}")
}
```

---

## 目录结构

```
picapi/
├── build.gradle                 # Kotlin JVM 库，OkHttp + Gson
├── settings.gradle              # 根项目 + socket.io / engine.io 子模块
├── gradle.properties
├── src/main/kotlin/com/picaapi/
│   ├── PicaClient.kt            # REST 客户端（55 个接口）
│   ├── PicaConfig.kt            # 配置
│   ├── PicaResult.kt            # 结果与异常
│   ├── PicaSignature.kt         # HMAC-SHA256 签名
│   ├── PicaNetworking.kt        # TLS / DNS 策略
│   ├── PicaImages.kt            # 图片地址拼装
│   ├── PicaWakaClient.kt        # 旧版 Waka 服务
│   ├── Pica.kt                  # 全局单例 + token 持久化
│   ├── PicaTokenStore.kt        # 令牌存储接口 / 内存 / 文件实现
│   └── ChatroomClient.kt        # Socket.IO 聊天室
├── src/main/java/com/picacomic/fregata/objects/   # 反混淆后的模型类（114 个）
├── socket.io-client-java/       # Socket.IO 客户端（完整源码）
└── engine.io-client-java/       # Engine.IO 客户端（完整源码）
```

---

## FAQ

**Q：需要 Android 吗？**
A：不需要，纯 JVM 库。模型中的 Parcelable/SharedPreferences/Sugar ORM 依赖已剔除。

**Q：为什么用 OkHttp 而不是 Retrofit？**
A：需求如此。所有接口用统一 `request` 帮助函数封装，避免 Retrofit 动态代理与注解。

**Q：接口会阻塞线程吗？**
A：会。所有 REST 接口同步执行，请放在子线程。

**Q：聊天室收不到消息？**
A：确认 `setProfile` 后再 `connect`，并用 `ChatroomCallbacks` 注册 `message`/`privateMessage` 等回调。

**Q：请求报签名错误（1004 等）？**
A：检查设备时间是否准确；本库会依据 `Server-Time` 自动校正偏移。
