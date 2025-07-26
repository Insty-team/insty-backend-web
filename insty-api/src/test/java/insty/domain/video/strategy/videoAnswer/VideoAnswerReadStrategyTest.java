package insty.domain.video.strategy.videoAnswer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import insty.domain.video.repository.VideoAnswerRepository;
import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class VideoAnswerReadStrategyTest {

    @InjectMocks
    private VideoAnswerReadStrategy videoAnswerReadStrategy;

    @Mock
    private VideoAnswerRepository videoAnswerRepository;

    @Test
    void getVideoUuid_정상() {
        // given
        Long parentId = 1L;

        // mock
        UUID fixedUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");
        when(videoAnswerRepository.findVideoUuidByCommunityAnswerId(parentId))
                .thenReturn(Optional.of(fixedUuid));

        // when
        UUID videoUuid = videoAnswerReadStrategy.getVideoUuid(parentId);

        // then
        assertThat(videoUuid).isEqualTo(fixedUuid);
    }

    @Test
    void getVideoUuid_에러_존재하지_않는_답변영상() {
        // given
        Long parentId = 1L;

        // mock
        when(videoAnswerRepository.findVideoUuidByCommunityAnswerId(parentId))
                .thenReturn(Optional.empty());

        // when

        // then
        assertThatThrownBy(() -> videoAnswerReadStrategy.getVideoUuid(parentId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_NOT_FOUND);
    }
}