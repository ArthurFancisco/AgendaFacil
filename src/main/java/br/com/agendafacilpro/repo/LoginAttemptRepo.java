package br.com.agendafacilpro.repo;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.agendafacilpro.domain.LoginAttempt;

public interface LoginAttemptRepo extends JpaRepository<LoginAttempt, Long> {

    boolean existsByUsernameNormalizedAndBlockedUntilAfter(String usernameNormalized, LocalDateTime now);

    boolean existsByIpAddressAndBlockedUntilAfter(String ipAddress, LocalDateTime now);

    long countByUsernameNormalizedAndSuccessFalseAndAttemptedAtAfter(String usernameNormalized, LocalDateTime since);

    long countByIpAddressAndSuccessFalseAndAttemptedAtAfter(String ipAddress, LocalDateTime since);

    Optional<LoginAttempt> findTopByUsernameNormalizedAndSuccessTrueOrderByAttemptedAtDesc(String usernameNormalized);

    Optional<LoginAttempt> findTopByIpAddressAndSuccessTrueOrderByAttemptedAtDesc(String ipAddress);
}
