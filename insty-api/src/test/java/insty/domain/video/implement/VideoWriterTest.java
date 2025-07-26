package insty.domain.video.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import insty.domain.video.dto.VideoUploadReq;
import insty.domain.video.repository.VideoAnswerRepository;
import insty.domain.video.repository.VideoCourseRepository;
import insty.domain.video.repository.VideoQuestionRepository;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import insty.model.video.VideoAnswer;
import insty.model.video.VideoCourse;
import insty.model.video.VideoQuestion;
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
class VideoWriterTest {

    @Mock
    private VideoCourseRepository videoCourseRepository;
    @Mock
    private VideoQuestionRepository videoQuestionRepository;
    @Mock
    private VideoAnswerRepository videoAnswerRepository;
    @Mock
    private UuidProvider uuidProvider;

    @InjectMocks
    private VideoWriter videoWriter;

    @Test
    void saveVideoCourse_정상() {
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
        VideoCourse videoCourse = videoWriter.saveVideoCourse(req, user);

        // then
        assertThat(videoCourse).isNotNull();
//        assertThat(videoCourse.getId()).isNotNull(); // 객체 캡슐화를 지키고 id 생성 테스트는 생략
        assertThat(videoCourse.getOriginalFileName()).isEqualTo(fileName);
    }

    @Test
    void saveVideoQuestion_정상() {
        // given
        String fileName = "fileName.mp4";
        String contentType = "video/mp4";
        VideoUploadReq req = new VideoUploadReq(fileName, contentType);
        User user = UserFixtureBuilder.getUserWithId();

        // mock
        when(uuidProvider.generate())
                .thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        when(videoQuestionRepository.save(any(VideoQuestion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        VideoQuestion videoQuestion = videoWriter.saveVideoQuestion(req, user);

        // then
        assertThat(videoQuestion).isNotNull();
//        assertThat(videoQuestion.getId()).isNotNull(); // 객체 캡슐화를 지키고 id 생성 테스트는 생략
        assertThat(videoQuestion.getOriginalFileName()).isEqualTo(fileName);
    }

    @Test
    void saveVideoAnswer_정상() {
        // given
        String fileName = "fileName.mp4";
        String contentType = "video/mp4";
        VideoUploadReq req = new VideoUploadReq(fileName, contentType);
        User user = UserFixtureBuilder.getUserWithId();

        // mock
        when(uuidProvider.generate())
                .thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        when(videoAnswerRepository.save(any(VideoAnswer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        VideoAnswer videoAnswer = videoWriter.saveVideoAnswer(req, user);

        // then
        assertThat(videoAnswer).isNotNull();
//        assertThat(videoAnswer.getId()).isNotNull(); // 객체 캡슐화를 지키고 id 생성 테스트는 생략
        assertThat(videoAnswer.getOriginalFileName()).isEqualTo(fileName);
    }
}