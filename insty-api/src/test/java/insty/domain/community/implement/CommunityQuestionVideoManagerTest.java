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
    void updateAndGetLinkedVideo_새로운비디오_기존비디오없음() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        when(question.getId()).thenReturn(1L);
        UUID newVideoUuid = UUID.randomUUID();
        VideoQuestion newVideoQuestion = mock(VideoQuestion.class);
        when(videoQuestionRepository.findByCommunityQuestionIdAndIsDeleted(1L, false)).thenReturn(Optional.empty());
        when(videoQuestionRepository.findByVideoUuid(newVideoUuid)).thenReturn(Optional.of(newVideoQuestion));
        when(videoQuestionRepository.save(newVideoQuestion)).thenReturn(newVideoQuestion);

        // when
        VideoQuestion result = videoManager.updateAndGetLinkedVideo(question, newVideoUuid);

        // then
        assertThat(result).isEqualTo(newVideoQuestion);
        verify(videoQuestionRepository, never()).delete(any());
        verify(newVideoQuestion).updateCommunityQuestion(question);
    }

    @Test
    void updateAndGetLinkedVideo_새로운비디오_기존비디오있음() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        when(question.getId()).thenReturn(1L);
        UUID oldVideoUuid = UUID.randomUUID();
        UUID newVideoUuid = UUID.randomUUID();
        VideoQuestion oldVideoQuestion = mock(VideoQuestion.class);
        when(oldVideoQuestion.getVideoUuid()).thenReturn(oldVideoUuid);
        VideoQuestion newVideoQuestion = mock(VideoQuestion.class);
        VideoEncoding oldVideoEncoding = mock(VideoEncoding.class);

        when(videoQuestionRepository.findByCommunityQuestionIdAndIsDeleted(1L, false)).thenReturn(Optional.of(oldVideoQuestion));
        when(videoEncodingRepository.findByVideoUuid(oldVideoUuid)).thenReturn(Optional.of(oldVideoEncoding));
        when(videoQuestionRepository.findByVideoUuid(newVideoUuid)).thenReturn(Optional.of(newVideoQuestion));
        when(videoQuestionRepository.save(newVideoQuestion)).thenReturn(newVideoQuestion);

        // when
        VideoQuestion result = videoManager.updateAndGetLinkedVideo(question, newVideoUuid);

        // then
        assertThat(result).isEqualTo(newVideoQuestion);
        verify(videoQuestionRepository).delete(oldVideoQuestion);
        verify(videoEncodingRepository).delete(oldVideoEncoding);
        verify(aiRequester).deleteAiVideoInfo(oldVideoUuid);
        verify(s3FileManager).deleteAllByDirectory(any());
        verify(newVideoQuestion).updateCommunityQuestion(question);
    }

    @Test
    void updateAndGetLinkedVideo_동일비디오() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        when(question.getId()).thenReturn(1L);
        UUID videoUuid = UUID.randomUUID();
        VideoQuestion existingVideo = mock(VideoQuestion.class);
        when(existingVideo.getVideoUuid()).thenReturn(videoUuid);
        when(videoQuestionRepository.findByCommunityQuestionIdAndIsDeleted(1L, false)).thenReturn(Optional.of(existingVideo));

        // when
        VideoQuestion result = videoManager.updateAndGetLinkedVideo(question, videoUuid);

        // then
        assertThat(result).isEqualTo(existingVideo);
        verify(videoQuestionRepository, never()).delete(any());
        verify(videoQuestionRepository, never()).save(any());
    }

    @Test
    void updateAndGetLinkedVideo_null_기존비디오있음() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        when(question.getId()).thenReturn(1L);
        UUID oldVideoUuid = UUID.randomUUID();
        VideoQuestion oldVideoQuestion = mock(VideoQuestion.class);
        when(oldVideoQuestion.getVideoUuid()).thenReturn(oldVideoUuid);
        VideoEncoding oldVideoEncoding = mock(VideoEncoding.class);
        when(videoQuestionRepository.findByCommunityQuestionIdAndIsDeleted(1L, false)).thenReturn(Optional.of(oldVideoQuestion));
        when(videoEncodingRepository.findByVideoUuid(oldVideoUuid)).thenReturn(Optional.of(oldVideoEncoding));

        // when
        VideoQuestion result = videoManager.updateAndGetLinkedVideo(question, null);

        // then
        assertThat(result).isNull();
        verify(videoQuestionRepository).delete(oldVideoQuestion);
        verify(videoEncodingRepository).delete(oldVideoEncoding);
        verify(aiRequester).deleteAiVideoInfo(oldVideoUuid);
        verify(s3FileManager).deleteAllByDirectory(any());
    }

    @Test
    void updateAndGetLinkedVideo_null_기존비디오없음() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        when(question.getId()).thenReturn(1L);
        when(videoQuestionRepository.findByCommunityQuestionIdAndIsDeleted(1L, false)).thenReturn(Optional.empty());

        // when
        VideoQuestion result = videoManager.updateAndGetLinkedVideo(question, null);

        // then
        assertThat(result).isNull();
        verify(videoQuestionRepository, never()).delete(any());
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
