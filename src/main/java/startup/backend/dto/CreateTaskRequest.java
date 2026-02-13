package startup.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import startup.backend.enums.TaskPriority;
import startup.backend.enums.TaskType;

@Data
public class CreateTaskRequest {

    @NotBlank(message = "Title is mandatory")
    private String title;

    private String description;

    @NotNull(message = "Priority is mandatory")
    private TaskPriority priority;

    @NotNull(message = "Type is mandatory")
    private TaskType type;

    private Long parentId; // null = root
}
