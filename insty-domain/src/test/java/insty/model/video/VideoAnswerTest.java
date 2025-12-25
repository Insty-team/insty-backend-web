package insty.model.video;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.model.courseqna.CommunityAnswerFixtureBuilder;
import insty.model.courseqna.CommunityQuestionFixtureBuilder;
import insty.model.courseqna.CourseAnswer;
import insty.model.courseqna.CourseQuestion;
import insty.model.user.User;
import insty.model.user.UserFixture;
import insty.model.user.UserFixtureBuilder;
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
        User user = UserFixtureBuilder.getUserWithId();

        // when
        VideoAnswer videoAnswer = VideoAnswer.create(fileName, uuid, user);

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
    void create_에러_fileName이_null이다() {
        // given
        String fileName = null;
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000001");
        User user = UserFixtureBuilder.getUserWithId();

        // when

        // then
        assertThatThrownBy(() -> VideoAnswer.create(fileName, uuid, user))
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
        assertThatThrownBy(() -> VideoAnswer.create(fileName, uuid, user))
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
        assertThatThrownBy(() -> VideoAnswer.create(fileName, uuid, user))
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
        assertThatThrownBy(() -> VideoAnswer.create(fileName, uuid, user))
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
        assertThatThrownBy(() -> VideoAnswer.create(fileName, uuid, user))
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
        assertThatThrownBy(() -> VideoAnswer.create(fileName, uuid, user))
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
        assertThatThrownBy(() -> VideoAnswer.create(fileName, uuid, user))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_INVALID_FILE_NAME);
    }

    @Test
    void updateCommunityAnswer_정상() {
        // given
        VideoAnswer videoAnswer = VideoFixtureBuilder.getVideoAnswerWithIdAndUser();
        CourseQuestion question = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        CourseAnswer answer = CommunityAnswerFixtureBuilder.getCommunityAnswerWithIdAndUser(question);

        // when
        videoAnswer.updateCommunityAnswer(answer);

        // then
        assertThat(videoAnswer.getCourseAnswer()).isEqualTo(answer);
    }
}