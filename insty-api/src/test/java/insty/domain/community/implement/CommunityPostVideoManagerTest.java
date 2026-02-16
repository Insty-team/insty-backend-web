package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.ai.adapter.AiRequester;
import insty.domain.video.repository.VideoCommunityPostRepository;
import insty.domain.video.repository.VideoEncodingRepository;
import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityPost;
import insty.model.community.CommunityPostFixtureBuilder;
import insty.model.user.UserFixtureBuilder;
import insty.model.video.VideoCommunityPost;
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
class CommunityPostVideoManagerTest {

    @InjectMocks
    private CommunityPostVideoManager communityPostVideoManager;

    @Mock
    private AiRequester aiRequester;
    @Mock
    private S3FileManager s3FileManager;
    @Mock
    private VideoEncodingRepository videoEncodingRepository;
    @Mock
    private VideoCommunityPostRepository videoCommunityPostRepository;

    @Test
    void attachVideo_정상() {
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        UUID videoUuid = UUID.randomUUID();
        VideoCommunityPost video = VideoCommunityPost.create("v.mp4", videoUuid, UserFixtureBuilder.getUserWithId());

        when(videoCommunityPostRepository.findByVideoUuid(videoUuid)).thenReturn(Optional.of(video));
        when(videoCommunityPostRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        VideoCommunityPost saved = communityPostVideoManager.attachVideo(post, videoUuid);

        assertThat(saved.getCommunityPost()).isEqualTo(post);
        assertThat(saved.getVideoUuid()).isEqualTo(videoUuid);
    }

    @Test
    void updateAndGetLinkedVideo_동일영상이면_재사용() {
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        VideoCommunityPost current = VideoCommunityPost.create("v.mp4", UUID.randomUUID(),
                UserFixtureBuilder.getUserWithId());
        ReflectionTestUtils.setField(current, "communityPost", post);

        when(videoCommunityPostRepository.findByCommunityPostIdAndIsDeleted(post.getId(), false))
                .thenReturn(Optional.of(current));

        VideoCommunityPost result = communityPostVideoManager.updateAndGetLinkedVideo(post, current.getVideoUuid());

        assertThat(result).isEqualTo(current);
        verify(videoEncodingRepository, never()).findByVideoUuid(any());
    }

    @Test
    void updateAndGetLinkedVideo_null이면_삭제() {
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        UUID videoUuid = UUID.randomUUID();
        VideoCommunityPost current = VideoCommunityPost.create("v.mp4", videoUuid,
                UserFixtureBuilder.getUserWithId());

        when(videoCommunityPostRepository.findByCommunityPostIdAndIsDeleted(post.getId(), false))
                .thenReturn(Optional.of(current));
        when(videoCommunityPostRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        VideoCommunityPost result = communityPostVideoManager.updateAndGetLinkedVideo(post, null);

        assertThat(result).isNull();
        assertThat(current.isDeleted()).isTrue();
        verify(videoCommunityPostRepository).save(current);
    }

    @Test
    void deleteVideo_정상_soft삭제() {
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        VideoCommunityPost current = VideoCommunityPost.create("v.mp4", UUID.randomUUID(),
                UserFixtureBuilder.getUserWithId());

        when(videoCommunityPostRepository.findByCommunityPostIdAndIsDeleted(post.getId(), false))
                .thenReturn(Optional.of(current));
        when(videoCommunityPostRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        communityPostVideoManager.deleteVideo(post);

        assertThat(current.isDeleted()).isTrue();
        verify(videoCommunityPostRepository).save(current);
    }

    @Test
    void deleteVideo_영상없으면_무시() {
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        when(videoCommunityPostRepository.findByCommunityPostIdAndIsDeleted(post.getId(), false))
                .thenReturn(Optional.empty());

        assertThatCode(() -> communityPostVideoManager.deleteVideo(post)).doesNotThrowAnyException();
        verify(videoEncodingRepository, never()).findByVideoUuid(any());
    }
}
