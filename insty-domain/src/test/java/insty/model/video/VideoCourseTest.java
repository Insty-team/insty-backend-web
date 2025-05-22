package insty.model.video;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class VideoCourseTest {

    @Test
    void create_정상() {
        // given
        String fileName = "fileName.mp4";

        // when
        VideoCourse videoCourse = VideoCourse.create(fileName);

        // then
        assertThat(videoCourse).isNotNull();
        assertThat(videoCourse.getId()).isNull();
        assertThat(videoCourse.getVideoUuid()).isNotNull();
        assertThat(videoCourse.getS3Key())
                .startsWith("vod/" + VideoType.COURSE + "/mp4/")
                .endsWith("/" + fileName);
        assertThat(videoCourse.getExtension()).isEqualTo("mp4");
        assertThat(videoCourse.getOriginalFileName()).isEqualTo(fileName);
        assertThat(videoCourse.getEncodingStatus()).isEqualTo(EncodingStatus.PROCESSING);
        assertThat(videoCourse.getEncodingAt()).isNotNull();
        assertThat(videoCourse.getAnalysisStatus()).isEqualTo(AnalysisStatus.WAITING);
        assertThat(videoCourse.getAnalysisAt()).isNull();
    }

    @Test
    void create_에러_확장자명이_없다() {
        // given
        String fileName = "fileName";

        // when

        // then
        assertThatThrownBy(() -> VideoCourse.create(fileName))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_INVALID_FILE_NAME);
    }
}