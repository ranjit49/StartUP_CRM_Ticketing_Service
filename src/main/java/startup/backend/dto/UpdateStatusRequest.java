package startup.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import startup.backend.enums.TaskStatus;

@Data
public class UpdateStatusRequest {

    @NotNull
    private TaskStatus status;
}
