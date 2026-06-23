package br.com.agendafacilpro.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.agendafacilpro.domain.BookingAttempt;
import br.com.agendafacilpro.domain.Establishment;
import br.com.agendafacilpro.domain.EstablishmentSettings;
import br.com.agendafacilpro.repo.BookingAttemptRepo;
import br.com.agendafacilpro.util.PhoneNormalizer;

@Service
public class BookingGuardService {

    private static final Logger log = LoggerFactory.getLogger(BookingGuardService.class);
    private final BookingAttemptRepo attempts;
    private final EstablishmentSettingsService settingsService;

    public BookingGuardService(BookingAttemptRepo attempts, EstablishmentSettingsService settingsService) {
        this.attempts = attempts;
        this.settingsService = settingsService;
    }

    public String normalizePhone(String phone) {
        return PhoneNormalizer.normalize(phone);
    }

    /**
     * Aplica as barreiras antifraude da página pública antes de criar a reserva.
     * A decisão registra tentativas por estabelecimento, telefone e IP sem expor dados sensíveis no log.
     */
    @Transactional
    public Decision check(Establishment est, String phone, String ip, String honeypot) {
        String normalized = PhoneNormalizer.digitsWithoutBrazilCountryCode(phone);
        EstablishmentSettings settings = settingsService.forEstablishment(est);
        if (honeypot != null && !honeypot.isBlank()) {
            return block(est, normalized, ip, "Honeypot preenchido", "Não foi possível continuar agora. Fale com o estabelecimento.");
        }
        if (!PhoneNormalizer.isValidNormalized(normalized)) {
            return block(est, normalized, ip, "Telefone inválido", "Confira o número do WhatsApp e tente de novo.");
        }
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        if (attempts.countByEstablishmentIdAndPhoneNormalizedAndCreatedAtAfter(est.getId(), normalized, since) >= settings.getMaxAttemptsPerPhoneHour()) {
            return block(est, normalized, ip, "Limite por telefone", "Para marcar um novo horário, fale com o estabelecimento.");
        }
        if (attempts.countByEstablishmentIdAndIpAddressAndCreatedAtAfter(est.getId(), safe(ip), since) >= settings.getMaxAttemptsPerIpHour()) {
            return block(est, normalized, ip, "Limite por IP", "Não foi possível continuar agora. Fale com o estabelecimento.");
        }
        save(est, normalized, ip, false, "Aceita");
        return new Decision(true, "ok", normalized);
    }

    private Decision block(Establishment e, String phone, String ip, String reason, String message) {
        save(e, phone, ip, true, reason);
        log.warn("Tentativa suspeita: est={}, motivo={}", e.getId(), reason);
        return new Decision(false, message, phone);
    }

    private void save(Establishment e, String phone, String ip, boolean suspicious, String reason) {
        BookingAttempt a = new BookingAttempt();
        a.setEstablishment(e);
        a.setPhoneNormalized(phone);
        a.setIpAddress(safe(ip));
        a.setSuspicious(suspicious);
        a.setReason(reason);
        attempts.save(a);
    }

    private String safe(String ip) {
        return ip == null || ip.isBlank() ? "unknown" : ip;
    }

    public record Decision(boolean allowed, String message, String normalizedPhone) {

    }
}
