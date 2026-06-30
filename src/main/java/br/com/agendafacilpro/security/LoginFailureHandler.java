package br.com.agendafacilpro.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import br.com.agendafacilpro.service.LoginAttemptService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final LoginAttemptService attempts;

    public LoginFailureHandler(LoginAttemptService attempts) {
        this.attempts = attempts;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        boolean blocked = attempts.recordFailure(request.getParameter("email"), request.getRemoteAddr(), request.getHeader("User-Agent"));
        response.sendRedirect(request.getContextPath() + (blocked ? "/login?blocked" : "/login?error"));
    }
}
