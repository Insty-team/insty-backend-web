package insty.domain.mention.implement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.notification.event.UserMentionedEvent;
import insty.model.community.CommunityQuestion;
import insty.model.mention.Mention;
import insty.model.user.User;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class MentionNotificationManagerTest {

    @InjectMocks
    private MentionNotificationManager notificationManager;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    void sendMentionsNotification_단일멘션_정상() {
        // given
        User mentionerUser = mock(User.class);
        User mentionedUser = mock(User.class);
        CommunityQuestion question = mock(CommunityQuestion.class);
        Mention mention = mock(Mention.class);

        when(mention.getMentionerUser()).thenReturn(mentionerUser);
        when(mention.getMentionedUser()).thenReturn(mentionedUser);

        List<Mention> mentions = List.of(mention);

        // when
        notificationManager.sendMentionsNotification(mentions, question);

        // then
        verify(eventPublisher).publishEvent(any(UserMentionedEvent.class));
    }

    @Test
    void sendMentionsNotification_다중멘션_정상() {
        // given
        User mentionerUser = mock(User.class);
        User mentionedUser1 = mock(User.class);
        User mentionedUser2 = mock(User.class);
        CommunityQuestion question = mock(CommunityQuestion.class);
        
        Mention mention1 = mock(Mention.class);
        Mention mention2 = mock(Mention.class);

        when(mention1.getMentionerUser()).thenReturn(mentionerUser);
        when(mention1.getMentionedUser()).thenReturn(mentionedUser1);
        when(mention2.getMentionerUser()).thenReturn(mentionerUser);
        when(mention2.getMentionedUser()).thenReturn(mentionedUser2);

        List<Mention> mentions = List.of(mention1, mention2);

        // when
        notificationManager.sendMentionsNotification(mentions, question);

        // then
        // 각 멘션에 대해 이벤트 발행 (총 2번)
        verify(eventPublisher, times(2)).publishEvent(any(UserMentionedEvent.class));
    }

    @Test
    void sendMentionsNotification_빈멘션리스트_이벤트발행안함() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        List<Mention> mentions = List.of();

        // when
        notificationManager.sendMentionsNotification(mentions, question);

        // then
        // 이벤트 발행하지 않음
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void sendMentionsNotification_null멘션리스트_이벤트발행안함() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);

        // when
        notificationManager.sendMentionsNotification(null, question);

        // then
        // 이벤트 발행하지 않음
        verify(eventPublisher, never()).publishEvent(any());
    }
}
