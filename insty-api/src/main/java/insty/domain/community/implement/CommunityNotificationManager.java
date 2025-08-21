package insty.domain.community.implement;

import insty.domain.notification.event.AnswerAcceptedNotificationEvent;
import insty.domain.notification.event.NewCommunityQuestionEvent;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
import insty.model.user.User;
import java.util.Set;
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
        eventPublisher.publishEvent(new NewCommunityQuestionEvent(question));
    }

    /**
     * 답변 채택 알림 전송
     */
    public void sendAnswerAcceptedNotification(CommunityQuestion question, CommunityAnswer answer) {
        User creator = question.getCourse().getUser();
        Set<User> participants = communityAnswerReader.getParticipantsByQuestionId(question.getId());

        if (!creator.getId().equals(question.getUser().getId())) {
            eventPublisher.publishEvent(new AnswerAcceptedNotificationEvent(creator, question, answer));
        }

        participants.stream()
                .filter(participant -> !participant.getId().equals(question.getUser().getId()))
                .forEach(participant -> {
                    eventPublisher.publishEvent(new AnswerAcceptedNotificationEvent(participant, question, answer));
                });
    }
}
