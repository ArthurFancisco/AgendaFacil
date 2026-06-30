package br.com.agendafacilpro.web.form;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TimeBlockForm(
        @NotBlank(message = "Informe o título do bloqueio.")
        @Size(max = 120, message = "Informe um título menor.")
        String title,
        Long professionalId,
        @NotNull(message = "Informe o início do bloqueio.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime startAt,
        @NotNull(message = "Informe o fim do bloqueio.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime endAt,
        @Size(max = 255, message = "O motivo está muito longo.")
        String reason
) {
}
