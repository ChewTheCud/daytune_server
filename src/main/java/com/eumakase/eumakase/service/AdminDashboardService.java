package com.eumakase.eumakase.service;

import com.eumakase.eumakase.domain.User;
import com.eumakase.eumakase.dto.admin.AdminCostsResponseDto;
import com.eumakase.eumakase.dto.admin.AdminSystemResponseDto;
import com.eumakase.eumakase.dto.admin.AdminUsageResponseDto;
import com.eumakase.eumakase.exception.AdminException;
import com.eumakase.eumakase.repository.DiaryRepository;
import com.eumakase.eumakase.repository.ExternalApiUsageRepository;
import com.eumakase.eumakase.repository.MusicRepository;
import com.eumakase.eumakase.repository.ShareUrlRepository;
import com.eumakase.eumakase.repository.UserRepository;
import com.eumakase.eumakase.util.AdminStatsUtil;
import com.eumakase.eumakase.util.enums.ExternalApiOperation;
import com.eumakase.eumakase.util.enums.ExternalApiProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 관리자 대시보드 집계 (이용 현황·외부 API 과금·시스템 상태)
 * 날짜·시각은 서버 타임존(Asia/Seoul) 기준이다. 기간 [from 00:00, to+1일 00:00)
 */
@Slf4j
@Service
@RequiredArgsConstructor
// 클래스 단위 트랜잭션을 두지 않는다: 배치 메타 테이블 조회 실패가 바깥 트랜잭션을 rollback-only로 만들어
// 응답 전체가 실패하는 것을 막기 위해서다. 각 조회는 리포지토리 기본 트랜잭션(readOnly)으로 실행된다
public class AdminDashboardService {

    static final String SOURCE_ESTIMATED = "ESTIMATED";
    static final String SOURCE_NONE = "NONE";
    private static final int TOP_LIMIT = 10;
    private static final int BATCH_RUN_LIMIT = 10;
    private static final int MIN_HOURS = 1;
    private static final int MAX_HOURS = 168;

    private final UserRepository userRepository;
    private final DiaryRepository diaryRepository;
    private final MusicRepository musicRepository;
    private final ShareUrlRepository shareUrlRepository;
    private final ExternalApiUsageRepository externalApiUsageRepository;
    private final JobExplorer jobExplorer;

    /**
     * 이용 현황
     */
    public AdminUsageResponseDto getUsage(LocalDate from, LocalDate to) {
        AdminStatsUtil.validateRange(from, to);
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        Map<LocalDate, Long> signups = AdminStatsUtil.toDailyCounts(userRepository.countDailySignups(start, end));
        Map<LocalDate, Long> diaries = new HashMap<>();
        Map<LocalDate, Long> activeUsers = new HashMap<>();
        for (Object[] row : diaryRepository.countDaily(start, end)) {
            diaries.put((LocalDate) row[0], ((Number) row[1]).longValue());
            activeUsers.put((LocalDate) row[0], ((Number) row[2]).longValue());
        }
        Map<LocalDate, Long> musics = AdminStatsUtil.toDailyCounts(musicRepository.countDaily(start, end));
        Map<LocalDate, Long> shares = AdminStatsUtil.toDailyCounts(shareUrlRepository.countDaily(start, end));
        Map<LocalDate, Long> pushes = AdminStatsUtil.toDailyCounts(
                externalApiUsageRepository.countDailySuccess(ExternalApiOperation.PUSH_SEND, start, end));

        List<AdminUsageResponseDto.Daily> daily = AdminStatsUtil.datesBetween(from, to).stream()
                .map(date -> new AdminUsageResponseDto.Daily(date,
                        signups.getOrDefault(date, 0L),
                        activeUsers.getOrDefault(date, 0L),
                        diaries.getOrDefault(date, 0L),
                        musics.getOrDefault(date, 0L),
                        shares.getOrDefault(date, 0L),
                        pushes.getOrDefault(date, 0L)))
                .collect(Collectors.toList());

        AdminUsageResponseDto.Counts totals = new AdminUsageResponseDto.Counts(
                sum(signups),
                diaryRepository.countDistinctUsers(start, end),
                sum(diaries),
                sum(musics),
                sum(shares),
                sum(pushes));

        return new AdminUsageResponseDto(userRepository.countByDeletedDateIsNull(), totals, daily);
    }

    /**
     * 외부 API 과금. 비용은 OPENAI만 추정값(ESTIMATED), 나머지는 산정하지 않는다(NONE)
     */
    public AdminCostsResponseDto getCosts(LocalDate from, LocalDate to) {
        AdminStatsUtil.validateRange(from, to);
        LocalDateTime asOf = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        // 제공사별 (호출 0건 제공사도 포함)
        Map<ExternalApiProvider, Object[]> byProvider = indexBy(
                externalApiUsageRepository.summarizeByProvider(start, end), row -> (ExternalApiProvider) row[0]);
        List<AdminCostsResponseDto.ProviderCost> providers = new ArrayList<>();
        for (ExternalApiProvider provider : ExternalApiProvider.values()) {
            Object[] row = byProvider.get(provider);
            boolean estimated = provider == ExternalApiProvider.OPENAI;
            providers.add(new AdminCostsResponseDto.ProviderCost(
                    provider,
                    row == null ? 0 : toLong(row[1]),
                    row == null ? 0 : toLong(row[2]),
                    estimated ? orZero(row == null ? null : toNullableLong(row[3])) : null,
                    estimated ? orZero(row == null ? null : toNullableLong(row[4])) : null,
                    costOf(provider, row == null ? null : (BigDecimal) row[5]),
                    estimated ? SOURCE_ESTIMATED : SOURCE_NONE));
        }

        // 일 × 제공사 (빈 칸은 0)
        Map<String, Object[]> byDayProvider = indexBy(
                externalApiUsageRepository.summarizeDailyByProvider(start, end), row -> row[0] + "|" + row[1]);
        List<AdminCostsResponseDto.DailyCost> daily = new ArrayList<>();
        for (LocalDate date : AdminStatsUtil.datesBetween(from, to)) {
            for (ExternalApiProvider provider : ExternalApiProvider.values()) {
                Object[] row = byDayProvider.get(date + "|" + provider);
                daily.add(new AdminCostsResponseDto.DailyCost(date, provider,
                        row == null ? 0 : toLong(row[2]),
                        costOf(provider, row == null ? null : (BigDecimal) row[3])));
            }
        }

        // 기능별 상위 10 (비용 → 호출 수)
        List<AdminCostsResponseDto.OperationCost> topOperations = externalApiUsageRepository.summarizeByOperation(start, end).stream()
                .map(row -> {
                    ExternalApiOperation operation = (ExternalApiOperation) row[0];
                    return new AdminCostsResponseDto.OperationCost(operation.getProvider(), operation,
                            toLong(row[1]), costOf(operation.getProvider(), (BigDecimal) row[3]));
                })
                .sorted(Comparator.comparing((AdminCostsResponseDto.OperationCost c) -> costForSort(c.getEstimatedCostUsd())).reversed()
                        .thenComparing(Comparator.comparingLong(AdminCostsResponseDto.OperationCost::getCalls).reversed()))
                .limit(TOP_LIMIT)
                .collect(Collectors.toList());

        // 유저별 상위 10 (DB에서 비용 → 호출 수 순으로 정렬·제한)
        List<Object[]> userRows = externalApiUsageRepository.summarizeByUser(start, end, PageRequest.of(0, TOP_LIMIT));
        Map<Long, String> nicknames = userRepository.findAllById(
                        userRows.stream().map(row -> (Long) row[0]).collect(Collectors.toList())).stream()
                .filter(user -> user.getNickname() != null)
                .collect(Collectors.toMap(User::getId, User::getNickname));
        List<AdminCostsResponseDto.UserCost> topUsers = userRows.stream()
                .map(row -> new AdminCostsResponseDto.UserCost((Long) row[0], nicknames.get((Long) row[0]),
                        toLong(row[1]), scaled((BigDecimal) row[2])))
                .collect(Collectors.toList());

        return new AdminCostsResponseDto(asOf, providers, daily, topOperations, topUsers);
    }

    /**
     * 시스템 상태 (최근 hours시간)
     */
    public AdminSystemResponseDto getSystem(int hours) {
        if (hours < MIN_HOURS || hours > MAX_HOURS) {
            throw new AdminException(HttpStatus.BAD_REQUEST, "hours는 " + MIN_HOURS + "~" + MAX_HOURS + " 사이여야 합니다.");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowFrom = now.minusHours(hours);

        Map<ExternalApiOperation, Object[]> byOperation = indexBy(
                externalApiUsageRepository.summarizeByOperation(windowFrom, now), row -> (ExternalApiOperation) row[0]);
        Map<ExternalApiOperation, List<Integer>> latencies = new EnumMap<>(ExternalApiOperation.class);
        for (Object[] row : externalApiUsageRepository.findLatencies(windowFrom, now)) {
            latencies.computeIfAbsent((ExternalApiOperation) row[0], k -> new ArrayList<>()).add(((Number) row[1]).intValue());
        }

        List<AdminSystemResponseDto.ApiHealth> apis = new ArrayList<>();
        for (ExternalApiOperation operation : ExternalApiOperation.values()) {
            Object[] row = byOperation.get(operation);
            long calls = row == null ? 0 : toLong(row[1]);
            long failures = row == null ? 0 : toLong(row[2]);
            apis.add(new AdminSystemResponseDto.ApiHealth(operation.getProvider(), operation, calls, failures,
                    AdminStatsUtil.failureRate(failures, calls), AdminStatsUtil.p95(latencies.get(operation))));
        }

        List<AdminSystemResponseDto.RecentError> recentErrors = externalApiUsageRepository
                .findTop20BySuccessFalseAndCreatedDateGreaterThanEqualOrderByCreatedDateDescIdDesc(windowFrom).stream()
                .map(usage -> new AdminSystemResponseDto.RecentError(usage.getCreatedDate(), usage.getProvider(),
                        usage.getOperation(), usage.getErrorCode()))
                .collect(Collectors.toList());

        return new AdminSystemResponseDto(now.truncatedTo(ChronoUnit.SECONDS), windowFrom.truncatedTo(ChronoUnit.SECONDS),
                apis, recentBatchRuns(), recentErrors);
    }

    /**
     * Spring Batch 메타 테이블에서 최신 실행 10건. 메타 테이블이 없거나 조회에 실패하면 빈 목록 (로컬 H2 등)
     */
    private List<AdminSystemResponseDto.BatchRun> recentBatchRuns() {
        try {
            List<JobExecution> executions = new ArrayList<>();
            for (String jobName : jobExplorer.getJobNames()) {
                for (JobInstance instance : jobExplorer.findJobInstancesByJobName(jobName, 0, BATCH_RUN_LIMIT)) {
                    executions.addAll(jobExplorer.getJobExecutions(instance));
                }
            }
            return executions.stream()
                    .sorted(Comparator.comparing(JobExecution::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                    .limit(BATCH_RUN_LIMIT)
                    .map(execution -> new AdminSystemResponseDto.BatchRun(
                            execution.getJobInstance().getJobName(),
                            execution.getStatus().name(),
                            execution.getStartTime() != null ? execution.getStartTime() : execution.getCreateTime(),
                            execution.getEndTime(),
                            execution.getExitStatus().getExitCode()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("[관리자 시스템 상태] 배치 실행 이력 조회에 실패했습니다: {}", e.getClass().getSimpleName());
            return List.of();
        }
    }

    // OPENAI는 비용이 없으면 0, 나머지 제공사는 항상 null
    private static BigDecimal costOf(ExternalApiProvider provider, BigDecimal cost) {
        if (provider != ExternalApiProvider.OPENAI) {
            return null;
        }
        return scaled(cost == null ? BigDecimal.ZERO : cost);
    }

    private static BigDecimal scaled(BigDecimal cost) {
        return cost == null ? null : cost.setScale(6, RoundingMode.HALF_UP);
    }

    private static BigDecimal costForSort(BigDecimal cost) {
        return cost == null ? BigDecimal.valueOf(-1) : cost;
    }

    private static <K> Map<K, Object[]> indexBy(List<Object[]> rows, Function<Object[], K> key) {
        Map<K, Object[]> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put(key.apply(row), row);
        }
        return map;
    }

    private static long sum(Map<LocalDate, Long> counts) {
        return counts.values().stream().mapToLong(Long::longValue).sum();
    }

    private static long toLong(Object value) {
        return value == null ? 0 : ((Number) value).longValue();
    }

    private static Long toNullableLong(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    private static Long orZero(Long value) {
        return value == null ? 0L : value;
    }
}
