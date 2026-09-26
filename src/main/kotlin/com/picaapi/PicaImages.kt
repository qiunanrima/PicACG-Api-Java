package com.picaapi

import com.picacomic.fregata.objects.ComicPageObject
import com.picacomic.fregata.objects.ThumbnailObject
import com.picacomic.fregata.objects.UserProfileObject
import com.picacomic.fregata.objects.responses.ComicDetailResponse

/**
 * 根据 API 的 [ThumbnailObject] 拼装图片绝对地址。
 * Builds absolute image URLs from the API's [ThumbnailObject].
 *
 * 逻辑与原 `utils.g.b()` 一致：
 *  - `http://loremPixel.com` 资源原样返回；
 *  - 否则优先使用配置的图片 CDN（`imageServer`）；
 *  - 最后回退到 `fileServer + "/static/" + path`。
 *
 * Mirrors the original `utils.g.b()`:
 *  - `http://loremPixel.com` assets are used verbatim;
 *  - otherwise a configured image CDN (`imageServer`) is preferred;
 *  - finally it falls back to `fileServer + "/static/" + path`.
 */
object PicaImages {

    /** 拼装缩略图 / 媒体对象的图片完整 URL。 */
    @JvmStatic
    @JvmOverloads
    fun thumbnailUrl(thumbnail: ThumbnailObject?, imageServer: String? = null): String? {
        if (thumbnail == null) return null
        val fileServer = thumbnail.fileServer ?: return null
        if (fileServer.equals("http://lorempixel.com", ignoreCase = true)) {
            return fileServer + thumbnail.path
        }
        if (!imageServer.isNullOrBlank()) {
            return imageServer + thumbnail.path
        }
        return "$fileServer/static/${thumbnail.path}"
    }

    /** 拼装漫画单页图片的完整 URL。 / Builds URL for comic page. */
    @JvmStatic
    @JvmOverloads
    fun pageUrl(page: ComicPageObject?, imageServer: String? = null): String? =
        thumbnailUrl(page?.media, imageServer)

    /** 拼装用户头像的完整 URL。 / Builds URL for user avatar. */
    @JvmStatic
    @JvmOverloads
    fun avatarUrl(profile: UserProfileObject?, imageServer: String? = null): String? =
        thumbnailUrl(profile?.avatar, imageServer)

    /** 拼装漫画详情封面的完整 URL。 / Builds URL for comic detail cover. */
    @JvmStatic
    @JvmOverloads
    fun coverUrl(detail: ComicDetailResponse?, imageServer: String? = null): String? =
        thumbnailUrl(detail?.comic?.thumb, imageServer)
}

/** Kotlin 扩展：获取缩略图 URL。 */
fun ThumbnailObject?.toImageUrl(imageServer: String? = null): String? =
    PicaImages.thumbnailUrl(this, imageServer)

/** Kotlin 扩展：获取漫画图片页 URL。 */
fun ComicPageObject?.toImageUrl(imageServer: String? = null): String? =
    PicaImages.pageUrl(this, imageServer)

/** Kotlin 扩展：获取用户头像 URL。 */
fun UserProfileObject?.avatarUrl(imageServer: String? = null): String? =
    PicaImages.avatarUrl(this, imageServer)

/** Kotlin 扩展：获取漫画封面 URL。 */
fun ComicDetailResponse?.coverUrl(imageServer: String? = null): String? =
    PicaImages.coverUrl(this, imageServer)

/** Kotlin 扩展：下载单页图片并保存到文件。 */
fun ComicPageObject.download(
    targetFile: java.io.File,
    client: PicaClient = Pica.client,
    overwrite: Boolean = false
): PicaResult<java.io.File> {
    val url = PicaImages.pageUrl(this, client.config.imageServer)
        ?: return PicaResult.failure(IllegalStateException("No image URL found for comic page"))
    return client.downloadImage(url, targetFile, overwrite)
}

/** Kotlin 扩展：下载漫画封面并保存到文件。 */
fun ComicDetailResponse.downloadCover(
    targetFile: java.io.File,
    client: PicaClient = Pica.client,
    overwrite: Boolean = false
): PicaResult<java.io.File> {
    val url = PicaImages.coverUrl(this, client.config.imageServer)
        ?: return PicaResult.failure(IllegalStateException("No cover URL found for comic detail"))
    return client.downloadImage(url, targetFile, overwrite)
}
