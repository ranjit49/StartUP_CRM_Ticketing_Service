package startup.backend.dto;
import lombok.Builder;
import lombok.Getter;
import startup.backend.enums.NotificationType;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponse {

    private Long id;
    private Long taskId;
    private String message;
    private NotificationType type;
    private boolean isRead;
    private LocalDateTime createdAt;
}
