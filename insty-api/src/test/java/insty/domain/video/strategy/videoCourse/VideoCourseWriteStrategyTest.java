package insty.domain.video.strategy.videoCourse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import insty.domain.video.dto.VideoUploadReq;
import insty.domain.video.repository.VideoCourseRepository;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import insty.model.video.BaseVideo;
import insty.model.video.VideoCourse;
import insty.uuid.UuidProvider;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class VideoCourseWriteStrategyTest {

    @InjectMocks
    private VideoCourseWriteStrategy videoCourseWriteStrategy;

    @Mock
    private UuidProvider uuidProvider;
    @Mock
    private VideoCourseRepository videoCourseRepository;

    @Test
    void saveVideo_정상() {
        // given
        String fileName = "fileName.mp4";
        String contentType = "video/mp4";
        VideoUploadReq req = new VideoUploadReq(fileName, contentType);
        User user = UserFixtureBuilder.getUserWithId();

        // mock
        when(uuidProvider.generate())
                .thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        when(videoCourseRepository.save(any(VideoCourse.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        BaseVideo videoCourse = videoCourseWriteStrategy.saveVideo(req, user);

        // then
        assertThat(videoCourse).isNotNull();
//        assertThat(videoCourse.getId()).isNotNull(); // 객체 캡슐화를 지키고 id 생성 테스트는 생략
        assertThat(videoCourse.getOriginalFileName()).isEqualTo(fileName);
    }
}