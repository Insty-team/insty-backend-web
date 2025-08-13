package insty.domain.community.implement;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.community.repository.CommunityQuestionRepository;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CommunityQuestionStatusManagerTest {

    @InjectMocks
    private CommunityQuestionStatusManager statusManager;

    @Mock
    private CommunityAnswerReader communityAnswerReader;

    @Mock
    private CommunityQuestionRepository communityQuestionRepository;

    @Test
    void updateStatusAfterAnswerCreated_정상() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);

        // when
        statusManager.updateStatusAfterAnswerCreated(question);

        // then
        verify(question).changeStatusByAnswer(true);
        verify(communityQuestionRepository).save(question);
    }

    @Test
    void updateStatusAfterAnswerDeleted_정상_남은답변있음() {
        // given
        CommunityAnswer deletedAnswer = mock(CommunityAnswer.class);
        CommunityQuestion question = mock(CommunityQuestion.class);
        Long questionId = 1L;
        
        when(deletedAnswer.getCommunityQuestion()).thenReturn(question);
        when(deletedAnswer.isAccepted()).thenReturn(false); // 일반 답변
        when(question.getId()).thenReturn(questionId);
        when(communityAnswerReader.countActiveAnswersByQuestionId(questionId)).thenReturn(3);

        // when
        statusManager.updateStatusAfterAnswerDeleted(deletedAnswer);

        // then
        verify(question).changeStatusByAnswer(true); // 3 - 1 = 2 > 0, 답변 있음
        verify(communityQuestionRepository).save(question);
    }

    @Test
    void updateStatusAfterAnswerDeleted_정상_남은답변없음() {
        // given
        CommunityAnswer deletedAnswer = mock(CommunityAnswer.class);
        CommunityQuestion question = mock(CommunityQuestion.class);
        Long questionId = 1L;
        
        when(deletedAnswer.getCommunityQuestion()).thenReturn(question);
        when(deletedAnswer.isAccepted()).thenReturn(false); // 일반 답변
        when(question.getId()).thenReturn(questionId);
        when(communityAnswerReader.countActiveAnswersByQuestionId(questionId)).thenReturn(1);

        // when
        statusManager.updateStatusAfterAnswerDeleted(deletedAnswer);

        // then
        verify(question).changeStatusByAnswer(false); // 1 - 1 = 0, 답변 없음
        verify(communityQuestionRepository).save(question);
    }

    @Test
    void updateStatusAfterAnswerDeleted_정상_여러답변중하나삭제() {
        // given
        CommunityAnswer deletedAnswer = mock(CommunityAnswer.class);
        CommunityQuestion question = mock(CommunityQuestion.class);
        Long questionId = 1L;
        
        when(deletedAnswer.getCommunityQuestion()).thenReturn(question);
        when(deletedAnswer.isAccepted()).thenReturn(false); // 일반 답변
        when(question.getId()).thenReturn(questionId);
        when(communityAnswerReader.countActiveAnswersByQuestionId(questionId)).thenReturn(5);

        // when
        statusManager.updateStatusAfterAnswerDeleted(deletedAnswer);

        // then
        verify(question).changeStatusByAnswer(true); // 5 - 1 = 4 > 0, 여전히 답변 있음
        verify(communityQuestionRepository).save(question);
    }

    @Test
    void updateStatusAfterAnswerDeleted_채택된답변삭제_남은답변있음() {
        // given
        CommunityAnswer deletedAnswer = mock(CommunityAnswer.class);
        CommunityQuestion question = mock(CommunityQuestion.class);
        Long questionId = 1L;
        
        when(deletedAnswer.getCommunityQuestion()).thenReturn(question);
        when(deletedAnswer.isAccepted()).thenReturn(true); // 채택된 답변
        when(question.getId()).thenReturn(questionId);
        when(communityAnswerReader.countActiveAnswersByQuestionId(questionId)).thenReturn(3);

        // when
        statusManager.updateStatusAfterAnswerDeleted(deletedAnswer);

        // then
        verify(question).handleAcceptedAnswerDeleted(true); // 3 - 1 = 2 > 0, 답변 있음
        verify(communityQuestionRepository).save(question);
    }

    @Test
    void updateStatusAfterAnswerDeleted_채택된답변삭제_남은답변없음() {
        // given
        CommunityAnswer deletedAnswer = mock(CommunityAnswer.class);
        CommunityQuestion question = mock(CommunityQuestion.class);
        Long questionId = 1L;
        
        when(deletedAnswer.getCommunityQuestion()).thenReturn(question);
        when(deletedAnswer.isAccepted()).thenReturn(true); // 채택된 답변
        when(question.getId()).thenReturn(questionId);
        when(communityAnswerReader.countActiveAnswersByQuestionId(questionId)).thenReturn(1);

        // when
        statusManager.updateStatusAfterAnswerDeleted(deletedAnswer);

        // then
        verify(question).handleAcceptedAnswerDeleted(false); // 1 - 1 = 0, 답변 없음
        verify(communityQuestionRepository).save(question);
    }
}
