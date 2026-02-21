package insty.domain.courseqna.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.notification.dto.event.NotificationReq;
import insty.model.course.Course;
import insty.model.courseqna.CourseAnswer;
import insty.model.courseqna.CourseQuestion;
import insty.model.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CourseNotificationManagerTest {

    @InjectMocks
    private CourseNotificationManager courseNotificationManager;

    @Mock
    private CourseAnswerReader courseAnswerReader;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private CourseQuestionViewManager courseQuestionViewManager;

    @Test
    @DisplayName("새 답변이 있고 크리에이터가 멘션되지 않으면 답변 알림을 보낸다")
    void sendNewAnswerNotification_새답변있고_크리에이터미멘션이면_알림전송() {
        // given
        Long questionId = 1L;
        Long answerId = 2L;
        Long creatorId = 13L;
        Long answerAuthorId = 21L;
        CourseQuestion question = createQuestion(questionId, creatorId);
        CourseAnswer answer = createAnswer(answerId, answerAuthorId, "일반 답변 내용");
        when(question.getTitle()).thenReturn("질문 제목");
        when(answer.getUser().getNickname()).thenReturn("답변자");
        when(courseQuestionViewManager.hasNewAnswersAfterCreatorLastView(questionId, creatorId)).thenReturn(true);

        // when
        courseNotificationManager.sendNewAnswerNotification(question, answer);

        // then
        ArgumentCaptor<NotificationReq> requestCaptor = ArgumentCaptor.forClass(NotificationReq.class);
        verify(eventPublisher).publishEvent(requestCaptor.capture());
        NotificationReq request = requestCaptor.getValue();

        assertThat(request.receiverId()).isEqualTo(creatorId);
        assertThat(request.getQuestionId()).isEqualTo(questionId);
        assertThat(request.getAnswerId()).isEqualTo(answerId);
        assertThat(request.getAnswerAuthorNickname()).isEqualTo("답변자");
    }

    @Test
    @DisplayName("크리에이터가 답변에서 멘션되면 답변 알림을 보내지 않는다")
    void sendNewAnswerNotification_크리에이터멘션이면_답변알림전송안함() {
        // given
        Long creatorId = 13L;
        CourseQuestion question = createQuestion(1L, creatorId);
        CourseAnswer answer = createAnswer(2L, 21L, "안녕하세요 @[사나운낙지304](13)");

        // when
        courseNotificationManager.sendNewAnswerNotification(question, answer);

        // then
        verify(courseQuestionViewManager, never()).hasNewAnswersAfterCreatorLastView(anyLong(), anyLong());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("골뱅이가 없는 링크 텍스트는 멘션으로 처리하지 않는다")
    void sendNewAnswerNotification_골뱅이없는링크면_알림전송() {
        // given
        Long questionId = 1L;
        Long creatorId = 13L;
        CourseQuestion question = createQuestion(questionId, creatorId);
        CourseAnswer answer = createAnswer(2L, 21L, "[사나운낙지304](13)");
        when(question.getTitle()).thenReturn("질문 제목");
        when(answer.getUser().getNickname()).thenReturn("답변자");
        when(courseQuestionViewManager.hasNewAnswersAfterCreatorLastView(questionId, creatorId)).thenReturn(true);

        // when
        courseNotificationManager.sendNewAnswerNotification(question, answer);

        // then
        verify(courseQuestionViewManager).hasNewAnswersAfterCreatorLastView(questionId, creatorId);
        verify(eventPublisher).publishEvent(any(NotificationReq.class));
    }

    @Test
    @DisplayName("답변 작성자가 크리에이터와 같으면 답변 알림을 보내지 않는다")
    void sendNewAnswerNotification_답변작성자가크리에이터면_알림전송안함() {
        // given
        Long creatorId = 13L;
        CourseQuestion question = createQuestion(1L, creatorId);
        CourseAnswer answer = createAnswer(2L, creatorId, "답변 내용");

        // when
        courseNotificationManager.sendNewAnswerNotification(question, answer);

        // then
        verify(courseQuestionViewManager, never()).hasNewAnswersAfterCreatorLastView(anyLong(), anyLong());
        verify(eventPublisher, never()).publishEvent(any());
    }

    private CourseQuestion createQuestion(Long questionId, Long creatorId) {
        CourseQuestion question = mock(CourseQuestion.class);
        Course course = mock(Course.class);
        User creator = mock(User.class);

        lenient().when(question.getId()).thenReturn(questionId);
        lenient().when(question.getCourse()).thenReturn(course);
        lenient().when(course.getUser()).thenReturn(creator);
        lenient().when(creator.getId()).thenReturn(creatorId);
        return question;
    }

    private CourseAnswer createAnswer(Long answerId, Long answerAuthorId, String content) {
        CourseAnswer answer = mock(CourseAnswer.class);
        User answerAuthor = mock(User.class);

        lenient().when(answer.getId()).thenReturn(answerId);
        lenient().when(answer.getUser()).thenReturn(answerAuthor);
        lenient().when(answer.getContent()).thenReturn(content);
        lenient().when(answerAuthor.getId()).thenReturn(answerAuthorId);
        return answer;
    }
}
