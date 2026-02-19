package startup.backend.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TaskLifecycleException.class)
    public ResponseEntity<Map<String, Object>> handleTaskLifecycleException(TaskLifecycleException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getErrorType().name());
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message, String error) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("status", status.value());
        response.put("error", error);
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }
}
