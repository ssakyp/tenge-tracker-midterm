package com.tengetracker.security;

import com.tengetracker.model.User;
import com.tengetracker.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Runs once per request before Spring Security's authorization checks.
 * Reads "Authorization: Bearer <jwt>", verifies it and, if valid, puts an {@link AuthUser}
 * into the SecurityContext. It never rejects a request itself: if the token is missing or
 * invalid the request continues anonymously, and the authorization rules in
 * {@link SecurityConfig} decide whether the endpoint is public (OK) or protected (401).
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Request attribute used to pass the reason for a rejected token to the 401 handler. */
    public static final String AUTH_ERROR_ATTRIBUTE = "tengetracker.authError";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length()).trim();
            try {
                Claims claims = jwtService.parse(token);
                Long userId = Long.valueOf(claims.getSubject());
                Integer version = claims.get(JwtService.CLAIM_VERSION, Integer.class);
                Optional<User> user = userRepository.findById(userId);

                if (user.isEmpty()) {
                    request.setAttribute(AUTH_ERROR_ATTRIBUTE, "The user of this token no longer exists");
                } else if (version == null || !version.equals(user.get().getTokenVersion())) {
                    request.setAttribute(AUTH_ERROR_ATTRIBUTE, "Token was revoked because the password changed. Please log in again");
                } else {
                    AuthUser principal = new AuthUser(user.get().getId(), user.get().getUsername());
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            principal, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (ExpiredJwtException e) {
                request.setAttribute(AUTH_ERROR_ATTRIBUTE, "Token has expired. Please log in again");
            } catch (JwtException | IllegalArgumentException e) {
                request.setAttribute(AUTH_ERROR_ATTRIBUTE, "Invalid token");
            }
        }

        chain.doFilter(request, response);
    }
}
