package br.com.agendafacilpro.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

import org.junit.jupiter.api.Test;

import br.com.agendafacilpro.service.ManualAppointmentRequest;
import br.com.agendafacilpro.web.form.ProfessionalForm;
import br.com.agendafacilpro.web.form.PublicBookingForm;
import br.com.agendafacilpro.web.form.ServiceForm;
import br.com.agendafacilpro.web.form.TimeBlockForm;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class FormValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsEmptyCustomerName() {
        PublicBookingForm form = new PublicBookingForm(2L, 3L, LocalDate.now().plusDays(1), LocalTime.of(9, 0), "", "(17) 98888-7777", "");

        assertThat(messages(validator.validate(form))).contains("Informe seu nome.");
    }

    @Test
    void rejectsInvalidPhone() {
        ManualAppointmentRequest request = new ManualAppointmentRequest("Ana", "abc", 2L, 3L, LocalDate.now().plusDays(1), LocalTime.of(9, 0), null, false);

        assertThat(messages(validator.validate(request))).contains("O WhatsApp informado parece inválido.");
    }

    @Test
    void rejectsHugeDescriptionInvalidDurationAndNegativePrice() {
        ServiceForm form = new ServiceForm("Corte", 0, new BigDecimal("-1.00"), "x".repeat(501), true);

        assertThat(messages(validator.validate(form)))
                .contains("A duração mínima é de 15 minutos.", "O preço não pode ser negativo.", "A descrição está muito longa.");
    }

    @Test
    void rejectsHugeProfessionalBio() {
        ProfessionalForm form = new ProfessionalForm("Ana", "x".repeat(501), "", null, true);

        assertThat(messages(validator.validate(form))).contains("A apresentação está muito longa.");
    }

    @Test
    void rejectsHugeBlockReason() {
        TimeBlockForm form = new TimeBlockForm("Almoço", null, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2), "x".repeat(256));

        assertThat(messages(validator.validate(form))).contains("O motivo está muito longo.");
    }

    private Set<String> messages(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream().map(ConstraintViolation::getMessage).collect(java.util.stream.Collectors.toSet());
    }
}
