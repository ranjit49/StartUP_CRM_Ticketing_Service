package startup.backend.Exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JwtTokenParseException extends RuntimeException {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenParseException.class);



    public JwtTokenParseException(String message, Throwable cause) {
        super(message, cause);
        logError(message, cause);
    }

    private void logError(String message, Throwable cause) {
        logger.error("JWT Token Parsing Error: {}. Cause: {}", message, cause.getMessage(), cause);
    }
}
