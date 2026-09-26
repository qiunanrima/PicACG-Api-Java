package com.picaapi

import com.picacomic.fregata.objects.ComicPageObject
import com.picacomic.fregata.objects.responses.DataClass.ComicPageResponse.ComicPagesResponse
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Pattern

/**
 * 下载进度实体。
 * Download progress state.
 *
 * @param current 已完成的图片数 / completed item count
 * @param total 总图片数 / total item count
 * @param currentFile 最近完成写入的文件 / most recently downloaded file
 */
data class DownloadProgress(
    val current: Int,
    val total: Int,
    val currentFile: File? = null
) {
    /** 完成百分比（0.0 - 100.0）。 */
    val percentage: Float
        get() = if (total > 0) (current.toFloat() / total) * 100f else 0f

    /** 是否全部下载完成。 */
    val isFinished: Boolean
        get() = total > 0 && current >= total
}

/**
 * 进度监听回调函数式接口。
 */
fun interface DownloadProgressListener {
    fun onProgress(progress: DownloadProgress)
}

/**
 * PicACG 图片下载管理器与文件操作工具。
 * 借鉴与对齐 JMComic 核心能力：
 * <ul>
 *   <li>内置主流系统（特别是 Windows）非法文件名过滤 [sanitizeFilename]；</li>
 *   <li>原子文件写入 [writeAtomically]（临时 `.tmp` 写入后原子替换，防止意外中断导致文件残缺损坏）；</li>
 *   <li>单图下载 [downloadImage] 与二进制数据抓取 [fetchImageBytes]；</li>
 *   <li>章节多图并发流水线下载 [downloadEpisode] 与实时进度通知。</li>
 * </ul>
 */
object PicaDownloader {

    private val ILLEGAL_CHARACTERS_PATTERN = Pattern.compile("[\\\\/:*?\"<>|\\p{Cntrl}]")

    private val defaultHttpClient by lazy {
        OkHttpClient.Builder().build()
    }

    /**
     * 过滤/净化文件名中的非法字符（如 Windows 下的 `\ / : * ? " < > |` 及控制字符），
     * 防止路径穿越与文件创建异常。
     *
     * @param input 原始文件名或本子标题
     * @param replacement 替换字符，默认为 "_"
     * @return 净化后的安全文件名
     */
    @JvmStatic
    @JvmOverloads
    fun sanitizeFilename(input: String?, replacement: String = "_"): String {
        if (input.isNullOrEmpty()) return ""
        return ILLEGAL_CHARACTERS_PATTERN.matcher(input).replaceAll(replacement).trim()
    }

    /**
     * 原子保存二进制数据至目标文件。
     * 先写入同目录下的临时 `.tmp` 文件，全部刷新写入后再通过原子操作重命名为目标文件。
     *
     * @param targetFile 目标文件
     * @param bytes 文件字节数组
     */
    @JvmStatic
    fun writeAtomically(targetFile: File, bytes: ByteArray) {
        val parent = targetFile.parentFile ?: File(".")
        if (!parent.exists()) {
            parent.mkdirs()
        }
        val tmpFile = File(parent, "${targetFile.name}.${UUID.randomUUID().toString().substring(0, 8)}.tmp")
        try {
            tmpFile.outputStream().use { it.write(bytes) }
            moveTmpToTarget(tmpFile, targetFile)
        } finally {
            if (tmpFile.exists()) {
                tmpFile.delete()
            }
        }
    }

    /**
     * 原子保存输入流至目标文件。
     *
     * @param targetFile 目标文件
     * @param inputStream 输入流
     */
    @JvmStatic
    fun writeAtomically(targetFile: File, inputStream: InputStream) {
        val parent = targetFile.parentFile ?: File(".")
        if (!parent.exists()) {
            parent.mkdirs()
        }
        val tmpFile = File(parent, "${targetFile.name}.${UUID.randomUUID().toString().substring(0, 8)}.tmp")
        try {
            tmpFile.outputStream().use { out ->
                inputStream.copyTo(out)
            }
            moveTmpToTarget(tmpFile, targetFile)
        } finally {
            if (tmpFile.exists()) {
                tmpFile.delete()
            }
        }
    }

    private fun moveTmpToTarget(tmpFile: File, targetFile: File) {
        try {
            Files.move(
                tmpFile.toPath(),
                targetFile.toPath(),
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(
                tmpFile.toPath(),
                targetFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )
        }
    }

    /**
     * 下载获取单张图片的原始二进制数据。
     *
     * @param imageUrl 图片完整下载地址
     * @param okHttpClient 使用的 OkHttpClient（可选）
     * @return PicaResult 包装的字节数组
     */
    @JvmStatic
    @JvmOverloads
    fun fetchImageBytes(
        imageUrl: String,
        okHttpClient: OkHttpClient = defaultHttpClient
    ): PicaResult<ByteArray> {
        return PicaResult.runCatching {
            val req = Request.Builder().url(imageUrl).build()
            okHttpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    throw IOException("HTTP ${resp.code} downloading image from $imageUrl")
                }
                resp.body?.bytes() ?: throw IOException("Empty response body from $imageUrl")
            }
        }
    }

    /**
     * 下载图片并原子保存到目标文件。
     *
     * @param imageUrl 图片完整下载地址
     * @param targetFile 保存的目标文件
     * @param overwrite 若目标文件已存在是否覆盖；若为 false 且文件非空将直接跳过下载
     * @param okHttpClient 使用的 OkHttpClient（可选）
     * @return PicaResult 包装的目标文件对象
     */
    @JvmStatic
    @JvmOverloads
    fun downloadImage(
        imageUrl: String,
        targetFile: File,
        overwrite: Boolean = false,
        okHttpClient: OkHttpClient = defaultHttpClient
    ): PicaResult<File> {
        return PicaResult.runCatching {
            if (targetFile.exists() && !overwrite && targetFile.length() > 0) {
                return@runCatching targetFile
            }
            val req = Request.Builder().url(imageUrl).build()
            okHttpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    throw IOException("HTTP ${resp.code} downloading image from $imageUrl")
                }
                val body = resp.body ?: throw IOException("Empty response body from $imageUrl")
                body.byteStream().use { input ->
                    writeAtomically(targetFile, input)
                }
            }
            targetFile
        }
    }

    /**
     * 并发下载单话/单章节的全部图片。
     *
     * @param client PicaClient 客户端实例
     * @param comicId 漫画 ID
     * @param episodeOrder 章节序号（从 1 开始）
     * @param targetDir 本地保存目录
     * @param concurrency 并发线程数，默认为 4
     * @param overwrite 是否覆盖已有文件，默认为 false
     * @param listener 进度监听器
     * @return PicaResult 包装的按页面顺序排列的文件列表
     */
    @JvmStatic
    @JvmOverloads
    fun downloadEpisode(
        client: PicaClient,
        comicId: String,
        episodeOrder: Int,
        targetDir: File,
        concurrency: Int = 4,
        overwrite: Boolean = false,
        listener: DownloadProgressListener? = null
    ): PicaResult<List<File>> {
        val pagesRes = client.getAllComicPagesByOrder(comicId, episodeOrder)
        val pages = when (pagesRes) {
            is PicaResult.Success -> pagesRes.data
            is PicaResult.Failure -> return pagesRes
        }
        if (pages.isEmpty()) {
            return PicaResult.Success(emptyList())
        }
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }

        val total = pages.size
        val downloadedCount = AtomicInteger(0)
        val downloadedFiles = ConcurrentHashMap<Int, File>()
        val executor = Executors.newFixedThreadPool(concurrency.coerceIn(1, 16))

        try {
            val futures = pages.mapIndexed { index, page ->
                val pageIndex = index + 1
                val imageUrl = PicaImages.pageUrl(page, client.config.imageServer)
                    ?: return@mapIndexed CompletableFuture.failedFuture<File>(
                        IllegalStateException("Failed to resolve image URL for page $pageIndex")
                    )

                val ext = imageUrl.substringAfterLast('.', "jpg").substringBefore('?')
                val filename = String.format("%03d.%s", pageIndex, ext)
                val targetFile = File(targetDir, filename)

                CompletableFuture.supplyAsync({
                    val res = downloadImage(imageUrl, targetFile, overwrite, client.rawHttpClient)
                    when (res) {
                        is PicaResult.Success -> {
                            val current = downloadedCount.incrementAndGet()
                            downloadedFiles[pageIndex] = res.data
                            listener?.onProgress(DownloadProgress(current, total, res.data))
                            res.data
                        }
                        is PicaResult.Failure -> throw IOException("Failed to download page $pageIndex: ${res.message ?: res.cause?.message}")
                    }
                }, executor)
            }

            CompletableFuture.allOf(*futures.toTypedArray()).join()
            val resultList = (1..total).mapNotNull { downloadedFiles[it] }
            return PicaResult.Success(resultList)
        } catch (e: Throwable) {
            val cause = if (e is java.util.concurrent.CompletionException && e.cause != null) e.cause!! else e
            return PicaResult.failure(cause)
        } finally {
            executor.shutdown()
        }
    }
}
