package startup.backend.service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import startup.backend.Exception.CustomAccessDeniedException;
import startup.backend.client.AuthServiceClient;
import startup.backend.dto.ApiResponse;
import startup.backend.dto.NotificationResponse;
import startup.backend.dto.UserResponse;
import startup.backend.entity.Notification;
import startup.backend.enums.NotificationType;
import startup.backend.repository.NotificationRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
//@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;
    private final AuthServiceClient authServiceClient;


    public NotificationService(NotificationRepository notificationRepository,
                               @Autowired(required = false) JavaMailSender mailSender,
                               AuthServiceClient authServiceClient) {
        this.notificationRepository = notificationRepository;
        this.mailSender = mailSender;
        this.authServiceClient = authServiceClient;
    }
    /**
     * Create notification (Called from TaskService / CommentService)
     */
    @Transactional
    public void createNotification(Long userId,
                                   Long taskId,
                                   String message,
                                   NotificationType type,
                                   String email) {

        Notification notification = Notification.builder()
                .userId(userId)
                .taskId(taskId)
                .message(message)
                .type(type)
                .build();

        notificationRepository.save(notification);
        ApiResponse<UserResponse> response =
                authServiceClient.getUserById(userId);

        email = response.getData().getEmail();
        if (email != null && !email.isBlank()) {
            sendEmail(email, message);
        }
    }

    /**
     * Fetch all notifications of logged-in user
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(Long userId) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Fetch only unread notifications
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        return notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Mark notification as read
     */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NoSuchElementException("Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new CustomAccessDeniedException(
                    "You are not allowed to modify this notification",
                    "ACCESS_DENIED_NOTIFICATION"
            );
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    private NotificationResponse mapToResponse(Notification notification) {

        return NotificationResponse.builder()
                .id(notification.getId())
                .taskId(notification.getTaskId())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    private void sendEmail(String to, String message) {

        if (mailSender == null) {
            // Safe fallback (important for tests)
            return;
        }

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setSubject("Task Notification");
        mail.setText(message);

        mailSender.send(mail);
    }
}
