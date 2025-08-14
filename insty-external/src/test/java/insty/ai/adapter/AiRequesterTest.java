package insty.ai.adapter;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;

import insty.ai.error.AiErrorCode;
import insty.exception.CustomException;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Tag("unit")
class AiRequesterTest {

    private MockWebServer mockWebServer;
    private AiRequester aiRequester;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();
        WebClient testWebClient = WebClient.builder()
            .baseUrl(baseUrl)
            .build();

        aiRequester = new AiRequester(testWebClient);
    }

    @Test
    void deleteAiVideoInfo_정상() {
        // given
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        WebClient client = WebClient.builder()
                .baseUrl("https://insty.test.com")
                .exchangeFunction(request -> Mono.just(
                        ClientResponse.create(HttpStatus.NO_CONTENT).build()))
                .build();
        aiRequester = new AiRequester(client);

        // when

        // then
        assertThatCode(() -> aiRequester.deleteAiVideoInfo(uuid))
                .doesNotThrowAnyException();
    }

    @Test
    void deleteAiVideoInfo_에러_AI서버_통신오류() {
        // given
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        WebClient client = WebClient.builder()
                .baseUrl("https://insty.test.com")
                .exchangeFunction(request -> Mono.just(
                        ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build()))
                .build();
        aiRequester = new AiRequester(client);

        // when

        // then
        assertThatThrownBy(() -> aiRequester.deleteAiVideoInfo(uuid))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(AiErrorCode.AI_API_REQUEST_FAILED);
    }

    @Test
    void deleteAiVideoInfo_에러_통신시도오류_AI서버의_오류가_아니면_상위_트랜잭션을_롤백하지_않는다() {
        // given
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        WebClient client = WebClient.builder()
                .baseUrl("https://insty.test.com")
                .exchangeFunction(request -> Mono.error(new RuntimeException("기타 오류")))
                .build();
        aiRequester = new AiRequester(client);

        // when

        // then
        assertThatCode(() -> aiRequester.deleteAiVideoInfo(uuid))
                .doesNotThrowAnyException();
    }

    @Test
    void 사용자_삭제_요청이_1초동안_처리되면_성공적으로_완료된다() {
        // given
        MockResponse delayedResponse = new MockResponse.Builder()
            .code(200)
            .headersDelay(1, TimeUnit.SECONDS)
            .build();
        mockWebServer.enqueue(delayedResponse);

        WebClient testClient = WebClient.builder()
            .baseUrl(mockWebServer.url("/").toString())
            .build();

        AtomicBoolean isCompleted = new AtomicBoolean(false);
        AtomicBoolean hasError = new AtomicBoolean(false);

        // when
        testClient.delete()
            .uri("/api/v1/ai/users/me/ai-data")
            .header("Authorization", "Bearer test-token")
            .retrieve()
            .toBodilessEntity()
            .subscribe(
                result -> isCompleted.set(true),
                error -> hasError.set(true)
            );

        // then - network & test여유시간
        await().atMost(3, TimeUnit.SECONDS).until(isCompleted::get);
        assertThat(isCompleted.get()).isTrue();
        assertThat(hasError.get()).isFalse();
    }

    @Test
    void 사용자_삭제_요청이_실패하면_에러_핸들러가_호출된다() {
        // given
        MockResponse errorResponse = new MockResponse.Builder()
            .code(500)
            .build();
        mockWebServer.enqueue(errorResponse);

        WebClient testClient = WebClient.builder()
            .baseUrl(mockWebServer.url("/").toString())
            .build();

        AtomicBoolean isCompleted = new AtomicBoolean(false);
        AtomicBoolean hasError = new AtomicBoolean(false);

        // when
        testClient.delete()
            .uri("/api/v1/ai/users/me/ai-data")
            .header("Authorization", "Bearer test-token")
            .retrieve()
            .toBodilessEntity()
            .subscribe(
                result -> isCompleted.set(true),
                error -> hasError.set(true)
            );

        // then
        await().atMost(3, TimeUnit.SECONDS).until(hasError::get);

        // 에러가 발생했고 완료되지 않았어야 함
        assertThat(hasError.get()).isTrue();
        assertThat(isCompleted.get()).isFalse();
    }
}