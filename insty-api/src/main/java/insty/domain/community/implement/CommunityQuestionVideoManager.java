package insty.domain.community.implement;

import insty.ai.adapter.AiRequester;
import insty.domain.common.VideoInfo;
import insty.domain.video.repository.VideoQuestionRepository;
import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityQuestion;
import insty.model.video.VideoAnswer;
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

    public VideoQuestion attachmentQuestion(CommunityQuestion question, UUID videoUuid) {
        VideoQuestion videoQuestion = videoQuestionRepository.findByVideoUuid(videoUuid)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FOUND));
        videoQuestion.updateCommunityQuestion(question);
        return videoQuestionRepository.save(videoQuestion);
    }

    /**
     * 기존 질문 영상은 가상삭제하고, 새로운 질문 영상을 질문과 연결한다
     * videoUuid가 null이면 기존에 연결된 영상을 반환한다
     */
    public VideoInfo updateAndGetLinkedVideo(CommunityQuestion question, UUID videoUuid) {
        if (videoUuid == null) {
            Optional<VideoQuestion> existingVideo = videoQuestionRepository.findByCommunityQuestionIdAndIsDeleted(question.getId(), false);
            return existingVideo.map(VideoInfo::of).orElse(null);
        }
        softDeleteQuestionVideo(question.getId());
        VideoQuestion newVideo = attachmentQuestion(question, videoUuid);
        return VideoInfo.of(newVideo);
    }

    @Transactional(readOnly = true)
    public VideoQuestion getAttachQuestionVideo(Long questionId) {
        return videoQuestionRepository.findByCommunityQuestionIdAndIsDeleted(questionId, false)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FOUND));
    }

    /**
     * 영상을 찾을 수 없는 경우 작업을 수행하지 않는다.
     * 연결된 질문이 있다면 논리적 삭제하고, AI 벡터 업데이트를 위한 API 호출을 진행한다.
     */
    public void softDeleteQuestionVideo(Long questionId) {
        Optional<VideoQuestion> videoQuestion = videoQuestionRepository.findByCommunityQuestionIdAndIsDeleted(questionId, false);
        if (videoQuestion.isEmpty()) {
            return;
        }

        videoQuestionRepository.deleteLogicallyById(videoQuestion.get().getId());
        aiRequester.deleteAiVideoInfo(videoQuestion.get().getVideoUuid());
    }

    /**
     * 질문에 비디오 추가(교체)
     */
    public VideoInfo saveQuestionVideo(CommunityQuestion question, UUID videoUuid) {
        if (videoUuid == null) {
            return null;
        }
        VideoQuestion videoQuestion = attachmentQuestion(question, videoUuid);
        return VideoInfo.of(videoQuestion);
    }

    /**
     * 질문 비디오 추출
     */
    public VideoInfo getQuestionVideoInfo(CommunityQuestion question) {
        Optional<VideoQuestion> videoQuestion = videoQuestionRepository.findByCommunityQuestionIdAndIsDeleted(question.getId(), false);
        return videoQuestion.map(VideoInfo::of).orElse(null);
    }

}
