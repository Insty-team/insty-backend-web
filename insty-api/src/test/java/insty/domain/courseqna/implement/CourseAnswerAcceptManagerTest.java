package insty.domain.courseqna.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.courseqna.repository.CourseQuestionRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.courseqna.CourseAnswer;
import insty.model.courseqna.CourseQuestion;
import insty.model.user.User;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CourseAnswerAcceptManagerTest {

    @InjectMocks
    private CourseAnswerAcceptManager service;
    @Mock
    private CourseQuestionRepository repository;

    @Test
    void acceptAnswer_정상_아무답변도채택되지않은경우() {
        // given
        CourseQuestion question = mock(CourseQuestion.class);
        CourseAnswer answer = mock(CourseAnswer.class);
        User questionUser = mock(User.class);
        User answerUser = mock(User.class);
        
        when(question.getUser()).thenReturn(questionUser);
        when(answer.getUser()).thenReturn(answerUser);
        when(questionUser.getId()).thenReturn(1L);
        when(answerUser.getId()).thenReturn(2L);
        when(question.getAcceptedAnswer()).thenReturn(null);
        when(answer.getId()).thenReturn(1L);
        when(question.getId()).thenReturn(1L);
        when(answer.getCourseQuestion()).thenReturn(question);

        // when
        var result = service.acceptAnswer(question, answer);

        // then
        verify(question).acceptAnswer(answer);
        verify(repository).save(question);
        assertThat(result.accepted()).isTrue();
        assertThat(result.answerId()).isEqualTo(1L);
    }

    @Test
    void acceptAnswer_정상_이미채택된답변을다시클릭_취소() {
        // given
        CourseAnswer answer = mock(CourseAnswer.class);
        CourseQuestion question = mock(CourseQuestion.class);
        User questionUser = mock(User.class);
        User answerUser = mock(User.class);
        
        when(question.getUser()).thenReturn(questionUser);
        when(answer.getUser()).thenReturn(answerUser);
        when(questionUser.getId()).thenReturn(1L);
        when(answerUser.getId()).thenReturn(2L);
        when(question.getAcceptedAnswer()).thenReturn(answer);
        when(answer.getId()).thenReturn(1L);
        when(question.getId()).thenReturn(1L);
        when(answer.getCourseQuestion()).thenReturn(question);

        // when
        var result = service.acceptAnswer(question, answer);

        // then
        verify(question).unacceptAnswer();
        verify(repository).save(question);
        assertThat(result.accepted()).isFalse();
        assertThat(result.answerId()).isEqualTo(1L);
    }

    @Test
    void acceptAnswer_에러_이미다른답변이채택되어있을때() {
        // given
        CourseAnswer currentAccepted = mock(CourseAnswer.class);
        CourseAnswer anotherAnswer = mock(CourseAnswer.class);
        CourseQuestion question = mock(CourseQuestion.class);
        User questionUser = mock(User.class);
        User answerUser = mock(User.class);
        
        when(question.getUser()).thenReturn(questionUser);
        when(anotherAnswer.getUser()).thenReturn(answerUser);
        when(questionUser.getId()).thenReturn(1L);
        when(answerUser.getId()).thenReturn(2L);
        when(question.getAcceptedAnswer()).thenReturn(currentAccepted);
        when(currentAccepted.getId()).thenReturn(1L);
        when(anotherAnswer.getId()).thenReturn(2L);
        when(question.getId()).thenReturn(1L);
        when(anotherAnswer.getCourseQuestion()).thenReturn(question);

        // when & then
        assertThatThrownBy(() -> service.acceptAnswer(question, anotherAnswer))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COURSE_ALREADY_ACCEPTED_ANSWER);
        verify(question, never()).acceptAnswer(any());
        verify(question, never()).unacceptAnswer();
        verify(repository, never()).save(any());
    }

    @Test
    void acceptAnswer_정상_질문작성자가자신의답변을채택() {
        // given
        CourseQuestion question = mock(CourseQuestion.class);
        CourseAnswer answer = mock(CourseAnswer.class);
        User sameUser = mock(User.class);
        
        when(question.getUser()).thenReturn(sameUser);
        when(answer.getUser()).thenReturn(sameUser);
        when(sameUser.getId()).thenReturn(1L);
        when(question.getAcceptedAnswer()).thenReturn(null);
        when(answer.getId()).thenReturn(1L);
        when(question.getId()).thenReturn(1L);
        when(answer.getCourseQuestion()).thenReturn(question);

        // when
        var result = service.acceptAnswer(question, answer);

        // then
        verify(question).acceptAnswer(answer);
        verify(repository).save(question);
        assertThat(result.accepted()).isTrue();
        assertThat(result.answerId()).isEqualTo(1L);
    }

    @Test
    void acceptAnswer_에러_답변작성자가null인경우() {
        // given
        CourseQuestion question = mock(CourseQuestion.class);
        CourseAnswer answer = mock(CourseAnswer.class);
        User questionUser = mock(User.class);
        
        when(question.getUser()).thenReturn(questionUser);
        when(answer.getUser()).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> service.acceptAnswer(question, answer))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COURSE_ANSWER_INVALID_USER_ID);
        verify(question, never()).acceptAnswer(any());
        verify(question, never()).unacceptAnswer();
        verify(repository, never()).save(any());
    }

    @Test
    void acceptAnswer_에러_질문작성자가null인경우() {
        // given
        CourseQuestion question = mock(CourseQuestion.class);
        CourseAnswer answer = mock(CourseAnswer.class);
        User answerUser = mock(User.class);
        
        when(question.getUser()).thenReturn(null);
        when(answer.getUser()).thenReturn(answerUser);

        // when & then
        assertThatThrownBy(() -> service.acceptAnswer(question, answer))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COURSE_ANSWER_INVALID_USER_ID);
        verify(question, never()).acceptAnswer(any());
        verify(question, never()).unacceptAnswer();
        verify(repository, never()).save(any());
    }

    @Test
    void acceptAnswer_에러_답변이해당질문에속하지않음() {
        // given
        CourseQuestion question = mock(CourseQuestion.class);
        CourseAnswer answer = mock(CourseAnswer.class);
        User questionUser = mock(User.class);
        User answerUser = mock(User.class);
        CourseQuestion differentQuestion = mock(CourseQuestion.class);
        
        when(question.getUser()).thenReturn(questionUser);
        when(answer.getUser()).thenReturn(answerUser);
        when(questionUser.getId()).thenReturn(1L);
        when(answerUser.getId()).thenReturn(2L);
        when(question.getId()).thenReturn(1L);
        when(answer.getCourseQuestion()).thenReturn(differentQuestion);
        when(differentQuestion.getId()).thenReturn(2L);

        // when & then
        assertThatThrownBy(() -> service.acceptAnswer(question, answer))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COURSE_ANSWER_NOT_BELONG_TO_QUESTION);
        verify(question, never()).acceptAnswer(any());
        verify(question, never()).unacceptAnswer();
        verify(repository, never()).save(any());
    }

    @Test
    void acceptAnswer_에러_답변의질문이null인경우() {
        // given
        CourseQuestion question = mock(CourseQuestion.class);
        CourseAnswer answer = mock(CourseAnswer.class);
        User questionUser = mock(User.class);
        User answerUser = mock(User.class);
        
        when(question.getUser()).thenReturn(questionUser);
        when(answer.getUser()).thenReturn(answerUser);
        when(questionUser.getId()).thenReturn(1L);
        when(answerUser.getId()).thenReturn(2L);
        when(answer.getCourseQuestion()).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> service.acceptAnswer(question, answer))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COURSE_ANSWER_NOT_BELONG_TO_QUESTION);
        verify(question, never()).acceptAnswer(any());
        verify(question, never()).unacceptAnswer();
        verify(repository, never()).save(any());
    }
}