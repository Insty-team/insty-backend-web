package insty.domain.community.implement;

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
import insty.model.community.CommunityAnswer;
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
class CommunityAnswerVideoManagerTest {

    @InjectMocks
    private CommunityAnswerVideoManager videoManager;
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
        CommunityAnswer answer = mock(CommunityAnswer.class);
        UUID videoUuid = UUID.randomUUID();
        VideoAnswer videoAnswer = mock(VideoAnswer.class);
        when(videoAnswerRepository.findByVideoUuid(videoUuid)).thenReturn(Optional.of(videoAnswer));
        when(videoAnswerRepository.save(videoAnswer)).thenReturn(videoAnswer);

        // when
        VideoAnswer result = videoManager.attachVideoToAnswer(answer, videoUuid);

        // then
        assertThat(result).isEqualTo(videoAnswer);
        verify(videoAnswer).updateCommunityAnswer(answer);
        verify(videoAnswerRepository).save(videoAnswer);
    }

    @Test
    void attachVideoToAnswer_에러_비디오존재하지않음() {
        // given
        CommunityAnswer answer = mock(CommunityAnswer.class);
        UUID videoUuid = UUID.randomUUID();
        when(videoAnswerRepository.findByVideoUuid(videoUuid)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> videoManager.attachVideoToAnswer(answer, videoUuid))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_NOT_FOUND);
    }

    @Test
    void updateAndGetLinkedVideo_정상_새로운비디오() {
        // given
        CommunityAnswer answer = mock(CommunityAnswer.class);
        when(answer.getId()).thenReturn(1L);
        UUID videoUuid = UUID.randomUUID();
        VideoAnswer videoAnswer = mock(VideoAnswer.class);
        when(videoAnswerRepository.findByVideoUuid(videoUuid)).thenReturn(Optional.of(videoAnswer));
        when(videoAnswerRepository.save(videoAnswer)).thenReturn(videoAnswer);
        // getVideoAnswer가 null을 반환하도록 설정 (기존 비디오 없음)
        when(videoAnswerRepository.findByCommunityAnswerIdAndIsDeleted(1L, false))
                .thenReturn(Optional.empty());

        // when
        VideoAnswer result = videoManager.updateAndGetLinkedVideo(answer, videoUuid);

        // then
        assertThat(result).isEqualTo(videoAnswer);
        verify(videoAnswer).updateCommunityAnswer(answer);
        verify(videoAnswerRepository).save(videoAnswer);
    }

    @Test
    void updateAndGetLinkedVideo_정상_videoUuid가null() {
        // given
        CommunityAnswer answer = mock(CommunityAnswer.class);
        when(answer.getId()).thenReturn(1L);
        VideoAnswer existingVideo = mock(VideoAnswer.class);
        when(videoAnswerRepository.findByCommunityAnswerIdAndIsDeleted(1L, false))
                .thenReturn(Optional.of(existingVideo));

        // when
        VideoAnswer result = videoManager.updateAndGetLinkedVideo(answer, null);

        // then
        assertThat(result).isEqualTo(existingVideo);
        verify(videoAnswerRepository, never()).findByVideoUuid(any());
    }

    @Test
    void getVideoAnswer_정상_비디오존재() {
        // given
        CommunityAnswer answer = mock(CommunityAnswer.class);
        when(answer.getId()).thenReturn(1L);
        VideoAnswer videoAnswer = mock(VideoAnswer.class);
        when(videoAnswerRepository.findByCommunityAnswerIdAndIsDeleted(1L, false))
                .thenReturn(Optional.of(videoAnswer));

        // when
        VideoAnswer result = videoManager.getVideoAnswer(answer);

        // then
        assertThat(result).isEqualTo(videoAnswer);
    }

    @Test
    void getVideoAnswer_정상_비디오존재하지않음() {
        // given
        CommunityAnswer answer = mock(CommunityAnswer.class);
        when(answer.getId()).thenReturn(1L);
        when(videoAnswerRepository.findByCommunityAnswerIdAndIsDeleted(1L, false))
                .thenReturn(Optional.empty());

        // when
        VideoAnswer result = videoManager.getVideoAnswer(answer);

        // then
        assertThat(result).isNull();
    }

    @Test
    void deleteAnswerVideo_정상_비디오존재() {
        // given
        CommunityAnswer answer = mock(CommunityAnswer.class);
        when(answer.getId()).thenReturn(1L);
        VideoAnswer videoAnswer = mock(VideoAnswer.class);
        UUID videoUuid = UUID.randomUUID();
        VideoEncoding videoEncoding = mock(VideoEncoding.class);
        String directory = "some/directory";

        when(videoAnswerRepository.findByCommunityAnswerIdAndIsDeleted(1L, false))
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
        CommunityAnswer answer = mock(CommunityAnswer.class);
        when(answer.getId()).thenReturn(1L);
        when(videoAnswerRepository.findByCommunityAnswerIdAndIsDeleted(1L, false))
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
        CommunityAnswer answer = mock(CommunityAnswer.class);
        when(answer.getId()).thenReturn(1L);
        VideoAnswer videoAnswer = mock(VideoAnswer.class);
        when(videoAnswerRepository.findByCommunityAnswerIdAndIsDeleted(1L, false))
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
        CommunityAnswer answer1 = mock(CommunityAnswer.class);
        CommunityAnswer answer2 = mock(CommunityAnswer.class);
        when(answer1.getId()).thenReturn(1L);
        when(answer2.getId()).thenReturn(2L);
        
        VideoAnswer video1 = mock(VideoAnswer.class);
        VideoAnswer video2 = mock(VideoAnswer.class);
        when(video1.getCommunityAnswer()).thenReturn(answer1);
        when(video2.getCommunityAnswer()).thenReturn(answer2);
        
        List<CommunityAnswer> answers = List.of(answer1, answer2);
        when(videoAnswerRepository.findAllByCommunityAnswerIds(List.of(1L, 2L)))
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
        List<CommunityAnswer> answers = List.of();

        // when
        Map<Long, VideoAnswer> result = videoManager.getVideoMapByAnswers(answers);

        // then
        assertThat(result).isEmpty();
        verify(videoAnswerRepository, never()).findAllByCommunityAnswerIds(any());
    }

    @Test
    void getVideoMapByAnswers_정상_비디오없음() {
        // given
        CommunityAnswer answer = mock(CommunityAnswer.class);
        when(answer.getId()).thenReturn(1L);
        List<CommunityAnswer> answers = List.of(answer);
        when(videoAnswerRepository.findAllByCommunityAnswerIds(List.of(1L)))
                .thenReturn(List.of());

        // when
        Map<Long, VideoAnswer> result = videoManager.getVideoMapByAnswers(answers);

        // then
        assertThat(result).isEmpty();
    }
}
