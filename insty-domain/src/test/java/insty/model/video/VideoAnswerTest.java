package insty.model.video;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class VideoAnswerTest {

    @Test
    void create_정상() {
        // given
        String fileName = "fileName.mp4";
        UUID uuid = UUID.randomUUID();

        // when
        VideoAnswer videoAnswer = VideoAnswer.create(fileName, uuid);

        // then
        assertThat(videoAnswer).isNotNull();
        assertThat(videoAnswer.getId()).isNull();
        assertThat(videoAnswer.getVideoUuid()).isEqualTo(uuid);
        assertThat(videoAnswer.getS3Key())
                .startsWith("vod/" + VideoType.ANSWER + "/mp4/")
                .endsWith("/" + fileName);
        assertThat(videoAnswer.getExtension()).isEqualTo("mp4");
        assertThat(videoAnswer.getOriginalFileName()).isEqualTo(fileName);
        assertThat(videoAnswer.getEncodingStatus()).isEqualTo(EncodingStatus.PROCESSING);
        assertThat(videoAnswer.getEncodingAt()).isNotNull();
    }

    @Test
    void create_에러_확장자명이_없다() {
        // given
        String fileName = "fileName";
        UUID uuid = UUID.randomUUID();

        // when

        // then
        assertThatThrownBy(() -> VideoCourse.create(fileName, uuid))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_INVALID_FILE_NAME);
    }
}