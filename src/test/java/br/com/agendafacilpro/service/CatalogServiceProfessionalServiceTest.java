package br.com.agendafacilpro.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import br.com.agendafacilpro.domain.Establishment;
import br.com.agendafacilpro.domain.Professional;
import br.com.agendafacilpro.domain.ServiceItem;
import br.com.agendafacilpro.repo.EstablishmentRepo;
import br.com.agendafacilpro.repo.ProfessionalRepo;
import br.com.agendafacilpro.repo.ServiceItemRepo;

class CatalogServiceProfessionalServiceTest {

    private final EstablishmentRepo establishments = mock(EstablishmentRepo.class);
    private final ServiceItemRepo services = mock(ServiceItemRepo.class);
    private final ProfessionalRepo professionals = mock(ProfessionalRepo.class);
    private final CatalogService catalog = new CatalogService(establishments, services, professionals);

    @Test
    void publicCatalogListsOnlyProfessionalsLinkedToService() {
        Establishment establishment = establishment();
        ServiceItem service = service(establishment);
        Professional linked = professional(establishment, 3L, "Profissional vinculado");

        when(services.findByIdAndEstablishmentId(2L, 1L)).thenReturn(Optional.of(service));
        when(professionals.findActiveQualified(1L, 2L)).thenReturn(List.of(linked));

        assertThat(catalog.professionalsForService(1L, 2L))
                .extracting(Professional::getName)
                .containsExactly("Profissional vinculado");
    }

    private Establishment establishment() {
        Establishment establishment = new Establishment();
        establishment.setId(1L);
        establishment.setName("AgendaFácil Demo");
        establishment.setSlug("agenda-demo");
        establishment.setWhatsapp("5517999999999");
        return establishment;
    }

    private ServiceItem service(Establishment establishment) {
        ServiceItem service = new ServiceItem();
        service.setId(2L);
        service.setEstablishment(establishment);
        service.setName("Atendimento Completo");
        service.setDurationMinutes(60);
        service.setActive(true);
        return service;
    }

    private Professional professional(Establishment establishment, Long id, String name) {
        Professional professional = new Professional();
        professional.setId(id);
        professional.setEstablishment(establishment);
        professional.setName(name);
        professional.setActive(true);
        return professional;
    }
}
