package com.picaapi

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

/**
 * 手动集成探针：直连真实 PicACG API 验证完整链路。
 * Manual integration probe against the real PicACG API.
 *
 * 默认禁用（依赖真实网络与测试账号）；本地验证时手动移除 @Disabled 运行。
 */
@Disabled("manual: needs real network + test account")
class RealApiProbeTest {

    @Test
    fun probeFullFlow() {
        val client = PicaClient(
            PicaConfig(
                enableLogging = false,
            ),
        )

        println("== login ==")
        val login = client.login("test12345678901234567890", "test12345678901234567890")
        when (login) {
            is PicaResult.Success -> println("login ok, token=${login.data.token.take(24)}...")
            is PicaResult.Failure -> {
                println("login FAILED http=${login.httpCode} err=${login.errorCode} msg=${login.message} raw=${login.rawBody}")
                return
            }
        }

        println("== comics list (category) ==")
        val list = client.getComics(page = 1, category = "嗶咔漢化", sort = PicaSort.NEWEST)
        val firstId: String
        when (val r = list) {
            is PicaResult.Success -> {
                val docs = r.data.comics?.docs.orEmpty()
                println("total=${r.data.comics?.total} pages=${r.data.comics?.pages} docs=${docs.size}")
                docs.take(3).forEach { println("  id=${it.comicId} title=${it.title}") }
                firstId = docs.firstOrNull()?.comicId ?: return println("no docs")
            }
            is PicaResult.Failure -> {
                println("list FAILED http=${r.httpCode} err=${r.errorCode} msg=${r.message} raw=${r.rawBody}")
                return
            }
        }

        println("== comic detail id=$firstId ==")
        when (val d = client.getComicDetail(firstId)) {
            is PicaResult.Success -> println("detail ok title=${d.data.comic?.title} eps=${d.data.comic?.episodeCount}")
            is PicaResult.Failure -> println("detail FAILED http=${d.httpCode} err=${d.errorCode} msg=${d.message} raw=${d.rawBody}")
        }

        println("== episodes id=$firstId ==")
        when (val e = client.getComicEpisodes(firstId, page = 1)) {
            is PicaResult.Success -> println("eps ok count=${e.data.eps?.docs?.size} pages=${e.data.eps?.pages}")
            is PicaResult.Failure -> println("eps FAILED http=${e.httpCode} err=${e.errorCode} msg=${e.message} raw=${e.rawBody}")
        }

        println("== pages id=$firstId (order=1, page=1) ==")
        when (val p = client.getComicPagesByOrder(firstId, order = 1, page = 1)) {
            is PicaResult.Success -> {
                val urls = p.data.pages?.docs.orEmpty().mapNotNull { it.toImageUrl() }
                println("pages ok count=${urls.size} first=${urls.firstOrNull()?.take(60)}")
            }
            is PicaResult.Failure -> println("pages FAILED http=${p.httpCode} err=${p.errorCode} msg=${p.message} raw=${p.rawBody}")
        }

        println("== bad id probe ==")
        when (val b = client.getComicDetail("garbage-id")) {
            is PicaResult.Success -> println("bad id OK?!")
            is PicaResult.Failure -> println("bad id http=${b.httpCode} err=${b.errorCode} msg=${b.message} raw=${b.rawBody}")
        }
    }
}
