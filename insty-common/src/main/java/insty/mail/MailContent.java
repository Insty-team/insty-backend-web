package insty.mail;

import java.util.Map;

public interface MailContent {
    String to();
    MailType mailType();
    Map<String, Object> variables();
}
