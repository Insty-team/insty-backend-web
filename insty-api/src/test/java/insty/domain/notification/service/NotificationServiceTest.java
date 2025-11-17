package insty.domain.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.constants.NotificationConstants;
import insty.domain.notification.dto.NotificationData;
import insty.domain.notification.dto.NotificationRequest;
import insty.domain.notification.dto.NotificationResponse;
import insty.domain.notification.repository.NotificationRepository;
import insty.domain.user.repository.UserRepository;
import insty.error.NotificationErrorCode;
import insty.error.UserErrorCode;
import insty.exception.CustomException;
import insty.model.notification.Notification;
import insty.model.notification.NotificationState;
import insty.model.user.User;
import insty.notification.NotificationType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void 사용자_알림_조회_성공() {
        // Given
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .build();

        Notification notification1 = Notification.create(
                userId,
                NotificationType.NEW_COMMUNITY_QUESTION,
                "새로운 질문",
                "질문이 등록되었습니다",
                "/questions/1"
        );

        Notification notification2 = Notification.create(
                userId,
                NotificationType.NEW_COMMUNITY_ANSWER,
                "새로운 답변",
                "답변이 등록되었습니다",
                "/questions/1#answer-1"
        );

        List<Notification> notifications = List.of(notification1, notification2);
        Page<Notification> notificationPage = new PageImpl<>(notifications);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(notificationRepository.findActiveByUserId(any(Long.class), any(Pageable.class)))
                .thenReturn(notificationPage);

        // When
        List<NotificationResponse> result = notificationService.getUserNotifications(userId);

        // Then
        assertEquals(2, result.size());
        verify(userRepository).findById(userId);
        verify(notificationRepository).findActiveByUserId(any(Long.class), any(Pageable.class));
    }

    @Test
    void 사용자_알림_조회_실패_사용자_없음() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> notificationService.getUserNotifications(userId));

        assertEquals(UserErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void 알림_저장_성공() {
        // Given
        Long receiverId = 1L;
        NotificationRequest request = NotificationRequest.newCommunityQuestion(
                receiverId,
                1L,
                "질문 제목",
                "질문 내용",
                "작성자",
                "강의명"
        );

        NotificationData data = new NotificationData(
                "새로운 질문",
                "질문이 등록되었습니다",
                "/questions/1"
        );

        // When
        notificationService.saveNotification(request, data);

        // Then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification savedNotification = captor.getValue();
        assertEquals(receiverId, savedNotification.getUserId());
        assertEquals(NotificationType.NEW_COMMUNITY_QUESTION, savedNotification.getType());
        assertEquals("새로운 질문", savedNotification.getTitle());
        assertEquals("질문이 등록되었습니다", savedNotification.getMessage());
        assertEquals("/questions/1", savedNotification.getRedirectUrl());
    }

    @Test
    void 알림_읽음_처리_및_리다이렉트_성공() {
        // Given
        Long notificationId = 1L;
        Long userId = 1L;
        String redirectUrl = "/questions/1";

        Notification notification = Notification.create(
                userId,
                NotificationType.NEW_COMMUNITY_QUESTION,
                "새로운 질문",
                "질문이 등록되었습니다",
                redirectUrl
        );

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        // When
        String result = notificationService.markAsReadAndRedirect(notificationId, userId);

        // Then
        assertEquals(redirectUrl, result);
        verify(notificationRepository).save(notification);
        assertEquals(NotificationState.READ, notification.getState());
    }

    @Test
    void 알림_읽음_처리_실패_알림_없음() {
        // Given
        Long notificationId = 999L;
        Long userId = 1L;

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> notificationService.markAsReadAndRedirect(notificationId, userId));

        assertEquals(NotificationErrorCode.NOTIFICATION_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void 알림_읽음_처리_실패_권한_없음() {
        // Given
        Long notificationId = 1L;
        Long userId = 1L;
        Long differentUserId = 2L;

        Notification notification = Notification.create(
                differentUserId,
                NotificationType.NEW_COMMUNITY_QUESTION,
                "새로운 질문",
                "질문이 등록되었습니다",
                "/questions/1"
        );

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> notificationService.markAsReadAndRedirect(notificationId, userId));

        assertEquals(UserErrorCode.USER_NOTIFICATION_PREFERENCE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void 알림_읽음_처리_이미_읽은_알림은_저장하지_않음() {
        // Given
        Long notificationId = 1L;
        Long userId = 1L;
        String redirectUrl = "/questions/1";

        Notification notification = Notification.create(
                userId,
                NotificationType.NEW_COMMUNITY_QUESTION,
                "새로운 질문",
                "질문이 등록되었습니다",
                redirectUrl
        );
        notification.markAsRead();

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        // When
        String result = notificationService.markAsReadAndRedirect(notificationId, userId);

        // Then
        assertEquals(redirectUrl, result);
        verify(notificationRepository).findById(notificationId);
        assertEquals(NotificationState.READ, notification.getState());
    }
}
