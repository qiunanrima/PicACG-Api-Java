package com.picaapi

import com.google.gson.Gson
import com.picacomic.fregata.objects.responses.WakaInitResponse
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor

/**
 * 旧版 “Waka” 服务器的精简客户端，对应原 `RestWakaClient`。
 * Minimal client for the legacy "Waka" server used by the original
 * `RestWakaClient`.
 *
 * 该服务器**不**使用 picaapi 签名协议，只暴露一个普通 `GET init` 接口。
 * 默认主机名与原项目保持一致，调用方也可传入任意主机。
 * 原项目对 Waka 始终使用“信任所有证书”策略，故 [trustAllSsl] 默认为 true。
 *
 * This server does **not** use the signed `picaapi` protocol: it exposes a
 * single plain `GET init` endpoint over HTTP(S). The default host is kept for
 * parity with the original app; callers may pass any host. The original always
 * used a trust-all policy for Waka, so [trustAllSsl] defaults to true.
 */
class PicaWakaClient @JvmOverloads constructor(
    private val baseUrl: String = DEFAULT_BASE_URL,
    enableLogging: Boolean = false,
    trustAllSsl: Boolean = true,
) {
    private val gson = Gson()
    private val httpClient: OkHttpClient = PicaNetworking
        .applySslPolicy(OkHttpClient.Builder(), trustAllSsl)
        .apply {
            if (enableLogging) {
                addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            }
        }
        .build()

    /** 旧版服务器的 `GET init`。 / `GET init` on the legacy server. */
    fun init(): PicaResult<WakaInitResponse> {
        val url = if (baseUrl.endsWith("/")) "${baseUrl}init" else "$baseUrl/init"
        return try {
            httpClient.newCall(Request.Builder().url(url).get().build()).execute().use { response ->
                val text = response.body?.string()
                if (!response.isSuccessful) {
                    PicaResult.Failure(httpCode = response.code, rawBody = text)
                } else {
                    PicaResult.Success(gson.fromJson(text, WakaInitResponse::class.java), response.code)
                }
            }
        } catch (t: Throwable) {
            PicaResult.Failure(httpCode = null, message = t.message, cause = t)
        }
    }

    companion object {
        const val DEFAULT_BASE_URL = "http://68.183.234.72/"
    }
}
