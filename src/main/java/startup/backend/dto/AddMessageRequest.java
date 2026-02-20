package startup.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddMessageRequest {

    @NotBlank(message = "Message must not be empty")
    private String message;
}
