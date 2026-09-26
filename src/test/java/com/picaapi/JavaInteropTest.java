package com.picaapi;

import com.picacomic.fregata.objects.ComicPageObject;
import com.picacomic.fregata.objects.ThumbnailObject;
import com.picacomic.fregata.objects.UserProfileObject;
import com.picacomic.fregata.objects.responses.ActionResponse;
import com.picacomic.fregata.objects.responses.ComicDetailResponse;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class JavaInteropTest {

    private MockWebServer server;

    @BeforeEach
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    public void tearDown() {
        server.close();
    }

    @Test
    public void testPicaConfigBuilder() {
        PicaConfig config = PicaConfig.builder()
                .baseUrl("https://custom.api.com/")
                .imageQuality(PicaImageQuality.HIGH)
                .enableLogging(true)
                .disableSslVerification(true)
                .dnsIps("1.1.1.1", "8.8.8.8")
                .build();

        assertEquals("https://custom.api.com/", config.getBaseUrl());
        assertEquals("high", config.getImageQuality());
        assertTrue(config.getEnableLogging());
        assertTrue(config.getDisableSslVerification());
        assertEquals(List.of("1.1.1.1", "8.8.8.8"), config.getDnsIps());

        PicaConfig modified = config.toBuilder()
                .imageQuality(PicaImageQuality.LOW)
                .build();
        assertEquals("low", modified.getImageQuality());
        assertTrue(modified.getEnableLogging());
    }

    @Test
    public void testPicaResultJavaInterop() {
        PicaResult<String> success = PicaResult.success("hello", 200);
        assertTrue(success.isSuccess());
        assertFalse(success.isFailure());
        assertEquals("hello", success.getOrNull());
        assertEquals("hello", success.getOrDefault("world"));
        assertEquals("hello", success.getOrThrow());
        assertNull(success.getFailureOrNull());
        assertNull(success.exceptionOrNull());

        Optional<String> optional = Optional.ofNullable(success.getOrNull());
        assertTrue(optional.isPresent());
        assertEquals("hello", optional.get());

        AtomicReference<String> callbackValue = new AtomicReference<>();
        success.onSuccess(callbackValue::set)
                .onFailure(f -> fail("Should not be called"));
        assertEquals("hello", callbackValue.get());

        PicaResult<Integer> mapped = success.map(String::length);
        assertEquals(5, mapped.getOrNull());

        PicaResult<Integer> flatMapped = success.flatMap(s -> PicaResult.success(s.length() * 2));
        assertEquals(10, flatMapped.getOrNull());

        String folded = success.fold(
                val -> "Success: " + val,
                err -> "Failure: " + err.getMessage()
        );
        assertEquals("Success: hello", folded);

        // Failure tests
        PicaResult<String> failure = PicaResult.failure(404, "NOT_FOUND", "Not found", null, null);
        assertFalse(failure.isSuccess());
        assertTrue(failure.isFailure());
        assertNull(failure.getOrNull());
        assertEquals("default", failure.getOrDefault("default"));
        assertEquals("fallback", failure.getOrElse(f -> "fallback"));
        assertNotNull(failure.getFailureOrNull());
        assertEquals("NOT_FOUND", failure.getFailureOrNull().getErrorCode());

        AtomicBoolean failureCalled = new AtomicBoolean(false);
        failure.onSuccess(d -> fail("Should not be called"))
                .onFailure(f -> failureCalled.set(true));
        assertTrue(failureCalled.get());

        assertThrows(PicaException.class, failure::getOrThrow);
    }

    @Test
    public void testComicQueryBuilder() {
        ComicQuery query = ComicQuery.builder()
                .category("Cosplay")
                .sort(PicaSort.NEWEST)
                .page(2)
                .build();

        assertEquals("Cosplay", query.getCategory());
        assertEquals("dd", query.getSort());
        assertEquals(2, query.getPage());
        assertEquals("Cosplay", query.toQueryMap().get("c"));
        assertEquals("dd", query.toQueryMap().get("s"));
        assertEquals(2, query.toQueryMap().get("page"));
    }

    @Test
    public void testPicaClientOverloadsInJava() throws InterruptedException {
        server.enqueue(new MockResponse.Builder()
                .code(200)
                .body("{\"code\":200,\"message\":\"success\",\"data\":{\"action\":\"like\"}}")
                .build());

        PicaClient client = PicaClient.create(
                PicaConfig.builder().baseUrl(server.url("/").toString()).build()
        );

        // Testing overload: likeComic
        PicaResult<ActionResponse> result = client.likeComic("comic-123");
        assertTrue(result.isSuccess());

        // Testing overload: postComicComment convenient method with String
        server.enqueue(new MockResponse.Builder()
                .code(200)
                .body("{\"code\":200,\"message\":\"success\",\"data\":{\"action\":\"create\"}}")
                .build());
        client.postComicComment("comic-123", "Nice comic!");

        var recorded = server.takeRequest();
        assertEquals("/comics/comic-123/like", recorded.getUrl().encodedPath());
        var recordedComment = server.takeRequest();
        assertEquals("/comics/comic-123/comments", recordedComment.getUrl().encodedPath());
        assertTrue(recordedComment.getBody().utf8().contains("Nice comic!"));
    }

    @Test
    public void testAsyncClientInJava() throws ExecutionException, InterruptedException {
        server.enqueue(new MockResponse.Builder()
                .code(200)
                .body("{\"code\":200,\"message\":\"success\",\"data\":{\"action\":\"favourite\"}}")
                .build());

        PicaClient client = new PicaClient(
                PicaConfig.builder().baseUrl(server.url("/").toString()).build()
        );

        CompletableFuture<PicaResult<ActionResponse>> future = client.async().favouriteComic("comic-abc");
        PicaResult<ActionResponse> result = future.get();
        assertTrue(result.isSuccess());
        assertEquals("favourite", result.getOrThrow().getAction());
    }

    @Test
    public void testChatroomCallbacksBuilder() {
        AtomicBoolean connected = new AtomicBoolean(false);
        AtomicReference<String> message = new AtomicReference<>();

        ChatroomListener listener = ChatroomCallbacks.builder()
                .onConnected(() -> connected.set(true))
                .onNotification(message::set)
                .build();

        listener.onConnected();
        listener.onNotification("Welcome");

        assertTrue(connected.get());
        assertEquals("Welcome", message.get());
    }

    @Test
    public void testPicaImagesHelpers() {
        ThumbnailObject thumb = new ThumbnailObject();
        thumb.setFileServer("https://storage.picacomic.com");
        thumb.setPath("abc.jpg");

        String url = PicaImages.thumbnailUrl(thumb);
        assertEquals("https://storage.picacomic.com/static/abc.jpg", url);

        ComicPageObject page = new ComicPageObject("p1", thumb);
        String pageUrl = PicaImages.pageUrl(page);
        assertEquals("https://storage.picacomic.com/static/abc.jpg", pageUrl);

        UserProfileObject profile = new UserProfileObject();
        profile.setAvatar(thumb);
        String avatarUrl = PicaImages.avatarUrl(profile);
        assertEquals("https://storage.picacomic.com/static/abc.jpg", avatarUrl);
    }

    @Test
    public void testPicaDownloaderJavaInterop() {
        String safe = PicaDownloader.sanitizeFilename("comic/test:name?*<>|");
        assertEquals("comic_test_name_____", safe);

        assertNotNull(PicaDownloader.INSTANCE);
        PicaClient client = new PicaClient();
        assertNotNull(client.getDownloader());
        assertNotNull(Pica.getDownloader());
    }
}
