package br.com.agendafacilpro.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import br.com.agendafacilpro.service.LoginAttemptService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final LoginAttemptService attempts;

    public LoginSuccessHandler(LoginAttemptService attempts) {
        this.attempts = attempts;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        attempts.recordSuccess(authentication.getName(), request.getRemoteAddr(), request.getHeader("User-Agent"));
        response.sendRedirect(request.getContextPath() + "/panel");
    }
}
