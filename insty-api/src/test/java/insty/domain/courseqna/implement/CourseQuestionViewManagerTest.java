package insty.domain.courseqna.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.courseqna.repository.CourseQuestionViewRepository;
import insty.model.courseqna.CourseQuestion;
import insty.model.courseqna.CourseQuestionView;
import insty.model.user.User;
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
class CourseQuestionViewManagerTest {

    @InjectMocks
    private CourseQuestionViewManager courseQuestionViewManager;

    @Mock
    private CourseQuestionViewRepository courseQuestionViewRepository;

    @Mock
    private CourseQuestionReader courseQuestionReader;

    @Test
    void recordQuestionView_기존조회기록_업데이트() {
        // given
        Long questionId = 1L;
        Long userId = 1L;
        
        CourseQuestion question = createMockQuestion(questionId);
        CourseQuestionView existingView = createMockView();

        when(courseQuestionViewRepository.findByQuestionIdAndUserId(questionId, userId))
                .thenReturn(Optional.of(existingView));

        // when
        courseQuestionViewManager.recordQuestionView(question, userId);

        // then
        verify(existingView).updateLastViewedAt();
        verify(courseQuestionViewRepository, never()).save(any(CourseQuestionView.class));
    }

    @Test
    void recordQuestionView_신규조회기록_생성() {
        // given
        Long questionId = 1L;
        Long userId = 1L;
        
        CourseQuestion question = createMockQuestion(questionId);

        when(courseQuestionViewRepository.findByQuestionIdAndUserId(questionId, userId))
                .thenReturn(Optional.empty());
        when(courseQuestionViewRepository.save(any(CourseQuestionView.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        courseQuestionViewManager.recordQuestionView(question, userId);

        // then
        verify(courseQuestionViewRepository).save(any(CourseQuestionView.class));
    }

    @Test
    void recordQuestionViewIfAuthorOrCreator_질문작성자_조회기록생성() {
        // given
        Long questionId = 1L;
        Long questionAuthorId = 1L;
        
        CourseQuestion question = createMockQuestionWithUser(questionId, questionAuthorId);

        when(courseQuestionReader.getCommunityQuestionWithFilesById(questionId))
                .thenReturn(question);
        when(courseQuestionReader.getCreatorIdByQuestionId(questionId))
                .thenReturn(2L);
        when(courseQuestionViewRepository.findByQuestionIdAndUserId(questionId, questionAuthorId))
                .thenReturn(Optional.empty());
        when(courseQuestionViewRepository.save(any(CourseQuestionView.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        courseQuestionViewManager.recordQuestionViewIfAuthorOrCreator(questionId, questionAuthorId);

        // then
        verify(courseQuestionViewRepository).save(any(CourseQuestionView.class));
    }

    @Test
    void recordQuestionViewIfAuthorOrCreator_강의개시자_조회기록생성() {
        // given
        Long questionId = 1L;
        Long courseCreatorId = 2L;
        
        CourseQuestion question = createMockQuestionWithUser(questionId, 1L);

        when(courseQuestionReader.getCommunityQuestionWithFilesById(questionId))
                .thenReturn(question);
        when(courseQuestionReader.getCreatorIdByQuestionId(questionId))
                .thenReturn(courseCreatorId);
        when(courseQuestionViewRepository.findByQuestionIdAndUserId(questionId, courseCreatorId))
                .thenReturn(Optional.empty());
        when(courseQuestionViewRepository.save(any(CourseQuestionView.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        courseQuestionViewManager.recordQuestionViewIfAuthorOrCreator(questionId, courseCreatorId);

        // then
        verify(courseQuestionViewRepository).save(any(CourseQuestionView.class));
    }

    @Test
    void recordQuestionViewIfAuthorOrCreator_다른사용자_조회기록생성안함() {
        // given
        Long questionId = 1L;
        Long otherUserId = 999L;
        
        CourseQuestion question = createMockQuestionWithUser(questionId, 1L);

        when(courseQuestionReader.getCommunityQuestionWithFilesById(questionId))
                .thenReturn(question);
        when(courseQuestionReader.getCreatorIdByQuestionId(questionId))
                .thenReturn(2L);

        // when
        courseQuestionViewManager.recordQuestionViewIfAuthorOrCreator(questionId, otherUserId);

        // then
        verify(courseQuestionViewRepository, never()).save(any(CourseQuestionView.class));
    }

    @Test
    void recordQuestionViewIfAuthorOrCreator_작성자겸개시자_중복방지() {
        // given
        Long questionId = 1L;
        Long authorCreatorId = 1L;

        CourseQuestion question = createMockQuestionWithUser(questionId, authorCreatorId);

        when(courseQuestionReader.getCommunityQuestionWithFilesById(questionId))
                .thenReturn(question);
        when(courseQuestionReader.getCreatorIdByQuestionId(questionId))
                .thenReturn(authorCreatorId);
        when(courseQuestionViewRepository.findByQuestionIdAndUserId(questionId, authorCreatorId))
                .thenReturn(Optional.empty());
        when(courseQuestionViewRepository.save(any(CourseQuestionView.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        courseQuestionViewManager.recordQuestionViewIfAuthorOrCreator(questionId, authorCreatorId);

        // then
        verify(courseQuestionViewRepository).save(any(CourseQuestionView.class));
    }

    @Test
    void getHasNewAnswersForQuestions_정상() {
        // given
        List<Long> questionIds = List.of(1L, 2L, 3L);
        Long viewerId = 100L;

        CourseQuestionView view1 = createMockViewWithLastViewedAt(Instant.now().minusSeconds(3600));
        CourseQuestionView view2 = createMockViewWithLastViewedAt(Instant.now().minusSeconds(1800));

        // 질문1: 조회 기록 있음, 새로운 답변 있음
        when(courseQuestionViewRepository.findByQuestionIdAndUserId(1L, viewerId))
                .thenReturn(Optional.of(view1));
        when(courseQuestionViewRepository.hasNewAnswersAfter(eq(1L), eq(viewerId), any(Instant.class)))
                .thenReturn(true);

        // 질문2: 조회 기록 있음, 새로운 답변 없음
        when(courseQuestionViewRepository.findByQuestionIdAndUserId(2L, viewerId))
                .thenReturn(Optional.of(view2));
        when(courseQuestionViewRepository.hasNewAnswersAfter(eq(2L), eq(viewerId), any(Instant.class)))
                .thenReturn(false);

        // 질문3: 조회 기록 없음, 타인 답변 있음
        when(courseQuestionViewRepository.findByQuestionIdAndUserId(3L, viewerId))
                .thenReturn(Optional.empty());
        when(courseQuestionViewRepository.existsOtherUserAnswers(3L, viewerId))
                .thenReturn(true);

        // when
        Map<Long, Boolean> result = courseQuestionViewManager.getHasNewAnswersForQuestions(questionIds, viewerId);

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

        when(courseQuestionViewRepository.findByQuestionIdAndUserId(1L, viewerId))
                .thenReturn(Optional.empty());
        when(courseQuestionViewRepository.existsOtherUserAnswers(1L, viewerId))
                .thenReturn(false);

        // when
        Map<Long, Boolean> result = courseQuestionViewManager.getHasNewAnswersForQuestions(questionIds, viewerId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(1L)).isFalse();
    }

    @Test
    void hasNewAnswersAfterCreatorLastView_정상_새로운답변있음() {
        // given
        Long questionId = 1L;
        Long creatorId = 1L;
        
        CourseQuestionView view = createMockViewWithLastViewedAt(Instant.now().minusSeconds(3600));

        when(courseQuestionViewRepository.findByQuestionIdAndUserId(questionId, creatorId))
                .thenReturn(Optional.of(view));
        when(courseQuestionViewRepository.hasNewAnswersAfter(eq(questionId), eq(creatorId), any(Instant.class)))
                .thenReturn(true);

        // when
        boolean result = courseQuestionViewManager.hasNewAnswersAfterCreatorLastView(questionId, creatorId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void hasNewAnswersAfterCreatorLastView_정상_새로운답변없음() {
        // given
        Long questionId = 1L;
        Long creatorId = 1L;
        
        CourseQuestionView view = createMockViewWithLastViewedAt(Instant.now().minusSeconds(3600));

        when(courseQuestionViewRepository.findByQuestionIdAndUserId(questionId, creatorId))
                .thenReturn(Optional.of(view));
        when(courseQuestionViewRepository.hasNewAnswersAfter(eq(questionId), eq(creatorId), any(Instant.class)))
                .thenReturn(false);

        // when
        boolean result = courseQuestionViewManager.hasNewAnswersAfterCreatorLastView(questionId, creatorId);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void hasNewAnswersAfterCreatorLastView_조회기록없음_새로운답변없음() {
        // given
        Long questionId = 1L;
        Long creatorId = 1L;

        when(courseQuestionViewRepository.findByQuestionIdAndUserId(questionId, creatorId))
                .thenReturn(Optional.empty());

        // when
        boolean result = courseQuestionViewManager.hasNewAnswersAfterCreatorLastView(questionId, creatorId);

        // then
        assertThat(result).isFalse();
    }

    private CourseQuestion createMockQuestion(Long questionId) {
        CourseQuestion question = org.mockito.Mockito.mock(CourseQuestion.class);
        when(question.getId()).thenReturn(questionId);
        return question;
    }

    private CourseQuestion createMockQuestionWithUser(Long questionId, Long userId) {
        CourseQuestion question = createMockQuestion(questionId);
        User user = org.mockito.Mockito.mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(question.getUser()).thenReturn(user);
        return question;
    }

    private CourseQuestionView createMockView() {
        return org.mockito.Mockito.mock(CourseQuestionView.class);
    }

    private CourseQuestionView createMockViewWithLastViewedAt(Instant lastViewedAt) {
        CourseQuestionView view = org.mockito.Mockito.mock(CourseQuestionView.class);
        when(view.getLastViewedAt()).thenReturn(lastViewedAt);
        return view;
    }
}
