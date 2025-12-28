package insty.model.video;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityComment;
import insty.model.community.CommunityCommentFixtureBuilder;
import insty.model.community.CommunityPostFixtureBuilder;
import insty.model.user.User;
import insty.model.user.UserFixture;
import insty.model.user.UserFixtureBuilder;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class VideoCommunityCommentTest {

    @Test
    void create_정상() {
        // given
        String fileName = "fileName.mp4";
        UUID uuid = UUID.randomUUID();
        User user = UserFixtureBuilder.getUserWithId();

        // when
        VideoCommunityComment video = VideoCommunityComment.create(fileName, uuid, user);

        // then
        assertThat(video).isNotNull();
        assertThat(video.getId()).isNull();
        assertThat(video.getVideoUuid()).isEqualTo(uuid);
        assertThat(video.getS3Key())
                .startsWith("vod/" + VideoType.COMMUNITY_COMMENT + "/mp4/")
                .endsWith("/" + fileName);
        assertThat(video.getExtension()).isEqualTo("mp4");
        assertThat(video.getOriginalFileName()).isEqualTo(fileName);
        assertThat(video.getEncodingStatus()).isEqualTo(EncodingStatus.PROCESSING);
        assertThat(video.getEncodingAt()).isNotNull();
    }

    @Test
    void create_에러_fileName이_null이다() {
        // given
        String fileName = null;
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000001");
        User user = UserFixtureBuilder.getUserWithId();

        // when, then
        assertThatThrownBy(() -> VideoCommunityComment.create(fileName, uuid, user))
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

        // when, then
        assertThatThrownBy(() -> VideoCommunityComment.create(fileName, uuid, user))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_CREATE_ERROR);
    }

    @Test
    void create_에러_fileName이_150자를_초과했다() {
        // given
        String fileName = "a".repeat(147) + ".mp4"; // 151 chars
        assertThat(fileName.length()).isEqualTo(151);

        UUID uuid = UUID.randomUUID();
        User user = UserFixtureBuilder.getUserWithId();

        // when, then
        assertThatThrownBy(() -> VideoCommunityComment.create(fileName, uuid, user))
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

        // when, then
        assertThatThrownBy(() -> VideoCommunityComment.create(fileName, uuid, user))
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

        // when, then
        assertThatThrownBy(() -> VideoCommunityComment.create(fileName, uuid, user))
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

        // when, then
        assertThatThrownBy(() -> VideoCommunityComment.create(fileName, uuid, user))
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

        // when, then
        assertThatThrownBy(() -> VideoCommunityComment.create(fileName, uuid, user))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_INVALID_FILE_NAME);
    }

    @Test
    void updateCommunityComment_정상() {
        // given
        VideoCommunityComment video = VideoCommunityComment.create("fileName.mp4", UUID.randomUUID(),
                UserFixtureBuilder.getUserWithId());
        CommunityComment comment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(
                CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser());

        // when
        video.updateCommunityComment(comment);

        // then
        assertThat(video.getCommunityComment()).isEqualTo(comment);
    }
}
