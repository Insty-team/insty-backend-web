package insty.ai.adapter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

@Tag("integration")
public class AiRequesterIntTest {

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

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.close();
    }


    @Test
    void deleteUserData가_올바른_경로와_헤더로_요청을_발행한다() throws InterruptedException {
        // given
        mockWebServer.enqueue(new MockResponse.Builder().code(204).build());

        // when - Mono 반환
        StepVerifier.create(aiRequester.deleteUserData("test-token", 123L))
            .expectComplete()
            .verify(Duration.ofSeconds(3));

        // then
        RecordedRequest recorded = mockWebServer.takeRequest();
        assertThat(recorded.getMethod()).isEqualTo("DELETE");
        assertThat(recorded.getUrl().encodedPath()).isEqualTo("/api/v1/ai/users/me/ai-data");
        assertThat(recorded.getHeaders().get("Authorization")).isEqualTo("Bearer test-token");
    }

    @Test
    void 사용자_삭제_요청이_1초동안_처리되면_성공적으로_완료된다() {
        // given
        MockResponse delayedResponse = new MockResponse.Builder()
            .code(200)
            .headersDelay(1, TimeUnit.SECONDS)
            .build();
        mockWebServer.enqueue(delayedResponse);

        // when & then - 여유시간
        StepVerifier.create(aiRequester.deleteUserData("test-token", 123L))
            .expectComplete()
            .verify(Duration.ofSeconds(3));
    }

    @Test
    void deleteUserData가_재시도_후_성공한다() {
        // given - 첫 번째는 실패, 두 번째는 성공
        mockWebServer.enqueue(new MockResponse.Builder().code(500).build());
        mockWebServer.enqueue(new MockResponse.Builder().code(200).build());

        // when & then
        StepVerifier.create(aiRequester.deleteUserData("test-token", 123L))
            .expectComplete()
            .verify(Duration.ofSeconds(10));

        // 재시도로 2번 호출되었는지 확인
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    @Test
    void 사용자_삭제_요청이_실패해도_에러가_무시된다() {
        // given
        mockWebServer.enqueue(new MockResponse.Builder().code(500).build());
        mockWebServer.enqueue(new MockResponse.Builder().code(200).build());

        // when & then
        StepVerifier.create(aiRequester.deleteUserData("test-token", 123L))
            .expectComplete()
            .verify(Duration.ofSeconds(5));
    }
}
