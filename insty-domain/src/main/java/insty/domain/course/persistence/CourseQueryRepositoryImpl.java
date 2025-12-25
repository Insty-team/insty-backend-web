package insty.domain.course.persistence;

import static insty.model.course.QCourse.course;
import static insty.model.course.QCourseProgress.courseProgress;
import static insty.model.course.QCourseTag.courseTag;
import static insty.model.courseqna.QCourseQuestion.courseQuestion;
import static insty.model.tag.QTags.tags;
import static insty.model.user.QUser.user;
import static insty.model.video.QVideoCourse.videoCourse;

import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import insty.domain.common.dto.CreatorInfo;
import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.common.repository.QuerydslRepositorySupport;
import insty.domain.course.dto.CourseMySearchInfo;
import insty.domain.course.dto.CourseProgressSearchInfo;
import insty.domain.course.dto.CourseSearchFilter;
import insty.domain.course.dto.CourseSearchInfo;
import insty.domain.course.repository.CourseQueryRepository;
import insty.model.course.Course;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class CourseQueryRepositoryImpl extends QuerydslRepositorySupport implements CourseQueryRepository {

    protected CourseQueryRepositoryImpl() {
        super(Course.class);
    }

    @Override
    public List<CourseSearchInfo> searchCourses(PaginationReq paginationReq, CourseSearchFilter filter) {
        return select(
                Projections.constructor(
                        CourseSearchInfo.class,
                        course.id,
                        Projections.constructor(
                                CreatorInfo.class,
                                user.id,
                                user.nickname
                        ),
                        course.title,
                        course.description,
                        Expressions.nullExpression(List.class),
                        Expressions.nullExpression(String.class),
                        videoCourse.duration
                )
        )
                .from(course)
                .join(course.user, user)
                .leftJoin(videoCourse).on(videoCourse.course.id.eq(course.id))
                .where(searchCourseConditions(filter))
                .orderBy(createOrderSpecifier(null))
                .offset(paginationReq.getOffset())
                .limit(paginationReq.pageSize())
                .fetch();
    }

    @Override
    public PaginationRes countSearchCourses(PaginationReq paginationReq, CourseSearchFilter filter) {
        Long totalItems = select(course.count())
                .from(course)
                .where(searchCourseConditions(filter))
                .fetchOne();

        if (totalItems == null) {
            totalItems = 0L;
        }
        return PaginationRes.of(totalItems.intValue(), paginationReq.page(), paginationReq.pageSize());
    }

    @Override
    public Map<Long, List<String>> getCourseTags(List<Long> courseIds) {
        return queryFactory()
                .select(course.id, tags.tagName)
                .from(course)
                .leftJoin(courseTag).on(courseTag.course.id.eq(course.id))
                .leftJoin(courseTag.tags, tags)
                .where(course.id.in(courseIds))
                .transform(GroupBy.groupBy(course.id).as(
                        GroupBy.list(tags.tagName)
                ));
    }

    @Override
    public List<CourseMySearchInfo> searchMyCourses(PaginationReq paginationReq, Long userId, Boolean isShow) {
        return select(
                Projections.constructor(
                        CourseMySearchInfo.class,
                        course.id,
                        course.title,
                        course.price,
                        course.viewCount,
                        courseQuestion.count(),
                        Expressions.nullExpression(List.class),
                        Expressions.nullExpression(String.class),
                        course.isShow,
                        course.createdAt
                )
        )
                .from(course)
                .join(user).on(user.id.eq(course.user.id)
                        .and(user.id.eq(userId)))
                .leftJoin(courseQuestion).on(courseQuestion.course.id.eq(course.id))
                .where(searchIsShow(isShow))
                .groupBy(course.id)
                .orderBy(createOrderSpecifier(null))
                .offset(paginationReq.getOffset())
                .limit(paginationReq.pageSize())
                .fetch();
    }

    @Override
    public PaginationRes countSearchMyCourses(PaginationReq paginationReq, Long userId, Boolean isShow) {
        Long totalItems = select(course.count())
                .from(course)
                .join(user).on(user.id.eq(course.user.id)
                        .and(user.id.eq(userId)))
                .where(searchIsShow(isShow))
                .fetchOne();

        if (totalItems == null) {
            totalItems = 0L;
        }
        return PaginationRes.of(totalItems.intValue(), paginationReq.page(), paginationReq.pageSize());
    }

    @Override
    public Map<Long, UUID> getCourseVideoUuids(List<Long> courseIds) {
        return queryFactory()
                .select(course.id, videoCourse.videoUuid)
                .from(course)
                .join(videoCourse).on(videoCourse.course.id.eq(course.id))
                .where(course.id.in(courseIds))
                .transform(GroupBy.groupBy(course.id)
                        .as(videoCourse.videoUuid));
    }

    @Override
    public List<CourseProgressSearchInfo> searchCourseProgresses(PaginationReq pagination, Long userId) {
        return queryFactory()
                .select(Projections.constructor(
                        CourseProgressSearchInfo.class,
                        course.id,
                        course.title,
                        Expressions.nullExpression(Long.class),
                        Expressions.nullExpression(String.class),
                        courseProgress.createdAt
                ))
                .from(courseProgress)
                .join(courseProgress.course, course)
                .where(courseProgress.user.id.eq(userId))
                .orderBy(courseProgress.createdAt.desc())
                .offset(pagination.getOffset())
                .limit(pagination.pageSize())
                .fetch();
    }

    @Override
    public PaginationRes countSearchCourseProgresses(PaginationReq pagination, Long userId) {

        Long total = queryFactory()
                .select(courseProgress.count())
                .from(courseProgress)
                .where(courseProgress.user.id.eq(userId))
                .fetchOne();

        int totalItems = total != null ? Math.toIntExact(total) : 0;
        return PaginationRes.of(totalItems, pagination.page(), pagination.pageSize());
    }


    /**
     * 필터
     */
    private BooleanExpression[] searchCourseConditions(CourseSearchFilter filter) {
        return new BooleanExpression[]{
                searchFilter(filter.search()), // 검색
        };
    }

    private BooleanExpression searchIsShow(Boolean isShow) {
        return isShow != null ? course.isShow.eq(isShow) : null;
    }

    private BooleanExpression searchFilter(String search) {
        if (search == null || search.isEmpty()) {
            return null;
        }
        return course.title.contains(search);
    }
}
