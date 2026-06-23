package br.com.agendafacilpro.service;

import org.springframework.stereotype.Service;

import br.com.agendafacilpro.domain.AppUser;
import br.com.agendafacilpro.domain.Appointment;
import br.com.agendafacilpro.domain.AppointmentAudit;
import br.com.agendafacilpro.repo.AppointmentAuditRepo;

@Service
public class AppointmentAuditService {

    private final AppointmentAuditRepo audits;

    public AppointmentAuditService(AppointmentAuditRepo audits) {
        this.audits = audits;
    }

    /**
     * Registra a trilha de decisao administrativa sem gravar dados sensiveis no log da aplicacao.
     */
    public void record(Appointment appointment, AppUser user, String action, String details) {
        AppointmentAudit audit = new AppointmentAudit();
        audit.setEstablishment(appointment.getEstablishment());
        audit.setAppointment(appointment);
        audit.setUser(user);
        audit.setAction(action);
        audit.setDetails(limit(details));
        audits.save(audit);
    }

    private String limit(String details) {
        if (details == null || details.isBlank()) {
            return null;
        }
        String trimmed = details.trim();
        return trimmed.length() <= 500 ? trimmed : trimmed.substring(0, 500);
    }
}
