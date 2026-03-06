package org.example.shopuserservice.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Component
public class GatewayAuthFilter extends OncePerRequestFilter {

    @Value("${internal.internal-secret}")
    private String secret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger") ||
                path.startsWith("/api/internal/")) {
            filterChain.doFilter(request, response);
            return;
        }


        String userId = request.getHeader("X-User-Id");
        String rolesHeader = request.getHeader("X-Roles");
        String secretHeader = request.getHeader("X-Internal-Auth");

        if (!secret.equals(secretHeader)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        if (userId == null || rolesHeader == null || rolesHeader.isEmpty()) {
            AccessDeniedException ex = new AccessDeniedException("User has no roles");
            SecurityContextHolder.clearContext();
            throw ex;
        }

        List<? extends GrantedAuthority> authorities = Arrays
                .stream(rolesHeader.split(","))
                .map(SimpleGrantedAuthority::new)
                .toList();

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(Long.valueOf(userId), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(token);

        filterChain.doFilter(request, response);
    }
}
