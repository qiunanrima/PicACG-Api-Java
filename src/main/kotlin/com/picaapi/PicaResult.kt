package com.picaapi

import java.util.Optional
import java.util.function.Consumer
import java.util.function.Function

/**
 * 单次 API 调用的结果。每个接口要么返回带 `data` 载荷的 [Success]，
 * 要么返回携带诊断信息的 [Failure]。
 *
 * Result of a single API call. Every endpoint returns either [Success] with the
 * unwrapped `data` payload, or [Failure] with as much diagnostic information as
 * the transport could recover.
 *
 * ### Java / Kotlin 友好特性 / Interop Highlights
 * - Kotlin 支持解构：`val (data, error) = result`
 * - Java 支持流畅的函数式回调：`result.onSuccess(data -> ...)`
 * - 支持直接抛出异常：`result.getOrThrow()`
 * - 支持 Java 8 [Optional]：`result.toOptional()`
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

        /** 构造等价的 [PicaException]。 / Converts to an equivalent [PicaException]. */
        fun toException(): PicaException = PicaException(
            httpCode = httpCode,
            errorCode = errorCode,
            message = message,
            rawBody = rawBody,
            cause = cause,
        )
    }

    /** 是否成功。Kotlin 与 Java 均可直接调用。 / Whether the call succeeded; callable from both Kotlin and Java. */
    val isSuccess: Boolean
        get() = this is Success

    /** 是否失败。Kotlin 与 Java 均可直接调用。 / Whether the call failed; callable from both Kotlin and Java. */
    val isFailure: Boolean
        get() = this is Failure

    /** 成功时返回数据，否则返回 null。 / Returns the data on success, otherwise null. */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Failure -> null
    }

    /** 失败时返回 [Failure]，成功时返回 null。便于 Java 免转型读取错误信息。 / Returns [Failure] on failure, or null on success. */
    fun getFailureOrNull(): Failure? = when (this) {
        is Success -> null
        is Failure -> this
    }

    /** 成功时返回数据，失败时返回 [default]。 / Returns the data on success, or [default] on failure. */
    fun getOrDefault(default: @UnsafeVariance T): T = when (this) {
        is Success -> data
        is Failure -> default
    }

    /** 成功时返回数据，否则抛出 [PicaException]。 / Returns data on success, or throws [PicaException]. */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Failure -> throw toException()
    }

    /** 获取可能关联的异常。 / Returns the cause or constructed [PicaException] on failure. */
    fun exceptionOrNull(): Throwable? = when (this) {
        is Success -> null
        is Failure -> cause ?: toException()
    }

    /** 转换为 Java 8 [Optional]。 / Converts to Java 8 [Optional]. */
    fun toOptional(): Optional<out T> = Optional.ofNullable(getOrNull())

    /** 成功时返回数据，失败时由回调提供备选值。 / Returns data on success, or computes fallback on failure. */
    @JvmSynthetic
    inline fun getOrElse(fallback: (Failure) -> @UnsafeVariance T): T = when (this) {
        is Success -> data
        is Failure -> fallback(this)
    }

    /** Java 友好版备用值获取。 / Java-friendly fallback retriever. */
    @JvmName("getOrElse")
    fun getOrElse(fallback: Function<in Failure, out @UnsafeVariance T>): T = when (this) {
        is Success -> data
        is Failure -> fallback.apply(this)
    }

    // region 链式回调 / Callbacks ------------------------------------------------------------------

    /** Kotlin: 成功时执行 [block]，返回原结果以便链式调用。 */
    @JvmSynthetic
    inline fun onSuccess(crossinline block: (T) -> Unit): PicaResult<T> {
        if (this is Success) block(data)
        return this
    }

    /** Java: 成功时执行 [consumer]，返回原结果以便链式调用。 */
    @JvmName("onSuccess")
    fun onSuccess(consumer: Consumer<in T>): PicaResult<T> {
        if (this is Success) consumer.accept(data)
        return this
    }

    /** Kotlin: 失败时执行 [block]，返回原结果以便链式调用。 */
    @JvmSynthetic
    inline fun onFailure(crossinline block: (Failure) -> Unit): PicaResult<T> {
        if (this is Failure) block(this)
        return this
    }

    /** Java: 失败时执行 [consumer]，返回原结果以便链式调用。 */
    @JvmName("onFailure")
    fun onFailure(consumer: Consumer<in Failure>): PicaResult<T> {
        if (this is Failure) consumer.accept(this)
        return this
    }

    // endregion

    // region 函数式变换 / Transformations -----------------------------------------------------------

    /** Kotlin: 变换成功数据。 */
    @JvmSynthetic
    inline fun <R> map(crossinline transform: (T) -> R): PicaResult<R> = when (this) {
        is Success -> Success(transform(data), httpCode)
        is Failure -> this
    }

    /** Java: 变换成功数据。 */
    @JvmName("map")
    fun <R> map(transform: Function<in T, out R>): PicaResult<R> = when (this) {
        is Success -> Success(transform.apply(data), httpCode)
        is Failure -> this
    }

    /** Kotlin: 平铺变换。 */
    @JvmSynthetic
    inline fun <R> flatMap(crossinline transform: (T) -> PicaResult<R>): PicaResult<R> = when (this) {
        is Success -> transform(data)
        is Failure -> this
    }

    /** Java: 平铺变换。 */
    @JvmName("flatMap")
    fun <R> flatMap(transform: Function<in T, out PicaResult<R>>): PicaResult<R> = when (this) {
        is Success -> transform.apply(data)
        is Failure -> this
    }

    /** Kotlin: 分支处理收敛为单个结果。 */
    @JvmSynthetic
    inline fun <R> fold(crossinline onSuccess: (T) -> R, crossinline onFailure: (Failure) -> R): R = when (this) {
        is Success -> onSuccess(data)
        is Failure -> onFailure(this)
    }

    /** Java: 分支处理收敛为单个结果。 */
    @JvmName("fold")
    fun <R> fold(
        onSuccess: Function<in T, out R>,
        onFailure: Function<in Failure, out R>,
    ): R = when (this) {
        is Success -> onSuccess.apply(data)
        is Failure -> onFailure.apply(this)
    }

    // endregion

    // endregion

    companion object {
        /** 创建成功结果。 / Creates a [Success]. */
        @JvmStatic
        @JvmOverloads
        fun <T> success(data: T, httpCode: Int = 200): PicaResult<T> = Success(data, httpCode)

        /** 创建失败结果。 / Creates a [Failure]. */
        @JvmStatic
        @JvmOverloads
        fun failure(
            httpCode: Int? = null,
            errorCode: String? = null,
            message: String? = null,
            rawBody: String? = null,
            cause: Throwable? = null,
        ): PicaResult<Nothing> = Failure(
            httpCode = httpCode,
            errorCode = errorCode,
            message = message,
            rawBody = rawBody,
            cause = cause,
        )

        /** 根据异常创建失败结果。 / Creates a [Failure] from a [Throwable]. */
        @JvmStatic
        fun failure(cause: Throwable): PicaResult<Nothing> = Failure(
            httpCode = null,
            message = cause.message,
            cause = cause,
        )

        /** 执行代码块并自动捕获异常为 [PicaResult]。 / Runs block and catches exceptions into [PicaResult]. */
        @JvmStatic
        inline fun <T> runCatching(block: () -> T): PicaResult<T> = try {
            Success(block())
        } catch (e: Throwable) {
            failure(e)
        }
    }
}

/** 兼容旧版顶层扩展函数：成功时返回数据，否则抛出 [PicaException]。 */
fun <T> PicaResult<T>.getOrThrow(): T = (this as PicaResult<T>).getOrThrow()

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
