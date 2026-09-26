package com.picaapi

/**
 * 漫画列表检索查询参数。
 * Query parameters for comic listing and search.
 *
 * 支持通过 Kotlin 数据类构造，或 Java 的 [Builder] 链式构造。
 * Supports Kotlin data class construction as well as fluent Java [Builder].
 */
data class ComicQuery @JvmOverloads constructor(
    val page: Int = 1,
    val category: String? = null,
    val tag: String? = null,
    val author: String? = null,
    val finished: String? = null,
    val sort: String? = null,
    val categoryType: String? = null,
    val categoryArea: String? = null,
) {
    /** 转换为 OkHttp 请求的查询字典。 / Converts to query map for OkHttp. */
    fun toQueryMap(): Map<String, Any?> = mapOf(
        "page" to page,
        "c" to category,
        "t" to tag,
        "a" to author,
        "f" to finished,
        "s" to sort,
        "ct" to categoryType,
        "ca" to categoryArea,
    )

    fun toBuilder(): Builder = Builder()
        .page(page)
        .category(category)
        .tag(tag)
        .author(author)
        .finished(finished)
        .sort(sort)
        .categoryType(categoryType)
        .categoryArea(categoryArea)

    class Builder {
        var page: Int = 1
        var category: String? = null
        var tag: String? = null
        var author: String? = null
        var finished: String? = null
        var sort: String? = null
        var categoryType: String? = null
        var categoryArea: String? = null

        fun page(page: Int) = apply { this.page = page }
        fun category(category: String?) = apply { this.category = category }
        fun tag(tag: String?) = apply { this.tag = tag }
        fun author(author: String?) = apply { this.author = author }
        fun finished(finished: String?) = apply { this.finished = finished }
        fun sort(sort: String?) = apply { this.sort = sort }
        fun categoryType(categoryType: String?) = apply { this.categoryType = categoryType }
        fun categoryArea(categoryArea: String?) = apply { this.categoryArea = categoryArea }

        fun build(): ComicQuery = ComicQuery(
            page = page,
            category = category,
            tag = tag,
            author = author,
            finished = finished,
            sort = sort,
            categoryType = categoryType,
            categoryArea = categoryArea,
        )
    }

    companion object {
        @JvmStatic
        fun builder(): Builder = Builder()

        inline fun build(block: Builder.() -> Unit): ComicQuery = Builder().apply(block).build()
    }
}
