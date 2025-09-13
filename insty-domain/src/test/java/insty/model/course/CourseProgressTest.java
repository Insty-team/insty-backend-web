package insty.model.course;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.CourseProgressErrorCode;
import insty.exception.CustomException;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import org.junit.jupiter.api.Test;

class CourseProgressTest {

    @Test
    void create_정상() {
        //given
        User user = UserFixtureBuilder.getUserWithId();
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        //when
        CourseProgress courseProgress = CourseProgress.create(user, course, CourseProgressStatus.COMPLETED);
        //then
        assertThat(courseProgress).isNotNull();
        assertThat(courseProgress.getStatus()).isEqualTo(CourseProgressStatus.COMPLETED);
        assertThat(courseProgress.getUser()).isEqualTo(user);
        assertThat(courseProgress.getCourse()).isEqualTo(course);
    }


    @Test
    void create_User_Null일때() {
        //given
        User user = null;
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        //when

        //then
        assertThatThrownBy(() -> CourseProgress.create(user, course,CourseProgressStatus.COMPLETED))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseProgressErrorCode.COURSE_PROGRESS_CREATE_ERROR);
    }


    @Test
    void create_Course_Null일때() {
        //given
        User user = UserFixtureBuilder.getUserWithId();
        Course course = null;
        //when

        //then
        assertThatThrownBy(() -> CourseProgress.create(user, course,CourseProgressStatus.COMPLETED))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseProgressErrorCode.COURSE_PROGRESS_CREATE_ERROR);
    }
}