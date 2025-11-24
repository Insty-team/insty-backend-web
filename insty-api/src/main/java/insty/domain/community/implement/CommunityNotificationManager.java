package insty.domain.community.implement;

import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
import insty.model.course.Course;
import insty.model.user.User;
import insty.domain.notification.dto.event.NotificationReq;
import java.util.Objects;
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

        NotificationReq request = NotificationReq.newCommunityQuestion(
                courseCreator.getId(),
                question.getId(),
                question.getTitle(),
                question.getContent(),
                questionAuthor.getNickname(),
                course.getTitle()
        );

        eventPublisher.publishEvent(request);
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
        List<User> safeMentionedUsers = (mentionedUsers != null) ? mentionedUsers : List.of();
        boolean creatorMentioned = safeMentionedUsers.stream()
                .anyMatch(user -> Objects.equals(user.getId(), creator.getId()));
        
        if (!Objects.equals(answerAuthor.getId(), creator.getId()) && !creatorMentioned) {
            // creator가 마지막으로 질문을 조회한 시점 이후에 새로운 답변이 있는지 확인
            boolean hasNewAnswersAfterCreatorLastView = communityQuestionViewManager.hasNewAnswersAfterCreatorLastView(
                    question.getId(), creator.getId());

            if (hasNewAnswersAfterCreatorLastView) {
                NotificationReq request = NotificationReq.newAnswer(
                        creator.getId(),
                        question.getId(),
                        answer.getId(),
                        question.getTitle(),
                        answer.getContent(),
                        answerAuthor.getNickname()
                );

                eventPublisher.publishEvent(request);
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

        // 답변 작성자에게 알림 전송
        NotificationReq acceptRequest = NotificationReq.answerAccepted(
                answer.getUser().getId(),
                question.getId(),
                answer.getId(),
                question.getTitle(),
                answer.getContent()
        );
        eventPublisher.publishEvent(acceptRequest);

        // 다른 참여자들에게 알림 전송
        participants.stream()
                .filter(participant -> !participant.getId().equals(answer.getUser().getId()))
                .filter(participant -> !participant.getId().equals(questionAuthor.getId()))
                .filter(participant -> !participant.getId().equals(creator.getId()))
                .forEach(participant -> {
                    NotificationReq participantRequest = NotificationReq.answerAccepted(
                            participant.getId(),
                            question.getId(),
                            answer.getId(),
                            question.getTitle(),
                            answer.getContent()
                    );
                    eventPublisher.publishEvent(participantRequest);
                });
    }
}
