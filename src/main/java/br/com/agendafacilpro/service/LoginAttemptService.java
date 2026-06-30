package br.com.agendafacilpro.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.agendafacilpro.domain.LoginAttempt;
import br.com.agendafacilpro.repo.LoginAttemptRepo;

@Service
public class LoginAttemptService {

    static final int MAX_FAILURES_PER_USERNAME = 5;
    static final int MAX_FAILURES_PER_IP = 10;
    static final int WINDOW_MINUTES = 15;
    static final int BLOCK_MINUTES = 15;

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);

    private final LoginAttemptRepo attempts;

    public LoginAttemptService(LoginAttemptRepo attempts) {
        this.attempts = attempts;
    }

    @Transactional(readOnly = true)
    public boolean isBlocked(String username, String ip) {
        LocalDateTime now = LocalDateTime.now();
        String normalized = normalizeUsername(username);
        String safeIp = safeIp(ip);
        return normalized != null && attempts.existsByUsernameNormalizedAndBlockedUntilAfter(normalized, now)
                || attempts.existsByIpAddressAndBlockedUntilAfter(safeIp, now);
    }

    @Transactional
    public boolean recordFailure(String username, String ip, String userAgent) {
        LocalDateTime now = LocalDateTime.now();
        String normalized = normalizeUsername(username);
        String safeIp = safeIp(ip);
        LocalDateTime since = now.minusMinutes(WINDOW_MINUTES);
        LocalDateTime usernameSince = lastSuccessForUsername(normalized, since);
        LocalDateTime ipSince = lastSuccessForIp(safeIp, since);

        long userFailures = normalized == null ? 0 : attempts.countByUsernameNormalizedAndSuccessFalseAndAttemptedAtAfter(normalized, usernameSince);
        long ipFailures = attempts.countByIpAddressAndSuccessFalseAndAttemptedAtAfter(safeIp, ipSince);
        boolean blocked = normalized != null && userFailures + 1 >= MAX_FAILURES_PER_USERNAME || ipFailures + 1 >= MAX_FAILURES_PER_IP;

        LoginAttempt attempt = newAttempt(normalized, safeIp, userAgent, false);
        if (blocked) {
            attempt.setBlockedUntil(now.plusMinutes(BLOCK_MINUTES));
            log.warn("Login administrativo bloqueado temporariamente: user={}, ip={}", mask(normalized), safeIp);
        }
        attempts.save(attempt);
        return blocked;
    }

    @Transactional
    public void recordSuccess(String username, String ip, String userAgent) {
        attempts.save(newAttempt(normalizeUsername(username), safeIp(ip), userAgent, true));
    }

    private LocalDateTime lastSuccessForUsername(String username, LocalDateTime fallback) {
        if (username == null) {
            return fallback;
        }
        return attempts.findTopByUsernameNormalizedAndSuccessTrueOrderByAttemptedAtDesc(username)
                .map(LoginAttempt::getAttemptedAt)
                .filter(time -> time.isAfter(fallback))
                .orElse(fallback);
    }

    private LocalDateTime lastSuccessForIp(String ip, LocalDateTime fallback) {
        return attempts.findTopByIpAddressAndSuccessTrueOrderByAttemptedAtDesc(ip)
                .map(LoginAttempt::getAttemptedAt)
                .filter(time -> time.isAfter(fallback))
                .orElse(fallback);
    }

    private LoginAttempt newAttempt(String username, String ip, String userAgent, boolean success) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setUsernameNormalized(username);
        attempt.setIpAddress(ip);
        attempt.setUserAgent(limit(userAgent, 255));
        attempt.setSuccess(success);
        return attempt;
    }

    private String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return limit(username.trim().toLowerCase(), 160);
    }

    private String safeIp(String ip) {
        return ip == null || ip.isBlank() ? "unknown" : limit(ip.trim(), 80);
    }

    private String limit(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

    private String mask(String username) {
        if (username == null || username.length() < 3) {
            return "***";
        }
        return username.substring(0, 2) + "***";
    }
}
