package br.com.agendafacilpro.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.agendafacilpro.domain.BookingAttempt;
import br.com.agendafacilpro.domain.Establishment;
import br.com.agendafacilpro.domain.EstablishmentSettings;
import br.com.agendafacilpro.repo.BookingAttemptRepo;

class BookingGuardServiceSettingsTest {

    private final BookingAttemptRepo attempts = mock(BookingAttemptRepo.class);
    private final FakeSettingsService settingsService = new FakeSettingsService();
    private final BookingGuardService service = new BookingGuardService(attempts, settingsService);
    private Establishment establishment;

    @BeforeEach
    void setUp() {
        establishment = new Establishment();
        establishment.setId(1L);
        establishment.setName("Teste");
        settingsService.settings = EstablishmentSettings.defaultsFor(establishment);
    }

    @Test
    void phoneAttemptLimitUsesEstablishmentSettings() {
        settingsService.settings.setMaxAttemptsPerPhoneHour(2);
        when(attempts.countByEstablishmentIdAndPhoneNormalizedAndCreatedAtAfter(eq(1L), eq("17988887777"), any(LocalDateTime.class))).thenReturn(2L);

        BookingGuardService.Decision decision = service.check(establishment, "(17) 98888-7777", "127.0.0.1", "");

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.message()).contains("fale com o estabelecimento");
    }

    @Test
    void ipAttemptLimitUsesEstablishmentSettings() {
        settingsService.settings.setMaxAttemptsPerIpHour(2);
        when(attempts.countByEstablishmentIdAndIpAddressAndCreatedAtAfter(eq(1L), eq("127.0.0.1"), any(LocalDateTime.class))).thenReturn(2L);

        BookingGuardService.Decision decision = service.check(establishment, "(17) 98888-7777", "127.0.0.1", "");

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.message()).contains("Não foi possível continuar agora");
    }

    @Test
    void honeypotBlocksAndRecordsSuspiciousAttempt() {
        BookingGuardService.Decision decision = service.check(establishment, "(17) 98888-7777", "127.0.0.1", "filled");

        assertThat(decision.allowed()).isFalse();
        verify(attempts).save(any(BookingAttempt.class));
    }

    private static class FakeSettingsService extends EstablishmentSettingsService {
        private EstablishmentSettings settings;

        FakeSettingsService() {
            super(null);
        }

        @Override
        public EstablishmentSettings forEstablishment(Establishment establishment) {
            return settings;
        }
    }
}
