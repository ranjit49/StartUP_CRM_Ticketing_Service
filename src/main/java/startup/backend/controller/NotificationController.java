package startup.backend.controller;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import startup.backend.dto.NotificationResponse;
import startup.backend.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/ticket-tasks/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class NotificationController {

    private final NotificationService notificationService;


    /**
     * GET /notifications
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications() {
        System.out.println("Current user id : "+getCurrentUserId());
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
//    private Long getCurrentUserId() {
//
//        // Example only — replace with your actual implementation
//        return Long.parseLong(
//                org.springframework.security.core.context.SecurityContextHolder
//                        .getContext()
//                        .getAuthentication()
//                        .getName()
//        );
//    }


    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        Object details = authentication.getDetails();
        if (!(details instanceof Claims claims)) {
            throw new RuntimeException("JWT claims not found in authentication details");
        }

        Object idObj = claims.get("id");
        if (idObj instanceof Integer i) return i.longValue();
        if (idObj instanceof Long l) return l;
        if (idObj instanceof String s) return Long.parseLong(s);

        throw new RuntimeException("User ID not found in token");
    }
}
