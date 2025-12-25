package insty.domain.courseqna.repository;

import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.courseqna.dto.CourseQuestionSearchFilter;
import insty.domain.courseqna.dto.CourseQuestionSearchInfo;
import java.util.List;
import java.util.Map;

public interface CourseQuestionQueryRepository {
    /**
     * 검색 조건, 정렬, 페이지네이션에 따라 CourseQuestion 목록을 조회합니다.
     */
    List<CourseQuestionSearchInfo> searchQuestions(PaginationReq paginationReq, CourseQuestionSearchFilter filter, String sort);

    /**
     * 검색 조건에 해당하는 전체 CourseQuestion 개수를 반환합니다. (페이지네이션 정보 포함)
     */
    PaginationRes countSearchQuestions(PaginationReq paginationReq, CourseQuestionSearchFilter filter);

    /**
     * 질문 ID 리스트에 해당하는 답변 개수를 조회합니다.
     * @param questionIds 질문 ID 리스트
     * @return Map<질문ID, 답변개수>
     */
    Map<Long, Long> getAnswerCountsByQuestionIds(List<Long> questionIds);

    Map<Long, Long> countByCourseIds(List<Long> courseIds);
}