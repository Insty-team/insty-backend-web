package insty.mail.builder;

import insty.mail.MailPayload;

/**
 * 메일 빌더 전략 인터페이스
 * 각 메일 타입별로 구현체를 만들어 subject와 body를 생성
 *
 * @param <T> MailPayload 타입
 */
public interface MailBuilder<T extends MailPayload> {

    /**
     * 메일 제목 생성
     * @param payload 메일 Payload
     * @return 메일 제목
     */
    String buildSubject(T payload);

    /**
     * 메일 본문(HTML) 생성
     * @param payload 메일 Payload
     * @return HTML 형식의 메일 본문
     */
    String buildBody(T payload);
}
