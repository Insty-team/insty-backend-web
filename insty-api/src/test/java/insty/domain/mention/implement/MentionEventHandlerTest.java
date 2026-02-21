package insty.domain.mention.implement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.mention.dto.MentionCreateEvent;
import insty.domain.mention.dto.MentionedUserInfo;
import insty.domain.user.repository.UserRepository;
import insty.error.MentionErrorCode;
import insty.exception.CustomException;
import insty.model.mention.Mention;
import insty.model.mention.MentionTargetType;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class MentionEventHandlerTest {

    @InjectMocks
    private MentionEventHandler mentionEventHandler;

    @Mock
    private MentionParser mentionParser;

    @Mock
    private MentionWriter mentionWriter;

    @Mock
    private MentionNotificationManager mentionNotificationManager;

    @Mock
    private UserRepository userRepository;

    @Test
    void handle_정상_파싱저장알림을_순차처리() {
        // given
        MentionCreateEvent event = MentionCreateEvent.of(
                1L, MentionTargetType.COMMUNITY_COMMENT, 10L, "안녕하세요 @[홍길동](2)님"
        );
        User mentionerUser = UserFixtureBuilder.getUserWithId(1L);
        MentionedUserInfo mentionedUserInfo = new MentionedUserInfo(2L, "홍길동");
        Mention mention = org.mockito.Mockito.mock(Mention.class);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mentionerUser));
        when(mentionParser.parseMentionedUserInfos(event.content(), mentionerUser))
                .thenReturn(List.of(mentionedUserInfo));
        when(mentionWriter.saveMentions(
                List.of(mentionedUserInfo), mentionerUser, MentionTargetType.COMMUNITY_COMMENT, 10L))
                .thenReturn(List.of(mention));

        // when
        mentionEventHandler.handle(event);

        // then
        InOrder inOrder = inOrder(mentionWriter, mentionNotificationManager);
        inOrder.verify(mentionWriter).validateMentionCooldown(List.of(mentionedUserInfo), mentionerUser);
        inOrder.verify(mentionWriter).saveMentions(
                List.of(mentionedUserInfo), mentionerUser, MentionTargetType.COMMUNITY_COMMENT, 10L);
        inOrder.verify(mentionNotificationManager).sendMentionsNotification(
                List.of(mention), event.content(), MentionTargetType.COMMUNITY_COMMENT, 10L);
    }

    @Test
    void handle_멘션파싱오류가_발생해도_예외를_전파하지_않는다() {
        // given
        MentionCreateEvent event = MentionCreateEvent.of(
                1L, MentionTargetType.COURSE_QUESTION, 3L, "안녕하세요 @[홍길동](2a)"
        );
        User mentionerUser = UserFixtureBuilder.getUserWithId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mentionerUser));
        when(mentionParser.parseMentionedUserInfos(event.content(), mentionerUser))
                .thenThrow(new CustomException(MentionErrorCode.MENTION_INVALID_FORMAT));

        // when
        mentionEventHandler.handle(event);

        // then
        verify(mentionWriter, never()).validateMentionCooldown(any(), any());
        verify(mentionWriter, never()).saveMentions(any(), any(), any(), any());
        verify(mentionNotificationManager, never()).sendMentionsNotification(any(), any(), any(), any());
    }

    @Test
    void handle_멘션작성자가_없으면_처리를_중단한다() {
        // given
        MentionCreateEvent event = MentionCreateEvent.of(
                999L, MentionTargetType.COURSE_ANSWER, 22L, "안녕하세요 @[홍길동](2)"
        );
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when
        mentionEventHandler.handle(event);

        // then
        verify(mentionParser, never()).parseMentionedUserInfos(any(), any());
        verify(mentionWriter, never()).validateMentionCooldown(any(), any());
        verify(mentionWriter, never()).saveMentions(any(), any(), any(), any());
        verify(mentionNotificationManager, never()).sendMentionsNotification(any(), any(), any(), any());
    }

    @Test
    void handle_멘션저장실패가_발생해도_예외를_전파하지_않는다() {
        // given
        MentionCreateEvent event = MentionCreateEvent.of(
                1L, MentionTargetType.COURSE_ANSWER, 22L, "안녕하세요 @[홍길동](2)"
        );
        User mentionerUser = UserFixtureBuilder.getUserWithId(1L);
        MentionedUserInfo mentionedUserInfo = new MentionedUserInfo(2L, "홍길동");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mentionerUser));
        when(mentionParser.parseMentionedUserInfos(event.content(), mentionerUser))
                .thenReturn(List.of(mentionedUserInfo));
        when(mentionWriter.saveMentions(
                List.of(mentionedUserInfo), mentionerUser, MentionTargetType.COURSE_ANSWER, 22L))
                .thenThrow(new CustomException(MentionErrorCode.MENTION_CREATE_ERROR));

        // when
        mentionEventHandler.handle(event);

        // then
        verify(mentionWriter).validateMentionCooldown(List.of(mentionedUserInfo), mentionerUser);
        verify(mentionNotificationManager, never()).sendMentionsNotification(any(), any(), any(), any());
    }
}
