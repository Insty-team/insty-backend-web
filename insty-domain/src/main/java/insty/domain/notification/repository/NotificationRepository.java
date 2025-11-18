package insty.domain.notification.repository;

import insty.model.notification.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n " +
            "WHERE n.userId = :userId " +
            "AND n.state <> insty.model.notification.NotificationState.DELETED " +
            "ORDER BY n.createdAt DESC")
    Page<Notification> findActiveByUserId(@Param("userId") Long userId, Pageable pageable);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);
}
