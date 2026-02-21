package insty.domain.mention.implement;

import insty.domain.mention.dto.MentionCreateEvent;
import insty.domain.mention.dto.MentionedUserInfo;
import insty.domain.user.repository.UserRepository;
import insty.exception.CustomException;
import insty.model.mention.Mention;
import insty.model.user.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MentionEventHandler {

    private final MentionParser mentionParser;
    private final MentionWriter mentionWriter;
    private final MentionNotificationManager mentionNotificationManager;
    private final UserRepository userRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(MentionCreateEvent event) {
        try {
            User mentionerUser = userRepository.findById(event.mentionerUserId())
                    .orElse(null);
            if (mentionerUser == null) {
                log.warn("멘션 처리 중단 - 멘션 작성자 없음 (mentionerUserId={})", event.mentionerUserId());
                return;
            }

            List<MentionedUserInfo> mentionedUserInfos = mentionParser
                    .parseMentionedUserInfos(event.content(), mentionerUser);
            mentionWriter.validateMentionCooldown(mentionedUserInfos, mentionerUser);

            List<Mention> savedMentions = mentionWriter.saveMentions(
                    mentionedUserInfos,
                    mentionerUser,
                    event.targetType(),
                    event.targetId()
            );
            mentionNotificationManager.sendMentionsNotification(
                    savedMentions,
                    event.content(),
                    event.targetType(),
                    event.targetId()
            );
        } catch (CustomException e) {
            log.warn("멘션 비동기 처리 실패 - code={}, message={}, mentionerUserId={}, targetType={}, targetId={}",
                    e.getErrorCode().getCode(), e.getErrorCode().getMessage(),
                    event.mentionerUserId(), event.targetType(), event.targetId());
        } catch (Exception e) {
            log.error("멘션 비동기 처리 중 예기치 않은 오류 - mentionerUserId={}, targetType={}, targetId={}",
                    event.mentionerUserId(), event.targetType(), event.targetId(), e);
        }
    }
}
