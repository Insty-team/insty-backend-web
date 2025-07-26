package insty.model.video;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.model.user.User;
import insty.model.user.UserFixture;
import insty.model.user.UserFixtureBuilder;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class VideoQuestionTest {

    @Test
    void create_정상() {
        // given
        String fileName = "fileName.mp4";
        UUID uuid = UUID.randomUUID();
        User user = UserFixtureBuilder.getUserWithId();

        // when
        VideoQuestion videoQuestion = VideoQuestion.create(fileName, uuid, user);

        // then
        assertThat(videoQuestion).isNotNull();
        assertThat(videoQuestion.getId()).isNull();
        assertThat(videoQuestion.getVideoUuid()).isEqualTo(uuid);
        assertThat(videoQuestion.getS3Key())
                .startsWith("vod/" + VideoType.QUESTION + "/mp4/")
                .endsWith("/" + fileName);
        assertThat(videoQuestion.getExtension()).isEqualTo("mp4");
        assertThat(videoQuestion.getOriginalFileName()).isEqualTo(fileName);
        assertThat(videoQuestion.getEncodingStatus()).isEqualTo(EncodingStatus.PROCESSING);
        assertThat(videoQuestion.getEncodingAt()).isNotNull();
    }

    @Test
    void create_에러_fileName이_null이다() {
        // given
        String fileName = null;
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000001");
        User user = UserFixtureBuilder.getUserWithId();

        // when

        // then
        assertThatThrownBy(() -> VideoQuestion.create(fileName, uuid, user))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_CREATE_ERROR);
    }

    @Test
    void create_에러_fileName이_비었다() {
        // given
        String fileName = "  \n\t\r";
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000001");
        User user = UserFixtureBuilder.getUserWithId();

        // when

        // then
        assertThatThrownBy(() -> VideoQuestion.create(fileName, uuid, user))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_CREATE_ERROR);
    }

    @Test
    void create_에러_fileName이_150자를_초과했다() {
        // given
        String fileName = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa.mp4";
        assertThat(fileName.length()).isEqualTo(151);

        UUID uuid = UUID.randomUUID();
        User user = UserFixtureBuilder.getUserWithId();

        // when

        // then
        assertThatThrownBy(() -> VideoQuestion.create(fileName, uuid, user))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_CREATE_ERROR);
    }

    @Test
    void create_에러_uuid가_null이다() {
        // given
        String fileName = "fileName.mp4";
        UUID uuid = null;
        User user = UserFixtureBuilder.getUserWithId();

        // when

        // then
        assertThatThrownBy(() -> VideoQuestion.create(fileName, uuid, user))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_CREATE_ERROR);
    }

    @Test
    void create_에러_user가_null이다() {
        // given
        String fileName = "fileName.mp4";
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000001");
        User user = null;

        // when

        // then
        assertThatThrownBy(() -> VideoQuestion.create(fileName, uuid, user))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_CREATE_ERROR);
    }

    @Test
    void create_에러_user_id가_null이다() {
        // given
        String fileName = "fileName.mp4";
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000001");
        User user = UserFixture.getUser();

        // when

        // then
        assertThatThrownBy(() -> VideoQuestion.create(fileName, uuid, user))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_CREATE_ERROR);
    }

    @Test
    void create_에러_확장자명이_없다() {
        // given
        String fileName = "fileName";
        UUID uuid = UUID.randomUUID();
        User user = UserFixtureBuilder.getUserWithId();

        // when

        // then
        assertThatThrownBy(() -> VideoQuestion.create(fileName, uuid, user))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_INVALID_FILE_NAME);
    }
}