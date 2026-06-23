package br.com.agendafacilpro.repo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.agendafacilpro.domain.TimeBlock;

public interface TimeBlockRepo extends JpaRepository<TimeBlock, Long> {

    List<TimeBlock> findTop20ByEstablishmentIdOrderByStartAtDesc(Long establishmentId);

    Optional<TimeBlock> findByIdAndEstablishmentId(Long id, Long establishmentId);

    @Query("""
    select count(b)>0 from TimeBlock b
    where b.establishment.id=:establishmentId and b.active=true
      and (b.professional is null or b.professional.id=:professionalId)
      and b.startAt<:endAt and b.endAt>:startAt
  """)
    boolean existsOverlap(@Param("establishmentId") Long establishmentId, @Param("professionalId") Long professionalId, @Param("startAt") LocalDateTime startAt, @Param("endAt") LocalDateTime endAt);
}
