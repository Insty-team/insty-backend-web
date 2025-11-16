package insty.domain.user.event;

import insty.model.user.User;

/**
 * User 생성 이벤트
 * 회원가입 시 User가 생성되면 발행됨
 */
public record UserCreatedEvent(User user) {
}
