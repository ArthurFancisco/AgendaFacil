package br.com.agendafacilpro.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;

import br.com.agendafacilpro.domain.Appointment;
import br.com.agendafacilpro.domain.AppointmentStatus;
import br.com.agendafacilpro.domain.Establishment;
import br.com.agendafacilpro.domain.EstablishmentSettings;
import br.com.agendafacilpro.domain.Professional;
import br.com.agendafacilpro.domain.ServiceItem;
import br.com.agendafacilpro.service.AppointmentService;
import br.com.agendafacilpro.service.CatalogService;
import br.com.agendafacilpro.service.EstablishmentSettingsService;

class PublicBookingControllerFlowTest {

    private final Establishment establishment = establishment();
    private final ServiceItem service = service(establishment);
    private final Professional professional = professional(establishment, service);
    private final FakeCatalogService catalog = new FakeCatalogService(establishment, service, professional);
    private final FakeAppointmentService appointments = new FakeAppointmentService();
    private final PublicBookingController controller = new PublicBookingController(catalog, appointments, new FakeSettingsService());

    @Test
    void reviewStepDoesNotPersistAppointment() {
        String view = controller.review(
                "agenda-demo",
                2L,
                3L,
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0),
                "Ana Cliente",
                "(17) 98888-7777",
                "",
                new ExtendedModelMap()
        );

        assertThat(view).isEqualTo("public/confirm");
        assertThat(appointments.validated).isEqualTo(1);
        assertThat(appointments.created).isZero();
    }

    @Test
    void finalConfirmationPersistsAppointment() {
        String view = controller.confirm(
                "agenda-demo",
                2L,
                3L,
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0),
                "Ana Cliente",
                "(17) 98888-7777",
                "",
                new MockHttpServletRequest(),
                new ExtendedModelMap()
        );

        assertThat(view).isEqualTo("redirect:/agenda/agenda-demo/sucesso/public-token-123456789012");
        assertThat(appointments.created).isEqualTo(1);
    }

    private static Establishment establishment() {
        Establishment establishment = new Establishment();
        establishment.setId(1L);
        establishment.setName("AgendaFácil Demo");
        establishment.setSlug("agenda-demo");
        establishment.setWhatsapp("5517999999999");
        return establishment;
    }

    private static ServiceItem service(Establishment establishment) {
        ServiceItem service = new ServiceItem();
        service.setId(2L);
        service.setEstablishment(establishment);
        service.setName("Atendimento Completo");
        service.setDurationMinutes(60);
        service.setActive(true);
        return service;
    }

    private static Professional professional(Establishment establishment, ServiceItem service) {
        Professional professional = new Professional();
        professional.setId(3L);
        professional.setEstablishment(establishment);
        professional.setName("Profissional 1");
        professional.setActive(true);
        professional.getServices().add(service);
        return professional;
    }

    private static class FakeCatalogService extends CatalogService {
        private final Establishment establishment;
        private final ServiceItem service;
        private final Professional professional;

        FakeCatalogService(Establishment establishment, ServiceItem service, Professional professional) {
            super(null, null, null);
            this.establishment = establishment;
            this.service = service;
            this.professional = professional;
        }

        @Override
        public Establishment establishment(String slug) {
            return establishment;
        }

        @Override
        public ServiceItem service(Long id, Long est) {
            return service;
        }

        @Override
        public Professional professionalForService(Long id, Long serviceId, Long est) {
            return professional;
        }

        @Override
        public List<Professional> professionalsForService(Long est, Long serviceId) {
            return List.of(professional);
        }
    }

    private static class FakeAppointmentService extends AppointmentService {
        private int validated;
        private int created;

        FakeAppointmentService() {
            super(null, null, null, null, null, null, null, null, null);
        }

        @Override
        public void validatePublicSlot(Establishment est, Long serviceId, Long professionalId, LocalDate date, LocalTime time) {
            validated++;
        }

        @Override
        public Appointment create(Establishment est, Long serviceId, Long professionalId, LocalDate date, LocalTime time, String name, String phone, String ip, String honeypot) {
            created++;
            Appointment appointment = new Appointment();
            appointment.setPublicToken("public-token-123456789012");
            appointment.setStatus(AppointmentStatus.CONFIRMED);
            return appointment;
        }
    }

    private static class FakeSettingsService extends EstablishmentSettingsService {
        FakeSettingsService() {
            super(null);
        }

        @Override
        public EstablishmentSettings forEstablishment(Establishment establishment) {
            return EstablishmentSettings.defaultsFor(establishment);
        }
    }
}
