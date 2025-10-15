package insty.domain.community.implement;

import insty.ai.adapter.AiRequester;
import insty.domain.video.repository.VideoEncodingRepository;
import insty.domain.video.repository.VideoQuestionRepository;
import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityQuestion;
import insty.model.video.VideoAnswer;
import insty.model.video.VideoEncoding;
import insty.model.video.VideoQuestion;
import java.util.UUID;

import insty.s3.adapter.S3FileManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityQuestionVideoManager {

    private final AiRequester aiRequester;
    private final S3FileManager s3FileManager;
    private final VideoEncodingRepository videoEncodingRepository;
    private final VideoQuestionRepository videoQuestionRepository;

    /**
     * 질문에 비디오 첨부
     */
    public VideoQuestion attachVideoToQuestion(CommunityQuestion question, UUID videoUuid) {
        if(videoUuid == null){
            return null;
        }
        VideoQuestion videoQuestion = videoQuestionRepository.findByVideoUuid(videoUuid)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FOUND));
        videoQuestion.updateCommunityQuestion(question);
        return videoQuestionRepository.save(videoQuestion);
    }

    /**
     * 질문에 비디오를 업데이트하고 결과를 반환합니다.
     * 1. videoUuid = null인 경우 -> 삭제처리
     * 2. videoUuid와 기존 영상 Uuid가 동일한 경우 -> 변경 없음
     * 3. videoUuid와 기존 영상이 다르거나 추가해야하는 경우 -> 교체 및 비디오 연결
     */
    public VideoQuestion updateAndGetLinkedVideo(CommunityQuestion question, UUID videoUuid) {
        VideoQuestion currentVideo = getVideoQuestion(question);

        if (videoUuid == null) {
            if (currentVideo != null) {
                deleteQuestionVideo(question);
            }
            return null;
        }

        if (currentVideo != null && currentVideo.getVideoUuid().equals(videoUuid)) {
            return currentVideo;
        }

        if (currentVideo != null) {
            deleteQuestionVideo(question);
        }
        return attachVideoToQuestion(question, videoUuid);
    }

    /**
     * 질문 비디오 조회, 존재하지 않다면 null을 반환한다
     */
    public VideoQuestion getVideoQuestion(CommunityQuestion question) {
        return videoQuestionRepository.findByCommunityQuestionIdAndIsDeleted(question.getId(),false)
                .orElse(null);
    }

    /**
     * 비디오를 완전 삭제한다
     */
    public void deleteQuestionVideo(CommunityQuestion question){
        VideoQuestion videoQuestion = getVideoQuestion(question);
        if (videoQuestion == null) {
            return;
        }
        VideoEncoding videoEncoding = videoEncodingRepository.findByVideoUuid(videoQuestion.getVideoUuid())
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FINISHED_ENCODING));
        String directory = videoEncoding.getEncodingVideoDirectoryPath();
        videoQuestionRepository.delete(videoQuestion);
        videoEncodingRepository.delete(videoEncoding);
        aiRequester.deleteAiVideoInfo(videoQuestion.getVideoUuid());
        s3FileManager.deleteAllByDirectory(directory);
    }
}
