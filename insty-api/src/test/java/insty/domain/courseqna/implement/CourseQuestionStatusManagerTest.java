package insty.domain.courseqna.implement;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.courseqna.repository.CourseQuestionRepository;
import insty.model.courseqna.CourseAnswer;
import insty.model.courseqna.CourseQuestion;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CourseQuestionStatusManagerTest {

    @InjectMocks
    private CourseQuestionStatusManager statusManager;

    @Mock
    private CourseAnswerReader communityAnswerReader;

    @Mock
    private CourseQuestionRepository courseQuestionRepository;

    @Test
    void updateStatusAfterAnswerCreated_정상() {
        // given
        CourseQuestion question = mock(CourseQuestion.class);

        // when
        statusManager.updateStatusAfterAnswerCreated(question);

        // then
        verify(question).changeStatusByAnswer(true);
        verify(courseQuestionRepository).save(question);
    }

    @Test
    void updateStatusAfterAnswerDeleted_정상_남은답변있음() {
        // given
        CourseAnswer deletedAnswer = mock(CourseAnswer.class);
        CourseQuestion question = mock(CourseQuestion.class);
        Long questionId = 1L;
        
        when(deletedAnswer.getCourseQuestion()).thenReturn(question);
        when(deletedAnswer.isAccepted()).thenReturn(false); // 일반 답변
        when(question.getId()).thenReturn(questionId);
        when(communityAnswerReader.countActiveAnswersByQuestionId(questionId)).thenReturn(3);

        // when
        statusManager.updateStatusAfterAnswerDeleted(deletedAnswer);

        // then
        verify(question).changeStatusByAnswer(true); // 3 - 1 = 2 > 0, 답변 있음
        verify(courseQuestionRepository).save(question);
    }

    @Test
    void updateStatusAfterAnswerDeleted_정상_남은답변없음() {
        // given
        CourseAnswer deletedAnswer = mock(CourseAnswer.class);
        CourseQuestion question = mock(CourseQuestion.class);
        Long questionId = 1L;
        
        when(deletedAnswer.getCourseQuestion()).thenReturn(question);
        when(deletedAnswer.isAccepted()).thenReturn(false); // 일반 답변
        when(question.getId()).thenReturn(questionId);
        when(communityAnswerReader.countActiveAnswersByQuestionId(questionId)).thenReturn(1);

        // when
        statusManager.updateStatusAfterAnswerDeleted(deletedAnswer);

        // then
        verify(question).changeStatusByAnswer(false); // 1 - 1 = 0, 답변 없음
        verify(courseQuestionRepository).save(question);
    }

    @Test
    void updateStatusAfterAnswerDeleted_정상_여러답변중하나삭제() {
        // given
        CourseAnswer deletedAnswer = mock(CourseAnswer.class);
        CourseQuestion question = mock(CourseQuestion.class);
        Long questionId = 1L;
        
        when(deletedAnswer.getCourseQuestion()).thenReturn(question);
        when(deletedAnswer.isAccepted()).thenReturn(false); // 일반 답변
        when(question.getId()).thenReturn(questionId);
        when(communityAnswerReader.countActiveAnswersByQuestionId(questionId)).thenReturn(5);

        // when
        statusManager.updateStatusAfterAnswerDeleted(deletedAnswer);

        // then
        verify(question).changeStatusByAnswer(true); // 5 - 1 = 4 > 0, 여전히 답변 있음
        verify(courseQuestionRepository).save(question);
    }

    @Test
    void updateStatusAfterAnswerDeleted_채택된답변삭제_남은답변있음() {
        // given
        CourseAnswer deletedAnswer = mock(CourseAnswer.class);
        CourseQuestion question = mock(CourseQuestion.class);
        Long questionId = 1L;
        
        when(deletedAnswer.getCourseQuestion()).thenReturn(question);
        when(deletedAnswer.isAccepted()).thenReturn(true); // 채택된 답변
        when(question.getId()).thenReturn(questionId);
        when(communityAnswerReader.countActiveAnswersByQuestionId(questionId)).thenReturn(3);

        // when
        statusManager.updateStatusAfterAnswerDeleted(deletedAnswer);

        // then
        verify(question).handleAcceptedAnswerDeleted(true); // 3 - 1 = 2 > 0, 답변 있음
        verify(courseQuestionRepository).save(question);
    }

    @Test
    void updateStatusAfterAnswerDeleted_채택된답변삭제_남은답변없음() {
        // given
        CourseAnswer deletedAnswer = mock(CourseAnswer.class);
        CourseQuestion question = mock(CourseQuestion.class);
        Long questionId = 1L;
        
        when(deletedAnswer.getCourseQuestion()).thenReturn(question);
        when(deletedAnswer.isAccepted()).thenReturn(true); // 채택된 답변
        when(question.getId()).thenReturn(questionId);
        when(communityAnswerReader.countActiveAnswersByQuestionId(questionId)).thenReturn(1);

        // when
        statusManager.updateStatusAfterAnswerDeleted(deletedAnswer);

        // then
        verify(question).handleAcceptedAnswerDeleted(false); // 1 - 1 = 0, 답변 없음
        verify(courseQuestionRepository).save(question);
    }
}
