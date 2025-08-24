package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.community.repository.CommunityQuestionViewRepository;
import insty.model.community.CommunityQuestion;
import insty.model.community.CommunityQuestionView;
import insty.model.community.CommunityQuestionFixtureBuilder;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CommunityQuestionViewManagerTest {

    @InjectMocks
    private CommunityQuestionViewManager communityQuestionViewManager;

    @Mock
    private CommunityQuestionViewRepository communityQuestionViewRepository;

    @Mock
    private CommunityQuestionReader communityQuestionReader;

    @Test
    void recordQuestionView_기존조회기록_업데이트() {
        // given
        Long questionId = 1L;
        Long userId = 1L;
        
        CommunityQuestion question = createMockQuestion(questionId);
        CommunityQuestionView existingView = createMockView();

        when(communityQuestionViewRepository.findByQuestionIdAndUserId(questionId, userId))
                .thenReturn(Optional.of(existingView));

        // when
        communityQuestionViewManager.recordQuestionView(question, userId);

        // then
        verify(existingView).updateLastViewedAt();
        verify(communityQuestionViewRepository, never()).save(any(CommunityQuestionView.class));
    }

    @Test
    void recordQuestionView_신규조회기록_생성() {
        // given
        Long questionId = 1L;
        Long userId = 1L;
        
        CommunityQuestion question = createMockQuestion(questionId);

        when(communityQuestionViewRepository.findByQuestionIdAndUserId(questionId, userId))
                .thenReturn(Optional.empty());
        when(communityQuestionViewRepository.save(any(CommunityQuestionView.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        communityQuestionViewManager.recordQuestionView(question, userId);

        // then
        verify(communityQuestionViewRepository).save(any(CommunityQuestionView.class));
    }

    @Test
    void recordQuestionViewIfAuthorOrCreator_질문작성자_조회기록생성() {
        // given
        Long questionId = 1L;
        Long questionAuthorId = 1L;
        
        CommunityQuestion question = createMockQuestionWithUser(questionId, questionAuthorId);

        when(communityQuestionReader.getCommunityQuestionWithFilesById(questionId))
                .thenReturn(question);
        when(communityQuestionReader.getCreatorIdByQuestionId(questionId))
                .thenReturn(2L);
        when(communityQuestionViewRepository.findByQuestionIdAndUserId(questionId, questionAuthorId))
                .thenReturn(Optional.empty());
        when(communityQuestionViewRepository.save(any(CommunityQuestionView.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        communityQuestionViewManager.recordQuestionViewIfAuthorOrCreator(questionId, questionAuthorId);

        // then
        verify(communityQuestionViewRepository).save(any(CommunityQuestionView.class));
    }

    @Test
    void recordQuestionViewIfAuthorOrCreator_강의개시자_조회기록생성() {
        // given
        Long questionId = 1L;
        Long courseCreatorId = 2L;
        
        CommunityQuestion question = createMockQuestionWithUser(questionId, 1L);

        when(communityQuestionReader.getCommunityQuestionWithFilesById(questionId))
                .thenReturn(question);
        when(communityQuestionReader.getCreatorIdByQuestionId(questionId))
                .thenReturn(courseCreatorId);
        when(communityQuestionViewRepository.findByQuestionIdAndUserId(questionId, courseCreatorId))
                .thenReturn(Optional.empty());
        when(communityQuestionViewRepository.save(any(CommunityQuestionView.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        communityQuestionViewManager.recordQuestionViewIfAuthorOrCreator(questionId, courseCreatorId);

        // then
        verify(communityQuestionViewRepository).save(any(CommunityQuestionView.class));
    }

    @Test
    void recordQuestionViewIfAuthorOrCreator_다른사용자_조회기록생성안함() {
        // given
        Long questionId = 1L;
        Long otherUserId = 999L;
        
        CommunityQuestion question = createMockQuestionWithUser(questionId, 1L);

        when(communityQuestionReader.getCommunityQuestionWithFilesById(questionId))
                .thenReturn(question);
        when(communityQuestionReader.getCreatorIdByQuestionId(questionId))
                .thenReturn(2L);

        // when
        communityQuestionViewManager.recordQuestionViewIfAuthorOrCreator(questionId, otherUserId);

        // then
        verify(communityQuestionViewRepository, never()).save(any(CommunityQuestionView.class));
    }

    @Test
    void recordQuestionViewIfAuthorOrCreator_작성자겸개시자_중복방지() {
        // given
        Long questionId = 1L;
        Long authorCreatorId = 1L;

        CommunityQuestion question = createMockQuestionWithUser(questionId, authorCreatorId);

        when(communityQuestionReader.getCommunityQuestionWithFilesById(questionId))
                .thenReturn(question);
        when(communityQuestionReader.getCreatorIdByQuestionId(questionId))
                .thenReturn(authorCreatorId);
        when(communityQuestionViewRepository.findByQuestionIdAndUserId(questionId, authorCreatorId))
                .thenReturn(Optional.empty());
        when(communityQuestionViewRepository.save(any(CommunityQuestionView.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        communityQuestionViewManager.recordQuestionViewIfAuthorOrCreator(questionId, authorCreatorId);

        // then
        verify(communityQuestionViewRepository).save(any(CommunityQuestionView.class));
    }

    @Test
    void getHasNewAnswersForQuestions_정상() {
        // given
        List<Long> questionIds = List.of(1L, 2L, 3L);
        Long viewerId = 100L;

        CommunityQuestionView view1 = createMockViewWithLastViewedAt(Instant.now().minusSeconds(3600));
        CommunityQuestionView view2 = createMockViewWithLastViewedAt(Instant.now().minusSeconds(1800));

        // 질문1: 조회 기록 있음, 새로운 답변 있음
        when(communityQuestionViewRepository.findByQuestionIdAndUserId(1L, viewerId))
                .thenReturn(Optional.of(view1));
        when(communityQuestionViewRepository.hasNewAnswersAfter(eq(1L), eq(viewerId), any(Instant.class)))
                .thenReturn(true);

        // 질문2: 조회 기록 있음, 새로운 답변 없음
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
        Map<Long, Boolean> result = communityQuestionViewManager.getHasNewAnswersForQuestions(questionIds, viewerId);

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(1L)).isTrue();
        assertThat(result.get(2L)).isFalse();
        assertThat(result.get(3L)).isTrue();
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
        Map<Long, Boolean> result = communityQuestionViewManager.getHasNewAnswersForQuestions(questionIds, viewerId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(1L)).isFalse();
    }

    @Test
    void hasNewAnswersAfterCreatorLastView_정상_새로운답변있음() {
        // given
        Long questionId = 1L;
        Long creatorId = 1L;
        
        CommunityQuestionView view = createMockViewWithLastViewedAt(Instant.now().minusSeconds(3600));

        when(communityQuestionViewRepository.findByQuestionIdAndUserId(questionId, creatorId))
                .thenReturn(Optional.of(view));
        when(communityQuestionViewRepository.hasNewAnswersAfter(eq(questionId), eq(creatorId), any(Instant.class)))
                .thenReturn(true);

        // when
        boolean result = communityQuestionViewManager.hasNewAnswersAfterCreatorLastView(questionId, creatorId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void hasNewAnswersAfterCreatorLastView_정상_새로운답변없음() {
        // given
        Long questionId = 1L;
        Long creatorId = 1L;
        
        CommunityQuestionView view = createMockViewWithLastViewedAt(Instant.now().minusSeconds(3600));

        when(communityQuestionViewRepository.findByQuestionIdAndUserId(questionId, creatorId))
                .thenReturn(Optional.of(view));
        when(communityQuestionViewRepository.hasNewAnswersAfter(eq(questionId), eq(creatorId), any(Instant.class)))
                .thenReturn(false);

        // when
        boolean result = communityQuestionViewManager.hasNewAnswersAfterCreatorLastView(questionId, creatorId);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void hasNewAnswersAfterCreatorLastView_조회기록없음_새로운답변없음() {
        // given
        Long questionId = 1L;
        Long creatorId = 1L;

        when(communityQuestionViewRepository.findByQuestionIdAndUserId(questionId, creatorId))
                .thenReturn(Optional.empty());

        // when
        boolean result = communityQuestionViewManager.hasNewAnswersAfterCreatorLastView(questionId, creatorId);

        // then
        assertThat(result).isFalse();
    }

    private CommunityQuestion createMockQuestion(Long questionId) {
        CommunityQuestion question = org.mockito.Mockito.mock(CommunityQuestion.class);
        when(question.getId()).thenReturn(questionId);
        return question;
    }

    private CommunityQuestion createMockQuestionWithUser(Long questionId, Long userId) {
        CommunityQuestion question = createMockQuestion(questionId);
        User user = org.mockito.Mockito.mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(question.getUser()).thenReturn(user);
        return question;
    }

    private CommunityQuestionView createMockView() {
        return org.mockito.Mockito.mock(CommunityQuestionView.class);
    }

    private CommunityQuestionView createMockViewWithLastViewedAt(Instant lastViewedAt) {
        CommunityQuestionView view = org.mockito.Mockito.mock(CommunityQuestionView.class);
        when(view.getLastViewedAt()).thenReturn(lastViewedAt);
        return view;
    }
}
