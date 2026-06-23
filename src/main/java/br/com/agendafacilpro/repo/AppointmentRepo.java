package br.com.agendafacilpro.repo;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.agendafacilpro.domain.Appointment;
import br.com.agendafacilpro.domain.AppointmentStatus;

public interface AppointmentRepo extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByIdAndEstablishmentId(Long id, Long establishmentId);

    long countByEstablishmentIdAndStartAtBetween(Long id, LocalDateTime start, LocalDateTime end);

    long countByEstablishmentIdAndStatusAndStartAtBetween(Long id, AppointmentStatus status, LocalDateTime start, LocalDateTime end);

    long countByEstablishmentIdAndStatusInAndStartAtBetween(Long id, Collection<AppointmentStatus> statuses, LocalDateTime start, LocalDateTime end);

    List<Appointment> findByEstablishmentIdAndStatusOrderByStartAtAsc(Long id, AppointmentStatus status);

    List<Appointment> findByEstablishmentIdAndStartAtBetweenOrderByStartAtAsc(Long id, LocalDateTime start, LocalDateTime end);

    List<Appointment> findByEstablishmentIdAndStatusInAndStartAtBetweenOrderByStartAtAsc(Long id, Collection<AppointmentStatus> statuses, LocalDateTime start, LocalDateTime end);

    List<Appointment> findTop20ByEstablishmentIdAndStatusInOrderByStartAtDesc(Long id, Collection<AppointmentStatus> statuses);

    List<Appointment> findByEstablishmentIdAndStatusAndStartAtBefore(Long id, AppointmentStatus status, LocalDateTime now);

    @Query("""
    select count(a)>0 from Appointment a
    where a.establishment.id=:establishmentId and a.professional.id=:professionalId
      and a.status in :statuses and a.startAt<:endAt and a.endAt>:startAt
      and (:ignoreId is null or a.id<>:ignoreId)
  """)
    boolean existsBlockingOverlap(@Param("establishmentId") Long establishmentId, @Param("professionalId") Long professionalId, @Param("startAt") LocalDateTime startAt, @Param("endAt") LocalDateTime endAt, @Param("statuses") Collection<AppointmentStatus> statuses, @Param("ignoreId") Long ignoreId);
}
