package insty.mail;

public enum MailType {

    AUTH("[INSTY] 메일 인증 안내드립니다.", "email-auth"),
    ;

    private final String subject;
    private final String template;

    MailType(String subject, String template) {
        this.subject = subject;
        this.template = template;
    }

    public String getTemplate() {
        return template;
    }

    public String getSubject() {
        return subject;
    }
}
