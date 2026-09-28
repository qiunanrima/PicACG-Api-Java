package com.picaapi

import com.picacomic.fregata.objects.UserProfileObject
import com.picacomic.fregata.objects.requests.SignInBody
import com.picacomic.fregata.objects.requests.UpdatePicaIdBody
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PicaClientTest {

    private lateinit var server: MockWebServer

    @BeforeEach
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @AfterEach
    fun tearDown() {
        server.close()
    }

    @Test
    fun testEmptyBodyPostDoesNotCrash() {
        server.enqueue(
            MockResponse.Builder()
                .code(200)
                .body("""{"code":200,"message":"success","data":{"action":"like"}}""")
                .build(),
        )

        val client = PicaClient(PicaConfig(baseUrl = server.url("/").toString()))
        val result = client.likeComic("test-comic-id")

        assertTrue(result.isSuccess)
        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("/comics/test-comic-id/like", recorded.url.encodedPath)
    }

    @Test
    fun testPunchInEmptyBodyPost() {
        server.enqueue(
            MockResponse.Builder()
                .code(200)
                .body("""{"code":200,"message":"success","data":{"res":{"status":"ok"}}}""")
                .build(),
        )

        val client = PicaClient(PicaConfig(baseUrl = server.url("/").toString()))
        val result = client.punchIn()

        assertTrue(result.isSuccess)
        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("/users/punch-in", recorded.url.encodedPath)
    }

    @Test
    fun testUnitResponseSuccess() {
        server.enqueue(
            MockResponse.Builder()
                .code(200)
                .body("""{"code":200,"message":"success"}""")
                .build(),
        )

        val client = PicaClient(PicaConfig(baseUrl = server.url("/").toString()))
        val result = client.updatePicaId(UpdatePicaIdBody("user@example.com", "new-name"))

        assertTrue(result.isSuccess)
        assertEquals(Unit, (result as PicaResult.Success).data)
    }

    @Test
    fun testHttp200BusinessErrorIsFailure() {
        server.enqueue(
            MockResponse.Builder()
                .code(200)
                .body("""{"code":400,"error":"1004","message":"invalid signature"}""")
                .build(),
        )

        val client = PicaClient(PicaConfig(baseUrl = server.url("/").toString()))
        val result = client.getCategories()

        assertTrue(result.isFailure)
        val failure = result.getFailureOrNull()
        assertEquals(200, failure?.httpCode)
        assertEquals("1004", failure?.errorCode)
        assertEquals("invalid signature", failure?.message)
    }

    @Test
    fun testAsyncHttp200BusinessErrorIsFailure() {
        server.enqueue(
            MockResponse.Builder()
                .code(200)
                .body("""{"code":401,"error":"1005","detail":"token expired"}""")
                .build(),
        )

        val client = PicaClient(PicaConfig(baseUrl = server.url("/").toString()))
        val result = client.async.getCategories().get()

        assertTrue(result.isFailure)
        val failure = result.getFailureOrNull()
        assertEquals("1005", failure?.errorCode)
        assertEquals("token expired", failure?.message)
    }

    @Test
    fun testUpdateConfigRebuildsHttpClient() {
        val client = PicaClient(PicaConfig(enableLogging = false))
        client.updateConfig(PicaConfig(enableLogging = true))
        assertTrue(client.config.enableLogging)
    }

    @Test
    fun testChatroomProfileSync() {
        val client = ChatroomClient()
        val user = UserProfileObject().apply {
            name = "OldName"
            character = "OldChar"
            title = "OldTitle"
            level = 1
            userId = "u123"
        }
        client.setProfile(user)

        val updateJson = JSONObject().apply {
            put("name", "NewName")
            put("character", "NewChar")
            put("title", "NewTitle")
            put("level", 5)
        }

        val updated = client.parseMessage(updateJson, ChatroomClient.TYPE_TEXT)
        assertNotNull(updated)
        assertEquals("NewName", updated?.name)
    }

    @Test
    fun testKotlinDslAndExtensions() {
        val client = PicaClient {
            imageQuality = PicaImageQuality.HIGH
            enableLogging = true
        }
        assertEquals("high", client.config.imageQuality)
        assertTrue(client.config.enableLogging)

        val cfg = picaConfig {
            imageQuality = PicaImageQuality.LOW
        }
        assertEquals("low", cfg.imageQuality)

        val thumb = com.picacomic.fregata.objects.ThumbnailObject().apply {
            fileServer = "https://cdn.example.com"
            path = "img.png"
        }
        assertEquals("https://cdn.example.com/static/img.png", thumb.toImageUrl())

        val page = com.picacomic.fregata.objects.ComicPageObject("p1", thumb)
        assertEquals("https://cdn.example.com/static/img.png", page.toImageUrl())

        val chat = ChatroomClient()
        var connected = false
        chat.listen {
            onConnected { connected = true }
        }
        chat.listener?.onConnected()
        assertTrue(connected)
    }

    @Test
    fun testSignInContentTypeUsesUppercaseUtf8() {
        // PicACG 服务端只接受 `charset=UTF-8`（大写），
        // 小写 `utf-8` 会返回 400/1023 "too many requests"。
        // The PicACG server only accepts uppercase `charset=UTF-8`;
        // lowercase `utf-8` returns 400/1023 "too many requests".
        server.enqueue(
            MockResponse.Builder()
                .code(200)
                .body("""{"code":200,"message":"success","data":{"token":"t"}}""")
                .build(),
        )

        val client = PicaClient(PicaConfig(baseUrl = server.url("/").toString()))
        val result = client.signIn(SignInBody("user@example.com", "secret123"))

        assertTrue(result.isSuccess)
        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals(
            "application/json; charset=UTF-8",
            recorded.headers["Content-Type"],
        )
    }

    @Test
    fun testPicaDownloaderSanitizeFilename() {
        assertEquals("comic_title_123", PicaDownloader.sanitizeFilename("comic/title:123"))
        assertEquals("safe_name", PicaDownloader.sanitizeFilename("safe?name"))
        assertEquals("safe_name____", PicaDownloader.sanitizeFilename("safe?name*<>|"))
        assertEquals("normal_name.jpg", PicaDownloader.sanitizeFilename("normal_name.jpg"))
        assertEquals("", PicaDownloader.sanitizeFilename(null))
    }

    @Test
    fun testAtomicWrite(@org.junit.jupiter.api.io.TempDir tempDir: java.io.File) {
        val target = java.io.File(tempDir, "test_image.bin")
        val data = "test-atomic-content".toByteArray(Charsets.UTF_8)
        PicaDownloader.writeAtomically(target, data)

        assertTrue(target.exists())
        assertEquals("test-atomic-content", target.readText())

        // Verify no leftover .tmp files
        val tmpFiles = tempDir.listFiles { _, name -> name.endsWith(".tmp") }
        assertTrue(tmpFiles.isNullOrEmpty())
    }

    @Test
    fun testDownloadImageAndEpisode(@org.junit.jupiter.api.io.TempDir tempDir: java.io.File) {
        val imageBytes = "binary-image-data".toByteArray(Charsets.UTF_8)

        // Mock image response
        server.enqueue(
            MockResponse.Builder()
                .code(200)
                .body(okio.Buffer().write(imageBytes))
                .build()
        )

        val imageUrl = server.url("/static/img001.jpg").toString()
        val client = PicaClient(PicaConfig(baseUrl = server.url("/").toString()))

        // Test fetchImageBytes
        val bytesRes = client.fetchImageBytes(imageUrl)
        assertTrue(bytesRes.isSuccess)
        assertArrayEquals(imageBytes, (bytesRes as PicaResult.Success).data)

        // Mock image response for downloadImage
        server.enqueue(
            MockResponse.Builder()
                .code(200)
                .body(okio.Buffer().write(imageBytes))
                .build()
        )

        val targetFile = java.io.File(tempDir, "page001.jpg")
        val downloadRes = client.downloadImage(imageUrl, targetFile)
        assertTrue(downloadRes.isSuccess)
        assertTrue(targetFile.exists())
        assertArrayEquals(imageBytes, targetFile.readBytes())

        // Test skip if already exists and overwrite = false
        val skippedRes = client.downloadImage(imageUrl, targetFile, overwrite = false)
        assertTrue(skippedRes.isSuccess)

        // Test ComicPageObject.download extension
        server.enqueue(
            MockResponse.Builder()
                .code(200)
                .body(okio.Buffer().write(imageBytes))
                .build()
        )
        val thumb = com.picacomic.fregata.objects.ThumbnailObject().apply {
            fileServer = server.url("").toString().removeSuffix("/")
            path = "page_ext.jpg"
        }
        val pageObj = com.picacomic.fregata.objects.ComicPageObject("p2", thumb)
        val extTarget = java.io.File(tempDir, "ext_page.jpg")
        val extRes = pageObj.download(extTarget, client)
        assertTrue(extRes.isSuccess)
        assertTrue(extTarget.exists())
    }
}
