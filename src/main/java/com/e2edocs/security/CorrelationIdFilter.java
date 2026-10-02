package com.e2edocs.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String MDC_CORRELATION_KEY = "correlationId";
    private static final Pattern SAFE_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{1,64}$");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String correlationId = extractOrGenerateCorrelationId(request);

        MDC.put(MDC_CORRELATION_KEY, correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_CORRELATION_KEY);
        }
    }

    private String extractOrGenerateCorrelationId(HttpServletRequest request) {
        String headerVal = request.getHeader(CORRELATION_ID_HEADER);
        if (headerVal == null || headerVal.isBlank()) {
            headerVal = request.getHeader(REQUEST_ID_HEADER);
        }

        if (headerVal != null && !headerVal.isBlank()) {
            String trimmed = headerVal.trim();
            if (SAFE_ID_PATTERN.matcher(trimmed).matches()) {
                return trimmed;
            }
        }

        return UUID.randomUUID().toString();
    }
}
