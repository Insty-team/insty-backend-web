package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.video.repository.VideoCommunityCommentRepository;
import insty.model.community.CommunityComment;
import insty.model.community.CommunityCommentFixtureBuilder;
import insty.model.community.CommunityPostFixtureBuilder;
import insty.model.user.UserFixtureBuilder;
import insty.model.video.VideoCommunityComment;
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
    }

    @Test
    void updateAndGetLinkedVideo_null이면_삭제() {
        CommunityComment comment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(
                CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser());
        UUID videoUuid = UUID.randomUUID();
        VideoCommunityComment current = VideoCommunityComment.create("v.mp4", videoUuid,
                UserFixtureBuilder.getUserWithId());

        when(videoCommunityCommentRepository.findByCommunityCommentIdAndIsDeleted(comment.getId(), false))
                .thenReturn(Optional.of(current));
        when(videoCommunityCommentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        VideoCommunityComment result = communityCommentVideoManager.updateAndGetLinkedVideo(comment, null);

        assertThat(result).isNull();
        assertThat(current.isDeleted()).isTrue();
        verify(videoCommunityCommentRepository).save(current);
    }

    @Test
    void deleteVideo_정상_soft삭제() {
        CommunityComment comment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(
                CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser());
        VideoCommunityComment current = VideoCommunityComment.create("v.mp4", UUID.randomUUID(),
                UserFixtureBuilder.getUserWithId());

        when(videoCommunityCommentRepository.findByCommunityCommentIdAndIsDeleted(comment.getId(), false))
                .thenReturn(Optional.of(current));
        when(videoCommunityCommentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        communityCommentVideoManager.deleteVideo(comment);

        assertThat(current.isDeleted()).isTrue();
        verify(videoCommunityCommentRepository).save(current);
    }

    @Test
    void deleteVideo_영상없으면_무시() {
        CommunityComment comment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(
                CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser());
        when(videoCommunityCommentRepository.findByCommunityCommentIdAndIsDeleted(comment.getId(), false))
                .thenReturn(Optional.empty());

        assertThatCode(() -> communityCommentVideoManager.deleteVideo(comment)).doesNotThrowAnyException();
    }
}
