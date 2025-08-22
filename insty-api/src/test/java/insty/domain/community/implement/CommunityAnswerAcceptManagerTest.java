package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.community.repository.CommunityQuestionRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
import insty.model.user.User;
import insty.model.user.UserType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CommunityAnswerAcceptManagerTest {

    @InjectMocks
    private CommunityAnswerAcceptManager service;
    @Mock
    private CommunityQuestionRepository repository;

    @Test
    void acceptAnswer_정상_아무답변도채택되지않은경우() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        CommunityAnswer answer = mock(CommunityAnswer.class);
        insty.model.user.User user = mock(insty.model.user.User.class);
        when(answer.getUser()).thenReturn(user);
        when(user.getUserType()).thenReturn(insty.model.user.UserType.CREATOR);
        when(question.getAcceptedAnswer()).thenReturn(null);
        when(answer.getId()).thenReturn(1L);
        when(question.getId()).thenReturn(1L);
        when(answer.getCommunityQuestion()).thenReturn(question);

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
        CommunityAnswer answer = mock(CommunityAnswer.class);
        User user = mock(User.class);
        when(answer.getUser()).thenReturn(user);
        when(user.getUserType()).thenReturn(UserType.CREATOR);
        CommunityQuestion question = mock(CommunityQuestion.class);
        when(question.getAcceptedAnswer()).thenReturn(answer);
        when(answer.getId()).thenReturn(1L);
        when(question.getId()).thenReturn(1L);
        when(answer.getCommunityQuestion()).thenReturn(question);

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
        CommunityAnswer currentAccepted = mock(CommunityAnswer.class);

        CommunityAnswer anotherAnswer = mock(CommunityAnswer.class);
        User user2 = mock(User.class);
        when(anotherAnswer.getUser()).thenReturn(user2);
        when(user2.getUserType()).thenReturn(UserType.CREATOR);

        CommunityQuestion question = mock(CommunityQuestion.class);
        when(question.getAcceptedAnswer()).thenReturn(currentAccepted);
        when(currentAccepted.getId()).thenReturn(1L);
        when(anotherAnswer.getId()).thenReturn(2L);
        when(question.getId()).thenReturn(1L);
        when(anotherAnswer.getCommunityQuestion()).thenReturn(question);

        // when & then
        assertThatThrownBy(() -> service.acceptAnswer(question, anotherAnswer))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_ALREADY_ACCEPTED_ANSWER);
        verify(question, never()).acceptAnswer(any());
        verify(question, never()).unacceptAnswer();
        verify(repository, never()).save(any());
    }

    @Test
    void acceptAnswer_에러_답변작성자유형이CREATOR가아닌경우() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        CommunityAnswer answer = mock(CommunityAnswer.class);
        insty.model.user.User user = mock(insty.model.user.User.class);
        when(answer.getUser()).thenReturn(user);
        when(user.getUserType()).thenReturn(insty.model.user.UserType.LEARNER);

        // when & then
        assertThatThrownBy(() -> service.acceptAnswer(question, answer))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_ANSWER_USER_TYPE_INVALID);
        verify(question, never()).acceptAnswer(any());
        verify(question, never()).unacceptAnswer();
        verify(repository, never()).save(any());
    }

    @Test
    void acceptAnswer_에러_답변작성자가null인경우() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        CommunityAnswer answer = mock(CommunityAnswer.class);
        when(answer.getUser()).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> service.acceptAnswer(question, answer))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_ANSWER_USER_TYPE_INVALID);
        verify(question, never()).acceptAnswer(any());
        verify(question, never()).unacceptAnswer();
        verify(repository, never()).save(any());
    }

    @Test
    void acceptAnswer_에러_답변작성자의UserType이null인경우() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        CommunityAnswer answer = mock(CommunityAnswer.class);
        User user = mock(User.class);
        when(answer.getUser()).thenReturn(user);
        when(user.getUserType()).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> service.acceptAnswer(question, answer))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_ANSWER_USER_TYPE_INVALID);
        verify(question, never()).acceptAnswer(any());
        verify(question, never()).unacceptAnswer();
        verify(repository, never()).save(any());
    }

    @Test
    void acceptAnswer_에러_답변이해당질문에속하지않음() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        CommunityAnswer answer = mock(CommunityAnswer.class);
        User user = mock(User.class);
        CommunityQuestion differentQuestion = mock(CommunityQuestion.class);
        
        when(answer.getUser()).thenReturn(user);
        when(user.getUserType()).thenReturn(UserType.CREATOR);
        when(question.getId()).thenReturn(1L);
        when(answer.getCommunityQuestion()).thenReturn(differentQuestion);
        when(differentQuestion.getId()).thenReturn(2L);

        // when & then
        assertThatThrownBy(() -> service.acceptAnswer(question, answer))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_ANSWER_NOT_BELONG_TO_QUESTION);
        verify(question, never()).acceptAnswer(any());
        verify(question, never()).unacceptAnswer();
        verify(repository, never()).save(any());
    }

    @Test
    void acceptAnswer_에러_답변의질문이null인경우() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        CommunityAnswer answer = mock(CommunityAnswer.class);
        User user = mock(User.class);
        
        when(answer.getUser()).thenReturn(user);
        when(user.getUserType()).thenReturn(UserType.CREATOR);
        when(answer.getCommunityQuestion()).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> service.acceptAnswer(question, answer))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_ANSWER_NOT_BELONG_TO_QUESTION);
        verify(question, never()).acceptAnswer(any());
        verify(question, never()).unacceptAnswer();
        verify(repository, never()).save(any());
    }

    @Test
    void acceptAnswer_정상_동일한답변ID로채택취소() {
        // given
        CommunityAnswer answer = mock(CommunityAnswer.class);
        User user = mock(User.class);
        when(answer.getUser()).thenReturn(user);
        when(user.getUserType()).thenReturn(UserType.CREATOR);
        when(answer.getId()).thenReturn(1L);
        
        CommunityQuestion question = mock(CommunityQuestion.class);
        CommunityAnswer acceptedAnswer = mock(CommunityAnswer.class);
        when(question.getAcceptedAnswer()).thenReturn(acceptedAnswer);
        when(acceptedAnswer.getId()).thenReturn(1L);
        when(question.getId()).thenReturn(1L);
        when(answer.getCommunityQuestion()).thenReturn(question);

        // when
        var result = service.acceptAnswer(question, answer);

        // then
        verify(question).unacceptAnswer();
        verify(repository).save(question);
        assertThat(result.accepted()).isFalse();
        assertThat(result.answerId()).isEqualTo(1L);
    }
}