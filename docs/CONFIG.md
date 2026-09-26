# 配置参考

---

## 目录

- [PicaConfig](#picaconfig)
- [默认常量](#默认常量)
- [网络参数](#网络参数)
- [请求头与签名](#请求头与签名)
- [服务端时间校正](#服务端时间校正)
- [全局单例 Pica](#全局单例-pica)
- [PicaWakaClient](#picawaka)
- [ChatroomClient](#chatroomclient)
- [依赖版本](#依赖版本)

---

## PicaConfig

`com.picaapi.PicaConfig`，不可变数据类，构造参数均有默认值。

| 参数 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `baseUrl` | `String` | `https://picaapi.picacomic.com/` | API 主机，必须以 `/` 结尾（内部会规范化） |
| `apiKey` | `String` | `C69BAF41DA5ABD1FFEDC6D2FEA56B` | `api-key` 头，参与签名 |
| `hmacKey` | `String` | 原生 Key2 | HMAC-SHA256 密钥 |
| `appVersion` | `String` | `2.2.1.3.3.4` | `app-version` 头 |
| `appBuildVersion` | `String` | `45` | `app-build-version` 头 |
| `appUuid` | `String` | 随机 32 位 | `app-uuid` 头，建议每安装稳定 |
| `appChannel` | `Int` | `1` | `app-channel` 头 |
| `imageQuality` | `String` | `medium` | `image-quality` 头 |
| `userAgent` | `String` | `okhttp/3.8.1` | `User-Agent` 头 |
| `authorization` | `String?` | `null` | 初始鉴权令牌 |
| `timeOffsetSeconds` | `Long` | `0` | 初始时钟偏移（秒） |
| `enableLogging` | `Boolean` | `false` | 打开 OkHttp BODY 日志 |
| `disableSslVerification` | `Boolean` | `false` | 跳过证书/主机名校验 |
| `dnsIps` | `List<String>` | `[]` | 非空时强制解析到这些 IP |

示例：

```kotlin
val config = PicaConfig(
    appUuid = "fixed-uuid-per-install",
    imageQuality = "high",
    enableLogging = true,
    disableSslVerification = false,
    dnsIps = listOf("104.18.1.1", "104.18.2.2"),
)
val pica = PicaClient(config)
```

运行时更新：

```kotlin
pica.updateConfig(config)
pica.updateAuthorization("token")
```

---

## 默认常量

`PicaConfig` 伴生对象：

| 常量 | 值 |
| --- | --- |
| `DEFAULT_BASE_URL` | `https://picaapi.picacomic.com/` |
| `DEFAULT_API_KEY` | `C69BAF41DA5ABD1FFEDC6D2FEA56B` |
| `DEFAULT_HMAC_KEY` | 原生 Key2 |
| `DEFAULT_APP_VERSION` | `2.2.1.3.3.4` |
| `DEFAULT_APP_BUILD_VERSION` | `45` |
| `DEFAULT_APP_CHANNEL` | `1` |
| `DEFAULT_IMAGE_QUALITY` | `medium` |
| `DEFAULT_USER_AGENT` | `okhttp/3.8.1` |

`imageQuality` 取值：`original` / `low` / `medium` / `high`（原客户端默认索引 2 = `medium`）。

---

## 网络参数

`PicaNetworking` 复刻原 `NetworkSecurityHelper`：

| 方法 | 说明 |
| --- | --- |
| `dns(ips)` | 返回自定义 `Dns`：`ips` 非空则解析到这些地址，否则系统 DNS |
| `applySystemTls(builder)` | 系统信任链 + 强制 `TLSv1.2`/`TLSv1.1`（仅启用运行时支持的协议） |
| `applyTrustAllSsl(builder)` | 信任所有证书 + 主机名恒真 |
| `applySslPolicy(builder, trustAll)` | 按布尔值选择上面两种 |

默认 `dnsIps` 为空 → 使用系统 DNS；默认 `disableSslVerification = false` → 系统 TLS。

**超时**：原客户端未设置任何超时，因此本库同样使用 OkHttp 默认值
（连接/读/写各 10 秒，`callTimeout = 0`，`retryOnConnectionFailure = true`）。
未提供配置项；如需自定义，可自行传入配置好的 `OkHttpClient`（聊天室）或扩展 `PicaConfig`。

---

## 请求头与签名

签名算法（`PicaSignature`）：

```
raw       = (path + time + nonce + method + apiKey).lowercase(Locale.ROOT)
signature = hex(HMAC_SHA256(raw, hmacKey))
```

`path` 是去掉 `baseUrl` 前缀后的部分（含 query）。

拦截器附加的请求头：

| Header | 来源 |
| --- | --- |
| `api-key` | `config.apiKey` |
| `accept` | `application/vnd.picacomic.com.v1+json`（固定） |
| `app-channel` | `config.appChannel` |
| `time` | `now(秒) + serverTimeOffset` |
| `nonce` | 随机 32 位（去掉 `-`） |
| `signature` | 上述 HMAC |
| `app-version` | `config.appVersion` |
| `app-uuid` | `config.appUuid` |
| `image-quality` | `config.imageQuality` |
| `app-platform` | `android`（固定） |
| `app-build-version` | `config.appBuildVersion` |
| `User-Agent` | `config.userAgent` |
| `authorization` | 仅需要登录的接口带 |

可手动调用：

```kotlin
val sig = PicaSignature.sign(
    path = "comics",
    time = (System.currentTimeMillis() / 1000).toString(),
    nonce = PicaConfig.randomUuid(),
    method = "GET",
    apiKey = PicaConfig.DEFAULT_API_KEY,
    hmacKey = PicaConfig.DEFAULT_HMAC_KEY,
)
```

---

## 服务端时间校正

每次响应都会读取 `Server-Time` 头：

```
serverTimeOffset = Server-Time - localNow(秒)
```

该偏移保存在 `AtomicLong` 中并用于后续签名的 `time`，避免本地时钟不准导致签名失败。

```kotlin
println(pica.serverTimeOffsetSeconds)
```

---

## 全局单例 Pica

`com.picaapi.Pica`，进程内共享一个 `PicaClient` 与登录令牌。

| 成员 | 说明 |
| --- | --- |
| `init(config = PicaConfig(), tokenStore = MemoryPicaTokenStore): PicaClient` | 初始化并用 `tokenStore` 恢复 token |
| `client: PicaClient` | 全局客户端，未初始化抛 `IllegalStateException` |
| `isInitialized: Boolean` | 是否已初始化 |
| `login(email, password, persist = true): PicaResult<SignInResponse>` | 登录成功自动保存 token |
| `saveToken(token: String?)` | 保存 token 并更新客户端 |
| `token(): String?` | 当前 token |
| `logout()` | 清除 token |

线程安全：内部 `@Volatile` + `synchronized`。Java 通过生成的静态方法调用。

### PicaTokenStore

```kotlin
interface PicaTokenStore {
    fun loadToken(): String?
    fun saveToken(token: String?)
}
```

| 实现 | 说明 |
| --- | --- |
| `MemoryPicaTokenStore` | 默认，仅内存，进程重启丢失 |
| `FilePicaTokenStore(File)` | 文件持久化，父目录自动创建，失败静默 |

自定义实现示例（Android）：

```kotlin
class PrefsTokenStore(private val prefs: SharedPreferences) : PicaTokenStore {
    override fun loadToken() = prefs.getString("token", null)
    override fun saveToken(token: String?) {
        prefs.edit().apply { if (token == null) remove("token") else putString("token", token) }.apply()
    }
}
```

---

## PicaWakaClient

```kotlin
class PicaWakaClient(
    baseUrl: String = "http://68.183.234.72/",
    enableLogging: Boolean = false,
    trustAllSsl: Boolean = true,     // 原客户端对 Waka 始终信任所有
)
```

| 方法 | 路径 | 返回 |
| --- | --- | --- |
| `init()` | `GET {baseUrl}init` | `PicaResult<WakaInitResponse>` |

该服务不使用签名协议，也没有 `authorization`。

---

## ChatroomClient

```kotlin
class ChatroomClient(
    defaultRoomUrl: String = "https://chat.picacomic.com",
    imageServer: String? = null,
    okHttpClient: OkHttpClient = defaultSocketClient(),
)
```

| 参数 | 说明 |
| --- | --- |
| `defaultRoomUrl` | `connect()` 不带地址时使用 |
| `imageServer` | 构造消息头像 URL 时使用的图片 CDN |
| `okHttpClient` | 用于 websocket；默认系统 TLS 策略，与 `NetworkSecurityHelper.createSocketClient` 一致 |

底层 Socket.IO 选项（内部固定）：

```kotlin
transports = arrayOf("websocket")
callFactory = okHttpClient
webSocketFactory = okHttpClient
forceNew = true
```

---

## 依赖版本

| 依赖 | 版本 |
| --- | --- |
| Kotlin | 2.2.21 |
| Gradle | 9.4.1 |
| JVM | 17 |
| OkHttp / logging-interceptor | 5.3.2 |
| Gson | 2.10.1 |
| org.json | 20231013 |
| socket.io-client / engine.io-client | 1.0.3-SNAPSHOT（本地源码） |
