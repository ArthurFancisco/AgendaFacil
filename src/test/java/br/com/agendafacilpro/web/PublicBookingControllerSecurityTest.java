package br.com.agendafacilpro.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;

class PublicBookingControllerSecurityTest {

    @Test
    void successRouteUsesPublicTokenInsteadOfSequentialId() {
        Method success = Arrays.stream(PublicBookingController.class.getDeclaredMethods())
                .filter(method -> method.getName().equals("success"))
                .findFirst()
                .orElseThrow();

        assertThat(success.getParameterTypes()[1]).isEqualTo(String.class);
        assertThat(success.getParameterTypes()).doesNotContain(Long.class);
        assertThat(success.getAnnotation(GetMapping.class).value()[0])
                .contains("publicToken")
                .contains("[A-Za-z0-9_-]{24,64}");
    }
}
