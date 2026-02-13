package startup.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignTaskRequest {

    @NotNull
    private Long assignedTo;
}
