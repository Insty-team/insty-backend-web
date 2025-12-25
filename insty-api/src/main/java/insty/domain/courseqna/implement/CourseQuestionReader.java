package insty.domain.courseqna.implement;

import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.courseqna.dto.CourseQuestionSearchFilter;
import insty.domain.courseqna.dto.CourseQuestionSearchInfo;
import insty.domain.courseqna.repository.CourseQuestionQueryRepository;
import insty.domain.courseqna.repository.CourseQuestionRepository;
import insty.error.CourseQnaErrorCode;
import insty.exception.CustomException;
import insty.model.courseqna.CourseQuestion;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseQuestionReader {

    private final CourseQuestionRepository courseQuestionRepository;
    private final CourseQuestionQueryRepository courseQuestionQueryRepository;

    /**
     * 필터, 검색 조건, 정렬을 기준으로 검색
     */
    public List<CourseQuestionSearchInfo> searchQuestions(PaginationReq paginationReq, CourseQuestionSearchFilter filter, String sort) {
        return courseQuestionQueryRepository.searchQuestions(paginationReq, filter, sort);
    }

    /**
     * 총 검색 개수
     */
    public PaginationRes countSearchQuestions(PaginationReq paginationReq, CourseQuestionSearchFilter filter) {
        return courseQuestionQueryRepository.countSearchQuestions(paginationReq, filter);
    }

    /**
     * 질문 ID 리스트에 해당하는 답변 개수를 조회
     */
    public Map<Long, Long> getAnswerCountsByQuestionIds(List<Long> questionIds) {
        return courseQuestionQueryRepository.getAnswerCountsByQuestionIds(questionIds);
    }

    public Map<Long, Long> getCountByCourseIds(List<Long> courseIds) {
        return courseQuestionQueryRepository.countByCourseIds(courseIds);
    }

    /**
     * 강좌 질문과 첨부파일을 포함한 결과
     * (파일 포함 & 질문 미포함)
     */
    public CourseQuestion getCourseQuestionWithFilesById(Long questionId) {
        CourseQuestion question = courseQuestionRepository.findDetailsWithUserAttachmentsById(questionId)
                .orElseThrow(() -> new CustomException(CourseQnaErrorCode.COURSE_QUESTION_NOT_FOUND));
        if (question.isDeleted()) {
            throw new CustomException(CourseQnaErrorCode.COURSE_QUESTION_ALREADY_DELETED);
        }
        return question;
    }

    /**
     * 강좌 질문과 답변 리스트를 포함한 결과
     * (파일 미포함 & 질문 미포함 - 질문 파일은 미포함)
     */
    public CourseQuestion getCourseQuestionWithAnswerById(Long questionId){
        CourseQuestion question = courseQuestionRepository.findDetailsWithUserAttachmentsById(questionId)
                .orElseThrow(() -> new CustomException(CourseQnaErrorCode.COURSE_QUESTION_NOT_FOUND));
        if (question.isDeleted()) {
            throw new CustomException(CourseQnaErrorCode.COURSE_QUESTION_ALREADY_DELETED);
        }
        return question;
    }

    /**
     * 질문의 강의 개시자 ID 조회
     */
    public Long getCreatorIdByQuestionId(Long questionId) {
        return courseQuestionRepository.findCreatorIdByQuestionId(questionId);
    }
}
