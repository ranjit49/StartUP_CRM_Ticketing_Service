package startup.backend.Exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JwtTokenExpiredException extends RuntimeException {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenExpiredException.class);


    public JwtTokenExpiredException(String message) {
        super(message);
        logError(message);
    }


    private void logError(String message) {
        logger.error("JWT Token Expired: {}", message);
    }


    @Override
    public String toString() {
        return "JwtTokenExpiredException: " + getMessage();
    }
}
