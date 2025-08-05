package insty.mail.template;

import lombok.RequiredArgsConstructor;
import org.thymeleaf.spring6.SpringTemplateEngine;

@RequiredArgsConstructor
public abstract class MailTemplate {

    protected final SpringTemplateEngine templateEngine;
}
