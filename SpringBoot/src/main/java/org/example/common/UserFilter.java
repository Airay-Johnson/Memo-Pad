package org.example.common;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UserFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String userId = req.getHeader("X-User-Id");
        if (userId != null && !userId.isBlank()) {
            UserContext.setUserId(Integer.parseInt(userId));
        }
        try {
            chain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}
