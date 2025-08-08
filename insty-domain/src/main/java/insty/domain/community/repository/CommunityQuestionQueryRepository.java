package insty.domain.community.repository;

import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.community.dto.CommunityQuestionSearchFilter;
import insty.domain.community.dto.CommunityQuestionSearchInfo;
import java.util.List;

public interface CommunityQuestionQueryRepository {
    /**
     * 검색 조건, 정렬, 페이지네이션에 따라 CommunityQuestion 목록을 조회합니다.
     */
    List<CommunityQuestionSearchInfo> searchQuestions(PaginationReq paginationReq, CommunityQuestionSearchFilter filter, String sort);

    /**
     * 검색 조건에 해당하는 전체 CommunityQuestion 개수를 반환합니다. (페이지네이션 정보 포함)
     */
    PaginationRes countSearchQuestions(PaginationReq paginationReq, CommunityQuestionSearchFilter filter);
}