package insty.ai.adapter;

import insty.ai.error.AiErrorCode;
import insty.exception.CustomException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiRequester {

    private final WebClient aiApiWebClient;

    public void deleteAiVideoInfo(UUID videoUuid) {
        try {
            aiApiWebClient.delete()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/ai/videos/{videoUuid}")
                            .build(videoUuid))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.info("AI 통신 오류 - 해당 videoUuid에 대해 처리되지 않았습니다. videoUuid: {}", videoUuid);
            } else {
                log.error("AI 통신 오류 - deleteAiVideoInfo : videoUuid={}, body={}", videoUuid,
                        e.getResponseBodyAsString(), e);
                throw new CustomException(AiErrorCode.AI_API_REQUEST_FAILED);
            }
        } catch (Exception e) {
            log.error("AI 통신 오류 - deleteAiVideoInfo : videoUuid={}", videoUuid, e);
        }
    }
}
