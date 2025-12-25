package insty.domain.courseqna.implement;

import insty.domain.mention.dto.MentionedUserInfo;
import insty.domain.mention.implement.MentionNotificationManager;
import insty.domain.mention.implement.MentionParser;
import insty.domain.mention.implement.MentionWriter;
import insty.model.courseqna.CourseAnswer;
import insty.model.mention.Mention;
import insty.model.user.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 커뮤니티 맨션 관리 서비스
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class CourseMentionManager {

    private final MentionParser mentionParser;
    private final MentionWriter mentionWriter;
    private final MentionNotificationManager mentionNotificationManager;

    /**
     * 답변에서 맨션을 처리하고 맨션된 사용자 목록을 반환한다
     * @return 맨션된 사용자 목록
     */
    public List<User> processMentions(CourseAnswer answer, User mentionerUser, String content) {
        List<MentionedUserInfo> mentionedUserInfos = mentionParser.parseMentionedUserInfos(content, mentionerUser);
        mentionWriter.validateMentionCooldown(mentionedUserInfos, mentionerUser);

        List<Mention> savedMentions = mentionWriter.saveMentions(mentionedUserInfos, mentionerUser, answer);

        mentionNotificationManager.sendMentionsNotification(savedMentions, answer.getCourseQuestion());
        
        return savedMentions.stream()
                .map(Mention::getMentionedUser)
                .toList();
    }
}
