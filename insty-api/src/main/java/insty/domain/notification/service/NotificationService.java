package insty.domain.notification.service;

import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
import insty.model.mention.Mention;
import java.util.List;

public interface NotificationService {

    void sendQuestionNotificationToCreator(CommunityQuestion question);

    void sendAnswerNotification(CommunityQuestion question, CommunityAnswer answer);

    void sendMentionNotification(List<Mention> mentions, String questionTitle);
}
