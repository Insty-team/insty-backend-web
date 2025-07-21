package insty.domain.community.implement;

import insty.domain.common.VideoInfo;
import insty.model.community.CommunityAnswer;
import insty.model.video.VideoType;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityAnswerVideoManager {


    /**
     * 답변에 비디오 추가(교체)
     */
    public VideoInfo saveAnswerVideo(CommunityAnswer answer, UUID uuid){
        if(uuid == null){
            return null;
        }
        // todo : UUID 비디오가 등록된 존재인지 검증
        VideoInfo videoInfo = new VideoInfo(VideoType.ANSWER, UUID.randomUUID(), "vidieo-original-name");
        // todo : 답변 비디오 업데이트
        return videoInfo;
    }

    /**
     * 답변 비디오 추출
     */
    public VideoInfo getAnswerVideoInfo(CommunityAnswer answer){

        // todo : 비디로 파일 변환
        VideoInfo videoInfo = new VideoInfo(VideoType.ANSWER, UUID.randomUUID(), "vidieo-original-name");
        // todo : 실재 로직으로 수정


        return videoInfo;
    }


}
