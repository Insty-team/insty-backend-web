package insty.mail;

/**
 * 메일 전송을 위한 Payload 인터페이스
 * 모든 메일 타입별 Payload는 이 인터페이스를 구현해야 함
 */
public interface MailPayload {
    /**
     * 메일 수신자 이메일 주소
     * @return 수신자 이메일
     */
    String getRecipient();
}
