package insty.domain.notification.repository;

import insty.model.notification.Notification;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n " +
            "WHERE n.userId = :userId " +
            "AND n.state <> insty.model.notification.NotificationState.DELETED " +
            "ORDER BY n.createdAt DESC")
    Page<Notification> findActiveByUserId(@Param("userId") Long userId, Pageable pageable);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.state = insty.model.notification.NotificationState.READ " +
            "WHERE n.userId = :userId " +
            "AND n.state = insty.model.notification.NotificationState.UNREAD")
    int markAllAsRead(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.state = insty.model.notification.NotificationState.DELETED " +
            "WHERE n.userId = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);
}
