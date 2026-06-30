package br.com.agendafacilpro.security;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import br.com.agendafacilpro.service.LoginAttemptService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginBlockFilter extends OncePerRequestFilter {

    private final LoginAttemptService attempts;

    public LoginBlockFilter(LoginAttemptService attempts) {
        this.attempts = attempts;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (isLoginPost(request) && attempts.isBlocked(request.getParameter("email"), request.getRemoteAddr())) {
            response.sendRedirect(request.getContextPath() + "/login?blocked");
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean isLoginPost(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod()) && "/login".equals(request.getServletPath());
    }
}
