package com.picaapi

/**
 * 漫画排序常量。
 * Sorting order constants for comic search and listings.
 */
object PicaSort {
    /** 默认排序 / Default order (usually oldest or server default) */
    const val DEFAULT = "ua"

    /** 最新发布 / Newest first (date descending) */
    const val NEWEST = "dd"

    /** 最早发布 / Oldest first (date ascending) */
    const val OLDEST = "da"

    /** 最多爱心 / Most liked */
    const val MOST_LIKES = "ld"

    /** 最多绅士指名（最多浏览） / Most views */
    const val MOST_VIEWS = "vd"
}

/**
 * 排行榜时间跨度常量。
 * Leaderboard time duration constants.
 */
object PicaLeaderboardTime {
    /** 过去 24 小时 / Past 24 hours */
    const val HOUR_24 = "H24"

    /** 过去 7 天 / Past 7 days */
    const val DAY_7 = "D7"

    /** 过去 30 天 / Past 30 days */
    const val DAY_30 = "D30"
}

/**
 * 图片画质参数常量。
 * Image quality header options.
 */
object PicaImageQuality {
    const val ORIGINAL = "original"
    const val LOW = "low"
    const val MEDIUM = "medium"
    const val HIGH = "high"
}
