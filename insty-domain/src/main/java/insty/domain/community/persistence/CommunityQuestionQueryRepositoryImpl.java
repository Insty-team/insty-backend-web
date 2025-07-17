package insty.domain.community.reposiotry;

import static insty.model.community.QCommunityQuestion.communityQuestion;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.common.repository.QuerydslRepositorySupport;
import insty.domain.community.dto.CommunityQuestionSearchFilter;
import insty.domain.community.repository.CommunityQuestionQueryRepository;
import insty.model.community.CommunityQuestion;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class CommunityQuestionQueryRepositoryImpl extends QuerydslRepositorySupport implements
        CommunityQuestionQueryRepository {
    public CommunityQuestionQueryRepositoryImpl() {
        super(CommunityQuestion.class);
    }

    /**
     * 검색 조건, 정렬, 페이지네이션에 따라 CommunityQuestion 목록을 조회합니다.
     */
    @Override
    public List<CommunityQuestion> searchQuestions(PaginationReq paginationReq, CommunityQuestionSearchFilter filter, String sort) {
        JPAQuery<CommunityQuestion> query = selectFrom(communityQuestion)
                .where(searchConditions(filter))
                .orderBy(createOrderSpecifier(sort))
                .offset(paginationReq.getOffset())
                .limit(paginationReq.pageSize());
        return query.fetch();
    }

    /**
     * 검색 조건에 해당하는 전체 CommunityQuestion 개수를 반환합니다. (페이지네이션 정보 포함)
     */
    @Override
    public PaginationRes countSearchQuestions(PaginationReq paginationReq, CommunityQuestionSearchFilter filter) {
        Long totalItems = select(communityQuestion.count())
                .from(communityQuestion)
                .where(searchConditions(filter))
                .fetchOne();
        if (totalItems == null) totalItems = 0L;
        return PaginationRes.of(totalItems.intValue(), paginationReq.page(), paginationReq.pageSize());
    }

    /**
     * CommunityQuestion 검색 조건을 BooleanExpression 배열로 분리
     */
    private BooleanExpression[] searchConditions(CommunityQuestionSearchFilter filter) {
        return new BooleanExpression[] {
                courseIdEq(filter.courseId()),
                isAnsweredEq(filter.isAnswered()),
                keywordContains(filter.keyword()),
                communityQuestion.isDeleted.eq(false)
        };
    }

    private BooleanExpression courseIdEq(Long courseId) {
        return courseId != null ? communityQuestion.course.id.eq(courseId) : null;
    }

    private BooleanExpression isAnsweredEq(Boolean isAnswered) {
        return isAnswered != null ? communityQuestion.isAnswered.eq(isAnswered) : null;
    }

    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        return communityQuestion.title.containsIgnoreCase(keyword)
                .or(communityQuestion.content.containsIgnoreCase(keyword));
    }
}