package com.picaapi

import java.io.File

/**
 * 令牌持久化接口。应用可自行实现（如 Android 的 SharedPreferences / DataStore）。
 * Token persistence abstraction. Applications may implement their own
 * (e.g. Android SharedPreferences / DataStore) and pass it to [Pica.init].
 */
interface PicaTokenStore {
    /** 读取已保存的令牌，没有则返回 null。 / Loads the saved token, or null. */
    fun loadToken(): String?

    /** 保存令牌；传 null 表示清除。 / Saves the token; null clears it. */
    fun saveToken(token: String?)
}

/** 默认实现：仅保存在内存中，进程重启即丢失。 / Default in-memory store; lost on process restart. */
object MemoryPicaTokenStore : PicaTokenStore {
    @Volatile
    private var token: String? = null

    override fun loadToken(): String? = token

    override fun saveToken(token: String?) {
        this.token = token
    }
}

/**
 * 基于文件的令牌存储，适合桌面/服务端等 JVM 环境。
 * File-based token store, suitable for desktop/server JVM environments.
 *
 * 目录不存在会自动创建；写入失败（如无权限）会被静默忽略。
 * The parent directory is created on demand; write failures are ignored silently.
 */
class FilePicaTokenStore(private val file: File) : PicaTokenStore {

    override fun loadToken(): String? = try {
        if (file.exists()) file.readText().trim().ifBlank { null } else null
    } catch (_: Exception) {
        null
    }

    override fun saveToken(token: String?) {
        try {
            if (token.isNullOrBlank()) {
                if (file.exists()) file.delete()
            } else {
                file.parentFile?.mkdirs()
                file.writeText(token)
            }
        } catch (_: Exception) {
            // 忽略持久化失败，不阻断登录流程。 / Ignore persistence failures; never block the login flow.
        }
    }
}
