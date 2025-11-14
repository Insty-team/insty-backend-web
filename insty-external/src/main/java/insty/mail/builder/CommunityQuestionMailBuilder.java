package insty.mail.builder;

import insty.mail.MailType;
import insty.mail.payload.CommunityQuestionMailPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

/**
 * 커뮤니티 새 질문 알림 메일 빌더
 */
@Component
@RequiredArgsConstructor
public class CommunityQuestionMailBuilder implements MailBuilder<CommunityQuestionMailPayload> {

    private final SpringTemplateEngine templateEngine;

    @Override
    public String buildSubject(CommunityQuestionMailPayload payload) {
        return MailType.COMMUNITY_QUESTION.getSubject();
    }

    @Override
    public String buildBody(CommunityQuestionMailPayload payload) {
        Context context = new Context();
        context.setVariable("questionTitle", payload.questionTitle());
        context.setVariable("questionContent", payload.questionContent());
        context.setVariable("questionAuthorName", payload.questionAuthorName());
        context.setVariable("courseName", payload.courseName());
        context.setVariable("questionUrl", payload.questionUrl());

        return templateEngine.process(MailType.COMMUNITY_QUESTION.getTemplate(), context);
    }
}
