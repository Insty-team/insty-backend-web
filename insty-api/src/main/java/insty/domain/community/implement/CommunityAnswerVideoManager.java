package insty.domain.community.implement;

import insty.ai.adapter.AiRequester;
import insty.domain.video.repository.VideoAnswerRepository;
import insty.domain.video.repository.VideoEncodingRepository;
import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityAnswer;
import insty.model.video.VideoAnswer;
import insty.model.video.VideoEncoding;
import insty.s3.adapter.S3FileManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityAnswerVideoManager {

    private final AiRequester aiRequester;
    private final S3FileManager s3FileManager;
    private final VideoEncodingRepository videoEncodingRepository;
    private final VideoAnswerRepository videoAnswerRepository;


    /**
     * 답변에 비디오 첨부
     */
    public VideoAnswer attachVideoToAnswer(CommunityAnswer answer, UUID videoUuid) {
        if(videoUuid == null){
            return null;
        }
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
        VideoAnswer currentVideo = getVideoAnswer(answer);
        if (videoUuid == null || (currentVideo != null && currentVideo.getVideoUuid().equals(videoUuid))) {
            return currentVideo;
        }
        deleteAnswerVideo(answer);
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
    public void deleteAnswerVideo(CommunityAnswer answer) {
        VideoAnswer videoAnswer =  getVideoAnswer(answer);
        if (videoAnswer == null) {
            return;
        }
        VideoEncoding videoEncoding = videoEncodingRepository.findByVideoUuid(videoAnswer.getVideoUuid())
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FINISHED_ENCODING));
        String directory = videoEncoding.getEncodingVideoDirectoryPath();
        videoAnswerRepository.delete(videoAnswer);
        videoEncodingRepository.delete(videoEncoding);
        aiRequester.deleteAiVideoInfo(videoAnswer.getVideoUuid());
        s3FileManager.deleteAllByDirectory(directory);
    }

    /**
     * 답변 목록에 대한 비디오를 배치 조회하여 answerId -> VideoAnswer 로 반환
     */
    public Map<Long, VideoAnswer> getVideoMapByAnswers(List<CommunityAnswer> answers) {
        List<Long> answerIds = answers.stream().map(CommunityAnswer::getId).toList();
        if (answerIds.isEmpty()) {
            return Map.of();
        }
        List<VideoAnswer> videos = videoAnswerRepository.findAllByCommunityAnswerIds(answerIds);
        return videos.stream().collect(Collectors.toMap(
                v -> v.getCommunityAnswer().getId(),
                v -> v,
                (a, b) -> a
        ));
    }
}
