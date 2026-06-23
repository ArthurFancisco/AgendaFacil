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

    Optional<Appointment> findByEstablishmentIdAndPublicToken(Long establishmentId, String publicToken);

    boolean existsByPublicToken(String publicToken);

    long countByEstablishmentIdAndStartAtBetween(Long id, LocalDateTime start, LocalDateTime end);

    long countByEstablishmentIdAndStatusAndStartAtBetween(Long id, AppointmentStatus status, LocalDateTime start, LocalDateTime end);

    long countByEstablishmentIdAndStatusInAndStartAtBetween(Long id, Collection<AppointmentStatus> statuses, LocalDateTime start, LocalDateTime end);

    List<Appointment> findByEstablishmentIdAndStatusOrderByStartAtAsc(Long id, AppointmentStatus status);

    List<Appointment> findByEstablishmentIdAndStartAtBetweenOrderByStartAtAsc(Long id, LocalDateTime start, LocalDateTime end);

    List<Appointment> findByEstablishmentIdAndStatusInAndStartAtBetweenOrderByStartAtAsc(Long id, Collection<AppointmentStatus> statuses, LocalDateTime start, LocalDateTime end);

    List<Appointment> findTop20ByEstablishmentIdAndStatusInOrderByStartAtDesc(Long id, Collection<AppointmentStatus> statuses);

    List<Appointment> findByEstablishmentIdAndStatusAndStartAtBefore(Long id, AppointmentStatus status, LocalDateTime now);

    List<Appointment> findByEstablishmentIdAndStatusAndCreatedAtBefore(Long id, AppointmentStatus status, LocalDateTime createdBefore);

    @Query("""
    select count(a)>0 from Appointment a
    where a.establishment.id=:establishmentId and a.professional.id=:professionalId
      and a.status in :statuses and a.startAt<:endAt and a.endAt>:startAt
      and (:ignoreId is null or a.id<>:ignoreId)
  """)
    boolean existsBlockingOverlap(@Param("establishmentId") Long establishmentId, @Param("professionalId") Long professionalId, @Param("startAt") LocalDateTime startAt, @Param("endAt") LocalDateTime endAt, @Param("statuses") Collection<AppointmentStatus> statuses, @Param("ignoreId") Long ignoreId);

    @Query("""
    select count(a) from Appointment a
    where a.establishment.id=:establishmentId
      and a.customer.phoneNormalized=:phoneNormalized
      and a.status in :statuses
      and a.startAt>:now
  """)
    long countFutureByPhone(@Param("establishmentId") Long establishmentId, @Param("phoneNormalized") String phoneNormalized, @Param("statuses") Collection<AppointmentStatus> statuses, @Param("now") LocalDateTime now);

    @Query("""
    select a from Appointment a
    where a.establishment.id=:establishmentId
      and a.startAt>=:startAt and a.startAt<:endAt
      and (:professionalId is null or a.professional.id=:professionalId)
      and (:status is null or a.status=:status)
    order by a.startAt asc
  """)
    List<Appointment> findAgenda(@Param("establishmentId") Long establishmentId, @Param("startAt") LocalDateTime startAt, @Param("endAt") LocalDateTime endAt, @Param("professionalId") Long professionalId, @Param("status") AppointmentStatus status);
}
