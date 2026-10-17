package com.eumakase.eumakase.repository;

import com.eumakase.eumakase.domain.ExternalApiUsage;
import com.eumakase.eumakase.util.enums.ExternalApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 미터링 로그 조회. 기간은 모두 [from, to) 이다
 */
@Repository
public interface ExternalApiUsageRepository extends JpaRepository<ExternalApiUsage, Long> {

    // 제공사별: provider, calls, failures, unitsIn, unitsOut, cost
    @Query("select e.provider, count(e), sum(case when e.success = false then 1 else 0 end),"
            + " sum(e.unitsIn), sum(e.unitsOut), sum(e.estimatedCostUsd)"
            + " from ExternalApiUsage e where e.createdDate >= :from and e.createdDate < :to"
            + " group by e.provider")
    List<Object[]> summarizeByProvider(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // 일×제공사: date, provider, calls, cost
    @Query("select cast(e.createdDate as LocalDate), e.provider, count(e), sum(e.estimatedCostUsd)"
            + " from ExternalApiUsage e where e.createdDate >= :from and e.createdDate < :to"
            + " group by cast(e.createdDate as LocalDate), e.provider")
    List<Object[]> summarizeDailyByProvider(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // 기능별: operation, calls, failures, cost
    @Query("select e.operation, count(e), sum(case when e.success = false then 1 else 0 end), sum(e.estimatedCostUsd)"
            + " from ExternalApiUsage e where e.createdDate >= :from and e.createdDate < :to"
            + " group by e.operation")
    List<Object[]> summarizeByOperation(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // 유저별(userId null 제외): userId, calls, cost — 비용 → 호출 수 순
    @Query("select e.userId, count(e), sum(e.estimatedCostUsd)"
            + " from ExternalApiUsage e where e.createdDate >= :from and e.createdDate < :to and e.userId is not null"
            + " group by e.userId order by coalesce(sum(e.estimatedCostUsd), 0) desc, count(e) desc")
    List<Object[]> summarizeByUser(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

    // 한 유저의 제공사별: provider, calls, cost
    @Query("select e.provider, count(e), sum(e.estimatedCostUsd)"
            + " from ExternalApiUsage e where e.userId = :userId and e.createdDate >= :from and e.createdDate < :to"
            + " group by e.provider")
    List<Object[]> summarizeUserByProvider(@Param("userId") Long userId,
                                           @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // 일별 성공 건수 (이용 현황의 pushes): date, count
    @Query("select cast(e.createdDate as LocalDate), count(e)"
            + " from ExternalApiUsage e where e.operation = :operation and e.success = true"
            + " and e.createdDate >= :from and e.createdDate < :to"
            + " group by cast(e.createdDate as LocalDate)")
    List<Object[]> countDailySuccess(@Param("operation") ExternalApiOperation operation,
                                     @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // 푸시 상세 목록 [from, to): 성공·실패 모두 (정렬은 Pageable)
    long countByOperationAndSuccessTrueAndCreatedDateGreaterThanEqualAndCreatedDateLessThan(
            ExternalApiOperation operation, LocalDateTime from, LocalDateTime to);

    Page<ExternalApiUsage> findByOperationAndSuccessTrueAndCreatedDateGreaterThanEqualAndCreatedDateLessThan(
            ExternalApiOperation operation, LocalDateTime from, LocalDateTime to, Pageable pageable);

    // p95 계산용 지연 시간: operation, latencyMs
    @Query("select e.operation, e.latencyMs from ExternalApiUsage e where e.createdDate >= :from and e.createdDate < :to")
    List<Object[]> findLatencies(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // 창 내 최신 실패 20건
    List<ExternalApiUsage> findTop20BySuccessFalseAndCreatedDateGreaterThanEqualOrderByCreatedDateDescIdDesc(LocalDateTime from);
}
