package insty.domain.courseqna.implement;

import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.courseqna.dto.CourseQuestionSearchFilter;
import insty.domain.courseqna.dto.CourseQuestionSearchInfo;
import insty.domain.courseqna.repository.CourseQuestionQueryRepository;
import insty.domain.courseqna.repository.CourseQuestionRepository;
import insty.error.CommunityErrorCode;
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

    /**
     * 모든 커뮤니티 질문 조회 (가급적 쓰지 말것)
     */
    public List<CourseQuestion> getAllCommunityQuestions() {
        return courseQuestionRepository.findAll();
    }

    /**
     * 특정 강좌의 모든 커뮤니티 질문 조회
     */
    public List<CourseQuestion> getAllCommunityQuestionsByCourseId(Long courseId) {
        return courseQuestionRepository.findAllByCourseId(courseId);
    }

    public Map<Long, Long> getCountByCourseIds(List<Long> courseIds) {
        return courseQuestionQueryRepository.countByCourseIds(courseIds);
    }

    /**
     * 커뮤니티 질문과 첨부파일을 포함한 결과
     * (파일 포함 & 질문 미포함)
     */
    public CourseQuestion getCommunityQuestionWithFilesById(Long questionId) {
        CourseQuestion question = courseQuestionRepository.findDetailsWithUserAttachmentsById(questionId)
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_QUESTION_NOT_FOUND));
        if (question.isDeleted()) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_QUESTION_ALREADY_DELETED);
        }
        return question;
    }

    /**
     * 커뮤니티 질문과 답변 리스트를 포함한 결과
     * (파일 미포함 & 질문 미포함 - 질문 파일은 미포함)
     */
    public CourseQuestion getCommunityQuestionWithAnswerById(Long questionId){
        CourseQuestion question = courseQuestionRepository.findDetailsWithUserAttachmentsById(questionId)
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_QUESTION_NOT_FOUND));
        if (question.isDeleted()) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_QUESTION_ALREADY_DELETED);
        }
        return question;
    }

    /**
     * 삭제된 질문을 포함한 커뮤니티 질문 상세조회
     */
    public CourseQuestion getCommunityQuestionDetailsByIdIncludingDeleted(Long questionId) {
        return courseQuestionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_QUESTION_NOT_FOUND));
    }

    /**
     * 질문의 강의 개시자 ID 조회
     */
    public Long getCreatorIdByQuestionId(Long questionId) {
        return courseQuestionRepository.findCreatorIdByQuestionId(questionId);
    }
}