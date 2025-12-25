package insty.domain.courseqna.persistence;

import static insty.model.courseqna.QCourseAnswer.courseAnswer;
import static insty.model.courseqna.QCourseQuestion.courseQuestion;
import static insty.model.user.QUser.user;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.common.dto.UserInfo;
import insty.domain.common.repository.QuerydslRepositorySupport;
import insty.domain.courseqna.dto.CourseQuestionCountDto;
import insty.domain.courseqna.dto.CourseQuestionSearchFilter;
import insty.domain.courseqna.dto.CourseQuestionSearchInfo;
import insty.domain.courseqna.repository.CourseQuestionQueryRepository;
import insty.model.courseqna.CommunityBoardType;
import insty.model.courseqna.CourseQuestion;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class CourseQuestionQueryRepositoryImpl extends QuerydslRepositorySupport implements
        CourseQuestionQueryRepository {

    public CourseQuestionQueryRepositoryImpl() {
        super(CourseQuestion.class);
    }

    @Override
    public List<CourseQuestionSearchInfo> searchQuestions(PaginationReq paginationReq, CourseQuestionSearchFilter filter, String sort) {
        return select(
                Projections.constructor(
                        CourseQuestionSearchInfo.class,
                        courseQuestion.id,
                        Projections.constructor(
                                UserInfo.class,
                                user.id,
                                user.nickname,
                                user.userType
                        ),
                        courseQuestion.course.id,
                        courseQuestion.title,
                        courseQuestion.content,
                        courseQuestion.status,
                        courseQuestion.createdAt,
                        courseQuestion.updatedAt
                )
        )
                .from(courseQuestion)
                .join(courseQuestion.user, user)
                .where(searchConditions(filter))
                .orderBy(createOrderSpecifier(sort))
                .offset(paginationReq.getOffset())
                .limit(paginationReq.pageSize())
                .fetch();
    }

    @Override
    public PaginationRes countSearchQuestions(PaginationReq paginationReq, CourseQuestionSearchFilter filter) {
        Long totalItems = select(courseQuestion.count())
                .from(courseQuestion)
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

        com.querydsl.core.types.dsl.NumberExpression<Long> countExpr = courseAnswer.id.count();
        List<com.querydsl.core.Tuple> results = select(
                Projections.tuple(
                        courseAnswer.courseQuestion.id,
                        countExpr
                )
        )
                .from(courseAnswer)
                .where(
                        courseAnswer.courseQuestion.id.in(questionIds),
                        courseAnswer.isDeleted.eq(false)
                )
                .groupBy(courseAnswer.courseQuestion.id)
                .fetch();

        // 메모리에서 Map으로 변환
        return results.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(courseAnswer.courseQuestion.id),
                        tuple -> tuple.get(countExpr)
                ));
    }


    @Override
    public Map<Long, Long> countByCourseIds(List<Long> courseIds) {
        List<CourseQuestionCountDto> result = select(
                        Projections.constructor(
                                CourseQuestionCountDto.class,
                                courseQuestion.course.id,
                                courseQuestion.count()
                        )
                )
                .from(courseQuestion)
                .where(
                        courseQuestion.course.id.in(courseIds),
                        courseQuestion.isDeleted.isFalse()
                )
                .groupBy(courseQuestion.course.id)
                .fetch();

        return result.stream()
                .collect(Collectors.toMap(
                        CourseQuestionCountDto::courseId,
                        CourseQuestionCountDto::count
                ));
    }

    private BooleanExpression[] searchConditions(CourseQuestionSearchFilter filter) {
        return new BooleanExpression[] {
                courseIdEq(filter.courseId()),
                statusesIn(filter.statuses()),
                queryContains(filter.query()),
                userIdEq(filter.userId()),
                boardTypeEq(filter.boardType()),
                courseQuestion.isDeleted.eq(false)
        };
    }

    private BooleanExpression courseIdEq(Long courseId) {
        return courseId != null ? courseQuestion.course.id.eq(courseId) : null;
    }

    private BooleanExpression statusesIn(java.util.List<insty.model.courseqna.QuestionStatus> statuses) {
        return (statuses != null && !statuses.isEmpty()) ? courseQuestion.status.in(statuses) : null;
    }

    private BooleanExpression userIdEq(Long userId) {
        return userId != null ? courseQuestion.user.id.eq(userId) : null;
    }

    private BooleanExpression boardTypeEq(CommunityBoardType boardType) {
        return boardType != null ? courseQuestion.boardType.eq(boardType) : null;
    }

    private BooleanExpression queryContains(String query) {
        if (query == null || query.isBlank()) return null;
        return courseQuestion.title.containsIgnoreCase(query)
                .or(courseQuestion.content.containsIgnoreCase(query));
    }
}