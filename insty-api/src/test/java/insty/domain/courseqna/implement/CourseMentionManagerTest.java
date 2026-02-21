package insty.domain.courseqna.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.mention.dto.MentionedUserInfo;
import insty.domain.mention.implement.MentionNotificationManager;
import insty.domain.mention.implement.MentionParser;
import insty.domain.mention.implement.MentionWriter;
import insty.model.courseqna.CourseAnswer;
import insty.model.courseqna.CommunityAnswerFixtureBuilder;
import insty.model.courseqna.CommunityQuestionFixtureBuilder;
import insty.model.mention.Mention;
import insty.model.mention.MentionTargetType;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CourseMentionManagerTest {

    @InjectMocks
    private CourseMentionManager courseMentionManager;

    @Mock
    private MentionParser mentionParser;

    @Mock
    private MentionWriter mentionWriter;

    @Mock
    private MentionNotificationManager mentionNotificationManager;

    @Test
    void processMentions_정상_맨션된사용자목록반환() {
        // given
        CourseAnswer answer = CommunityAnswerFixtureBuilder.getCommunityAnswerWithIdAndUser(
                CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser(1L, "질문 제목", "질문 내용"), 1L, "답변 내용");
        User mentionerUser = UserFixtureBuilder.getUserWithId(1L);
        String content = "답변 내용입니다. @[홍길동](2) @[김철수](3)";

        User mentionedUser1 = UserFixtureBuilder.getUserWithId(2L);
        User mentionedUser2 = UserFixtureBuilder.getUserWithId(3L);
        
        MentionedUserInfo mentionedUserInfo1 = new MentionedUserInfo(2L, "홍길동");
        MentionedUserInfo mentionedUserInfo2 = new MentionedUserInfo(3L, "김철수");
        
        Mention mention1 = org.mockito.Mockito.mock(Mention.class);
        Mention mention2 = org.mockito.Mockito.mock(Mention.class);

        when(mentionParser.parseMentionedUserInfos(content, mentionerUser))
                .thenReturn(List.of(mentionedUserInfo1, mentionedUserInfo2));
        when(mentionWriter.saveMentions(
                List.of(mentionedUserInfo1, mentionedUserInfo2), mentionerUser, MentionTargetType.COURSE_ANSWER, 1L))
                .thenReturn(List.of(mention1, mention2));
        when(mention1.getMentionedUser()).thenReturn(mentionedUser1);
        when(mention2.getMentionedUser()).thenReturn(mentionedUser2);

        // when
        List<User> result = courseMentionManager.processMentions(answer, mentionerUser, content);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(mentionedUser1, mentionedUser2);
        
        verify(mentionParser).parseMentionedUserInfos(content, mentionerUser);
        verify(mentionWriter).validateMentionCooldown(List.of(mentionedUserInfo1, mentionedUserInfo2), mentionerUser);
        verify(mentionWriter).saveMentions(
                List.of(mentionedUserInfo1, mentionedUserInfo2), mentionerUser, MentionTargetType.COURSE_ANSWER, 1L);
        verify(mentionNotificationManager).sendMentionsNotification(
                List.of(mention1, mention2), content, MentionTargetType.COURSE_ANSWER, 1L);
    }

    @Test
    void processMentions_맨션없음_빈목록반환() {
        // given
        CourseAnswer answer = CommunityAnswerFixtureBuilder.getCommunityAnswerWithIdAndUser(
                CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser(1L, "질문 제목", "질문 내용"), 1L, "답변 내용");
        User mentionerUser = UserFixtureBuilder.getUserWithId(1L);
        String content = "답변 내용입니다.";

        when(mentionParser.parseMentionedUserInfos(content, mentionerUser))
                .thenReturn(List.of());
        when(mentionWriter.saveMentions(List.of(), mentionerUser, MentionTargetType.COURSE_ANSWER, 1L))
                .thenReturn(List.of());

        // when
        List<User> result = courseMentionManager.processMentions(answer, mentionerUser, content);

        // then
        assertThat(result).isEmpty();
        
        verify(mentionParser).parseMentionedUserInfos(content, mentionerUser);
        verify(mentionWriter).validateMentionCooldown(List.of(), mentionerUser);
        verify(mentionWriter).saveMentions(List.of(), mentionerUser, MentionTargetType.COURSE_ANSWER, 1L);
        verify(mentionNotificationManager).sendMentionsNotification(
                List.of(), content, MentionTargetType.COURSE_ANSWER, 1L);
    }
}
