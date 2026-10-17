package com.eumakase.eumakase.domain;

import com.eumakase.eumakase.common.domain.BaseEntity;
import com.eumakase.eumakase.util.enums.ExternalApiOperation;
import com.eumakase.eumakase.util.enums.ExternalApiProvider;
import com.eumakase.eumakase.util.enums.ExternalApiRefType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

/**
 * 외부 유료 API 호출 1건 = 1행 (미터링 로그)
 * - user_id는 FK를 두지 않는다. 유저 삭제와 무관하게 로그를 보존한다
 * - 비용은 기록 시점 단가로 계산해 저장한다. 단가가 바뀌어도 과거 행은 그대로다
 * - enum 컬럼은 VARCHAR로 고정한다 (Hibernate 6 MySQL 기본값인 ENUM 타입이면 값 추가 때마다 ALTER가 필요하다)
 */
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Table(name = "external_api_usage", indexes = {
        @Index(name = "idx_eau_created", columnList = "created_date"),
        @Index(name = "idx_eau_provider_created", columnList = "provider, created_date"),
        @Index(name = "idx_eau_user_created", columnList = "user_id, created_date")
})
public class ExternalApiUsage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "provider", length = 20, nullable = false)
    private ExternalApiProvider provider;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "operation", length = 40, nullable = false)
    private ExternalApiOperation operation;

    @Column(name = "model", length = 60)
    private String model;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "ref_type", length = 20)
    private ExternalApiRefType refType;

    @Column(name = "ref_id")
    private Long refId;

    @Column(name = "success", nullable = false)
    private boolean success;

    // HTTP_{상태} | SUNO_{응답 code} | 예외 클래스명. 메시지 원문은 저장하지 않는다 (키·내부 URL 노출 방지)
    @Column(name = "error_code", length = 60)
    private String errorCode;

    @Column(name = "latency_ms", nullable = false)
    private int latencyMs;

    @Column(name = "units_in")
    private Long unitsIn;

    @Column(name = "units_out")
    private Long unitsOut;

    @Column(name = "unit_type", length = 20)
    private String unitType;

    @Column(name = "estimated_cost_usd", precision = 12, scale = 6)
    private BigDecimal estimatedCostUsd;
}
