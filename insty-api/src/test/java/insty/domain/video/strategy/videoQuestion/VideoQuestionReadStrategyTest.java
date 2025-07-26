package insty.domain.video.strategy.videoQuestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import insty.domain.video.repository.VideoQuestionRepository;
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
class VideoQuestionReadStrategyTest {

    @InjectMocks
    private VideoQuestionReadStrategy videoQuestionReadStrategy;

    @Mock
    private VideoQuestionRepository videoQuestionRepository;

    @Test
    void getVideoUuid_정상() {
        // given
        Long parentId = 1L;

        // mock
        UUID fixedUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");
        when(videoQuestionRepository.findVideoUuidByCommunityQuestionId(parentId))
                .thenReturn(Optional.of(fixedUuid));

        // when
        UUID videoUuid = videoQuestionReadStrategy.getVideoUuid(parentId);

        // then
        assertThat(videoUuid).isEqualTo(fixedUuid);
    }

    @Test
    void getVideoUuid_에러_존재하지_않는_답변영상() {
        // given
        Long parentId = 1L;

        // mock
        when(videoQuestionRepository.findVideoUuidByCommunityQuestionId(parentId))
                .thenReturn(Optional.empty());

        // when

        // then
        assertThatThrownBy(() -> videoQuestionReadStrategy.getVideoUuid(parentId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_NOT_FOUND);
    }
}