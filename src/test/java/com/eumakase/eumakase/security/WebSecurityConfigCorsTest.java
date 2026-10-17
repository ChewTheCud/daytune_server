package com.eumakase.eumakase.security;

import com.eumakase.eumakase.config.AdminProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * /admin/** 전용 CORS 설정 (허용 오리진은 설정값)
 */
class WebSecurityConfigCorsTest {

    private CorsConfigurationSource source(List<String> origins) {
        AdminProperties properties = new AdminProperties();
        properties.getCors().setAllowedOrigins(origins);
        return new WebSecurityConfig(mock(JwtAuthenticationFilter.class), mock(CustomUserDetailService.class),
                new UnauthorizedHandler(), new ForbiddenHandler(), properties).corsConfigurationSource();
    }

    private CorsConfiguration configFor(CorsConfigurationSource source, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", path);
        return source.getCorsConfiguration(request);
    }

    @Test
    void 관리자_경로는_설정된_오리진만_허용한다() {
        CorsConfiguration config = configFor(source(List.of("http://localhost:5173")), "/admin/usage");

        assertThat(config.checkOrigin("http://localhost:5173")).isEqualTo("http://localhost:5173");
        assertThat(config.checkOrigin("https://evil.example.com")).isNull();
        assertThat(config.checkHttpMethod(org.springframework.http.HttpMethod.GET)).isNotNull();
        assertThat(config.checkHttpMethod(org.springframework.http.HttpMethod.DELETE)).isNull();
        assertThat(config.checkHeaders(List.of("Authorization", "Content-Type"))).hasSize(2);
        assertThat(config.getAllowCredentials()).isFalse();
    }

    @Test
    void 허용_오리진이_비면_모두_거부한다() {
        CorsConfiguration config = configFor(source(List.of()), "/admin/auth/login");

        assertThat(config.checkOrigin("http://localhost:5173")).isNull();
    }

    @Test
    void 앱_API_경로에는_CORS_설정이_없다() {
        assertThat(configFor(source(List.of("http://localhost:5173")), "/api/v2/diary")).isNull();
    }
}
