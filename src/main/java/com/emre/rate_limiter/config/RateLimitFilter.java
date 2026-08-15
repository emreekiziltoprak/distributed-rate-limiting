package com.emre.rate_limiter.config;

import com.emre.rate_limiter.service.RateLimitingService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RateLimitFilter implements Filter {

    private final RateLimitingService rateLimitingService;

    public RateLimitFilter(RateLimitingService rateLimitingService){
        this.rateLimitingService = rateLimitingService;
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest  httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();

        if(requestURI.contains("/api/")){
            String clientIp = httpRequest.getRemoteAddr();
            boolean allowed = rateLimitingService.allowRequest(clientIp);

            if(!allowed) {
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setContentType("application/json");
            httpResponse.setCharacterEncoding("UTF-8");
            httpResponse.getWriter().write("{\"error\": \"Too Many Requests\", \"message\": \"Rate limit exceeded. Try again later.\"}");
            return;
            }
        }

        chain.doFilter(request, response);
    }

}
