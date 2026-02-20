package startup.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MessageResponse {

    private Long id;
    private Long taskId;
    private String message;
    private Long senderId;
    private LocalDateTime createdAt;
}
