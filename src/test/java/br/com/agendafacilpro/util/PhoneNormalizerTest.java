package br.com.agendafacilpro.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PhoneNormalizerTest {

    @Test
    void normalizesMaskedPhone() {
        assertThat(PhoneNormalizer.normalize("(17) 98888-7777")).isEqualTo("17988887777");
    }

    @Test
    void normalizesUnmaskedPhone() {
        assertThat(PhoneNormalizer.normalize("17988887777")).isEqualTo("17988887777");
    }

    @Test
    void removesBrazilCountryCodeWhenPresent() {
        assertThat(PhoneNormalizer.normalize("+55 (17) 98888-7777")).isEqualTo("17988887777");
    }

    @Test
    void rejectsInvalidPhone() {
        assertThatThrownBy(() -> PhoneNormalizer.normalize("12345"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Telefone inválido.");
    }
}
