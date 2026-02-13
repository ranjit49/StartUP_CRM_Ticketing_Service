package startup.backend.Exception;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.Serial;

@Getter
public class JwtTokenException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenException.class);
    private final HttpStatus status;
    private final String errorCode;




    public JwtTokenException(String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.BAD_REQUEST;
        this.errorCode = "JWT_TOKEN_ERROR";
        logError(message, cause);
    }


    private void logError(String message, Throwable cause) {
        if (cause == null) {
            logger.error("JWT Exception: {}", message);
        } else {
            logger.error("JWT Exception: {}. Cause: {}", message, cause.getMessage(), cause);
        }
    }

    @Override
    public String toString() {
        return String.format("JwtTokenException[status=%s, errorCode=%s, message=%s]", status, errorCode, getMessage());
    }
}
