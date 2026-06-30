package br.com.agendafacilpro.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import br.com.agendafacilpro.domain.LoginAttempt;
import br.com.agendafacilpro.repo.LoginAttemptRepo;

class LoginAttemptServiceTest {

    private final LoginAttemptRepo repo = mock(LoginAttemptRepo.class);
    private final LoginAttemptService service = new LoginAttemptService(repo);

    @Test
    void blocksAfterUsernameFailures() {
        when(repo.countByUsernameNormalizedAndSuccessFalseAndAttemptedAtAfter(eq("admin@demo.local"), any(LocalDateTime.class))).thenReturn(4L);
        when(repo.countByIpAddressAndSuccessFalseAndAttemptedAtAfter(eq("127.0.0.1"), any(LocalDateTime.class))).thenReturn(0L);

        boolean blocked = service.recordFailure("Admin@Demo.Local", "127.0.0.1", "JUnit");

        assertThat(blocked).isTrue();
    }

    @Test
    void blocksAfterIpFailures() {
        when(repo.countByUsernameNormalizedAndSuccessFalseAndAttemptedAtAfter(eq("admin@demo.local"), any(LocalDateTime.class))).thenReturn(0L);
        when(repo.countByIpAddressAndSuccessFalseAndAttemptedAtAfter(eq("127.0.0.1"), any(LocalDateTime.class))).thenReturn(9L);

        boolean blocked = service.recordFailure("admin@demo.local", "127.0.0.1", "JUnit");

        assertThat(blocked).isTrue();
    }

    @Test
    void successResetsWindowByCountingAfterLastSuccess() {
        LoginAttempt success = new LoginAttempt();
        success.setAttemptedAt(LocalDateTime.now().minusMinutes(1));
        when(repo.findTopByUsernameNormalizedAndSuccessTrueOrderByAttemptedAtDesc("admin@demo.local")).thenReturn(Optional.of(success));
        when(repo.countByUsernameNormalizedAndSuccessFalseAndAttemptedAtAfter(eq("admin@demo.local"), any(LocalDateTime.class))).thenReturn(0L);
        when(repo.countByIpAddressAndSuccessFalseAndAttemptedAtAfter(eq("127.0.0.1"), any(LocalDateTime.class))).thenReturn(0L);

        boolean blocked = service.recordFailure("admin@demo.local", "127.0.0.1", "JUnit");

        assertThat(blocked).isFalse();
    }

    @Test
    void detectsCurrentBlock() {
        when(repo.existsByUsernameNormalizedAndBlockedUntilAfter(eq("admin@demo.local"), any(LocalDateTime.class))).thenReturn(true);

        assertThat(service.isBlocked("admin@demo.local", "127.0.0.1")).isTrue();
    }
}
