package com.eumakase.eumakase.service;

import com.eumakase.eumakase.dto.admin.AdminCostsResponseDto;
import com.eumakase.eumakase.dto.admin.AdminUsageDetailResponseDto;
import com.eumakase.eumakase.dto.admin.AdminUsageResponseDto;
import com.eumakase.eumakase.exception.AdminException;
import com.eumakase.eumakase.repository.*;
import com.eumakase.eumakase.util.enums.ExternalApiProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 이용 현황 퍼널·카테고리·감정·히트맵, KPI 상세 파라미터 검증, SUNO 비용 집계 (스프링 컨텍스트 없음)
 */
class AdminUsageAggregationTest {

    private static final LocalDate FROM = LocalDate.of(2026, 10, 1);
    private static final LocalDate TO = LocalDate.of(2026, 10, 7);

    private UserRepository userRepository;
    private DiaryRepository diaryRepository;
    private DiaryEmotionInsightRepository emotionRepository;
    private MusicRepository musicRepository;
    private ShareUrlRepository shareUrlRepository;
    private ExternalApiUsageRepository usageRepository;
    private AdminDashboardService dashboardService;
    private AdminUsageDetailService detailService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        diaryRepository = mock(DiaryRepository.class);
        emotionRepository = mock(DiaryEmotionInsightRepository.class);
        musicRepository = mock(MusicRepository.class);
        shareUrlRepository = mock(ShareUrlRepository.class);
        usageRepository = mock(ExternalApiUsageRepository.class);
        dashboardService = new AdminDashboardService(userRepository, diaryRepository, emotionRepository,
                musicRepository, shareUrlRepository, usageRepository, mock(JobExplorer.class));
        detailService = new AdminUsageDetailService(userRepository, diaryRepository, emotionRepository,
                musicRepository, shareUrlRepository, usageRepository);
    }

    @Test
    void 퍼널은_가입_코호트_기준_단계별_수를_담는다() {
        when(userRepository.countDailySignups(any(), any())).thenReturn(List.<Object[]>of(
                new Object[]{FROM, 3L}, new Object[]{FROM.plusDays(1), 2L}));
        when(userRepository.countCohortWithDiary(any(), any())).thenReturn(4L);
        when(userRepository.countCohortWithMusic(any(), any())).thenReturn(3L);
        when(userRepository.countCohortWithShare(any(), any())).thenReturn(1L);

        AdminUsageResponseDto.Funnel funnel = dashboardService.getUsage(FROM, TO).getFunnel();

        assertThat(funnel.getSignedUp()).isEqualTo(5);
        assertThat(funnel.getWroteDiary()).isEqualTo(4);
        assertThat(funnel.getGeneratedMusic()).isEqualTo(3);
        assertThat(funnel.getShared()).isEqualTo(1);
    }

    @Test
    void 히트맵은_168칸을_0으로_채우고_월요일부터_정렬한다() {
        // 2026-10-05 월 09:30, 2026-10-11 일 23:59, 같은 칸 2건
        when(diaryRepository.findCreatedDates(any(), any())).thenReturn(List.of(
                LocalDateTime.of(2026, 10, 5, 9, 30), LocalDateTime.of(2026, 10, 5, 9, 59),
                LocalDateTime.of(2026, 10, 11, 23, 59)));

        List<AdminUsageResponseDto.HeatmapCell> heatmap = dashboardService.getUsage(FROM, TO).getHeatmap();

        assertThat(heatmap).hasSize(168);
        assertThat(heatmap.get(0).getDayOfWeek()).isEqualTo(1);
        assertThat(heatmap.get(0).getHour()).isEqualTo(0);
        assertThat(heatmap.get(167).getDayOfWeek()).isEqualTo(7);
        assertThat(heatmap.get(167).getHour()).isEqualTo(23);
        assertThat(heatmap.get(9).getCount()).isEqualTo(2); // 월 09시
        assertThat(heatmap.get(167).getCount()).isEqualTo(1); // 일 23시
        assertThat(heatmap.stream().mapToLong(AdminUsageResponseDto.HeatmapCell::getCount).sum()).isEqualTo(3);
    }

    @Test
    void 카테고리가_없는_일기는_미지정이고_감정_결과는_그대로_담긴다() {
        when(diaryRepository.countByCategory(any(), any())).thenReturn(List.<Object[]>of(
                new Object[]{2L, "평온", 5L}, new Object[]{null, null, 2L}));
        when(emotionRepository.countByEmotion(any(), any(), any())).thenReturn(List.<Object[]>of(new Object[]{"기쁨", 7L}));

        AdminUsageResponseDto usage = dashboardService.getUsage(FROM, TO);

        assertThat(usage.getCategories()).extracting(AdminUsageResponseDto.CategoryCount::getName)
                .containsExactly("평온", "미지정");
        assertThat(usage.getCategories().get(1).getCategoryId()).isNull();
        assertThat(usage.getEmotions()).extracting(AdminUsageResponseDto.EmotionCount::getEmotion).containsExactly("기쁨");
        assertThat(usage.getEmotions().get(0).getCount()).isEqualTo(7);
    }

    @Test
    void 상세_metric이_잘못되면_400이다() {
        assertThatThrownBy(() -> detailService.getDetails("unknown", FROM, TO, 0, 20))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThatThrownBy(() -> detailService.getDetails("SIGNUPS", FROM, TO, 0, 20))
                .isInstanceOf(AdminException.class);
        assertThatThrownBy(() -> detailService.getDetails(null, FROM, TO, 0, 20))
                .isInstanceOf(AdminException.class);
    }

    @Test
    void 상세_page_size_기간_위반은_400이다() {
        assertThatThrownBy(() -> detailService.getDetails("signups", FROM, TO, -1, 20)).isInstanceOf(AdminException.class);
        assertThatThrownBy(() -> detailService.getDetails("signups", FROM, TO, 0, 0)).isInstanceOf(AdminException.class);
        assertThatThrownBy(() -> detailService.getDetails("signups", FROM, TO, 0, 51)).isInstanceOf(AdminException.class);
        assertThatThrownBy(() -> detailService.getDetails("signups", TO, FROM, 0, 20)).isInstanceOf(AdminException.class);
    }

    @Test
    void 일기_상세는_카테고리와_감정만_담고_본문은_없다() {
        LocalDateTime at = LocalDateTime.of(2026, 10, 3, 10, 0);
        when(diaryRepository.countByCreatedDateGreaterThanEqualAndCreatedDateLessThan(any(), any())).thenReturn(2L);
        when(diaryRepository.findDetailRows(any(), any(), any())).thenReturn(List.<Object[]>of(
                new Object[]{11L, 5L, "닉", at, "기쁨"}, new Object[]{10L, null, null, at, null}));
        when(emotionRepository.findEmotionsByDiaryIds(any())).thenReturn(List.<Object[]>of(
                new Object[]{11L, "행복"}, new Object[]{11L, "설렘"}));

        AdminUsageDetailResponseDto result = detailService.getDetails("diaries", FROM, TO, 0, 20);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).getDetail()).isEqualTo("기쁨 · 행복, 설렘");
        assertThat(result.getContent().get(1).getDetail()).isEqualTo("미지정");
        assertThat(result.getContent().get(1).getUserId()).isNull();
    }

    @Test
    void 푸시_상세는_성공과_실패를_구분한다() {
        assertThat(AdminUsageDetailService.pushDetail(true, null)).isEqualTo("성공");
        assertThat(AdminUsageDetailService.pushDetail(false, "HTTP_500")).isEqualTo("실패 · HTTP_500");
        assertThat(AdminUsageDetailService.pushDetail(false, null)).isEqualTo("실패");
    }

    @Test
    void SUNO_비용이_기록된_기간은_빈_날짜를_0으로_채우고_source가_ESTIMATED다() {
        when(usageRepository.summarizeByProvider(any(), any())).thenReturn(List.<Object[]>of(
                new Object[]{ExternalApiProvider.SUNO, 2L, 0L, null, null, new BigDecimal("0.10")}));
        when(usageRepository.summarizeDailyByProvider(any(), any())).thenReturn(List.<Object[]>of(
                new Object[]{FROM, ExternalApiProvider.SUNO, 2L, new BigDecimal("0.10")}));

        AdminCostsResponseDto costs = dashboardService.getCosts(FROM, TO);

        AdminCostsResponseDto.ProviderCost suno = costs.getProviders().stream()
                .filter(p -> p.getProvider() == ExternalApiProvider.SUNO).findFirst().orElseThrow();
        assertThat(suno.getEstimatedCostUsd()).isEqualByComparingTo("0.10");
        assertThat(suno.getSource()).isEqualTo("ESTIMATED");
        List<AdminCostsResponseDto.DailyCost> sunoDaily = new ArrayList<>();
        costs.getDaily().stream().filter(d -> d.getProvider() == ExternalApiProvider.SUNO).forEach(sunoDaily::add);
        assertThat(sunoDaily).hasSize(7).allMatch(d -> d.getEstimatedCostUsd() != null);
        assertThat(sunoDaily.get(1).getEstimatedCostUsd()).isEqualByComparingTo("0");
    }

    @Test
    void SUNO_비용이_없으면_null이고_source가_NONE이다() {
        when(usageRepository.summarizeByProvider(any(), any())).thenReturn(List.<Object[]>of(
                new Object[]{ExternalApiProvider.SUNO, 2L, 0L, null, null, null}));

        AdminCostsResponseDto costs = dashboardService.getCosts(FROM, TO);

        AdminCostsResponseDto.ProviderCost suno = costs.getProviders().stream()
                .filter(p -> p.getProvider() == ExternalApiProvider.SUNO).findFirst().orElseThrow();
        assertThat(suno.getEstimatedCostUsd()).isNull();
        assertThat(suno.getSource()).isEqualTo("NONE");
        assertThat(costs.getDaily()).filteredOn(d -> d.getProvider() == ExternalApiProvider.SUNO)
                .allMatch(d -> d.getEstimatedCostUsd() == null);
    }
}
