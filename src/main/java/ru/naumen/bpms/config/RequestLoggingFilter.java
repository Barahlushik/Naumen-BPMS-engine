package ru.naumen.bpms.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Principal;

@Component
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long startedAt = System.currentTimeMillis();
        String method = request.getMethod();
        String path = request.getRequestURI();
        String query = request.getQueryString();
        String remoteAddress = request.getRemoteAddr();
        String username = getUsername(request);

        log.info("HTTP request started. method={}, path={}, query={}, remoteAddress={}, user={}",
                method, path, query, remoteAddress, username);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long elapsedMs = System.currentTimeMillis() - startedAt;
            int status = response.getStatus();

            if (status >= 500) {
                log.error("HTTP request completed with server error. method={}, path={}, status={}, elapsedMs={}, user={}",
                        method, path, status, elapsedMs, username);
            } else if (status >= 400) {
                log.warn("HTTP request completed with client error. method={}, path={}, status={}, elapsedMs={}, user={}",
                        method, path, status, elapsedMs, username);
            } else {
                log.info("HTTP request completed. method={}, path={}, status={}, elapsedMs={}, user={}",
                        method, path, status, elapsedMs, username);
            }
        }
    }

    private String getUsername(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        return principal != null ? principal.getName() : "anonymous";
    }
}

