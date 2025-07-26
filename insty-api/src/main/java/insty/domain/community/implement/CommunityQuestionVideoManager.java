package insty.domain.community.implement;

import insty.ai.adapter.AiRequester;
import insty.domain.common.VideoInfo;
import insty.domain.video.repository.VideoQuestionRepository;
import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityQuestion;
import insty.model.video.VideoQuestion;
import java.util.ArrayList;
import java.util.List;
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
     * 질문 비디오 추가(교체) - 여러 개 비디오 지원
     */
    public List<VideoInfo> saveQuestionVideos(CommunityQuestion question, List<UUID> videoUuids) {
        if (videoUuids == null || videoUuids.isEmpty()) {
            return new ArrayList<>();
        }

        List<VideoInfo> videoInfos = new ArrayList<>();
        for (UUID videoUuid : videoUuids) {
            VideoQuestion videoQuestion = attachmentQuestion(question, videoUuid);
            videoInfos.add(VideoInfo.of(videoQuestion));
        }
        return videoInfos;
    }

    /**
     * 질문 비디오 업데이트 - 1:N 매핑을 위한 메서드
     * 삭제할 비디오들을 먼저 삭제하고, 추가할 비디오들을 연결한다.
     *
     * @param question
     * @param addVideoUuids 추가할 비디오 UUID 목록
     * @param deleteVideoUuids 삭제할 비디오 UUID 목록
     */
    public List<VideoInfo> updateQuestionVideos(CommunityQuestion question, List<UUID> addVideoUuids, List<UUID> deleteVideoUuids) {
        // 1. 삭제할 비디오들을 먼저 삭제
        if (deleteVideoUuids != null && !deleteVideoUuids.isEmpty()) {
            for (UUID deleteVideoUuid : deleteVideoUuids) {
                deleteQuestionVideoByUuid(deleteVideoUuid);
            }
        }

        // 2. 추가할 비디오들을 연결
        if (addVideoUuids != null && !addVideoUuids.isEmpty()) {
            for (UUID addVideoUuid : addVideoUuids) {
                attachmentQuestion(question, addVideoUuid);
            }
        }

        // 3. 최종 비디오 목록 반환 (삭제된 것들은 제외되고, 새로 추가된 것들과 기존 것들이 모두 포함)
        return getQuestionVideoInfos(question);
    }

    /**
     * 특정 UUID의 질문 비디오를 삭제
     */
    public void deleteQuestionVideoByUuid(UUID videoUuid) {
        Optional<VideoQuestion> videoQuestion = videoQuestionRepository.findByVideoUuid(videoUuid);
        if (videoQuestion.isEmpty()) {
            return;
        }

        videoQuestionRepository.deleteLogicallyById(videoQuestion.get().getId());
        aiRequester.deleteAiVideoInfo(videoUuid);
    }

    @Transactional(readOnly = true)
    public VideoQuestion getAttachQuestionVideo(Long questionId) {
        return videoQuestionRepository.findByCommunityQuestionIdAndIsDeleted(questionId, false)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FOUND));
    }

    /**
     * 영상을 찾을 수 없는 경우 작업을 수행하지 않는다.<br> 연결된 질문이 있다면 논리적 삭제하고, AI 벡터 업데이트를 위한 API 호출을 진행한다.
     *
     * @param questionId
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
     * 질문 비디오 추출
     */
    public List<VideoInfo> getQuestionVideoInfos(CommunityQuestion question) {
        List<VideoQuestion> videoQuestions = videoQuestionRepository.findAllByCommunityQuestionIdAndIsDeleted(question.getId(), false);
        List<VideoInfo> videoInfos = new ArrayList<>();

        for (VideoQuestion videoQuestion : videoQuestions) {
            videoInfos.add(VideoInfo.of(videoQuestion));
        }

        return videoInfos;
    }
}
