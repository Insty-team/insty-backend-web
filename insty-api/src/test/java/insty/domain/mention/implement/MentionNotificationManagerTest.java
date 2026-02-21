package insty.domain.mention.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.courseqna.implement.CourseAnswerReader;
import insty.domain.notification.dto.event.NotificationReq;
import insty.error.CourseQnaErrorCode;
import insty.exception.CustomException;
import insty.model.mention.Mention;
import insty.model.mention.MentionTargetType;
import insty.model.user.User;
import java.util.List;
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
class MentionNotificationManagerTest {

    @InjectMocks
    private MentionNotificationManager mentionNotificationManager;

    @Mock
    private CourseAnswerReader courseAnswerReader;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    void sendMentionsNotification_강좌답변이면_질문답변아이디를_함께전달() {
        // given
        Mention mention = createMention(2L, 101L, "멘션작성자");
        when(courseAnswerReader.getQuestionIdByAnswerId(22L)).thenReturn(10L);

        // when
        mentionNotificationManager.sendMentionsNotification(
                List.of(mention), "안녕하세요 @[멘션대상](2)", MentionTargetType.COURSE_ANSWER, 22L
        );

        // then
        ArgumentCaptor<NotificationReq> requestCaptor = ArgumentCaptor.forClass(NotificationReq.class);
        verify(eventPublisher).publishEvent(requestCaptor.capture());

        NotificationReq request = requestCaptor.getValue();
        assertThat(request.receiverId()).isEqualTo(2L);
        assertThat(request.getContentType()).isEqualTo("ANSWER");
        assertThat(request.getRelatedId()).isEqualTo(10L);
        assertThat(request.getQuestionId()).isEqualTo(10L);
        assertThat(request.getAnswerId()).isEqualTo(22L);
    }

    @Test
    void sendMentionsNotification_질문아이디조회실패면_기존타겟아이디로전달() {
        // given
        Mention mention = createMention(2L, 101L, "멘션작성자");
        when(courseAnswerReader.getQuestionIdByAnswerId(22L))
                .thenThrow(new CustomException(CourseQnaErrorCode.COURSE_QNA_ANSWER_NOT_FOUND));

        // when
        mentionNotificationManager.sendMentionsNotification(
                List.of(mention), "안녕하세요 @[멘션대상](2)", MentionTargetType.COURSE_ANSWER, 22L
        );

        // then
        ArgumentCaptor<NotificationReq> requestCaptor = ArgumentCaptor.forClass(NotificationReq.class);
        verify(eventPublisher).publishEvent(requestCaptor.capture());

        NotificationReq request = requestCaptor.getValue();
        assertThat(request.receiverId()).isEqualTo(2L);
        assertThat(request.getContentType()).isEqualTo("ANSWER");
        assertThat(request.getRelatedId()).isEqualTo(22L);
        assertThat(request.getQuestionId()).isNull();
        assertThat(request.getAnswerId()).isNull();
    }

    private Mention createMention(Long mentionedUserId, Long mentionId, String mentionerNickname) {
        Mention mention = mock(Mention.class);
        User mentionedUser = mock(User.class);
        User mentionerUser = mock(User.class);

        when(mention.getId()).thenReturn(mentionId);
        when(mention.getMentionedUser()).thenReturn(mentionedUser);
        when(mention.getMentionerUser()).thenReturn(mentionerUser);
        when(mentionedUser.getId()).thenReturn(mentionedUserId);
        when(mentionerUser.getNickname()).thenReturn(mentionerNickname);
        return mention;
    }
}
