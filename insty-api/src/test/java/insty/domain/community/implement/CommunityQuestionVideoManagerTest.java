package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.ai.adapter.AiRequester;
import insty.domain.video.repository.VideoEncodingRepository;
import insty.domain.video.repository.VideoQuestionRepository;
import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityQuestion;
import insty.model.video.VideoEncoding;
import insty.model.video.VideoQuestion;
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
class CommunityQuestionVideoManagerTest {

    @InjectMocks
    private CommunityQuestionVideoManager videoManager;
    @Mock
    private AiRequester aiRequester;
    @Mock
    private S3FileManager s3FileManager;
    @Mock
    private VideoEncodingRepository videoEncodingRepository;
    @Mock
    private VideoQuestionRepository videoQuestionRepository;

    @Test
    void attachVideoToQuestion_정상() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        UUID videoUuid = UUID.randomUUID();
        VideoQuestion videoQuestion = mock(VideoQuestion.class);
        when(videoQuestionRepository.findByVideoUuid(videoUuid)).thenReturn(Optional.of(videoQuestion));
        when(videoQuestionRepository.save(videoQuestion)).thenReturn(videoQuestion);

        // when
        VideoQuestion result = videoManager.attachVideoToQuestion(question, videoUuid);

        // then
        assertThat(result).isEqualTo(videoQuestion);
        verify(videoQuestion).updateCommunityQuestion(question);
        verify(videoQuestionRepository).save(videoQuestion);
    }

    @Test
    void attachVideoToQuestion_에러_비디오존재하지않음() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        UUID videoUuid = UUID.randomUUID();
        when(videoQuestionRepository.findByVideoUuid(videoUuid)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> videoManager.attachVideoToQuestion(question, videoUuid))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_NOT_FOUND);
    }

    @Test
    void updateAndGetLinkedVideo_정상_새로운비디오() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        when(question.getId()).thenReturn(1L);
        UUID videoUuid = UUID.randomUUID();
        VideoQuestion videoQuestion = mock(VideoQuestion.class);
        when(videoQuestionRepository.findByVideoUuid(videoUuid)).thenReturn(Optional.of(videoQuestion));
        when(videoQuestionRepository.save(videoQuestion)).thenReturn(videoQuestion);
        // getVideoQuestion이 null을 반환하도록 설정 (기존 비디오 없음)
        when(videoQuestionRepository.findByCommunityQuestionIdAndIsDeleted(1L, false))
                .thenReturn(Optional.empty());

        // when
        VideoQuestion result = videoManager.updateAndGetLinkedVideo(question, videoUuid);

        // then
        assertThat(result).isEqualTo(videoQuestion);
        verify(videoQuestion).updateCommunityQuestion(question);
        verify(videoQuestionRepository).save(videoQuestion);
    }

    @Test
    void updateAndGetLinkedVideo_정상_videoUuid가null() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        when(question.getId()).thenReturn(1L);
        VideoQuestion existingVideo = mock(VideoQuestion.class);
        when(videoQuestionRepository.findByCommunityQuestionIdAndIsDeleted(1L, false))
                .thenReturn(Optional.of(existingVideo));

        // when
        VideoQuestion result = videoManager.updateAndGetLinkedVideo(question, null);

        // then
        assertThat(result).isEqualTo(existingVideo);
        verify(videoQuestionRepository, never()).findByVideoUuid(any());
    }

    @Test
    void getVideoQuestion_정상_비디오존재() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        when(question.getId()).thenReturn(1L);
        VideoQuestion videoQuestion = mock(VideoQuestion.class);
        when(videoQuestionRepository.findByCommunityQuestionIdAndIsDeleted(1L, false))
                .thenReturn(Optional.of(videoQuestion));

        // when
        VideoQuestion result = videoManager.getVideoQuestion(question);

        // then
        assertThat(result).isEqualTo(videoQuestion);
    }

    @Test
    void getVideoQuestion_정상_비디오존재하지않음() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        when(question.getId()).thenReturn(1L);
        when(videoQuestionRepository.findByCommunityQuestionIdAndIsDeleted(1L, false))
                .thenReturn(Optional.empty());

        // when
        VideoQuestion result = videoManager.getVideoQuestion(question);

        // then
        assertThat(result).isNull();
    }

    @Test
    void deleteQuestionVideo_정상_비디오존재() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        when(question.getId()).thenReturn(1L);
        VideoQuestion videoQuestion = mock(VideoQuestion.class);
        UUID videoUuid = UUID.randomUUID();
        VideoEncoding videoEncoding = mock(VideoEncoding.class);
        String directory = "some/directory";

        when(videoQuestionRepository.findByCommunityQuestionIdAndIsDeleted(1L, false))
            .thenReturn(Optional.of(videoQuestion));
        when(videoQuestion.getVideoUuid()).thenReturn(videoUuid);
        when(videoEncodingRepository.findByVideoUuid(videoUuid)).thenReturn(Optional.of(videoEncoding));
        when(videoEncoding.getEncodingVideoDirectoryPath()).thenReturn(directory);

        // when
        videoManager.deleteQuestionVideo(question);

        // then
        verify(videoQuestionRepository).delete(videoQuestion);
        verify(videoEncodingRepository).delete(videoEncoding);
        verify(aiRequester).deleteAiVideoInfo(videoUuid);
        verify(s3FileManager).deleteAllByDirectory(directory);
    }

    @Test
    void deleteQuestionVideo_정상_비디오존재하지않음() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        when(question.getId()).thenReturn(1L);
        when(videoQuestionRepository.findByCommunityQuestionIdAndIsDeleted(1L, false))
                .thenReturn(Optional.empty());

        // when
        videoManager.deleteQuestionVideo(question);

        // then
        verify(videoQuestionRepository, never()).delete(any());
        verify(aiRequester, never()).deleteAiVideoInfo(any());
    }

    @Test
    void deleteQuestionVideo_에러_인코딩이_완료되지_않은_경우() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        when(question.getId()).thenReturn(1L);
        VideoQuestion videoQuestion = mock(VideoQuestion.class);
        when(videoQuestionRepository.findByCommunityQuestionIdAndIsDeleted(1L, false))
            .thenReturn(Optional.of(videoQuestion));
        when(videoEncodingRepository.findByVideoUuid(any()))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> videoManager.deleteQuestionVideo(question))
            .isInstanceOf(CustomException.class)
            .extracting(e -> ((CustomException) e).getErrorCode())
            .isEqualTo(VideoErrorCode.VIDEO_NOT_FINISHED_ENCODING);
    }
}
