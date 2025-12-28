package insty.domain.courseqna.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.ai.adapter.AiRequester;
import insty.domain.video.repository.VideoAnswerRepository;
import insty.domain.video.repository.VideoEncodingRepository;
import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.model.courseqna.CourseAnswer;
import insty.model.video.VideoAnswer;
import insty.model.video.VideoEncoding;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import insty.s3.adapter.S3FileManager;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CourseAnswerVideoManagerTest {

    @InjectMocks
    private CourseAnswerVideoManager videoManager;
    @Mock
    private AiRequester aiRequester;
    @Mock
    private S3FileManager s3FileManager;
    @Mock
    private VideoEncodingRepository videoEncodingRepository;
    @Mock
    private VideoAnswerRepository videoAnswerRepository;

    @Test
    void attachVideoToAnswer_정상() {
        // given
        CourseAnswer answer = mock(CourseAnswer.class);
        UUID videoUuid = UUID.randomUUID();
        VideoAnswer videoAnswer = mock(VideoAnswer.class);
        when(videoAnswerRepository.findByVideoUuid(videoUuid)).thenReturn(Optional.of(videoAnswer));
        when(videoAnswerRepository.save(videoAnswer)).thenReturn(videoAnswer);

        // when
        VideoAnswer result = videoManager.attachVideoToAnswer(answer, videoUuid);

        // then
        assertThat(result).isEqualTo(videoAnswer);
        verify(videoAnswer).updateCourseAnswer(answer);
        verify(videoAnswerRepository).save(videoAnswer);
    }

    @Test
    void attachVideoToAnswer_에러_비디오존재하지않음() {
        // given
        CourseAnswer answer = mock(CourseAnswer.class);
        UUID videoUuid = UUID.randomUUID();
        when(videoAnswerRepository.findByVideoUuid(videoUuid)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> videoManager.attachVideoToAnswer(answer, videoUuid))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_NOT_FOUND);
    }

    @Test
    void updateAndGetLinkedVideo_새로운비디오_기존비디오없음() {
        // given
        CourseAnswer answer = mock(CourseAnswer.class);
        when(answer.getId()).thenReturn(1L);
        UUID newVideoUuid = UUID.randomUUID();
        VideoAnswer newVideoAnswer = mock(VideoAnswer.class);
        when(videoAnswerRepository.findByCourseAnswerIdAndIsDeleted(1L, false)).thenReturn(Optional.empty());
        when(videoAnswerRepository.findByVideoUuid(newVideoUuid)).thenReturn(Optional.of(newVideoAnswer));
        when(videoAnswerRepository.save(newVideoAnswer)).thenReturn(newVideoAnswer);

        // when
        VideoAnswer result = videoManager.updateAndGetLinkedVideo(answer, newVideoUuid);

        // then
        assertThat(result).isEqualTo(newVideoAnswer);
        verify(videoAnswerRepository, never()).delete(any());
        verify(newVideoAnswer).updateCourseAnswer(answer);
    }

    @Test
    void updateAndGetLinkedVideo_새로운비디오_기존비디오있음() {
        // given
        CourseAnswer answer = mock(CourseAnswer.class);
        when(answer.getId()).thenReturn(1L);
        UUID oldVideoUuid = UUID.randomUUID();
        UUID newVideoUuid = UUID.randomUUID();
        VideoAnswer oldVideoAnswer = mock(VideoAnswer.class);
        when(oldVideoAnswer.getVideoUuid()).thenReturn(oldVideoUuid);
        VideoAnswer newVideoAnswer = mock(VideoAnswer.class);
        VideoEncoding oldVideoEncoding = mock(VideoEncoding.class);

        when(videoAnswerRepository.findByCourseAnswerIdAndIsDeleted(1L, false)).thenReturn(Optional.of(oldVideoAnswer));
        when(videoEncodingRepository.findByVideoUuid(oldVideoUuid)).thenReturn(Optional.of(oldVideoEncoding));
        when(videoAnswerRepository.findByVideoUuid(newVideoUuid)).thenReturn(Optional.of(newVideoAnswer));
        when(videoAnswerRepository.save(newVideoAnswer)).thenReturn(newVideoAnswer);

        // when
        VideoAnswer result = videoManager.updateAndGetLinkedVideo(answer, newVideoUuid);

        // then
        assertThat(result).isEqualTo(newVideoAnswer);
        verify(videoAnswerRepository).delete(oldVideoAnswer);
        verify(videoEncodingRepository).delete(oldVideoEncoding);
        verify(aiRequester).deleteAiVideoInfo(oldVideoUuid);
        verify(s3FileManager).deleteAllByDirectory(any());
        verify(newVideoAnswer).updateCourseAnswer(answer);
    }

    @Test
    void updateAndGetLinkedVideo_동일비디오() {
        // given
        CourseAnswer answer = mock(CourseAnswer.class);
        when(answer.getId()).thenReturn(1L);
        UUID videoUuid = UUID.randomUUID();
        VideoAnswer existingVideo = mock(VideoAnswer.class);
        when(existingVideo.getVideoUuid()).thenReturn(videoUuid);
        when(videoAnswerRepository.findByCourseAnswerIdAndIsDeleted(1L, false)).thenReturn(Optional.of(existingVideo));

        // when
        VideoAnswer result = videoManager.updateAndGetLinkedVideo(answer, videoUuid);

        // then
        assertThat(result).isEqualTo(existingVideo);
        verify(videoAnswerRepository, never()).delete(any());
        verify(videoAnswerRepository, never()).save(any());
    }

    @Test
    void updateAndGetLinkedVideo_null_기존비디오있음() {
        // given
        CourseAnswer answer = mock(CourseAnswer.class);
        when(answer.getId()).thenReturn(1L);
        UUID oldVideoUuid = UUID.randomUUID();
        VideoAnswer oldVideoAnswer = mock(VideoAnswer.class);
        when(oldVideoAnswer.getVideoUuid()).thenReturn(oldVideoUuid);
        VideoEncoding oldVideoEncoding = mock(VideoEncoding.class);
        when(videoAnswerRepository.findByCourseAnswerIdAndIsDeleted(1L, false)).thenReturn(Optional.of(oldVideoAnswer));
        when(videoEncodingRepository.findByVideoUuid(oldVideoUuid)).thenReturn(Optional.of(oldVideoEncoding));

        // when
        VideoAnswer result = videoManager.updateAndGetLinkedVideo(answer, null);

        // then
        assertThat(result).isNull();
        verify(videoAnswerRepository).delete(oldVideoAnswer);
        verify(videoEncodingRepository).delete(oldVideoEncoding);
        verify(aiRequester).deleteAiVideoInfo(oldVideoUuid);
        verify(s3FileManager).deleteAllByDirectory(any());
    }

    @Test
    void updateAndGetLinkedVideo_null_기존비디오없음() {
        // given
        CourseAnswer answer = mock(CourseAnswer.class);
        when(answer.getId()).thenReturn(1L);
        when(videoAnswerRepository.findByCourseAnswerIdAndIsDeleted(1L, false)).thenReturn(Optional.empty());

        // when
        VideoAnswer result = videoManager.updateAndGetLinkedVideo(answer, null);

        // then
        assertThat(result).isNull();
        verify(videoAnswerRepository, never()).delete(any());
    }

    @Test
    void getVideoAnswer_정상_비디오존재() {
        // given
        CourseAnswer answer = mock(CourseAnswer.class);
        when(answer.getId()).thenReturn(1L);
        VideoAnswer videoAnswer = mock(VideoAnswer.class);
        when(videoAnswerRepository.findByCourseAnswerIdAndIsDeleted(1L, false))
                .thenReturn(Optional.of(videoAnswer));

        // when
        VideoAnswer result = videoManager.getVideoAnswer(answer);

        // then
        assertThat(result).isEqualTo(videoAnswer);
    }

    @Test
    void getVideoAnswer_정상_비디오존재하지않음() {
        // given
        CourseAnswer answer = mock(CourseAnswer.class);
        when(answer.getId()).thenReturn(1L);
        when(videoAnswerRepository.findByCourseAnswerIdAndIsDeleted(1L, false))
                .thenReturn(Optional.empty());

        // when
        VideoAnswer result = videoManager.getVideoAnswer(answer);

        // then
        assertThat(result).isNull();
    }

    @Test
    void deleteAnswerVideo_정상_비디오존재() {
        // given
        CourseAnswer answer = mock(CourseAnswer.class);
        when(answer.getId()).thenReturn(1L);
        VideoAnswer videoAnswer = mock(VideoAnswer.class);
        UUID videoUuid = UUID.randomUUID();
        VideoEncoding videoEncoding = mock(VideoEncoding.class);
        String directory = "some/directory";

        when(videoAnswerRepository.findByCourseAnswerIdAndIsDeleted(1L, false))
            .thenReturn(Optional.of(videoAnswer));
        when(videoAnswer.getVideoUuid()).thenReturn(videoUuid);
        when(videoEncodingRepository.findByVideoUuid(videoUuid)).thenReturn(Optional.of(videoEncoding));
        when(videoEncoding.getEncodingVideoDirectoryPath()).thenReturn(directory);

        // when
        videoManager.deleteAnswerVideo(answer);

        // then
        verify(videoAnswerRepository).delete(videoAnswer);
        verify(videoEncodingRepository).delete(videoEncoding);
        verify(aiRequester).deleteAiVideoInfo(videoUuid);
        verify(s3FileManager).deleteAllByDirectory(directory);
    }

    @Test
    void deleteAnswerVideo_정상_비디오존재하지않음() {
        // given
        CourseAnswer answer = mock(CourseAnswer.class);
        when(answer.getId()).thenReturn(1L);
        when(videoAnswerRepository.findByCourseAnswerIdAndIsDeleted(1L, false))
                .thenReturn(Optional.empty());

        // when
        videoManager.deleteAnswerVideo(answer);

        // then
        verify(videoAnswerRepository, never()).delete(any());
        verify(aiRequester, never()).deleteAiVideoInfo(any());
    }

    @Test
    void deleteAnswerVideo_에러_인코딩이_완료되지_않은_경우() {
        // given
        CourseAnswer answer = mock(CourseAnswer.class);
        when(answer.getId()).thenReturn(1L);
        VideoAnswer videoAnswer = mock(VideoAnswer.class);
        when(videoAnswerRepository.findByCourseAnswerIdAndIsDeleted(1L, false))
            .thenReturn(Optional.of(videoAnswer));
        when(videoEncodingRepository.findByVideoUuid(any()))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> videoManager.deleteAnswerVideo(answer))
            .isInstanceOf(CustomException.class)
            .extracting(e -> ((CustomException) e).getErrorCode())
            .isEqualTo(VideoErrorCode.VIDEO_NOT_FINISHED_ENCODING);
    }

    @Test
    void getVideoMapByAnswers_정상_비디오존재() {
        // given
        CourseAnswer answer1 = mock(CourseAnswer.class);
        CourseAnswer answer2 = mock(CourseAnswer.class);
        when(answer1.getId()).thenReturn(1L);
        when(answer2.getId()).thenReturn(2L);
        
        VideoAnswer video1 = mock(VideoAnswer.class);
        VideoAnswer video2 = mock(VideoAnswer.class);
        when(video1.getCourseAnswer()).thenReturn(answer1);
        when(video2.getCourseAnswer()).thenReturn(answer2);
        
        List<CourseAnswer> answers = List.of(answer1, answer2);
        when(videoAnswerRepository.findAllByCourseAnswerIds(List.of(1L, 2L)))
                .thenReturn(List.of(video1, video2));

        // when
        Map<Long, VideoAnswer> result = videoManager.getVideoMapByAnswers(answers);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(1L)).isEqualTo(video1);
        assertThat(result.get(2L)).isEqualTo(video2);
    }

    @Test
    void getVideoMapByAnswers_정상_빈리스트() {
        // given
        List<CourseAnswer> answers = List.of();

        // when
        Map<Long, VideoAnswer> result = videoManager.getVideoMapByAnswers(answers);

        // then
        assertThat(result).isEmpty();
        verify(videoAnswerRepository, never()).findAllByCourseAnswerIds(any());
    }

    @Test
    void getVideoMapByAnswers_정상_비디오없음() {
        // given
        CourseAnswer answer = mock(CourseAnswer.class);
        when(answer.getId()).thenReturn(1L);
        List<CourseAnswer> answers = List.of(answer);
        when(videoAnswerRepository.findAllByCourseAnswerIds(List.of(1L)))
                .thenReturn(List.of());

        // when
        Map<Long, VideoAnswer> result = videoManager.getVideoMapByAnswers(answers);

        // then
        assertThat(result).isEmpty();
    }
}
