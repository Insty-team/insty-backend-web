package insty.domain.community.implement;

import insty.domain.notification.event.AnswerAcceptedNotificationEvent;
import insty.domain.notification.event.NewAnswerNotificationEvent;
import insty.domain.notification.event.NewCommunityQuestionEvent;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
import insty.model.course.Course;
import insty.model.user.User;
import java.util.Set;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 커뮤니티 알림 전송 서비스
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CommunityNotificationManager {

    private final CommunityAnswerReader communityAnswerReader;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 새 질문 작성 알림 전송
     */
    public void sendNewQuestionNotification(CommunityQuestion question) {
        Course course = question.getCourse();
        User questionAuthor = question.getUser();
        User courseCreator = course.getUser();
        
        eventPublisher.publishEvent(new NewCommunityQuestionEvent(courseCreator, questionAuthor, question, course));
    }

    /**
     * 새 답변 작성 알림 전송
     * - 맨션된 사용자에게는 맨션 알림이 별도로 전송됨
     * - 질문의 creator에게는 답변 알림 전송 (답변 작성자가 creator인 경우 제외, creator가 맨션된 경우 제외)
     */
    public void sendNewAnswerNotification(CommunityQuestion question, CommunityAnswer answer, List<User> mentionedUsers) {
        User creator = question.getCourse().getUser();
        User answerAuthor = answer.getUser();

        boolean creatorMentioned = mentionedUsers.stream()
                .anyMatch(user -> user.getId().equals(creator.getId()));
        
        if (!answerAuthor.getId().equals(creator.getId()) && !creatorMentioned) {
            eventPublisher.publishEvent(new NewAnswerNotificationEvent(creator, answerAuthor, question, answer));
        }
    }

    /**
     * 답변 채택 알림 전송
     */
    public void sendAnswerAcceptedNotification(CommunityQuestion question, CommunityAnswer answer) {
        User creator = question.getCourse().getUser();
        User questionAuthor = question.getUser();
        Set<User> participants = communityAnswerReader.getParticipantsByQuestionId(question.getId());

        eventPublisher.publishEvent(new AnswerAcceptedNotificationEvent(creator, questionAuthor, question, answer));
        
        participants.stream()
                .filter(participant -> !participant.getId().equals(questionAuthor.getId()))
                .filter(participant -> !participant.getId().equals(creator.getId()))
                .forEach(participant -> {
                    eventPublisher.publishEvent(new AnswerAcceptedNotificationEvent(participant, questionAuthor, question, answer));
                });
    }
}
