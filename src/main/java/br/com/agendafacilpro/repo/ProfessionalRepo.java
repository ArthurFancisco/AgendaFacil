package br.com.agendafacilpro.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.agendafacilpro.domain.Professional;

public interface ProfessionalRepo extends JpaRepository<Professional, Long> {

    List<Professional> findByEstablishmentIdAndActiveTrueOrderBySortOrderAscNameAsc(Long establishmentId);

    List<Professional> findByEstablishmentIdOrderByActiveDescSortOrderAscNameAsc(Long establishmentId);

    Optional<Professional> findByIdAndEstablishmentId(Long id, Long establishmentId);
}
