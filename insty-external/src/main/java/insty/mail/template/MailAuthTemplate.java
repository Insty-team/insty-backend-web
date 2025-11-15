package insty.mail.template;

import insty.mail.MailTemplate;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Component
public class MailAuthTemplate extends MailTemplate {

    public MailAuthTemplate(SpringTemplateEngine templateEngine) {
        super(templateEngine);
    }

    public String renderEmailAuth(String code) {
        Context context = new Context();
        context.setVariable("code", code);
        return templateEngine.process("email", context);
    }

    // todo: 모듈 별 이메일 사용에 따라 템플릿 렌더링 후
}
