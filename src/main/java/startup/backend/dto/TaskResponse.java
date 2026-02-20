package startup.backend.dto;

import lombok.Builder;
import lombok.Data;
import startup.backend.enums.TaskPriority;
import startup.backend.enums.TaskStatus;
import startup.backend.enums.TaskType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TaskResponse {

    private Long id;
    private Long parentId;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private TaskType type;
    private Long assignedTo;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
	private List<TaskResponse> children;
}
