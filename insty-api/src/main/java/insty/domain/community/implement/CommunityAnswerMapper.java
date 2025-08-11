package insty.domain.community.implement;

import insty.domain.community.dto.CommunityAnswerRes;
import insty.model.community.CommunityAnswer;
import insty.model.video.VideoAnswer;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommunityAnswerMapper {

    private final CommunityAnswerFileReader communityAnswerFileReader;

    /**
     * 답변 리스트를 DTO 리스트로 변환한다
     */
    public List<CommunityAnswerRes> toCommunityAnswerResList(List<CommunityAnswer> answers, Map<Long, VideoAnswer> videoMap){
        return answers.stream()
                .map(answer -> CommunityAnswerRes.from(
                        answer,
                        communityAnswerFileReader.getAnswerFileInfos(answer),
                        videoMap.get(answer.getId())
                ))
                .toList();
    }

}
