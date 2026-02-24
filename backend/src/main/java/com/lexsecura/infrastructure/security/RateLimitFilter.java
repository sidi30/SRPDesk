package com.lexsecura.infrastructure.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> cvdBuckets = new ConcurrentHashMap<>();
    private final boolean enabled;
    private final int requestsPerMinute;
    private final int cvdRequestsPerMinute;

    public RateLimitFilter(
            @Value("${app.rate-limit.enabled:false}") boolean enabled,
            @Value("${app.rate-limit.requests-per-minute:120}") int requestsPerMinute,
            @Value("${app.rate-limit.cvd-requests-per-minute:5}") int cvdRequestsPerMinute) {
        this.enabled = enabled;
        this.requestsPerMinute = requestsPerMinute;
        this.cvdRequestsPerMinute = cvdRequestsPerMinute;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);

        // Stricter rate limiting for public CVD submission (5 req/min/IP)
        if ("POST".equals(request.getMethod()) && request.getRequestURI().equals("/api/v1/cvd/reports")) {
            Bucket cvdBucket = cvdBuckets.computeIfAbsent(clientIp, k -> createCvdBucket());
            if (!cvdBucket.tryConsume(1)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/problem+json");
                response.getWriter().write("""
                    {"type":"https://lexsecura.com/problems/rate-limit","title":"Too Many Requests","status":429,"detail":"CVD submission rate limit exceeded (max 5/min). Try again later."}""");
                return;
            }
        }

        Bucket bucket = buckets.computeIfAbsent(clientIp, k -> createBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/problem+json");
            response.getWriter().write("""
                {"type":"https://lexsecura.com/problems/rate-limit","title":"Too Many Requests","status":429,"detail":"Rate limit exceeded. Try again later."}""");
        }
    }

    private Bucket createBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(requestsPerMinute, Refill.greedy(requestsPerMinute, Duration.ofMinutes(1))))
                .build();
    }

    private Bucket createCvdBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(cvdRequestsPerMinute, Refill.greedy(cvdRequestsPerMinute, Duration.ofMinutes(1))))
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
