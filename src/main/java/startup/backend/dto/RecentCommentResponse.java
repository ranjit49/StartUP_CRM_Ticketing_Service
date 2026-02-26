package startup.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentCommentResponse {

    private Long taskId;
    private String taskTitle;
    private String comment;
    private LocalDateTime createdAt;
}
