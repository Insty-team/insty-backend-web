package insty.domain.user.repository;

import insty.model.user.User;
import insty.model.user.UserNotificationPreference;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @deprecated 더 이상 사용되지 않으며 추후 제거 예정.
 */
@Deprecated
public interface UserNotificationPreferenceRepository extends JpaRepository<UserNotificationPreference, Long> {
    
    Optional<UserNotificationPreference> findByUser(User user);
    
    Optional<UserNotificationPreference> findByUserId(Long userId);
    
    boolean existsByUser(User user);
    
    boolean existsByUserId(Long userId);
}