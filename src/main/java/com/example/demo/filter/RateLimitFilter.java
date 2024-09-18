package com.example.demo.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

public class RateLimitFilter implements Filter {

    private final Bucket bucket;
    private final Instant bucketCreationTime;

    public RateLimitFilter() {
        Bandwidth limit = Bandwidth.classic(1, Refill.greedy(1, Duration.ofMinutes(1)));
        this.bucket = Bucket4j.builder().addLimit(limit).build();
        this.bucketCreationTime = Instant.now();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (httpRequest.getMethod().equalsIgnoreCase("POST")) {
            if (bucket.tryConsume(1)) {
                chain.doFilter(request, response);
            } else {
                long elapsedSeconds = ChronoUnit.SECONDS.between(bucketCreationTime, Instant.now());
                long waitForRefill = 60 - (elapsedSeconds % 60);
                httpResponse.setStatus(TOO_MANY_REQUESTS.value());
                httpResponse.getWriter().write("Too many requests, please wait " + waitForRefill + " seconds");
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization code, if needed
    }

    @Override
    public void destroy() {
        // Cleanup code, if needed
    }
}