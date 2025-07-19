package insty.ai.adapter;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.ai.error.AiErrorCode;
import insty.exception.CustomException;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Tag("unit")
class AiRequesterTest {

    private AiRequester aiRequester;

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
}