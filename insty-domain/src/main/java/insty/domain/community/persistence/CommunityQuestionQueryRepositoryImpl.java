package insty.domain.community.persistence;

import static insty.model.community.QCommunityQuestion.communityQuestion;
import static insty.model.community.QCommunityAnswer.communityAnswer;
import static insty.model.user.QUser.user;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.common.dto.UserInfo;
import insty.domain.common.repository.QuerydslRepositorySupport;
import insty.domain.community.dto.CommunityQuestionSearchFilter;
import insty.domain.community.dto.CommunityQuestionSearchInfo;
import insty.domain.community.repository.CommunityQuestionQueryRepository;
import insty.model.community.CommunityQuestion;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class CommunityQuestionQueryRepositoryImpl extends QuerydslRepositorySupport implements CommunityQuestionQueryRepository {

    public CommunityQuestionQueryRepositoryImpl() {
        super(CommunityQuestion.class);
    }

    @Override
    public List<CommunityQuestionSearchInfo> searchQuestions(PaginationReq paginationReq, CommunityQuestionSearchFilter filter, String sort) {
        return select(
                Projections.constructor(
                        CommunityQuestionSearchInfo.class,
                        communityQuestion.id,
                        Projections.constructor(
                                UserInfo.class,
                                user.id,
                                user.nickname,
                                user.userType
                        ),
                        communityQuestion.course.id,
                        communityQuestion.title,
                        communityQuestion.content,
                        communityQuestion.status,
                        communityQuestion.createdAt,
                        communityQuestion.updatedAt
                )
        )
                .from(communityQuestion)
                .join(communityQuestion.user, user)
                .where(searchConditions(filter))
                .orderBy(createOrderSpecifier(sort))
                .offset(paginationReq.getOffset())
                .limit(paginationReq.pageSize())
                .fetch();
    }

    @Override
    public PaginationRes countSearchQuestions(PaginationReq paginationReq, CommunityQuestionSearchFilter filter) {
        Long totalItems = select(communityQuestion.count())
                .from(communityQuestion)
                .where(searchConditions(filter))
                .fetchOne();
        if (totalItems == null) totalItems = 0L;
        return PaginationRes.of(totalItems.intValue(), paginationReq.page(), paginationReq.pageSize());
    }

    @Override
    public Map<Long, Long> getAnswerCountsByQuestionIds(List<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return Map.of();
        }

        com.querydsl.core.types.dsl.NumberExpression<Long> countExpr = communityAnswer.id.count();
        List<com.querydsl.core.Tuple> results = select(
                Projections.tuple(
                        communityAnswer.communityQuestion.id,
                        countExpr
                )
        )
                .from(communityAnswer)
                .where(
                        communityAnswer.communityQuestion.id.in(questionIds),
                        communityAnswer.isDeleted.eq(false)
                )
                .groupBy(communityAnswer.communityQuestion.id)
                .fetch();

        // 메모리에서 Map으로 변환
        return results.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(communityAnswer.communityQuestion.id),
                        tuple -> tuple.get(countExpr)
                ));
    }

    private BooleanExpression[] searchConditions(CommunityQuestionSearchFilter filter) {
        return new BooleanExpression[] {
                courseIdEq(filter.courseId()),
                statusesIn(filter.statuses()),
                queryContains(filter.query()),
                userIdEq(filter.userId()),
                communityQuestion.isDeleted.eq(false)
        };
    }

    private BooleanExpression courseIdEq(Long courseId) {
        return courseId != null ? communityQuestion.course.id.eq(courseId) : null;
    }

    private BooleanExpression statusesIn(java.util.List<insty.model.community.QuestionStatus> statuses) {
        return (statuses != null && !statuses.isEmpty()) ? communityQuestion.status.in(statuses) : null;
    }

    private BooleanExpression userIdEq(Long userId) {
        return userId != null ? communityQuestion.user.id.eq(userId) : null;
    }

    private BooleanExpression queryContains(String query) {
        if (query == null || query.isBlank()) return null;
        return communityQuestion.title.containsIgnoreCase(query)
                .or(communityQuestion.content.containsIgnoreCase(query));
    }
}