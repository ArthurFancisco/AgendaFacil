package br.com.agendafacilpro.web.form;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ServiceForm(
        @NotBlank(message = "Informe o nome do serviço.")
        @Size(max = 120, message = "Informe um nome menor para o serviço.")
        String name,
        @NotNull(message = "Informe a duração do serviço.")
        @Min(value = 15, message = "A duração mínima é de 15 minutos.")
        @Max(value = 480, message = "A duração está muito longa.")
        Integer durationMinutes,
        @DecimalMin(value = "0.0", message = "O preço não pode ser negativo.")
        BigDecimal price,
        @Size(max = 500, message = "A descrição está muito longa.")
        String description,
        boolean active
) {
}
