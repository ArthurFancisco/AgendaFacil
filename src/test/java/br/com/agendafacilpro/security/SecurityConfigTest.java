package br.com.agendafacilpro.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SecurityConfigTest {

    @Test
    void contentSecurityPolicyKeepsFrameAncestorsClosed() {
        assertThat(SecurityConfig.CONTENT_SECURITY_POLICY)
                .contains("default-src 'self'")
                .contains("style-src 'self' 'unsafe-inline'")
                .contains("frame-ancestors 'none'")
                .contains("object-src 'none'");
    }
}
