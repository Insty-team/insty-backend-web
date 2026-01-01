package insty.domain.courseqna.implement;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.courseqna.repository.CourseAnswerRepository;
import insty.domain.courseqna.repository.CourseQuestionRepository;
import insty.model.courseqna.CourseAnswer;
import insty.model.courseqna.CourseQuestion;
import insty.model.course.Course;
import insty.model.course.CourseFixtureBuilder;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import java.util.List;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CourseQnaCleanerTest {

    @Mock
    private CourseQuestionRepository courseQuestionRepository;
    @Mock
    private CourseAnswerRepository courseAnswerRepository;
    @Mock
    private CourseQuestionFileWriter courseQuestionFileWriter;
    @Mock
    private CourseAnswerFileWriter courseAnswerFileWriter;
    @Mock
    private CourseQuestionVideoManager courseQuestionVideoManager;
    @Mock
    private CourseAnswerVideoManager courseAnswerVideoManager;

    @InjectMocks
    private CourseQnaCleaner courseQnaCleaner;

    @Test
    void deleteAllByCourseId_질문답변_첨부영상정리() {
        Long courseId = 20L;
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId(5L);
        CourseQuestion question = CourseQuestion.create(course, user, "title", "content");
        CourseAnswer answer = CourseAnswer.create(question, user, "answer");
        CourseAnswer answer2 = CourseAnswer.create(question, user, "answer2");
        ReflectionTestUtils.setField(question, "id", 100L);

        when(courseQuestionRepository.findAllByCourseId(courseId)).thenReturn(List.of(question));
        when(courseAnswerRepository.findAllByCourseQuestionIdIn(List.of(question.getId())))
                .thenReturn(List.of(answer, answer2));

        courseQnaCleaner.deleteAllByCourseId(courseId);

        verify(courseAnswerFileWriter, times(1)).deleteAnswerFiles(answer2);
        verify(courseAnswerVideoManager, times(1)).deleteAnswerVideo(answer2);
        verify(courseAnswerRepository, times(1)).delete(answer2);
        verify(courseAnswerFileWriter, times(1)).deleteAnswerFiles(answer);
        verify(courseAnswerVideoManager, times(1)).deleteAnswerVideo(answer);
        verify(courseAnswerRepository, times(1)).delete(answer);
        verify(courseQuestionFileWriter, times(1)).deleteQuestionFiles(question);
        verify(courseQuestionVideoManager, times(1)).deleteQuestionVideo(question);
        verify(courseQuestionRepository, times(1)).delete(question);
    }

    @Test
    void deleteQuestion_단건정리() {
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId(6L);
        CourseQuestion question = CourseQuestion.create(course, user, "title2", "content2");
        CourseAnswer answer = CourseAnswer.create(question, user, "answer2");
        ReflectionTestUtils.setField(question, "id", 200L);
        when(courseAnswerRepository.findAllByCourseQuestionIdIncludingDeleted(question.getId()))
                .thenReturn(List.of(answer));

        courseQnaCleaner.deleteQuestion(question);

        verify(courseAnswerFileWriter, times(1)).deleteAnswerFiles(answer);
        verify(courseAnswerVideoManager, times(1)).deleteAnswerVideo(answer);
        verify(courseAnswerRepository, times(1)).delete(answer);
        verify(courseQuestionFileWriter, times(1)).deleteQuestionFiles(question);
        verify(courseQuestionVideoManager, times(1)).deleteQuestionVideo(question);
        verify(courseQuestionRepository, times(1)).delete(question);
    }
}
