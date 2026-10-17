package com.eumakase.eumakase.service;

import com.amazonaws.AmazonServiceException;
import com.eumakase.eumakase.common.constant.OpenAiPricing;
import com.eumakase.eumakase.domain.ExternalApiUsage;
import com.eumakase.eumakase.repository.ExternalApiUsageRepository;
import com.eumakase.eumakase.security.UserPrincipal;
import com.eumakase.eumakase.util.enums.ExternalApiOperation;
import com.eumakase.eumakase.util.enums.ExternalApiProvider;
import com.eumakase.eumakase.util.enums.ExternalApiRefType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.HttpStatusCodeException;

import java.math.BigDecimal;

/**
 * 외부 유료 API 호출 1건을 미터링 로그(external_api_usage) 1행으로 기록한다.
 * - 본 기능을 절대 막지 않는다: 기록 실패는 로그만 남기고 삼킨다
 * - 별도 트랜잭션(REQUIRES_NEW)으로 저장해 호출한 쪽 트랜잭션이 롤백돼도 기록은 남는다
 * - 사용 방법: 호출 직전 start = System.nanoTime(), 호출 후 success/failure/record 호출
 */
@Slf4j
@Service
public class ExternalApiUsageRecorder {

    static final String UNIT_TYPE_TOKEN = "TOKEN";
    static final String UNIT_TYPE_CREDIT = "CREDIT";
    private static final int COLUMN_MAX_LENGTH = 60; // model·error_code 컬럼 길이

    private final ExternalApiUsageRepository externalApiUsageRepository;
    private final TransactionTemplate requiresNewTransaction;

    public ExternalApiUsageRecorder(ExternalApiUsageRepository externalApiUsageRepository,
                                    PlatformTransactionManager transactionManager) {
        this.externalApiUsageRepository = externalApiUsageRepository;
        this.requiresNewTransaction = new TransactionTemplate(transactionManager);
        this.requiresNewTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /**
     * 성공 기록 (현재 요청의 인증 유저, 연관 엔티티 없음)
     */
    public void success(ExternalApiOperation operation, long startNanos) {
        record(operation, startNanos, currentUserId(), null, null, null, null, null, null);
    }

    /**
     * 실패 기록 (예외로부터 error_code 산출)
     */
    public void failure(ExternalApiOperation operation, long startNanos, Throwable error) {
        record(operation, startNanos, currentUserId(), null, null, null, null, null, errorCode(error));
    }

    /**
     * 실패 기록 (예외 없이 응답 본문으로 실패를 판단한 경우)
     */
    public void failure(ExternalApiOperation operation, long startNanos, String errorCode) {
        record(operation, startNanos, currentUserId(), null, null, null, null, null, errorCode);
    }

    /**
     * 전체 항목 기록. errorCode가 null이면 성공이다.
     * OpenAI 성공 건은 model·토큰 수로 비용을 추정해 저장한다 (단가를 모르는 모델이면 null)
     */
    public void record(ExternalApiOperation operation, long startNanos,
                       Long userId, ExternalApiRefType refType, Long refId,
                       String model, Long unitsIn, Long unitsOut, String errorCode) {
        save(operation, startNanos, userId, refType, refId, model, unitsIn, unitsOut, errorCode, null, null);
    }

    /**
     * 크레딧 단위 기록 (Suno). creditCost는 성공 건의 추정 비용이고, 산정할 수 없으면 null
     */
    public void recordCredit(ExternalApiOperation operation, long startNanos,
                             Long userId, ExternalApiRefType refType, Long refId,
                             String model, Long credits, BigDecimal creditCost, String errorCode) {
        boolean success = errorCode == null;
        save(operation, startNanos, userId, refType, refId, model, null, success ? credits : null, errorCode,
                success && credits != null ? UNIT_TYPE_CREDIT : null, success ? creditCost : null);
    }

    private void save(ExternalApiOperation operation, long startNanos,
                      Long userId, ExternalApiRefType refType, Long refId,
                      String model, Long unitsIn, Long unitsOut, String errorCode,
                      String creditUnitType, BigDecimal creditCost) {
        try {
            int latencyMs = (int) Math.min(Integer.MAX_VALUE, (System.nanoTime() - startNanos) / 1_000_000);
            boolean success = errorCode == null;
            boolean hasTokens = unitsIn != null || unitsOut != null;
            BigDecimal cost = creditCost;
            if (success && operation.getProvider() == ExternalApiProvider.OPENAI) {
                cost = OpenAiPricing.estimate(model, unitsIn, unitsOut);
                if (cost == null) {
                    log.warn("[미터링] OpenAI 비용을 산정하지 못했습니다. 단가표에 없는 모델이거나 토큰 수가 없습니다: model={}", model);
                }
            }

            ExternalApiUsage usage = ExternalApiUsage.builder()
                    .provider(operation.getProvider())
                    .operation(operation)
                    .model(truncate(model))
                    .userId(userId)
                    .refType(refType)
                    .refId(refId)
                    .success(success)
                    .errorCode(truncate(errorCode))
                    .latencyMs(latencyMs)
                    .unitsIn(unitsIn)
                    .unitsOut(unitsOut)
                    .unitType(creditUnitType != null ? creditUnitType : hasTokens ? UNIT_TYPE_TOKEN : null)
                    .estimatedCostUsd(cost)
                    .build();

            requiresNewTransaction.executeWithoutResult(status -> externalApiUsageRepository.save(usage));
        } catch (Exception e) {
            // 미터링 실패가 일기·음악 생성을 실패시키면 안 된다
            log.error("[미터링] 외부 API 사용량 기록에 실패했습니다: operation={}, error={}", operation, e.getClass().getSimpleName(), e);
        }
    }

    /**
     * 현재 요청의 인증 유저 id. 배치·스케줄러 경로처럼 인증 정보가 없으면 null
     */
    public static Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getUserId();
        }
        return null;
    }

    /**
     * 예외 → error_code. HTTP 상태가 있으면 HTTP_{상태}, 없으면 가장 안쪽 원인의 클래스명. 메시지는 쓰지 않는다
     */
    public static String errorCode(Throwable error) {
        if (error == null) {
            return "UNKNOWN";
        }
        Throwable deepest = error;
        for (Throwable t = error; t != null; t = t.getCause()) {
            if (t instanceof HttpStatusCodeException httpError) {
                return "HTTP_" + httpError.getStatusCode().value();
            }
            if (t instanceof AmazonServiceException awsError) {
                return "HTTP_" + awsError.getStatusCode();
            }
            deepest = t;
        }
        return deepest.getClass().getSimpleName();
    }

    private static String truncate(String value) {
        if (value == null || value.length() <= COLUMN_MAX_LENGTH) {
            return value;
        }
        return value.substring(0, COLUMN_MAX_LENGTH);
    }
}
