# 聊天室（Socket.IO）文档

`ChatroomClient` 完整移植自原 `ChatroomViewModel` / `ChatroomScreens`，使用旧版
Socket.IO 1.x 协议 + websocket 传输。

---

## 目录

- [快速开始](#快速开始)
- [构造与生命周期](#构造与生命周期)
- [监听事件](#监听事件)
- [接收事件对照表](#接收事件对照表)
- [发送方法](#发送方法)
- [系统动作](#系统动作)
- [消息模型](#消息模型)
- [完整示例](#完整示例)
- [注意事项](#注意事项)

---

## 快速开始

```kotlin
import com.picaapi.ChatroomClient
import com.picaapi.ChatroomCallbacks

val chat = ChatroomClient()
chat.listener = ChatroomCallbacks(
    connected = { println("已连接") },
    message = { println("${it.name}: ${it.message}") },
)
chat.setProfile(userProfile)   // UserProfileObject
chat.connect()
chat.sendText("hello")
```

---

## 构造与生命周期

```kotlin
class ChatroomClient(
    defaultRoomUrl: String = "https://chat.picacomic.com",
    imageServer: String? = null,
    okHttpClient: OkHttpClient = defaultSocketClient(),
)
```

| 属性 | 类型 | 说明 |
| --- | --- | --- |
| `roomUrl` | `String?` | 当前房间地址，只读 |
| `profile` | `UserProfileObject?` | 当前资料，只读 |
| `isConnected` | `Boolean` | 是否已连接，只读 |
| `listener` | `ChatroomListener?` | 事件监听器，可读写 |

| 方法 | 说明 |
| --- | --- |
| `setProfile(userProfile)` | 更新资料（用于构造消息与 `init`） |
| `connect(roomUrl?, userProfile?)` | 连接；两参数都有默认值，可无参调用。会先断开旧连接 |
| `disconnect()` | 关闭连接并清空状态 |

```kotlin
chat.connect()                                  // 用默认房间 + 已设置的资料
chat.connect("https://chat.example.com")        // 指定房间
chat.connect(userProfile = profile)             // 只传资料
chat.connect("https://chat.example.com", profile)
```

---

## 监听事件

`ChatroomListener` 是接口，所有方法都有默认空实现，按需重写即可。
Java 端因启用了 JVM 默认方法，也可以只重写需要的方法。

### 方式一：`ChatroomCallbacks`（lambda，推荐 Kotlin）

```kotlin
chat.listener = ChatroomCallbacks(
    connected = { },
    disconnected = { reason -> },
    message = { msg -> },
    image = { msg -> },
    audio = { msg -> },
    privateMessage = { msg -> },
    ads = { msg -> },
    onlineCountChanged = { count -> },
    userJoined = { count -> },
    userLeft = { count -> },
    notification = { text -> },
    kicked = { text -> },
    profileUpdated = { json -> },
    characterIconChanged = { character -> },
    titleChanged = { userId, title -> },
    error = { throwable -> },
)
```

### 方式二：实现接口

```kotlin
chat.listener = object : ChatroomListener {
    override fun onConnected() { println("connected") }
    override fun onMessage(message: ChatMessageObject) { println(message.message) }
}
```

### 方式三：Java 匿名类

```java
chat.setListener(new ChatroomListener() {
    @Override public void onMessage(ChatMessageObject message) {
        System.out.println(message.getName() + ": " + message.getMessage());
    }
});
```

### `ChatroomListener` 全量回调

| 回调 | 参数 | 说明 |
| --- | --- | --- |
| `onConnected()` | — | 连接成功 |
| `onDisconnected(reason)` | `String?` | 断开 |
| `onMessage(message)` | `ChatMessageObject` | 公共文本消息 |
| `onImage(message)` | `ChatMessageObject` | 公共图片 |
| `onAudio(message)` | `ChatMessageObject` | 公共语音 |
| `onPrivateMessage(message)` | `ChatMessageObject` | 私聊消息（含 `to`） |
| `onAds(message)` | `ChatMessageObject` | 广告 |
| `onOnlineCountChanged(count)` | `String` | 在线人数变化 |
| `onUserJoined(count)` | `String` | 有人加入 |
| `onUserLeft(count)` | `String` | 有人离开 |
| `onNotification(text)` | `String` | 服务端通知 |
| `onKicked(text)` | `String` | 被踢出（之后自动断开） |
| `onProfileUpdated(json)` | `JSONObject` | 服务端更新你的资料 |
| `onCharacterIconChanged(character)` | `String` | 角色图标变化 |
| `onTitleChanged(userId, title)` | `String, String` | 头衔变化 |
| `onError(error)` | `Throwable` | 连接/发送异常 |

---

## 接收事件对照表

| Socket.IO 事件 | 消息类型 | 触发的回调 |
| --- | --- | --- |
| `connect` | — | `onConnected`（并上报 `init`） |
| `disconnect` | — | `onDisconnected` |
| `broadcast_message` | `0` | `onMessage` |
| `broadcast_image` | `1` | `onImage` |
| `broadcast_audio` | `2` | `onAudio` |
| `broadcast_ads` | `11` | `onAds` |
| `got_private_message` | `3` | `onPrivateMessage` |
| `new_connection` | — | `onOnlineCountChanged` + `onUserJoined` |
| `connection_close` | — | `onOnlineCountChanged` + `onUserLeft` |
| `receive_notification` | — | `onNotification` |
| `kick` | — | `onKicked` 并 `disconnect()` |
| `set_profile` | — | `onProfileUpdated` |
| `change_character_icon` | — | `onCharacterIconChanged` |
| `change_title` | — | `onTitleChanged` |
| `error` | — | `onError` |

消息类型常量：`ChatroomClient.TYPE_TEXT`/`TYPE_IMAGE`/`TYPE_AUDIO`/`TYPE_PRIVATE`/`TYPE_ADS`。

---

## 发送方法

| 方法 | 事件 | 说明 |
| --- | --- | --- |
| `sendMessage(message)` | `send_message` | 直接发已构建的消息 |
| `sendPrivateMessage(message)` | `send_private_message` | 私聊 |
| `sendImage(message)` | `send_image` | base64 在 `image` 字段 |
| `sendAudio(message)` | `send_audio` | base64 在 `audio` 字段 |
| `sendText(text, at, replyName, reply, blockUserId)` | `send_message` | 自动构建并发送 |
| `sendPrivateText(text, to, at, replyName, reply, blockUserId)` | `send_private_message` | 自动构建并发送 |
| `sendImageBase64(base64Image)` | `send_image` | 自动构建并发送 |
| `sendAudioBase64(base64Audio)` | `send_audio` | 自动构建并发送 |
| `emitSystemAction(action, type)` | `system_action` | 见下节 |
| `sendServerTalk(text)` | `send_message` | 发送 `##server talk @text` |
| `emitGameAction(action, data)` | `game` | 小游戏动作 |

`sendText` 等构建类方法返回发送的 `ChatMessageObject`，未设置 `profile` 时返回 `null`。

```kotlin
chat.sendText("你好", at = "嗶咔_某人", replyName = "某人", reply = "上一条")
chat.sendPrivateText("悄悄话", to = ChatroomToObject("某人", uniqueId, userId))
chat.sendImageBase64(base64)
```

---

## 系统动作

| 方法 | 动作 | 说明 |
| --- | --- | --- |
| `toggleTimestamp(email, enabled)` | `time` | 显示/隐藏时间 |
| `toggleImage(email, enabled)` | `image` | 允许/禁止发图 |
| `mute(email, name, userId, minutes)` | `mute` | 禁言 N 分钟 |
| `setAvatar(email, name, userId, no)` | `set_avatar` | 设置预设头像 |
| `setAvatarExtra(email, name, userId, extra)` | `set_avatar` | 设置额外头像 |
| `changeTitle(email, name, userId, title)` | `set_title` | 修改头衔 |

自定义动作：

```kotlin
chat.emitSystemAction(TimeAction("time", email, true, myName), TimeAction::class.java)
```

相关动作类在 `com.picacomic.fregata.objects.chatroomObjects`：
`TimeAction`、`ImageAction`、`MuteAction`、`SetAvatarAction`、`SetAvatarExtraAction`、`ChangeTitleAction`。

---

## 消息模型

`ChatMessageObject`（`com.picacomic.fregata.objects`）主要字段：

| 字段 | 说明 |
| --- | --- |
| `user_id` / `unique_id` | 用户 ID / 唯一 ID |
| `email` / `name` / `title` / `gender` | 用户信息 |
| `avatar` | 头像地址 |
| `level` / `verified` | 等级 / 认证 |
| `message` | 文本内容 |
| `image` / `audio` | base64 内容 |
| `at` | @ 目标 |
| `reply_name` / `reply` | 回复目标与内容 |
| `block_user_id` | 屏蔽用户 |
| `type` | 消息类型 |
| `to` | 私聊目标 `ChatroomToObject` |

工具方法：

```kotlin
// 用当前 profile 构造消息（不发送）
val msg = chat.buildMessage(text = "hi", image = "", audio = "", type = ChatroomClient.TYPE_TEXT)

// 解析服务端原始 JSON
val parsed = chat.parseMessage(jsonObject, ChatroomClient.TYPE_TEXT)
```

---

## 完整示例

```kotlin
class ChatController(private val profile: UserProfileObject) {
    private val chat = ChatroomClient()

    init {
        chat.listener = ChatroomCallbacks(
            connected = { println("已连接 ${chat.roomUrl}") },
            message = { println("${it.name}: ${it.message}") },
            privateMessage = { println("[私聊] ${it.name}: ${it.message}") },
            onlineCountChanged = { println("在线人数: $it") },
            notification = { println("[通知] $it") },
            kicked = { println("被踢出: $it") },
            error = { it.printStackTrace() },
        )
    }

    fun start() {
        chat.setProfile(profile)
        chat.connect()
    }

    fun send(text: String) = chat.sendText(text)

    fun sendImage(base64: String) = chat.sendImageBase64(base64)

    fun privateTo(target: ChatMessageObject) {
        chat.sendPrivateText(
            "hi",
            ChatroomToObject(target.name.orEmpty(), target.uniqueId.orEmpty(), target.userId.orEmpty()),
        )
    }

    fun stop() = chat.disconnect()
}
```

---

## 注意事项

1. **先设置资料再连接**：`connect` 的 `init` 会用当前 `profile`，为空则只连接、不上报。
2. **回调线程**：所有监听回调运行在 Socket.IO 网络线程，更新 UI 请切回主线程。
3. **连接状态**：`isConnected` 在 `connect` 事件后为 `true`，`disconnect`/`kick` 后为 `false`。
4. **SSRF/地址**：`connect` 传入的 URL 直接用于建连；生产环境请做白名单校验。
5. **重连**：底层 Socket.IO 默认开启自动重连（`reconnection = true`），断开后会自动尝试。
6. **被踢**：收到 `kick` 后客户端会自动 `disconnect()`，无需重复调用。
