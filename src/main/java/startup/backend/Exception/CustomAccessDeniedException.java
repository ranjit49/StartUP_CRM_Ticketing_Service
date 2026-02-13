package startup.backend.Exception;

import lombok.Getter;
import org.springframework.security.access.AccessDeniedException;

@Getter
public class CustomAccessDeniedException extends AccessDeniedException {

    private final String errorCode; // Add a custom error code

    public CustomAccessDeniedException(String message) {
        super(message);
        this.errorCode = "ACCESS_DENIED"; // Default error code
    }

    public CustomAccessDeniedException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

}