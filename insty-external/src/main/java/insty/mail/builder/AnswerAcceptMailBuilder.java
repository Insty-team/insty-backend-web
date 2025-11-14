package insty.mail.builder;

import insty.mail.MailType;
import insty.mail.payload.AnswerAcceptMailPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

/**
 * 답변 채택 알림 메일 빌더
 */
@Component
@RequiredArgsConstructor
public class AnswerAcceptMailBuilder implements MailBuilder<AnswerAcceptMailPayload> {

    private final SpringTemplateEngine templateEngine;

    @Override
    public String buildSubject(AnswerAcceptMailPayload payload) {
        return MailType.COMMUNITY_ANSWER_ACCEPT.getSubject();
    }

    @Override
    public String buildBody(AnswerAcceptMailPayload payload) {
        Context context = new Context();
        context.setVariable("questionTitle", payload.questionTitle());
        context.setVariable("answerContent", payload.answerContent());
        context.setVariable("answerAuthorName", payload.answerAuthorName());
        context.setVariable("questionAuthorName", payload.questionAuthorName());
        context.setVariable("questionUrl", payload.questionUrl());

        return templateEngine.process(MailType.COMMUNITY_ANSWER_ACCEPT.getTemplate(), context);
    }
}
