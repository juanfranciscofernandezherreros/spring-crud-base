package com.example.crudbase.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

/**
 * Logs every HTTP request as a single structured JSON line under the
 * "http.access" logger. Alloy already ships container stdout to Loki, so
 * this line alone is enough to power the "HTTP Access Log" Grafana panel
 * without any extra log-shipping configuration.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger ACCESS_LOG = LoggerFactory.getLogger("http.access");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        long startedAt = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            ObjectNode entry = OBJECT_MAPPER.createObjectNode();
            entry.put("type", "http-access");
            entry.put("timestamp", Instant.now().toString());
            entry.put("method", request.getMethod());
            entry.put("path", request.getRequestURI());
            entry.put("query", request.getQueryString());
            entry.put("status", response.getStatus());
            entry.put("durationMs", System.currentTimeMillis() - startedAt);
            entry.put("remoteAddr", request.getRemoteAddr());
            ACCESS_LOG.info(entry.toString());
        }
    }
}
