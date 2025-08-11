package insty.domain.community.implement;

import insty.ai.adapter.AiRequester;
import insty.domain.video.repository.VideoQuestionRepository;
import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityQuestion;
import insty.model.video.VideoQuestion;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityQuestionVideoManager {

    private final AiRequester aiRequester;
    private final VideoQuestionRepository videoQuestionRepository;

    /**
     * 질문에 비디오 첨부
     */
    public VideoQuestion attachVideoToQuestion(CommunityQuestion question, UUID videoUuid) {
        VideoQuestion videoQuestion = videoQuestionRepository.findByVideoUuid(videoUuid)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FOUND));
        videoQuestion.updateCommunityQuestion(question);
        return videoQuestionRepository.save(videoQuestion);
    }

    /**
     * 새로운 UUID가 들어오면 기존 영상은 삭제하고 새로운 비디오를 연결한다.
     * videoUuid가 null이면 기존에 연결된 영상을 반환한다.
     */
    public VideoQuestion updateAndGetLinkedVideo(CommunityQuestion question, UUID videoUuid) {
        if (videoUuid == null) {
            return getVideoQuestion(question);
        }
        deleteeQuestionVideo(question);
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
    public void deleteeQuestionVideo(CommunityQuestion question){
        VideoQuestion videoQuestion = getVideoQuestion(question);
        if (videoQuestion == null) {
            return;
        }
        videoQuestionRepository.delete(videoQuestion);
        aiRequester.deleteAiVideoInfo(videoQuestion.getVideoUuid());
    }
}
