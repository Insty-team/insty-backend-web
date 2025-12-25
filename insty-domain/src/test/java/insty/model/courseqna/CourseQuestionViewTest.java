package insty.model.courseqna;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import java.time.Instant;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class CourseQuestionViewTest {

    @Test
    void create_정상() {
        // given
        CourseQuestion courseQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        Long userId = 100L;

        // when
        CourseQuestionView view = CourseQuestionView.create(courseQuestion, userId);

        // then
        assertThat(view).isNotNull();
        assertThat(view.getCourseQuestionViewId()).isNotNull();
        assertThat(view.getCourseQuestionViewId().getCourseQuestion()).isEqualTo(courseQuestion.getId());
        assertThat(view.getCourseQuestionViewId().getUserId()).isEqualTo(userId);
        assertThat(view.getCourseQuestion()).isEqualTo(courseQuestion);
        assertThat(view.getLastViewedAt()).isNotNull();
        assertThat(view.getLastViewedAt()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    void create_에러_courseQuestion이_null이다() {
        // given
        CourseQuestion courseQuestion = null;
        Long userId = 100L;

        // when & then
        assertThatThrownBy(() -> CourseQuestionView.create(courseQuestion, userId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COURSE_CREATE_ERROR);
    }

    @Test
    void create_에러_userId가_null이다() {
        // given
        CourseQuestion courseQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        Long userId = null;

        // when & then
        assertThatThrownBy(() -> CourseQuestionView.create(courseQuestion, userId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COURSE_CREATE_ERROR);
    }

    @Test
    void updateLastViewedAt_정상() throws InterruptedException {
        // given
        CourseQuestion courseQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        Long userId = 100L;
        
        CourseQuestionView view = CourseQuestionView.create(courseQuestion, userId);
        Instant originalTime = view.getLastViewedAt();
        
        Thread.sleep(100); // 시간 차이를 위해 잠시 대기

        // when
        view.updateLastViewedAt();

        // then
        assertThat(view.getLastViewedAt()).isAfter(originalTime);
        assertThat(view.getLastViewedAt()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    void create_복합키_검증() {
        // given
        CourseQuestion courseQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        Long userId = 200L;

        // when
        CourseQuestionView view = CourseQuestionView.create(courseQuestion, userId);

        // then
        assertThat(view.getCourseQuestionViewId()).isNotNull();
        assertThat(view.getCourseQuestionViewId().getCourseQuestion()).isEqualTo(courseQuestion.getId());
        assertThat(view.getCourseQuestionViewId().getUserId()).isEqualTo(userId);
        
        // 복합키의 구성 요소들이 올바르게 설정되었는지 검증
        assertThat(view.getCourseQuestion().getId()).isEqualTo(courseQuestion.getId());
        assertThat(view.getCourseQuestionViewId().getUserId()).isEqualTo(userId);
    }

    @Test
    void create_엔티티_관계_검증() {
        // given
        CourseQuestion courseQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        Long userId = 300L;

        // when
        CourseQuestionView view = CourseQuestionView.create(courseQuestion, userId);

        // then
        assertThat(view.getCourseQuestion()).isNotNull();
        assertThat(view.getCourseQuestion()).isEqualTo(courseQuestion);
        assertThat(view.getCourseQuestion().getId()).isEqualTo(courseQuestion.getId());
        assertThat(view.getCourseQuestion().getTitle()).isEqualTo(courseQuestion.getTitle());
        assertThat(view.getCourseQuestion().getContent()).isEqualTo(courseQuestion.getContent());
    }
}
