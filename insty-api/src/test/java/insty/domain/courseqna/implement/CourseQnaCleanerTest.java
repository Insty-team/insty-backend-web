package insty.domain.courseqna.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.courseqna.repository.CourseAnswerRepository;
import insty.domain.courseqna.repository.CourseQuestionRepository;
import insty.model.courseqna.CourseAnswer;
import insty.model.courseqna.CourseQuestion;
import insty.model.courseqna.QuestionStatus;
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
    private insty.domain.courseqna.repository.CourseQuestionViewRepository courseQuestionViewRepository;
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
        ReflectionTestUtils.setField(course, "id", courseId);
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
        verify(courseAnswerRepository, times(1)).save(answer2);
        verify(courseAnswerFileWriter, times(1)).deleteAnswerFiles(answer);
        verify(courseAnswerVideoManager, times(1)).deleteAnswerVideo(answer);
        verify(courseAnswerRepository, times(1)).save(answer);
        verify(courseQuestionFileWriter, times(1)).deleteQuestionFiles(question);
        verify(courseQuestionVideoManager, times(1)).deleteQuestionVideo(question);
        verify(courseQuestionRepository, times(1)).save(question);
        assertThat(question.isDeleted()).isTrue();
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
        verify(courseAnswerRepository, times(1)).save(answer);
        verify(courseQuestionFileWriter, times(1)).deleteQuestionFiles(question);
        verify(courseQuestionVideoManager, times(1)).deleteQuestionVideo(question);
        verify(courseQuestionRepository, times(1)).save(question);
        assertThat(question.isDeleted()).isTrue();
    }

    @Test
    void deleteQuestion_채택된답변있는경우_참조해제후삭제() {
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId(7L);
        CourseQuestion question = CourseQuestion.create(course, user, "title3", "content3");
        CourseAnswer answer1 = CourseAnswer.create(question, user, "answer1");
        CourseAnswer answer2 = CourseAnswer.create(question, user, "answer2");

        ReflectionTestUtils.setField(question, "id", 300L);
        ReflectionTestUtils.setField(answer1, "id", 301L);
        ReflectionTestUtils.setField(answer2, "id", 302L);

        question.acceptAnswer(answer2);

        assertThat(question.getAcceptedAnswer()).isEqualTo(answer2);
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.ACCEPTED);
        assertThat(answer2.isAccepted()).isTrue();

        when(courseAnswerRepository.findAllByCourseQuestionIdIncludingDeleted(question.getId()))
                .thenReturn(List.of(answer1, answer2));

        courseQnaCleaner.deleteQuestion(question);

        assertThat(question.getAcceptedAnswer()).isNull();
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.ANSWERED);
        assertThat(answer2.isAccepted()).isFalse();

        verify(courseAnswerFileWriter, times(1)).deleteAnswerFiles(answer1);
        verify(courseAnswerVideoManager, times(1)).deleteAnswerVideo(answer1);
        verify(courseAnswerRepository, times(1)).save(answer1);
        verify(courseAnswerFileWriter, times(1)).deleteAnswerFiles(answer2);
        verify(courseAnswerVideoManager, times(1)).deleteAnswerVideo(answer2);
        verify(courseAnswerRepository, times(1)).save(answer2);
        verify(courseQuestionFileWriter, times(1)).deleteQuestionFiles(question);
        verify(courseQuestionVideoManager, times(1)).deleteQuestionVideo(question);
        verify(courseQuestionRepository, times(1)).save(question);
    }

    @Test
    void deleteAllByCourseId_채택된답변있는경우_참조해제후삭제() {
        Long courseId = 30L;
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        ReflectionTestUtils.setField(course, "id", courseId);
        User user = UserFixtureBuilder.getUserWithId(8L);

        CourseQuestion question1 = CourseQuestion.create(course, user, "q1", "content1");
        CourseQuestion question2 = CourseQuestion.create(course, user, "q2", "content2");

        CourseAnswer answer1 = CourseAnswer.create(question1, user, "answer1");
        CourseAnswer answer2 = CourseAnswer.create(question1, user, "answer2");
        CourseAnswer answer3 = CourseAnswer.create(question2, user, "answer3");

        ReflectionTestUtils.setField(question1, "id", 400L);
        ReflectionTestUtils.setField(question2, "id", 401L);
        ReflectionTestUtils.setField(answer1, "id", 402L);
        ReflectionTestUtils.setField(answer2, "id", 403L);
        ReflectionTestUtils.setField(answer3, "id", 404L);

        question1.acceptAnswer(answer2);
        question2.acceptAnswer(answer3);

        assertThat(question1.getAcceptedAnswer()).isEqualTo(answer2);
        assertThat(question2.getAcceptedAnswer()).isEqualTo(answer3);

        when(courseQuestionRepository.findAllByCourseId(courseId)).thenReturn(List.of(question1, question2));
        when(courseAnswerRepository.findAllByCourseQuestionIdIn(List.of(400L, 401L)))
                .thenReturn(List.of(answer1, answer2, answer3));

        courseQnaCleaner.deleteAllByCourseId(courseId);

        assertThat(question1.getAcceptedAnswer()).isNull();
        assertThat(question2.getAcceptedAnswer()).isNull();
        assertThat(answer2.isAccepted()).isFalse();
        assertThat(answer3.isAccepted()).isFalse();

        verify(courseAnswerRepository, times(3)).save(org.mockito.ArgumentMatchers.any(CourseAnswer.class));
        verify(courseQuestionRepository, times(2)).save(org.mockito.ArgumentMatchers.any(CourseQuestion.class));
    }
}
