package com.picaapi

/**
 * 单次 API 调用的结果。每个接口要么返回带 `data` 载荷的 [Success]，
 * 要么返回携带诊断信息的 [Failure]。
 *
 * Result of a single API call. Every endpoint returns either [Success] with the
 * unwrapped `data` payload, or [Failure] with as much diagnostic information as
 * the transport could recover.
 */
sealed class PicaResult<out T> {
    /** 调用成功 / Call succeeded. */
    data class Success<out T>(
        val data: T,
        val httpCode: Int = 200,
    ) : PicaResult<T>()

    /** 调用失败 / Call failed. */
    data class Failure(
        /** HTTP 状态码；请求未到达服务端时为 `null`。 / HTTP status, or `null` when the request never reached the server. */
        val httpCode: Int?,
        /** [GeneralResponse] / `NetworkErrorObject` 中的业务错误码。 / Business error code from [GeneralResponse] / `NetworkErrorObject`, if any. */
        val errorCode: String? = null,
        val message: String? = null,
        val rawBody: String? = null,
        val cause: Throwable? = null,
    ) : PicaResult<Nothing>() {
        /** 是否为网络层错误（未收到任何 HTTP 响应）。 / Whether this is a network-layer error (no HTTP response received). */
        val isNetworkError: Boolean get() = httpCode == null
    }
}

/** 是否成功 / Whether the result is a success. */
val PicaResult<*>.isSuccess: Boolean get() = this is PicaResult.Success

/** 成功时返回数据，否则返回 null。 / Returns the data on success, otherwise null. */
fun <T> PicaResult<T>.getOrNull(): T? = (this as? PicaResult.Success)?.data

/** 成功时返回数据，否则抛出 [PicaException]。 / Returns the data on success, otherwise throws [PicaException]. */
fun <T> PicaResult<T>.getOrThrow(): T = when (this) {
    is PicaResult.Success -> data
    is PicaResult.Failure -> throw PicaException(
        httpCode = httpCode,
        errorCode = errorCode,
        message = message,
        rawBody = rawBody,
        cause = cause,
    )
}

/** [getOrThrow] 在调用失败时抛出的异常。 / Thrown by [getOrThrow] when a call fails. */
class PicaException(
    /** HTTP 状态码 / HTTP status code */
    val httpCode: Int?,
    /** 业务错误码 / business error code */
    val errorCode: String?,
    override val message: String?,
    /** 原始响应体 / raw response body */
    val rawBody: String?,
    override val cause: Throwable?,
) : Exception(message, cause)
