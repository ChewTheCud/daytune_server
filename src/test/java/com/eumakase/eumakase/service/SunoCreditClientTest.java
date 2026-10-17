package com.eumakase.eumakase.service;

import com.eumakase.eumakase.config.SunoAIConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * 실제 호출 없이 MockRestServiceServer로만 검증한다
 */
class SunoCreditClientTest {

    private static final String URL = "http://localhost:1/suno/api/v1/generate/credit";

    private MockRestServiceServer server;
    private SunoCreditClient client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        SunoAIConfig.SunoAIProperties properties = new SunoAIConfig.SunoAIProperties();
        properties.setUrl("http://localhost:1/suno");
        client = new SunoCreditClient(restTemplate, properties, "dummy-key");
    }

    @Test
    void 정상_응답이면_잔여_크레딧을_반환하고_Bearer_키를_붙인다() {
        server.expect(requestTo(URL)).andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer dummy-key"))
                .andRespond(withSuccess("{\"code\":200,\"msg\":\"success\",\"data\":88}", MediaType.APPLICATION_JSON));

        assertThat(client.fetchBalance()).isEqualTo(88L);
        server.verify();
    }

    @Test
    void 오류_응답_비정상_본문_서버_오류는_null이다() {
        server.expect(requestTo(URL)).andRespond(withSuccess("{\"code\":401,\"msg\":\"no\",\"data\":null}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(URL)).andRespond(withSuccess("{\"code\":200,\"msg\":\"success\",\"data\":\"x\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(URL)).andRespond(withServerError());

        assertThat(client.fetchBalance()).isNull();
        assertThat(client.fetchBalance()).isNull();
        assertThat(client.fetchBalance()).isNull();
    }

    @Test
    void 연결_실패도_예외_없이_null이다() {
        SunoAIConfig.SunoAIProperties properties = new SunoAIConfig.SunoAIProperties();
        properties.setUrl("http://localhost:1");
        // 실제 소켓: 로컬 포트 1은 연결 거부된다 (외부 호출 없음)
        assertThat(new SunoCreditClient(new RestTemplate(), properties, "dummy-key").fetchBalance()).isNull();
    }
}
