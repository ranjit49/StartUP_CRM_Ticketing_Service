package startup.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import startup.backend.dto.NotificationResponse;
import startup.backend.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/ticket-tasks/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;


    /**
     * GET /notifications
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications() {
        Long userId = getCurrentUserId(); // implement using your JWT util

        return ResponseEntity.ok(
                notificationService.getUserNotifications(userId)
        );
    }

    /**
     * GET /notifications/unread
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications() {

        Long userId = getCurrentUserId();
        return ResponseEntity.ok(
                notificationService.getUnreadNotifications(userId)
        );
    }

    /**
     * PUT /notifications/{id}/read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {

        Long userId = getCurrentUserId();

        notificationService.markAsRead(id, userId);

        return ResponseEntity.ok().build();
    }

    /**
     * Extract userId from SecurityContext
     * Implement this according to your JWT logic
     */
    private Long getCurrentUserId() {

        // Example only — replace with your actual implementation
        return Long.parseLong(
                org.springframework.security.core.context.SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName()
        );
    }
}
