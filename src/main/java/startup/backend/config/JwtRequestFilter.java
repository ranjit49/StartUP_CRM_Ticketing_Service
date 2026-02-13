package startup.backend.config;


import io.jsonwebtoken.Claims;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import startup.backend.util.JwtTokenUtil;
import startup.backend.Exception.JwtTokenException;
import startup.backend.Exception.JwtTokenExpiredException;
import startup.backend.Exception.JwtTokenParseException;

import java.io.IOException;
import java.util.Objects;

@RequiredArgsConstructor
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    //private final CustomUserDetailsServiceImpl customUserDetailsService; // Change to CustomUserDetailsServiceImpl


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String token = extractTokenFromRequest(request);
        try {
            if (Objects.nonNull(token) && isValidJwtFormat(token)) {
                String username = jwtTokenUtil.extractUsername(token);

//                if (request.getRequestURI().startsWith("/auth/")) {
//                    filterChain.doFilter(request, response);
//                    return;
//                }

                // Use a primitive boolean expression here
                boolean isTokenValid = jwtTokenUtil.validateToken(token, username);
                boolean isNoAuthentication = Objects.isNull(SecurityContextHolder.getContext().getAuthentication());

                if (isTokenValid && isNoAuthentication) {

                    //  keeping username, roles in security context holder.
                    // not checking with db for username validation.just checking secret key and expiry
                    // so no need to have user entity and repo in this app.
                    // if username needed a validation with db then expose an endpoint in user app and call it

                    Claims claims = jwtTokenUtil.extractAllClaims(token);
                    Long userId = claims.get("id", Long.class);

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userId, token, jwtTokenUtil.getAuthoritiesFromToken(token));  //Collection<? extends GrantedAuthority> authorities

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication); // Authentication ONLY


//                    // just extract claims like username ,roles keep in security context holder.
//
//                    CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(username);
//
//                    if (userDetails != null) {
//                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
//                                userDetails, null, userDetails.getAuthorities());
//                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//
//                        SecurityContextHolder.getContext().setAuthentication(authentication); // Authentication ONLY
//                        // setting in security context holder helps to get this authentication object anywhere from it
//                        // @hasRole("admin") on controller level checks roles in this security context holder only then allow
//                    }
                }
            }
        } catch (JwtTokenExpiredException ex) {
            String errorMessage = "JWT Token Expired: The token has expired. Expiration time: " + ex.getMessage();
            logger.error(errorMessage);
            sendErrorResponse(response, errorMessage, HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } catch (JwtTokenException | JwtTokenParseException ex) {
            logger.warn("JWT Token Error: " + ex.getMessage());
            sendErrorResponse(response, ex.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
            return;
        } catch (Exception ex) {
            logger.warn("Failed to authenticate user with the token: " + token, ex);
            sendErrorResponse(response, "Authentication Failed", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(request, response);
    }


    private void sendErrorResponse(HttpServletResponse response, String message, int statusCode) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\": \"" + message + "\"}");
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private boolean isValidJwtFormat(String token) {
        return token.chars().filter(ch -> ch == '.').count() == 2;
    }
}
