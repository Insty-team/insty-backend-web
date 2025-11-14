package insty.mail.builder;

import insty.mail.MailType;
import insty.mail.payload.AuthMailPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

/**
 * 회원가입 인증 메일 빌더
 */
@Component
@RequiredArgsConstructor
public class AuthMailBuilder implements MailBuilder<AuthMailPayload> {

    private final SpringTemplateEngine templateEngine;

    @Override
    public String buildSubject(AuthMailPayload payload) {
        return MailType.AUTH.getSubject();
    }

    @Override
    public String buildBody(AuthMailPayload payload) {
        Context context = new Context();
        context.setVariable("username", payload.username());
        context.setVariable("verifyLink", payload.verifyLink());

        return templateEngine.process(MailType.AUTH.getTemplate(), context);
    }
}
