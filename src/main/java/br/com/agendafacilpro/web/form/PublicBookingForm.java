package br.com.agendafacilpro.web.form;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PublicBookingForm(
        @NotNull(message = "Escolha um serviço.")
        Long serviceId,
        @NotNull(message = "Escolha um profissional.")
        Long professionalId,
        @NotNull(message = "Escolha a data.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate date,
        @NotNull(message = "Escolha o horário.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
        LocalTime time,
        @NotBlank(message = "Informe seu nome.")
        @Size(max = 120, message = "Informe um nome menor.")
        String customerName,
        @NotBlank(message = "Informe seu WhatsApp.")
        @Size(min = 10, max = 20, message = "O WhatsApp informado parece inválido.")
        @Pattern(regexp = "^[0-9()\\s+\\-.]{10,20}$", message = "O WhatsApp informado parece inválido.")
        String customerPhone,
        @Size(max = 120, message = "Verifique os dados e tente novamente.")
        String website
) {
    public boolean hasTarget() {
        return serviceId != null && professionalId != null && date != null && time != null;
    }
}
