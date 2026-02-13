package startup.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    /**
     * The secret key used for signing and verifying JWTs.
     * It is critical that this key is kept secure and not exposed.
     */
    private String secretKey;

    /**
     * The expiration time for JWT access tokens, in milliseconds.
     * Defines the duration for which an access token remains valid.
     */
    private long expirationMs;

    /**
     * The expiration time for JWT refresh tokens, in milliseconds.
     * Specifies how long refresh tokens remain valid for obtaining new access tokens.
     */
    private long refreshExpirationMs;


}
