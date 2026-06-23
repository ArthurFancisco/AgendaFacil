package br.com.agendafacilpro.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.agendafacilpro.domain.AppointmentAudit;

public interface AppointmentAuditRepo extends JpaRepository<AppointmentAudit, Long> {
}
