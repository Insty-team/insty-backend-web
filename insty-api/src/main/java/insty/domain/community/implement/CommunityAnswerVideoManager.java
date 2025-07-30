package insty.domain.community.implement;

import insty.ai.adapter.AiRequester;
import insty.domain.common.VideoInfo;
import insty.domain.video.repository.VideoAnswerRepository;
import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityAnswer;
import insty.model.video.VideoAnswer;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityAnswerVideoManager {

    private final AiRequester aiRequester;
    private final VideoAnswerRepository videoAnswerRepository;

    public VideoAnswer attachmentAnswer(CommunityAnswer answer, UUID videoUuid) {
        VideoAnswer videoAnswer = videoAnswerRepository.findByVideoUuid(videoUuid)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FOUND));
        videoAnswer.updateCommunityAnswer(answer);
        return videoAnswerRepository.save(videoAnswer);
    }

    /**
     * 기존 답변 영상은 가상삭제하고, 새로운 답변 영상을 답변과 연결한다
     * videoUuid가 null이면 기존에 연결된 영상을 반환한다
     */
    public VideoInfo updateAndGetLinkedVideo(CommunityAnswer answer, UUID videoUuid) {
        if (videoUuid == null) {
            Optional<VideoAnswer> existingVideo = videoAnswerRepository.findByCommunityAnswerIdAndIsDeleted(answer.getId(), false);
            return existingVideo.map(VideoInfo::of).orElse(null);
        }
        softDeleteAnswerVideo(answer.getId());
        VideoAnswer newVideo = attachmentAnswer(answer, videoUuid);
        return VideoInfo.of(newVideo);
    }

    @Transactional(readOnly = true)
    public VideoAnswer getAttachAnswerVideo(Long answerId) {
        return videoAnswerRepository.findByCommunityAnswerIdAndIsDeleted(answerId, false)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FOUND));
    }

    /**
     * 영상을 찾을 수 없는 경우 작업을 수행하지 않는다.
     * 연결된 답변이 있다면 논리적 삭제하고, AI 벡터 업데이트를 위한 API 호출을 진행한다.
     */
    public void softDeleteAnswerVideo(Long answerId) {
        Optional<VideoAnswer> videoAnswer = videoAnswerRepository.findByCommunityAnswerIdAndIsDeleted(answerId, false);
        if (videoAnswer.isEmpty()) {
            return;
        }

        videoAnswerRepository.deleteLogicallyById(videoAnswer.get().getId());
        aiRequester.deleteAiVideoInfo(videoAnswer.get().getVideoUuid());
    }

    /**
     * 답변에 비디오 추가(교체)
     */
    public VideoInfo saveAnswerVideo(CommunityAnswer answer, UUID videoUuid) {
        if (videoUuid == null) {
            return null;
        }
        VideoAnswer videoAnswer = attachmentAnswer(answer, videoUuid);
        return VideoInfo.of(videoAnswer);
    }

    /**
     * 답변 비디오 추출
     */
    public VideoInfo getAnswerVideoInfo(CommunityAnswer answer) {
        Optional<VideoAnswer> videoAnswer = videoAnswerRepository.findByCommunityAnswerIdAndIsDeleted(answer.getId(), false);
        return videoAnswer.map(VideoInfo::of).orElse(null);
    }

}
