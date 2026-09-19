package com.picaapi

import com.google.gson.Gson
import com.picacomic.fregata.objects.ChatMessageObject
import com.picacomic.fregata.objects.ChatroomToObject
import com.picacomic.fregata.objects.UserProfileObject
import com.picacomic.fregata.objects.chatroomGameObjects.ChatroomGameEmit
import com.picacomic.fregata.objects.chatroomObjects.ChangeTitleAction
import com.picacomic.fregata.objects.chatroomObjects.ChatroomSystemAction
import com.picacomic.fregata.objects.chatroomObjects.ImageAction
import com.picacomic.fregata.objects.chatroomObjects.MuteAction
import com.picacomic.fregata.objects.chatroomObjects.SetAvatarAction
import com.picacomic.fregata.objects.chatroomObjects.SetAvatarExtraAction
import com.picacomic.fregata.objects.chatroomObjects.TimeAction
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject

/**
 * 完整的 Socket.IO 聊天室客户端，移植自原 `ChatroomViewModel` / `ChatroomScreens`。
 * Full Socket.IO chatroom client, ported from the original
 * `ChatroomViewModel` / `ChatroomScreens` implementation.
 *
 * ### 协议 / Protocol
 * 聊天室使用旧版 Socket.IO 1.x 协议并走 websocket 传输。客户端会：
 *  - 连接房间地址（默认 [DEFAULT_ROOM_URL]）；
 *  - 连接成功后发送 `init` 上报用户资料；
 *  - 发送 `send_message`、`send_private_message`、`send_image`、`send_audio`
 *    和 `system_action`；
 *  - 监听 `broadcast_*`、`got_private_message`、`new_connection`、
 *    `connection_close`、`receive_notification`、`kick`、`set_profile`、
 *    `change_character_icon`、`change_title`。
 *
 * The chatroom speaks the legacy Socket.IO 1.x protocol over a websocket
 * transport. The client connects, sends `init` with the profile, emits the
 * message/system events above and listens to the corresponding incoming events.
 *
 * ### 线程安全 / Threading
 * Socket.IO 回调运行在网络线程上，因此 [ChatroomListener] 方法内不要直接操作 UI。
 * 连接状态与资料字段为 `@Volatile`，所有 `emit` 均由 [emitLock] 加锁，保证多线程
 * 发送安全。
 *
 * Socket.IO callbacks are delivered on the library's networking threads, so
 * [ChatroomListener] methods must not touch UI directly. Connection state and
 * the profile are `@Volatile`, and every `emit` is guarded by [emitLock].
 */
class ChatroomClient @JvmOverloads constructor(
    private val defaultRoomUrl: String = DEFAULT_ROOM_URL,
    private val imageServer: String? = null,
    private val okHttpClient: OkHttpClient = defaultSocketClient(),
) {
    private val gson = Gson()
    private val emitLock = Any()

    @Volatile
    private var socket: Socket? = null

    /** 当前加入的房间地址（如有）。 / Currently joined room, if any. */
    @Volatile
    var roomUrl: String? = null
        private set

    /** 通过 `init` 上报到房间的用户资料。 / Profile announced to the room via `init`. */
    @Volatile
    var profile: UserProfileObject? = null
        private set

    /** 是否已连接。 / Whether the socket is connected. */
    @Volatile
    var isConnected: Boolean = false
        private set

    /** 所有聊天室事件的监听器。 / Listener notified for every chatroom event. */
    @Volatile
    var listener: ChatroomListener? = null

    /** 更新用于构造消息与 `init` 的资料。 / Updates the profile used to build outgoing messages and `init`. */
    fun setProfile(userProfile: UserProfileObject?) {
        profile = userProfile
    }

    /**
     * 连接到 [roomUrl]（为空时使用 [defaultRoomUrl]），会先断开旧连接。可从任意线程安全调用。
     * Connects to [roomUrl] (or [defaultRoomUrl] when null/blank). Any previous
     * connection is closed first. Safe to call repeatedly from any thread.
     */
    @JvmOverloads
    fun connect(roomUrl: String? = this.roomUrl, userProfile: UserProfileObject? = profile) {
        userProfile?.let { profile = it }
        val safeUrl = roomUrl?.takeIf { it.isNotBlank() } ?: defaultRoomUrl
        this.roomUrl = safeUrl
        disconnect()

        val options = IO.Options().apply {
            transports = arrayOf("websocket")
            callFactory = okHttpClient
            webSocketFactory = okHttpClient
            forceNew = true
        }
        val newSocket = try {
            IO.socket(safeUrl, options)
        } catch (t: Throwable) {
            try {
                IO.socket(defaultRoomUrl, options)
            } catch (t2: Throwable) {
                listener?.onError(t2)
                return
            }
        }
        socket = newSocket
        registerHandlers(newSocket)
        newSocket.connect()
    }

    /** 关闭连接并清理状态。 / Closes the socket and clears the connection state. */
    fun disconnect() {
        val current = socket
        socket = null
        isConnected = false
        current?.let {
            runCatching { it.disconnect() }
            runCatching { it.off() }
        }
    }

    // region 发送 / outbound --------------------------------------------------------------------

    /** 发送 `send_message`。 / Emits `send_message` with an already built message. */
    fun sendMessage(message: ChatMessageObject) = emit("send_message", toJson(message))

    /** 发送 `send_private_message`，需先设置 [ChatMessageObject.to]。 / Emits `send_private_message`. The message's [ChatMessageObject.to] should be set. */
    fun sendPrivateMessage(message: ChatMessageObject) = emit("send_private_message", toJson(message))

    /** 发送 `send_image`，base64 内容放在 [ChatMessageObject.image]。 / Emits `send_image` with a base64 payload in [ChatMessageObject.image]. */
    fun sendImage(message: ChatMessageObject) = emit("send_image", toJson(message))

    /** 发送 `send_audio`，base64 内容放在 [ChatMessageObject.audio]。 / Emits `send_audio` with a base64 payload in [ChatMessageObject.audio]. */
    fun sendAudio(message: ChatMessageObject) = emit("send_audio", toJson(message))

    /** 构造并发送文本 `send_message`。 / Builds and emits a plain text `send_message`. */
    @JvmOverloads
    fun sendText(
        text: String,
        at: String = "",
        replyName: String = "",
        reply: String = "",
        blockUserId: String = "",
    ): ChatMessageObject? {
        val message = buildMessage(text, "", "", TYPE_TEXT, at, replyName, reply, blockUserId) ?: return null
        sendMessage(message)
        return message
    }

    /** 构造并发送私聊文本 `send_private_message`。 / Builds and emits a private text `send_private_message`. */
    @JvmOverloads
    fun sendPrivateText(
        text: String,
        to: ChatroomToObject,
        at: String = "",
        replyName: String = "",
        reply: String = "",
        blockUserId: String = "",
    ): ChatMessageObject? {
        val message = buildMessage(text, "", "", TYPE_TEXT, at, replyName, reply, blockUserId, to) ?: return null
        sendPrivateMessage(message)
        return message
    }

    /** 构造并发送 base64 图片消息。 / Builds and emits a base64 image message. */
    fun sendImageBase64(base64Image: String): ChatMessageObject? {
        val message = buildMessage("", base64Image, "", TYPE_IMAGE) ?: return null
        sendImage(message)
        return message
    }

    /** 构造并发送 base64 语音消息。 / Builds and emits a base64 audio message. */
    fun sendAudioBase64(base64Audio: String): ChatMessageObject? {
        val message = buildMessage("", "", base64Audio, TYPE_AUDIO) ?: return null
        sendAudio(message)
        return message
    }

    /** 发送 `system_action`（禁言、设置头像、改头衔等）。 / Emits a `system_action` (e.g. mute, set avatar, change title). */
    fun emitSystemAction(action: ChatroomSystemAction, type: Class<out ChatroomSystemAction>) {
        emit("system_action", gson.toJson(action, type))
    }

    /** 切换某用户的显示时间开关。 / Toggles a user's timestamp visibility. */
    fun toggleTimestamp(email: String, enabled: Boolean) {
        emitSystemAction(TimeAction("time", email, enabled, profile?.name.orEmpty()), TimeAction::class.java)
    }

    /** 切换某用户的发图开关。 / Toggles a user's image permission. */
    fun toggleImage(email: String, enabled: Boolean) {
        emitSystemAction(ImageAction("image", email, enabled, profile?.name.orEmpty()), ImageAction::class.java)
    }

    /** 禁言某用户 [minutes] 分钟。 / Mutes a user for [minutes]. */
    fun mute(email: String, name: String, userId: String, minutes: Int) {
        emitSystemAction(
            MuteAction("mute", email, minutes, name, userId, profile?.name.orEmpty()),
            MuteAction::class.java,
        )
    }

    /** 给某用户设置预设头像 [no]。 / Assigns a preset avatar [no] to a user. */
    fun setAvatar(email: String, name: String, userId: String, no: Int) {
        emitSystemAction(
            SetAvatarAction("set_avatar", email, name, userId, profile?.name.orEmpty(), no),
            SetAvatarAction::class.java,
        )
    }

    /** 给某用户设置额外头像 [extra]。 / Assigns an extra avatar [extra] to a user. */
    fun setAvatarExtra(email: String, name: String, userId: String, extra: Int) {
        emitSystemAction(
            SetAvatarExtraAction("set_avatar", email, name, userId, profile?.name.orEmpty(), extra),
            SetAvatarExtraAction::class.java,
        )
    }

    /** 修改某用户头衔。 / Changes a user's title. */
    fun changeTitle(email: String, name: String, userId: String, title: String) {
        emitSystemAction(
            ChangeTitleAction("set_title", email, title, profile?.name.orEmpty(), name, userId),
            ChangeTitleAction::class.java,
        )
    }

    /** 发送自动喊话使用的 `##server talk` 指令。 / Sends the `##server talk` command used by the automated talk feature. */
    fun sendServerTalk(text: String) {
        val message = buildMessage(
            text = "##server talk @$text",
            image = "",
            audio = "",
            type = TYPE_TEXT,
        ) ?: return
        sendMessage(message)
    }

    /** 发送聊天室小游戏动作（[ChatroomGameEmit] 包裹）。 / Emits a chatroom mini-game action wrapped in [ChatroomGameEmit]. */
    @JvmOverloads
    fun emitGameAction(action: String, data: Any? = null) {
        val payload = if (data == null) ChatroomGameEmit<Any>(action) else ChatroomGameEmit(action, data)
        emit("game", gson.toJson(payload))
    }

    /**
     * 根据当前 [profile] 构造 [ChatMessageObject]；未设置资料时返回 null。
     * Builds a [ChatMessageObject] from the current [profile].
     * Returns null when no profile has been set yet.
     */
    @JvmOverloads
    fun buildMessage(
        text: String,
        image: String,
        audio: String,
        type: Int,
        at: String = "",
        replyName: String = "",
        reply: String = "",
        blockUserId: String = "",
        to: ChatroomToObject? = null,
    ): ChatMessageObject? {
        val user = profile ?: return null
        val displayName = user.name.orEmpty()
            .ifBlank { user.email.orEmpty().substringBefore("@") }
            .ifBlank { "Pica" }
        return ChatMessageObject(
            user.userId.orEmpty(),
            "local_${System.currentTimeMillis()}",
            user.level,
            user.email.orEmpty(),
            PicaImages.thumbnailUrl(user.avatar, imageServer).orEmpty(),
            displayName,
            user.title.orEmpty(),
            user.gender.orEmpty(),
            "android",
            user.activationDate.orEmpty(),
            at,
            replyName,
            reply,
            text,
            image,
            audio,
            blockUserId,
            type,
            user.isVerified,
            user.character.orEmpty(),
            user.charactersStringArray,
            null,
            null,
            to,
        )
    }

    // endregion

    // region 接收 / inbound ---------------------------------------------------------------------

    /** 注册全部服务端事件回调。 / Registers every server-side event handler. */
    private fun registerHandlers(target: Socket) {
        target.on("connect") {
            isConnected = true
            profile?.let { emit("init", gson.toJson(it, UserProfileObject::class.java)) }
            listener?.onConnected()
        }
        target.on("disconnect") { listener?.onDisconnected(roomUrl) }
        target.on("broadcast_message") { args -> dispatchMessage(args, TYPE_TEXT) { listener?.onMessage(it) } }
        target.on("broadcast_image") { args -> dispatchMessage(args, TYPE_IMAGE) { listener?.onImage(it) } }
        target.on("broadcast_audio") { args -> dispatchMessage(args, TYPE_AUDIO) { listener?.onAudio(it) } }
        target.on("broadcast_ads") { args -> dispatchMessage(args, TYPE_ADS) { listener?.onAds(it) } }
        target.on("got_private_message") { args ->
            val json = args.firstJsonObject() ?: return@on
            val message = parseMessage(json, TYPE_PRIVATE) ?: return@on
            message.to = ChatroomToObject(
                json.optString("name"),
                json.optString("unique_id"),
                json.optString("user_id").ifBlank { json.optString("userId") },
            )
            listener?.onPrivateMessage(message)
        }
        target.on("new_connection") { args ->
            val count = args.firstJsonObject()?.optString("connections").orEmpty()
            listener?.onOnlineCountChanged(count)
            listener?.onUserJoined(count)
        }
        target.on("connection_close") { args ->
            val count = args.firstJsonObject()?.optString("connections").orEmpty()
            listener?.onOnlineCountChanged(count)
            listener?.onUserLeft(count)
        }
        target.on("receive_notification") { args ->
            val notice = args.firstJsonObject()?.optString("message").orEmpty()
            if (notice.isNotBlank()) listener?.onNotification(notice)
        }
        target.on("kick") { args ->
            val notice = args.firstJsonObject()?.optString("message").orEmpty()
            listener?.onKicked(notice)
            disconnect()
        }
        target.on("set_profile") { args ->
            args.firstJsonObject()?.let { listener?.onProfileUpdated(it) }
        }
        target.on("change_character_icon") { args ->
            val character = args.firstJsonObject()?.optString("character").orEmpty()
            if (character.isNotBlank()) listener?.onCharacterIconChanged(character)
        }
        target.on("change_title") { args ->
            val json = args.firstJsonObject() ?: return@on
            val userId = json.optString("user_id")
            val title = json.optString("title")
            if (userId.isNotBlank() && title.isNotBlank()) listener?.onTitleChanged(userId, title)
        }
        target.on("error") { args -> listener?.onError(IllegalStateException(args.joinToString())) }
    }

    /** 解析并派发一条广播消息。 / Parses and dispatches one broadcast message. */
    private inline fun dispatchMessage(args: Array<Any>, type: Int, deliver: (ChatMessageObject) -> Unit) {
        val json = args.firstJsonObject() ?: return
        val message = parseMessage(json, type) ?: return
        deliver(message)
    }

    /**
     * 将 Socket.IO 原始载荷转换为 [ChatMessageObject]；无法解析时返回 null。
     * Converts a raw Socket.IO payload into a [ChatMessageObject].
     * Returns null when the payload cannot be parsed.
     */
    fun parseMessage(json: JSONObject, type: Int): ChatMessageObject? {
        return try {
            val message = ChatMessageObject()
            json.optString("id").takeIf { it.isNotBlank() }?.let(message::setUserId)
            json.optString("userId").takeIf { it.isNotBlank() }?.let(message::setUserId)
            json.optString("user_id").takeIf { it.isNotBlank() }?.let(message::setUserId)
            json.optString("unique_id").takeIf { it.isNotBlank() }?.let(message::setUniqueId)
            if (json.has("level")) message.level = json.optInt("level")
            json.optString("email").takeIf { it.isNotBlank() }?.let(message::setEmail)
            json.optString("avatar").takeIf { it.isNotBlank() }?.let(message::setAvatar)
            json.optString("name").takeIf { it.isNotBlank() }?.let(message::setName)
            json.optString("title").takeIf { it.isNotBlank() }?.let(message::setTitle)
            json.optString("gender").takeIf { it.isNotBlank() }?.let(message::setGender)
            json.optString("platform").takeIf { it.isNotBlank() }?.let(message::setPlatform)
            json.optString("activation_date").takeIf { it.isNotBlank() }?.let(message::setActivationDate)
            json.optString("activationDate").takeIf { it.isNotBlank() }?.let(message::setActivationDate)
            json.optString("at").takeIf { it.isNotBlank() }?.let(message::setAt)
            json.optString("reply_name").takeIf { it.isNotBlank() }?.let(message::setReplyName)
            json.optString("reply").takeIf { it.isNotBlank() }?.let(message::setReply)
            json.optString("message").takeIf { it.isNotBlank() }?.let(message::setMessage)
            json.optString("image").takeIf { it.isNotBlank() }?.let(message::setImage)
            json.optString("audio").takeIf { it.isNotBlank() }?.let(message::setAudio)
            json.optString("block_user_id").takeIf { it.isNotBlank() }?.let(message::setBlockUserId)
            if (json.has("verified")) message.isVerified = json.optBoolean("verified")
            json.optString("character").takeIf { it.isNotBlank() }?.let(message::setCharacter)
            message.characters = json.optStringArray("characters")
            json.optString("event_icon").takeIf { it.isNotBlank() }?.let(message::setEventIcon)
            message.eventColors = json.optStringArray("event_colors")
            message.type = type
            message
        } catch (_: Exception) {
            null
        }
    }

    /** 取第一个参数并转换为 JSON 对象。 / Takes the first argument and converts it to a JSON object. */
    private fun Array<Any>.firstJsonObject(): JSONObject? = firstOrNull()?.asJsonObject()

    private fun Any.asJsonObject(): JSONObject? = when (this) {
        is JSONObject -> unwrapChatPayload()
        is JSONArray -> opt(0)?.asJsonObject()
        is String -> try {
            JSONObject(this).unwrapChatPayload()
        } catch (_: Exception) {
            try {
                JSONArray(this).opt(0)?.asJsonObject()
            } catch (_: Exception) {
                null
            }
        }
        is Map<*, *> -> JSONObject(this).unwrapChatPayload()
        else -> null
    }

    /** 部分服务端帧会把载荷包在 `data` 字段中，这里做一次解包。 / Some server frames wrap the payload in a `data` field; unwrap it when present. */
    private fun JSONObject.unwrapChatPayload(): JSONObject {
        val data = opt("data")
        return when (data) {
            is JSONObject -> data
            is JSONArray -> data.opt(0) as? JSONObject ?: this
            is String -> try {
                JSONObject(data)
            } catch (_: Exception) {
                this
            }
            else -> this
        }
    }

    private fun JSONObject.optStringArray(name: String): Array<String>? {
        val array = optJSONArray(name) ?: return null
        if (array.length() == 0) return null
        return Array(array.length()) { array.optString(it) }
    }

    private fun toJson(message: ChatMessageObject): String = gson.toJson(message, ChatMessageObject::class.java)

    /** 线程安全地发送事件。 / Emits an event in a thread-safe manner. */
    private fun emit(event: String, payload: Any?) {
        val current = socket ?: return
        synchronized(emitLock) {
            runCatching { current.emit(event, payload) }
                .onFailure { listener?.onError(it) }
        }
    }

    // endregion

    companion object {
        const val DEFAULT_ROOM_URL = "https://chat.picacomic.com"

        /** 原客户端使用的消息类型。 / Message types used by the original client. */
        const val TYPE_TEXT = 0
        const val TYPE_IMAGE = 1
        const val TYPE_AUDIO = 2
        const val TYPE_PRIVATE = 3
        const val TYPE_ADS = 11

        /** 与原 `NetworkSecurityHelper.createSocketClient` 一致：系统 TLS 策略、默认超时。 / Same as the original `createSocketClient`: system TLS, default timeouts. */
        @JvmStatic
        fun defaultSocketClient(): OkHttpClient =
            PicaNetworking.applySystemTls(OkHttpClient.Builder()).build()
    }
}

/**
 * 聊天室事件监听器，所有方法默认空实现。
 * Receives chatroom events. All methods have no-op defaults.
 */
interface ChatroomListener {
    fun onConnected() {}
    fun onDisconnected(reason: String?) {}
    fun onMessage(message: ChatMessageObject) {}
    fun onImage(message: ChatMessageObject) {}
    fun onAudio(message: ChatMessageObject) {}
    fun onPrivateMessage(message: ChatMessageObject) {}
    fun onAds(message: ChatMessageObject) {}
    fun onOnlineCountChanged(onlineCount: String) {}
    fun onUserJoined(onlineCount: String) {}
    fun onUserLeft(onlineCount: String) {}
    fun onNotification(message: String) {}
    fun onKicked(message: String) {}
    fun onProfileUpdated(json: org.json.JSONObject) {}
    fun onCharacterIconChanged(character: String) {}
    fun onTitleChanged(userId: String, title: String) {}
    fun onError(error: Throwable) {}
}
