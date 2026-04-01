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

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // ✅ 1. Skip auth endpoints completely
        if (request.getRequestURI().startsWith("/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = extractTokenFromRequest(request);

            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                String username = jwtTokenUtil.extractUsername(token);

                if (jwtTokenUtil.validateToken(token, username)) {

                    Claims claims = jwtTokenUtil.extractAllClaims(token);

                    // ✅ extract userId safely
                    Long userId = null;
                    Object idObj = claims.get("id");
                    if (idObj instanceof Integer i) {
                        userId = i.longValue();
                    } else if (idObj instanceof Long l) {
                        userId = l;
                    }

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    username,   // ✅ principal
                                    null,
                                    jwtTokenUtil.getAuthoritiesFromToken(token) // MUST be ROLE_*
                            );

                    authentication.setDetails(jwtTokenUtil.extractAllClaims(token));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

        } catch (JwtTokenExpiredException ex) {
            sendErrorResponse(response, "JWT token expired", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } catch (Exception ex) {
            sendErrorResponse(response, "Invalid JWT token", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private void sendErrorResponse(HttpServletResponse response, String message, int status)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}