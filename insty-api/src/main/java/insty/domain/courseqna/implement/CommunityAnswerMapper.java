package insty.domain.courseqna.implement;

import insty.domain.courseqna.dto.CommunityAnswerRes;
import insty.model.courseqna.CourseAnswer;
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
    public List<CommunityAnswerRes> toCommunityAnswerResList(List<CourseAnswer> answers, Map<Long, VideoAnswer> videoMap){
        return answers.stream()
                .map(answer -> CommunityAnswerRes.from(
                        answer,
                        communityAnswerFileReader.getAnswerFileInfos(answer),
                        videoMap.get(answer.getId())
                ))
                .toList();
    }

}
