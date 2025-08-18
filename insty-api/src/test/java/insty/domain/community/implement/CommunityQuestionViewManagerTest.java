package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.community.repository.CommunityQuestionViewRepository;
import insty.model.community.CommunityQuestion;
import insty.model.community.CommunityQuestionView;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CommunityQuestionViewManagerTest {

    @InjectMocks
    private CommunityQuestionViewManager manager;

    @Mock
    private CommunityQuestionViewRepository communityQuestionViewRepository;

    @Test
    void recordQuestionView_기존조회기록_업데이트() {
        // given
        Long questionId = 1L;
        Long userId = 100L;
        CommunityQuestion question = mock(CommunityQuestion.class);
        CommunityQuestionView existingView = mock(CommunityQuestionView.class);
        
        when(question.getId()).thenReturn(questionId);
        when(communityQuestionViewRepository.findByQuestionIdAndUserId(questionId, userId))
                .thenReturn(Optional.of(existingView));

        // when
        manager.recordQuestionView(question, userId);

        // then
        verify(existingView).updateLastViewedAt();
        verify(communityQuestionViewRepository, never()).save(any(CommunityQuestionView.class));
    }

    @Test
    void recordQuestionView_신규조회기록_생성() {
        // given
        Long questionId = 1L;
        Long userId = 100L;
        CommunityQuestion question = mock(CommunityQuestion.class);
        
        when(question.getId()).thenReturn(questionId);
        when(communityQuestionViewRepository.findByQuestionIdAndUserId(questionId, userId))
                .thenReturn(Optional.empty());
        when(communityQuestionViewRepository.save(any(CommunityQuestionView.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        manager.recordQuestionView(question, userId);

        // then
        verify(communityQuestionViewRepository).save(any(CommunityQuestionView.class));
    }

    @Test
    void getHasNewAnswersForQuestions_정상() {
        // given
        List<Long> questionIds = List.of(1L, 2L, 3L);
        Long viewerId = 100L;
        
        CommunityQuestionView view1 = mock(CommunityQuestionView.class);
        when(view1.getLastViewedAt()).thenReturn(Instant.now().minusSeconds(3600));
        
        // 질문1: 조회 기록 있음, 새로운 답변 있음
        when(communityQuestionViewRepository.findByQuestionIdAndUserId(1L, viewerId))
                .thenReturn(Optional.of(view1));
        when(communityQuestionViewRepository.hasNewAnswersAfter(eq(1L), eq(viewerId), any(Instant.class)))
                .thenReturn(true);
        
        // 질문2: 조회 기록 있음, 새로운 답변 없음
        CommunityQuestionView view2 = mock(CommunityQuestionView.class);
        when(view2.getLastViewedAt()).thenReturn(Instant.now().minusSeconds(1800));
        when(communityQuestionViewRepository.findByQuestionIdAndUserId(2L, viewerId))
                .thenReturn(Optional.of(view2));
        when(communityQuestionViewRepository.hasNewAnswersAfter(eq(2L), eq(viewerId), any(Instant.class)))
                .thenReturn(false);
        
        // 질문3: 조회 기록 없음, 타인 답변 있음
        when(communityQuestionViewRepository.findByQuestionIdAndUserId(3L, viewerId))
                .thenReturn(Optional.empty());
        when(communityQuestionViewRepository.existsOtherUserAnswers(3L, viewerId))
                .thenReturn(true);

        // when
        Map<Long, Boolean> result = manager.getHasNewAnswersForQuestions(questionIds, viewerId);

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(1L)).isTrue();   // 조회 후 새로운 답변 있음
        assertThat(result.get(2L)).isFalse();  // 조회 후 새로운 답변 없음
        assertThat(result.get(3L)).isTrue();   // 조회 기록 없음, 타인 답변 있음
    }

    @Test
    void getHasNewAnswersForQuestions_조회기록없음_타인답변없음() {
        // given
        List<Long> questionIds = List.of(1L);
        Long viewerId = 100L;
        
        when(communityQuestionViewRepository.findByQuestionIdAndUserId(1L, viewerId))
                .thenReturn(Optional.empty());
        when(communityQuestionViewRepository.existsOtherUserAnswers(1L, viewerId))
                .thenReturn(false);

        // when
        Map<Long, Boolean> result = manager.getHasNewAnswersForQuestions(questionIds, viewerId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(1L)).isFalse();  // 조회 기록 없음, 타인 답변도 없음
        verify(communityQuestionViewRepository, never()).hasNewAnswersAfter(any(), any(), any());
    }

    @Test
    void getHasNewAnswersForQuestions_빈목록() {
        // given
        List<Long> questionIds = List.of();
        Long viewerId = 100L;

        // when
        Map<Long, Boolean> result = manager.getHasNewAnswersForQuestions(questionIds, viewerId);

        // then
        assertThat(result).isEmpty();
        verify(communityQuestionViewRepository, never()).findByQuestionIdAndUserId(any(), any());
    }

    @Test
    void hasNewAnswers_조회기록있음_새답변있음() {
        // given
        Long questionId = 1L;
        Long viewerId = 100L;
        CommunityQuestionView view = mock(CommunityQuestionView.class);
        Instant lastViewedAt = Instant.now().minusSeconds(3600);
        
        when(view.getLastViewedAt()).thenReturn(lastViewedAt);
        when(communityQuestionViewRepository.findByQuestionIdAndUserId(questionId, viewerId))
                .thenReturn(Optional.of(view));
        when(communityQuestionViewRepository.hasNewAnswersAfter(questionId, viewerId, lastViewedAt))
                .thenReturn(true);

        // when
        Map<Long, Boolean> result = manager.getHasNewAnswersForQuestions(List.of(questionId), viewerId);

        // then
        assertThat(result.get(questionId)).isTrue();
        verify(communityQuestionViewRepository).hasNewAnswersAfter(questionId, viewerId, lastViewedAt);
        verify(communityQuestionViewRepository, never()).existsOtherUserAnswers(any(), any());
    }

    @Test
    void hasNewAnswers_조회기록없음_타인답변있음() {
        // given
        Long questionId = 1L;
        Long viewerId = 100L;
        
        when(communityQuestionViewRepository.findByQuestionIdAndUserId(questionId, viewerId))
                .thenReturn(Optional.empty());
        when(communityQuestionViewRepository.existsOtherUserAnswers(questionId, viewerId))
                .thenReturn(true);

        // when
        Map<Long, Boolean> result = manager.getHasNewAnswersForQuestions(List.of(questionId), viewerId);

        // then
        assertThat(result.get(questionId)).isTrue();
        verify(communityQuestionViewRepository).existsOtherUserAnswers(questionId, viewerId);
        verify(communityQuestionViewRepository, never()).hasNewAnswersAfter(any(), any(), any());
    }
}
