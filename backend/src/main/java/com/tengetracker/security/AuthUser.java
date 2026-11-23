package com.tengetracker.security;

/**
 * The authenticated principal stored in Spring's SecurityContext after the JWT is verified.
 * Controllers receive it with {@code @AuthenticationPrincipal AuthUser me}.
 */
public record AuthUser(Long id, String username) {
}
