package insty.mail;

public enum MailType {

    AUTH("[INSTY] 메일 인증 안내드립니다.", "email-auth"),
    COMMUNITY_QUESTION("[INSTY] 새로운 질문이 등록되었습니다.", "community-question"),
    COMMUNITY_ANSWER("[INSTY] 새로운 답변이 등록되었습니다.", "community-answer"),
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

    public boolean hasTemplate() {
        return template != null && !template.isBlank();
    }
}
