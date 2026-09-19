package com.picaapi

import com.picacomic.fregata.objects.ThumbnailObject

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
}
