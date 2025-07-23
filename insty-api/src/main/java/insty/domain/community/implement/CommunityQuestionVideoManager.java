package insty.domain.community.implement;

import insty.domain.common.VideoInfo;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
import insty.model.video.VideoAnswer;
import insty.model.video.VideoType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityQuestionVideoManager {


    /**
     * 질문 비디오 추가(교체)
     */
    public List<VideoInfo> saveQuestionVideo(CommunityQuestion question, List<UUID> videoUrl){
        if(videoUrl.isEmpty()){
            return null;
        }
        // todo : UUID 비디오가 등록된 존재인지 검증
        List<VideoInfo> videoInfos = new ArrayList<>();
        // todo : 비디오 저장
        return videoInfos;
    }

    /**
     * 질문 비디오 추출
     */
    public List<VideoInfo> getAnswerVideoInfos(CommunityQuestion question){
        
        List<VideoInfo> videoInfos = null; // = new ArrayList<>();

        // todo : question의 비디오 추출
        
        return videoInfos;
    }


}
