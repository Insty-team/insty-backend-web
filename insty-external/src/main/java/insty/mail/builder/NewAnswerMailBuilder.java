package insty.mail.builder;

import insty.mail.MailType;
import insty.mail.payload.NewAnswerMailPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

/**
 * 새 답변 알림 메일 빌더
 */
@Component
@RequiredArgsConstructor
public class NewAnswerMailBuilder implements MailBuilder<NewAnswerMailPayload> {

    private final SpringTemplateEngine templateEngine;

    @Override
    public String buildSubject(NewAnswerMailPayload payload) {
        return MailType.COMMUNITY_ANSWER.getSubject();
    }

    @Override
    public String buildBody(NewAnswerMailPayload payload) {
        Context context = new Context();
        context.setVariable("questionTitle", payload.questionTitle());
        context.setVariable("answerContent", payload.answerContent());
        context.setVariable("answerAuthorNickname", payload.answerAuthorNickname());
        context.setVariable("questionUrl", payload.questionUrl());

        return templateEngine.process(MailType.COMMUNITY_ANSWER.getTemplate(), context);
    }
}
