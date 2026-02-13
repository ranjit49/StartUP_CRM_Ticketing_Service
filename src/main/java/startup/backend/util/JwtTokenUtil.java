package startup.backend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import startup.backend.Exception.JwtTokenException;
import startup.backend.Exception.JwtTokenExpiredException;
import startup.backend.Exception.JwtTokenParseException;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtTokenUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);

    private SecretKey secretKey;

    @Value("${jwt.secretKey}")
    private String secretKeyString;

    @Value("${jwt.expirationMs}")
    private Long expirationMs;
//
//    private final UserRepository userRepository;
//
//    public JwtTokenUtil(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
    @PostConstruct
    public void init() {
        if (Objects.isNull(expirationMs) || expirationMs <= 0) {
            expirationMs = 3600000L;

        }
        try {
            this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            logger.error("Error initializing JwtTokenUtil with the secret key: {}", e.getMessage(), e);
            this.secretKey = Keys.hmacShaKeyFor("defaultSecretKey".getBytes(StandardCharsets.UTF_8));
        }
    }


    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (JwtTokenParseException e) {
            logError("Failed to extract username from the token", e);
            throw new JwtTokenException("Failed to extract username from the token: " + e.getMessage(), e);
        }
    }

    public Long getUserIdFromRequest(HttpServletRequest request) {
        String token=extractTokenFromRequest(request);
        return extractAllClaims(token).get("id", Long.class); // ✅ Extract userId claim
    }
    public Long getUserIdFromToken(String token) {
        return extractAllClaims(token).get("id", Long.class); // ✅ Extract userId claim
    }

    public Date extractExpiration(String token) {
        try {
            return extractClaim(token, Claims::getExpiration);
        } catch (JwtTokenParseException e) {
            logError("Failed to extract expiration from the token", e);
            throw new JwtTokenException("Failed to extract expiration from the token: " + e.getMessage(), e);
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    // method to return all roles as  Collection<? extends GrantedAuthority> authorities
    public  Collection<? extends GrantedAuthority> getAuthoritiesFromToken(String token) {
        Claims claims = extractAllClaims(token);
        List<String> roles = claims.get("roles", List.class);

        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public Claims extractAllClaims(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            if (claims.getExpiration().before(new Date())) {
                throw new JwtTokenExpiredException("JWT token has expired.");
            }
            return claims;
        } catch (JwtException e) {
            throw new JwtTokenExpiredException("JWT token has expired: " + e.getMessage());
        }
    }

    private static final long ALLOWED_CLOCK_SKEW = 5 * 60 * 1000;
    public Boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            long currentTime = System.currentTimeMillis();
            long tokenExpirationTime = expiration.getTime();
            return tokenExpirationTime < (currentTime - ALLOWED_CLOCK_SKEW);
        } catch (JwtTokenException e) {
            logError("Error while checking token expiration", e);
            return true;
        }
    }



//    public String generateToken(String username, Integer userId, Set<Role> roles) {
//        String[] roleNames = roles.stream()
//                .map(role -> role.getName().name())
//                .toArray(String[]::new);
//
//        return Jwts.builder()
//                .setSubject(username)
//                .claim("roles", roleNames)
//                .claim("id", userId)
//                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
//                .signWith(secretKey, SignatureAlgorithm.HS256)
//                .compact();
//    }

    public Boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            if (extractedUsername.equals(username) && !isTokenExpired(token)) {
                logger.info("Token validation successful for user: {}", username);
                return true;
            } else {
                logger.warn("Token validation failed for user {}: Token expired or invalid username", username);
                return false;
            }
        } catch (JwtTokenException e) {
            logError("Token validation failed", e);
            return false;
        }
    }


    public Claims extractResetPasswordClaims(String token) {
        try {
            return extractAllClaims(token);
        } catch (JwtTokenParseException e) {
            logError("Failed to extract claims from reset password token", e);
            return Jwts.claims(Collections.emptyMap());
        }
    }

    private void logError(String message, Exception e) {
        logger.error("{}: {}", message, e.getMessage(), e);
    }

   //  Method to get the current user based on JWT token
//    public User getCurrentUser(HttpServletRequest request) {
//        String token = extractTokenFromRequest(request);  // Extract token from the request header
//        if (token == null || isTokenExpired(token)) {
//            throw new RuntimeException("Invalid or expired token");
//        }
//
//        Long userId = getUserIdFromToken(token);  // Extract user ID from token
//        if (userId == null) {
//            throw new RuntimeException("User ID is missing in the token");
//        }
//        return userRepository.findById(userId)  // Retrieve user by ID
//                .orElseThrow(() -> new RuntimeException("User not found"));
//    }

    // Helper method to extract token from the Authorization header
    private String extractTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);  // Extract the token (after "Bearer ")
        }
        return null;

    }
    // ----------------- ADDED HELPERS (non-breaking) -----------------

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Object rolesObj = claims.get("roles");
            if (rolesObj instanceof List<?>) {
                return ((List<?>) rolesObj).stream().map(Object::toString).toList();
            }
            Object single = claims.get("role");
            if (single != null) return List.of(single.toString());
        } catch (Exception ex) {
            // ignore parsing errors, return empty
        }
        return List.of();
    }

    public boolean hasAnyRole(String token, String... requiredRoles) {
        if (token == null) return false;
        List<String> roles = getRolesFromToken(token);
        if (roles.isEmpty()) return false;
        for (String required : requiredRoles) {
            for (String r : roles) {
                if (r.equalsIgnoreCase(required) || r.equalsIgnoreCase(required.replace("ROLE_", ""))) {
                    return true;
                }
            }
        }
        return false;
    }

    // --------------------------------------------------------------

}
