package aes.filter;

import io.github.bucket4j.*;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;

//@WebFilter(filterName = "RateLimitFilter", urlPatterns = {"/*"})
public class RateLimitFilter implements Filter {

    // Protects memory by limiting cached users and expiring inactive ones
    private final Cache<String, Bucket> cache = Caffeine.newBuilder()
            .maximumSize(10_000) 
            .expireAfterAccess(Duration.ofMinutes(5)) 
            .build();

    private Bucket createNewBucket() {
        Bandwidth sustainedLimit = Bandwidth.classic(600, Refill.greedy(600, Duration.ofMinutes(1)));

        return Bucket.builder()
                .addLimit(sustainedLimit)
                .build();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        
        // Skip rate limiting for static files to avoid wasting user tokens
        if (uri.matches(".*\\.(css|js|jpg|jpeg|png|gif|ico|svg|woff|woff2)$")) {
            chain.doFilter(request, response);
            return; 
        }
        
        // Use Session ID instead of IP to prevent blocking shared networks (CGNAT)
        String clientKey = req.getSession(true).getId();

        Bucket bucket = cache.get(clientKey, k -> createNewBucket());

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            res.setStatus(429);
            res.getWriter().write("Too many requests. Please try again later.");
        }
    }

    @Override
    public void destroy() {
    }
}