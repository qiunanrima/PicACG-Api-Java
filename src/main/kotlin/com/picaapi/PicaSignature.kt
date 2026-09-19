package com.picaapi

import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * 请求签名工具，移植自原 `GenerateSignature` 与原生辅助方法。
 * Request signing, ported from the original `GenerateSignature` + native helpers.
 *
 * 签名原文 / raw = (path + time + nonce + method + apiKey).lowercase()
 * 签名结果 / signature = hex(HMAC_SHA256(raw, key2))
 */
object PicaSignature {
    private const val HEX = "0123456789abcdef"

    /**
     * 计算请求签名。
     * Computes the request signature.
     *
     * @param path 去掉协议与域名的路径 / path with scheme and host stripped
     * @param time 秒级时间戳 / time in seconds
     * @param nonce 随机串 / random nonce
     * @param method HTTP 方法 / HTTP method
     * @param apiKey 公共 API Key / public API key
     * @param hmacKey HMAC 密钥 / HMAC secret
     */
    @JvmStatic
    fun sign(
        path: String,
        time: String,
        nonce: String,
        method: String,
        apiKey: String,
        hmacKey: String,
    ): String {
        val raw = (path + time + nonce + method + apiKey).lowercase(Locale.ROOT)
        return hmacSha256(raw, hmacKey)
    }

    /** 对 [raw] 计算 HMAC-SHA256 并返回小写十六进制字符串。 / HMAC-SHA256 of [raw] as lowercase hex. */
    @JvmStatic
    fun hmacSha256(raw: String, key: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        return toHex(mac.doFinal(raw.toByteArray(Charsets.UTF_8)))
    }

    private fun toHex(bytes: ByteArray): String {
        val out = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            out[i * 2] = HEX[v ushr 4]
            out[i * 2 + 1] = HEX[v and 0x0F]
        }
        return String(out)
    }
}
