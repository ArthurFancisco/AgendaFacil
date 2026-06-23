package br.com.agendafacilpro.repo;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.agendafacilpro.domain.BookingAttempt;

public interface BookingAttemptRepo extends JpaRepository<BookingAttempt, Long> {

    long countByEstablishmentIdAndPhoneNormalizedAndCreatedAtAfter(Long establishmentId, String phoneNormalized, LocalDateTime since);

    long countByEstablishmentIdAndIpAddressAndCreatedAtAfter(Long establishmentId, String ipAddress, LocalDateTime since);
}
