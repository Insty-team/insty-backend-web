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


    /**
     * 답변에 비디오 첨부
     */
    public VideoAnswer attachVideoToAnswer(CommunityAnswer answer, UUID videoUuid) {
        VideoAnswer videoAnswer = videoAnswerRepository.findByVideoUuid(videoUuid)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FOUND));
        videoAnswer.updateCommunityAnswer(answer);
        return videoAnswerRepository.save(videoAnswer);
    }


    /**
     * 새로운 UUID가 들어오면 기존 영상은 삭제하고 새로운 비디오를 연결한다.
     * videoUuid가 null이면 기존에 연결된 영상을 반환한다.
     */
    public VideoAnswer updateAndGetLinkedVideo(CommunityAnswer answer, UUID videoUuid) {
        if (videoUuid == null) {
            return getVideoAnswer(answer);
        }
        deleteeAnswerVideo(answer);
        return attachVideoToAnswer(answer, videoUuid);
    }

    /**
     * 답변 비디오 조회, 존재하지 않다면 null을 반환한다
     */
    public VideoAnswer getVideoAnswer(CommunityAnswer answer) {
        return videoAnswerRepository.findByCommunityAnswerIdAndIsDeleted(answer.getId(), false)
                .orElse(null);
    }

    /**
     * 비디오를 완전 삭제한다
     */
    public void deleteeAnswerVideo(CommunityAnswer answer) {
        VideoAnswer videoAnswer =  getVideoAnswer(answer);
        if (videoAnswer == null) {
            return;
        }
        videoAnswerRepository.delete(videoAnswer);
        aiRequester.deleteAiVideoInfo(videoAnswer.getVideoUuid());
    }
}
