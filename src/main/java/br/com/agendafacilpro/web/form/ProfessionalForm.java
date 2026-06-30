package br.com.agendafacilpro.web.form;

import java.util.List;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

public record ProfessionalForm(
        @NotBlank(message = "Informe o nome do profissional.")
        @Size(max = 120, message = "Informe um nome menor.")
        String name,
        @Size(max = 500, message = "A apresentação está muito longa.")
        String bio,
        @Size(max = 20, message = "O WhatsApp informado parece inválido.")
        @Pattern(regexp = "^$|^[0-9()\\s+\\-.]{10,20}$", message = "O WhatsApp informado parece inválido.")
        String whatsapp,
        List<Long> serviceIds,
        boolean active
) {
}
