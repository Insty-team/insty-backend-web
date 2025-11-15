package insty.domain.notification.service;

import insty.constants.NotificationConstants;
import insty.domain.notification.dto.NotificationResponse;
import insty.domain.notification.repository.NotificationRepository;
import insty.domain.notification.strategy.NotificationData;
import insty.domain.user.repository.UserRepository;
import insty.error.NotificationErrorCode;
import insty.error.UserErrorCode;
import insty.exception.CustomException;
import insty.model.notification.Notification;
import insty.model.notification.NotificationState;
import insty.model.user.User;
import insty.notification.NotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * 사용자 알림 조회
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(0, NotificationConstants.DEFAULT_NOTIFICATION_SIZE);
        Page<Notification> notifications = notificationRepository.findActiveByUserId(user.getId(), pageable);

        return notifications.map(NotificationResponse::from).getContent();
    }

    /**
     * 알림 저장
     */
    @Transactional
    public void saveNotification(NotificationRequest request, NotificationData data) {
        Notification notification = Notification.create(
                request.receiverId(),
                request.type(),
                data.title(),
                data.message(),
                data.redirectUrl()
        );

        notificationRepository.save(notification);
    }

    /**
     * 알림 읽음 처리 및 리다이렉트 URL 반환
     */
    public String markAsReadAndRedirect(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getUserId().equals(userId)) {
            throw new CustomException(UserErrorCode.USER_NOTIFICATION_PREFERENCE_NOT_FOUND);
        }

        if (notification.getState().equals(NotificationState.UNREAD)) {
            notification.markAsRead();
            notificationRepository.save(notification);
        }

        return notification.getRedirectUrl();
    }
}
