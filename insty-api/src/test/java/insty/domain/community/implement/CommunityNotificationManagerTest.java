package insty.domain.community.implement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.notification.event.AnswerAcceptedNotificationEvent;
import insty.domain.notification.event.NewCommunityQuestionEvent;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
import insty.model.course.Course;
import insty.model.user.User;
import java.util.Set;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CommunityNotificationManagerTest {

    @InjectMocks
    private CommunityNotificationManager notificationManager;

    @Mock
    private CommunityAnswerReader communityAnswerReader;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    void sendNewQuestionNotification_정상() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);

        // when
        notificationManager.sendNewQuestionNotification(question);

        // then
        verify(eventPublisher).publishEvent(any(NewCommunityQuestionEvent.class));
    }

    @Test
    void sendAnswerAcceptedNotification_정상_크리에이터와참여자들에게알림() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        CommunityAnswer answer = mock(CommunityAnswer.class);
        Course course = mock(Course.class);
        User creator = mock(User.class);
        User questionAuthor = mock(User.class);
        User participant1 = mock(User.class);
        User participant2 = mock(User.class);

        when(question.getId()).thenReturn(1L);
        when(question.getCourse()).thenReturn(course);
        when(question.getUser()).thenReturn(questionAuthor);
        when(course.getUser()).thenReturn(creator);
        when(creator.getId()).thenReturn(100L);
        when(questionAuthor.getId()).thenReturn(200L);
        when(participant1.getId()).thenReturn(300L);
        when(participant2.getId()).thenReturn(400L);

        Set<User> participants = Set.of(participant1, participant2);
        when(communityAnswerReader.getParticipantsByQuestionId(1L)).thenReturn(participants);

        // when
        notificationManager.sendAnswerAcceptedNotification(question, answer);

        // then
        // 크리에이터 1명 + 참여자 2명 = 총 3번의 이벤트 발행
        verify(eventPublisher, times(3)).publishEvent(any(AnswerAcceptedNotificationEvent.class));
    }

    @Test
    void sendAnswerAcceptedNotification_참여자중질문작성자가있는경우_해당참여자알림제외() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        CommunityAnswer answer = mock(CommunityAnswer.class);
        Course course = mock(Course.class);
        User creator = mock(User.class);
        User questionAuthor = mock(User.class);
        User participant1 = mock(User.class);
        User participant2 = mock(User.class);

        when(question.getId()).thenReturn(1L);
        when(question.getCourse()).thenReturn(course);
        when(question.getUser()).thenReturn(questionAuthor);
        when(course.getUser()).thenReturn(creator);
        when(creator.getId()).thenReturn(100L);
        when(questionAuthor.getId()).thenReturn(200L);
        when(participant1.getId()).thenReturn(200L);
        when(participant2.getId()).thenReturn(300L);

        Set<User> participants = Set.of(participant1, participant2);
        when(communityAnswerReader.getParticipantsByQuestionId(1L)).thenReturn(participants);

        // when
        notificationManager.sendAnswerAcceptedNotification(question, answer);

        // then
        // 크리에이터 1명 + participant2 1명 = 총 2번의 이벤트 발행 (participant1은 질문 작성자라서 제외)
        verify(eventPublisher, times(2)).publishEvent(any(AnswerAcceptedNotificationEvent.class));
    }

    @Test
    void sendAnswerAcceptedNotification_참여자중크리에이터가있는경우_중복알림방지() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        CommunityAnswer answer = mock(CommunityAnswer.class);
        Course course = mock(Course.class);
        User creator = mock(User.class);
        User questionAuthor = mock(User.class);
        User participant1 = mock(User.class);
        User participant2 = mock(User.class);

        when(question.getId()).thenReturn(1L);
        when(question.getCourse()).thenReturn(course);
        when(question.getUser()).thenReturn(questionAuthor);
        when(course.getUser()).thenReturn(creator);
        when(creator.getId()).thenReturn(100L);
        when(questionAuthor.getId()).thenReturn(200L);
        when(participant1.getId()).thenReturn(100L); // 크리에이터와 동일한 참여자
        when(participant2.getId()).thenReturn(300L);

        Set<User> participants = Set.of(participant1, participant2);
        when(communityAnswerReader.getParticipantsByQuestionId(1L)).thenReturn(participants);

        // when
        notificationManager.sendAnswerAcceptedNotification(question, answer);

        // then
        // 크리에이터 1명 + participant2 1명 = 총 2번의 이벤트 발행 (participant1은 크리에이터라서 중복 제외)
        verify(eventPublisher, times(2)).publishEvent(any(AnswerAcceptedNotificationEvent.class));
    }
}
