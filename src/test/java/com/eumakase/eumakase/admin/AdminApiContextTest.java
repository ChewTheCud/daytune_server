package com.eumakase.eumakase.admin;

import com.eumakase.eumakase.domain.Diary;
import com.eumakase.eumakase.domain.DiaryEmotionInsight;
import com.eumakase.eumakase.domain.Music;
import com.eumakase.eumakase.domain.ShareUrl;
import com.eumakase.eumakase.domain.User;
import com.eumakase.eumakase.repository.DiaryEmotionInsightRepository;
import com.eumakase.eumakase.repository.DiaryRepository;
import com.eumakase.eumakase.repository.MusicRepository;
import com.eumakase.eumakase.repository.ShareUrlRepository;
import com.eumakase.eumakase.repository.UserRepository;
import com.eumakase.eumakase.security.JwtIssuer;
import com.eumakase.eumakase.service.ExternalApiUsageRecorder;
import com.eumakase.eumakase.support.TestAdminCredentials;
import com.eumakase.eumakase.util.enums.ExternalApiOperation;
import com.eumakase.eumakase.util.enums.Role;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.eumakase.eumakase.security.JwtProperties;
import jakarta.servlet.Filter;
import jakarta.servlet.ServletException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 관리자 API 보안·CORS·응답 형식 (test 프로필, H2). 외부 API는 호출하지 않는다
 * - 다른 컨텍스트 테스트와 같은 설정(@SpringBootTest만)을 써서 컨텍스트를 공유한다.
 *   설정이 달라 두 번째 컨텍스트가 뜨면 FirebaseConfig가 "FirebaseApp name [DEFAULT] already exists!"로 실패한다
 * - 그래서 @AutoConfigureMockMvc 대신 MockMvc를 직접 만들고 시큐리티 필터 체인을 붙인다
 * - test 프로필은 CORS 허용 오리진이 비어 있다 → 거부만 여기서 확인. 허용은 WebSecurityConfigCorsTest
 */
@SpringBootTest
class AdminApiContextTest {

    private static final String ADMIN_EMAIL = "ctx-admin@example.com";
    private static final String PASSWORD = TestAdminCredentials.PASSWORD;
    private static final String HASH = TestAdminCredentials.HASH;

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DiaryRepository diaryRepository;
    @Autowired
    private DiaryEmotionInsightRepository emotionRepository;
    @Autowired
    private MusicRepository musicRepository;
    @Autowired
    private ShareUrlRepository shareUrlRepository;
    @Autowired
    private JwtIssuer jwtIssuer;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private ExternalApiUsageRecorder usageRecorder;
    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(context.getBean("springSecurityFilterChain", Filter.class))
                .build();
        if (userRepository.findByEmailAndRole(ADMIN_EMAIL, Role.ROLE_ADMIN).isEmpty()) {
            userRepository.save(User.builder().email(ADMIN_EMAIL).password(HASH).nickname("관리자").role(Role.ROLE_ADMIN).build());
        }
    }

    private String loginBody(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    private String adminToken() throws Exception {
        String body = mockMvc.perform(post("/admin/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(ADMIN_EMAIL, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("accessToken").asText();
    }

    private String range() {
        LocalDate today = LocalDate.now();
        return "?from=" + today.minusDays(6) + "&to=" + today;
    }

    // ---- 로그인 ----

    @Test
    void 로그인_성공은_ApiResponse로_토큰을_준다() throws Exception {
        mockMvc.perform(post("/admin/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(ADMIN_EMAIL, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").isString());
    }

    @Test
    void 로그인_실패는_401_ApiResponse다() throws Exception {
        mockMvc.perform(post("/admin/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("wrong-pw@example.com", "nope")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다."));
    }

    @Test
    void 같은_이메일_5회_실패_후에는_429다() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/admin/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content(loginBody("locked@example.com", "nope")))
                    .andExpect(status().isUnauthorized());
        }
        mockMvc.perform(post("/admin/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("locked@example.com", "nope")))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    void 일반_유저는_관리자_로그인이_안_된다() throws Exception {
        userRepository.save(User.builder().email("social@example.com").password(HASH).role(Role.ROLE_USER).build());
        mockMvc.perform(post("/admin/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("social@example.com", PASSWORD)))
                .andExpect(status().isUnauthorized());
    }

    // ---- 권한 ----

    // 앱이 받아 온 기존 401 본문 (UnauthorizedHandler). 공백까지 그대로여야 한다
    private static final String LEGACY_401_BODY = "{\"status\": \"ERROR\", \"message\": \"Access Token 값이 유효하지 않습니다.\"}";

    @Test
    void 토큰이_없으면_기존_401_본문이다() throws Exception {
        mockMvc.perform(get("/admin/usage" + range()))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8"))
                .andExpect(content().string(LEGACY_401_BODY));
    }

    @Test
    void 일반_유저_토큰은_403이다() throws Exception {
        String userToken = jwtIssuer.issue(999L, "user@example.com", List.of("ROLE_USER"));
        mockMvc.perform(get("/admin/usage" + range()).header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("권한이 없습니다."));
    }

    @Test
    void 위조_토큰은_기존_401_본문이다() throws Exception {
        for (String path : List.of("/admin/usage" + range(), "/api/v2/users/")) {
            mockMvc.perform(get(path).header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8"))
                    .andExpect(content().string(LEGACY_401_BODY));
        }
    }

    @Test
    void 만료_토큰은_기존_401_본문이다() throws Exception {
        String expired = JWT.create()
                .withSubject("1")
                .withExpiresAt(Instant.now().minusSeconds(60))
                .withClaim("e", ADMIN_EMAIL)
                .withClaim("a", List.of("ROLE_ADMIN"))
                .sign(Algorithm.HMAC256(jwtProperties.getSecretKey()));
        mockMvc.perform(get("/admin/usage" + range()).header(HttpHeaders.AUTHORIZATION, "Bearer " + expired))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(LEGACY_401_BODY));
    }

    @Test
    void 처리되지_않은_예외는_401로_바뀌지_않고_그대로_올라간다() {
        // 실제 시큐리티 필터 체인 뒤에 예외를 던지는 컨트롤러를 둔다. 서블릿 컨테이너에서는 이 예외가 500이 된다
        MockMvc throwingMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .addFilters(context.getBean("springSecurityFilterChain", Filter.class))
                .build();
        String userToken = jwtIssuer.issue(999L, "user@example.com", List.of("ROLE_USER"));

        assertThatThrownBy(() -> throwingMvc.perform(get("/api/v2/test-only/throw")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)))
                .isInstanceOf(ServletException.class)
                .rootCause().isInstanceOf(IllegalStateException.class).hasMessage("테스트용 처리되지 않은 예외");
    }

    @RestController
    static class ThrowingController {
        @GetMapping("/api/v2/test-only/throw")
        String fail() {
            throw new IllegalStateException("테스트용 처리되지 않은 예외");
        }
    }

    // ---- CORS ----

    @Test
    void 허용되지_않은_오리진의_프리플라이트는_거부한다() throws Exception {
        mockMvc.perform(options("/admin/usage")
                        .header(HttpHeaders.ORIGIN, "https://evil.example.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void 앱_API_경로에는_CORS를_적용하지_않는다() throws Exception {
        mockMvc.perform(options("/api/v2/diary")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    // ---- 관리자 API 응답 ----

    @Test
    void 관리자_API_5종이_ApiResponse로_응답한다() throws Exception {
        usageRecorder.record(ExternalApiOperation.LYRICS_GENERATE, System.nanoTime(), 1L, null, null,
                "gpt-4-turbo-2024-04-09", 1000L, 300L, null);
        usageRecorder.failure(ExternalApiOperation.MUSIC_STATUS_POLL, System.nanoTime(), "SUNO_430");
        String auth = "Bearer " + adminToken();

        String usage = mockMvc.perform(get("/admin/usage" + range()).header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.daily.length()").value(7))
                .andReturn().getResponse().getContentAsString();

        String costs = mockMvc.perform(get("/admin/costs" + range()).header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.providers.length()").value(4))
                .andExpect(jsonPath("$.data.providers[0].provider").value("OPENAI"))
                .andExpect(jsonPath("$.data.providers[0].source").value("ESTIMATED"))
                .andExpect(jsonPath("$.data.providers[1].source").value("NONE"))
                .andExpect(jsonPath("$.data.daily.length()").value(28))
                .andReturn().getResponse().getContentAsString();
        JsonNode costsData = objectMapper.readTree(costs).path("data");
        assertThat(costsData.path("providers").get(0).path("estimatedCostUsd").decimalValue()).isGreaterThan(java.math.BigDecimal.ZERO);
        assertThat(costsData.path("providers").get(1).has("estimatedCostUsd")).isTrue();
        assertThat(costsData.path("providers").get(1).path("estimatedCostUsd").isNull()).isTrue();
        assertThat(costsData.path("asOf").asText()).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}");

        mockMvc.perform(get("/admin/users?query=ctx-admin&size=5").header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].email").value(ADMIN_EMAIL))
                .andExpect(jsonPath("$.data.size").value(5));

        Long adminId = userRepository.findByEmailAndRole(ADMIN_EMAIL, Role.ROLE_ADMIN).get(0).getId();
        mockMvc.perform(get("/admin/users/" + adminId + range()).header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(adminId))
                .andExpect(jsonPath("$.data.costs.length()").value(4));

        String system = mockMvc.perform(get("/admin/system?hours=24").header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.apis.length()").value(8))
                .andExpect(jsonPath("$.data.batchRuns").isArray())
                .andReturn().getResponse().getContentAsString();
        assertThat(objectMapper.readTree(system).path("data").path("recentErrors").toString()).contains("SUNO_430");
        System.out.println("[usage 응답 예] " + usage.substring(0, Math.min(300, usage.length())));
    }

    @Test
    void 잘못된_파라미터는_400_없는_유저는_404다() throws Exception {
        String auth = "Bearer " + adminToken();
        LocalDate today = LocalDate.now();
        mockMvc.perform(get("/admin/usage?from=" + today + "&to=" + today.minusDays(1)).header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
        mockMvc.perform(get("/admin/costs?from=" + today.minusDays(92) + "&to=" + today).header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/admin/usage?from=" + today).header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/admin/system?hours=169").header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/admin/users?size=51").header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/admin/users/987654321" + range()).header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("유저를 찾을 수 없습니다."));
    }

    @Test
    void 이용_현황_퍼널_분포_상세가_실제_쿼리로_집계된다() throws Exception {
        User user = userRepository.save(User.builder().email("m4-user@example.com").password("x").nickname("m4유저").role(Role.ROLE_USER).build());
        Diary diary = diaryRepository.save(Diary.builder().user(user).content("m4-secret-body").build());
        emotionRepository.save(DiaryEmotionInsight.builder().diary(diary).emotion("m4emotion").reason("r").build());
        Music music = musicRepository.save(Music.builder().diary(diary).build());
        shareUrlRepository.save(ShareUrl.builder().music(music).url("https://example.com/s").build());
        String auth = "Bearer " + adminToken();

        String usage = mockMvc.perform(get("/admin/usage" + range()).header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.heatmap.length()").value(168))
                .andExpect(jsonPath("$.data.funnel.signedUp").isNumber())
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        JsonNode data = objectMapper.readTree(usage).path("data");
        assertThat(data.path("funnel").path("shared").asLong()).isGreaterThanOrEqualTo(1);
        assertThat(data.path("funnel").path("generatedMusic").asLong()).isGreaterThanOrEqualTo(1);
        assertThat(data.path("emotions").toString()).contains("m4emotion");
        assertThat(data.path("categories").toString()).contains("미지정");

        for (String metric : List.of("signups", "activeUsers", "diaries", "musics", "shares", "pushes")) {
            String body = mockMvc.perform(get("/admin/usage/details" + range() + "&metric=" + metric + "&size=5")
                            .header(HttpHeaders.AUTHORIZATION, auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
            assertThat(body).doesNotContain("m4-secret-body");
            if (!metric.equals("pushes")) {
                assertThat(objectMapper.readTree(body).path("data").path("totalElements").asLong()).isGreaterThanOrEqualTo(1);
            }
        }
        mockMvc.perform(get("/admin/usage/details" + range() + "&metric=diaries").header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(jsonPath("$.data.content[0].detail").value(org.hamcrest.Matchers.containsString("m4emotion")));
        mockMvc.perform(get("/admin/usage/details" + range() + "&metric=bogus").header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }
}
