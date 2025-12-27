package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.ai.adapter.AiRequester;
import insty.domain.video.repository.VideoCommunityCommentRepository;
import insty.domain.video.repository.VideoEncodingRepository;
import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityComment;
import insty.model.community.CommunityCommentFixtureBuilder;
import insty.model.community.CommunityPostFixtureBuilder;
import insty.model.user.UserFixtureBuilder;
import insty.model.video.VideoCommunityComment;
import insty.model.video.VideoEncoding;
import insty.s3.adapter.S3FileManager;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CommunityCommentVideoManagerTest {

    @InjectMocks
    private CommunityCommentVideoManager communityCommentVideoManager;

    @Mock
    private AiRequester aiRequester;
    @Mock
    private S3FileManager s3FileManager;
    @Mock
    private VideoEncodingRepository videoEncodingRepository;
    @Mock
    private VideoCommunityCommentRepository videoCommunityCommentRepository;

    @Test
    void attachVideo_정상() {
        CommunityComment comment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(
                CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser());
        UUID videoUuid = UUID.randomUUID();
        VideoCommunityComment video = VideoCommunityComment.create("v.mp4", videoUuid,
                UserFixtureBuilder.getUserWithId());

        when(videoCommunityCommentRepository.findByVideoUuid(videoUuid)).thenReturn(Optional.of(video));
        when(videoCommunityCommentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        VideoCommunityComment saved = communityCommentVideoManager.attachVideo(comment, videoUuid);

        assertThat(saved.getCommunityComment()).isEqualTo(comment);
    }

    @Test
    void updateAndGetLinkedVideo_동일영상이면_재사용() {
        CommunityComment comment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(
                CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser());
        VideoCommunityComment current = VideoCommunityComment.create("v.mp4", UUID.randomUUID(),
                UserFixtureBuilder.getUserWithId());
        ReflectionTestUtils.setField(current, "communityComment", comment);

        when(videoCommunityCommentRepository.findByCommunityCommentIdAndIsDeleted(comment.getId(), false))
                .thenReturn(Optional.of(current));

        VideoCommunityComment result = communityCommentVideoManager.updateAndGetLinkedVideo(comment, current.getVideoUuid());

        assertThat(result).isEqualTo(current);
        verify(videoEncodingRepository, never()).findByVideoUuid(any());
    }

    @Test
    void updateAndGetLinkedVideo_null이면_삭제() {
        CommunityComment comment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(
                CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser());
        VideoCommunityComment current = VideoCommunityComment.create("v.mp4", UUID.randomUUID(),
                UserFixtureBuilder.getUserWithId());
        VideoEncoding encoding = VideoEncoding.builder().videoUuid(current.getVideoUuid()).encodingS3Key("vod/x/hls/a/b").build();

        when(videoCommunityCommentRepository.findByCommunityCommentIdAndIsDeleted(comment.getId(), false))
                .thenReturn(Optional.of(current));
        when(videoEncodingRepository.findByVideoUuid(current.getVideoUuid()))
                .thenReturn(Optional.of(encoding));

        VideoCommunityComment result = communityCommentVideoManager.updateAndGetLinkedVideo(comment, null);

        assertThat(result).isNull();
        verify(videoCommunityCommentRepository).delete(current);
        verify(videoEncodingRepository).delete(encoding);
    }

    @Test
    void deleteVideo_인코딩없으면_예외() {
        CommunityComment comment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(
                CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser());
        VideoCommunityComment current = VideoCommunityComment.create("v.mp4", UUID.randomUUID(),
                UserFixtureBuilder.getUserWithId());

        when(videoCommunityCommentRepository.findByCommunityCommentIdAndIsDeleted(comment.getId(), false))
                .thenReturn(Optional.of(current));
        when(videoEncodingRepository.findByVideoUuid(current.getVideoUuid()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> communityCommentVideoManager.deleteVideo(comment))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_NOT_FINISHED_ENCODING);
    }

    @Test
    void deleteVideo_영상없으면_무시() {
        CommunityComment comment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(
                CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser());
        when(videoCommunityCommentRepository.findByCommunityCommentIdAndIsDeleted(comment.getId(), false))
                .thenReturn(Optional.empty());

        assertThatCode(() -> communityCommentVideoManager.deleteVideo(comment)).doesNotThrowAnyException();
        verify(videoEncodingRepository, never()).findByVideoUuid(any());
    }
}
