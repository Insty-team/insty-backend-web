package insty.domain.community.implement;

import insty.domain.notification.event.NewAnswerNotificationEvent;
import insty.domain.notification.event.AnswerAcceptedNotificationEvent;
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
    private final CommunityQuestionViewManager communityQuestionViewManager;

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
     * - 질문의 creator에게는 답변 알림 전송 (다음 조건을 모두 만족할 때만):
     *   1. 답변 작성자가 creator가 아님
     *   2. creator가 맨션되지 않음
     *   3. creator가 마지막으로 질문을 조회한 시점 이후에 새로운 답변이 있음
     */
    public void sendNewAnswerNotification(CommunityQuestion question, CommunityAnswer answer, List<User> mentionedUsers) {
        User creator = question.getCourse().getUser();
        User answerAuthor = answer.getUser();

        // 답변 작성자가 creator가 아니고, creator가 맨션되지 않은 경우에만 검사
        boolean creatorMentioned = mentionedUsers.stream()
                .anyMatch(user -> user.getId().equals(creator.getId()));
        
        if (!answerAuthor.getId().equals(creator.getId()) && !creatorMentioned) {
            // creator가 마지막으로 질문을 조회한 시점 이후에 새로운 답변이 있는지 확인
            boolean hasNewAnswersAfterCreatorLastView = communityQuestionViewManager.hasNewAnswersAfterCreatorLastView(
                    question.getId(), creator.getId());
            
            if (hasNewAnswersAfterCreatorLastView) {
                eventPublisher.publishEvent(new NewAnswerNotificationEvent(creator, answerAuthor, question, answer));
            }
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
