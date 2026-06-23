package br.com.agendafacilpro.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.agendafacilpro.domain.Establishment;
import br.com.agendafacilpro.domain.EstablishmentSettings;
import br.com.agendafacilpro.repo.EstablishmentSettingsRepo;

@Service
public class EstablishmentSettingsService {

    private final EstablishmentSettingsRepo settingsRepo;

    public EstablishmentSettingsService(EstablishmentSettingsRepo settingsRepo) {
        this.settingsRepo = settingsRepo;
    }

    @Transactional
    public EstablishmentSettings forEstablishment(Establishment establishment) {
        return settingsRepo.findByEstablishmentId(establishment.getId())
                .orElseGet(() -> settingsRepo.save(EstablishmentSettings.defaultsFor(establishment)));
    }

    @Transactional(readOnly = true)
    public EstablishmentSettings forEstablishmentId(Long establishmentId) {
        return settingsRepo.findByEstablishmentId(establishmentId)
                .orElseGet(() -> {
                    Establishment establishment = new Establishment();
                    establishment.setId(establishmentId);
                    return EstablishmentSettings.defaultsFor(establishment);
                });
    }

    @Transactional
    public EstablishmentSettings update(Establishment establishment, EstablishmentSettingsForm form) {
        EstablishmentSettings settings = forEstablishment(establishment);
        settings.setNewClientRequiresApproval(form.newClientRequiresApproval());
        settings.setPendingExpirationMinutes(clamp(form.pendingExpirationMinutes(), 5, 1440));
        settings.setMaxFutureAppointmentsPerPhone(clamp(form.maxFutureAppointmentsPerPhone(), 1, 20));
        settings.setMaxAttemptsPerPhoneHour(clamp(form.maxAttemptsPerPhoneHour(), 1, 50));
        settings.setMaxAttemptsPerIpHour(clamp(form.maxAttemptsPerIpHour(), 1, 200));
        settings.setNoShowCountForManualApproval(clamp(form.noShowCountForManualApproval(), 1, 20));
        settings.setNoShowCountForBlock(clamp(form.noShowCountForBlock(), 1, 20));
        settings.setMinHoursBeforeClientCancel(clamp(form.minHoursBeforeClientCancel(), 0, 168));
        settings.setLongServiceManualApprovalMinutes(clamp(form.longServiceManualApprovalMinutes(), 15, 480));
        settings.setShowPricesOnPublicPage(form.showPricesOnPublicPage());
        return settingsRepo.save(settings);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
