package insty.domain.course.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import insty.domain.course.repository.CourseProgressRepository;
import insty.model.course.Course;
import insty.model.course.CourseFixtureBuilder;
import insty.model.course.CourseProgress;
import insty.model.course.CourseProgressStatus;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CourseProgressWriterTest {

    @InjectMocks
    private CourseProgressWriter courseProgressWriter;

    @Mock
    private CourseProgressRepository courseProgressRepository;

    @Test
    void saveCourseProgress_정상(){
        //given
        User user = UserFixtureBuilder.getUserWithId();
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        //mock
        when(courseProgressRepository.save(any(CourseProgress.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        //when
        CourseProgress courseProgress = courseProgressWriter.saveCourseProgress(user, course);
        //then
        assertThat(courseProgress).isNotNull();
        assertThat(courseProgress.getStatus()).isEqualTo(CourseProgressStatus.COMPLETED);
        assertThat(courseProgress.getUser().getId()).isEqualTo(user.getId());
        assertThat(courseProgress.getCourse().getId()).isEqualTo(course.getId());
    }
}