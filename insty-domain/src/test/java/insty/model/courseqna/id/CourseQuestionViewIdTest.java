package insty.model.courseqna.id;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.CourseQnaErrorCode;
import insty.exception.CustomException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
public class CourseQuestionViewIdTest {

    @Test
    void create_정상() {
        // given
        Long questionId = 1L;
        Long userId = 2L;

        // when
        CourseQuestionViewId courseQuestionViewId = CourseQuestionViewId.create(questionId, userId);

        // then
        assertThat(courseQuestionViewId).isNotNull();
        assertThat(courseQuestionViewId.getCourseQuestion()).isEqualTo(questionId);
        assertThat(courseQuestionViewId.getUserId()).isEqualTo(userId);
    }

    @Test
    void equals_hashCode_정상() {
        // given
        Long questionId = 1L;
        Long userId = 2L;

        CourseQuestionViewId courseQuestionViewId1 = CourseQuestionViewId.create(questionId, userId);
        CourseQuestionViewId courseQuestionViewId2 = CourseQuestionViewId.create(questionId, userId);

        // when, then
        assertThat(courseQuestionViewId1).isEqualTo(courseQuestionViewId2);
        assertThat(courseQuestionViewId1.hashCode()).isEqualTo(courseQuestionViewId2.hashCode());
    }

    @Test
    void equals_다른객체_false() {
        // given
        Long questionId1 = 1L;
        Long userId1 = 2L;
        Long questionId2 = 3L;
        Long userId2 = 4L;

        CourseQuestionViewId courseQuestionViewId1 = CourseQuestionViewId.create(questionId1, userId1);
        CourseQuestionViewId courseQuestionViewId2 = CourseQuestionViewId.create(questionId2, userId2);

        // when, then
        assertThat(courseQuestionViewId1).isNotEqualTo(courseQuestionViewId2);
        assertThat(courseQuestionViewId1.hashCode()).isNotEqualTo(courseQuestionViewId2.hashCode());
    }

    @Test
    void create_에러_questionId가Null_예외() {
        // given
        Long questionId = null;
        Long userId = 2L;

        // when, then
        assertThatThrownBy(() -> CourseQuestionViewId.create(questionId, userId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseQnaErrorCode.COURSE_QNA_CREATE_ERROR);
    }

    @Test
    void create_에러_userId가Null_예외() {
        // given
        Long questionId = 1L;
        Long userId = null;

        // when, then
        assertThatThrownBy(() -> CourseQuestionViewId.create(questionId, userId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseQnaErrorCode.COURSE_QNA_CREATE_ERROR);
    }
}
