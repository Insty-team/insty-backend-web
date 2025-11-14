package insty.mail.builder;

import insty.mail.MailType;
import insty.mail.payload.MentionMailPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

/**
 * 사용자 멘션 알림 메일 빌더
 */
@Component
@RequiredArgsConstructor
public class MentionMailBuilder implements MailBuilder<MentionMailPayload> {

    private final SpringTemplateEngine templateEngine;

    @Override
    public String buildSubject(MentionMailPayload payload) {
        return MailType.MENTION.getSubject();
    }

    @Override
    public String buildBody(MentionMailPayload payload) {
        Context context = new Context();
        context.setVariable("questionTitle", payload.questionTitle());
        context.setVariable("mentionerName", payload.mentionerName());
        context.setVariable("questionUrl", payload.questionUrl());

        return templateEngine.process(MailType.MENTION.getTemplate(), context);
    }
}
