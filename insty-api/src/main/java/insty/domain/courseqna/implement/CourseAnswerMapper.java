package insty.domain.courseqna.implement;

import insty.domain.courseqna.dto.CourseAnswerRes;
import insty.model.courseqna.CourseAnswer;
import insty.model.video.VideoAnswer;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseAnswerMapper {

    private final CourseAnswerFileReader courseAnswerFileReader;

    /**
     * 답변 리스트를 DTO 리스트로 변환한다
     */
    public List<CourseAnswerRes> toCourseAnswerResList(List<CourseAnswer> answers, Map<Long, VideoAnswer> videoMap){
        return answers.stream()
                .map(answer -> CourseAnswerRes.from(
                        answer,
                        courseAnswerFileReader.getAnswerFileInfos(answer),
                        videoMap.get(answer.getId())
                ))
                .toList();
    }

}
