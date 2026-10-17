package com.eumakase.eumakase.service;

import com.eumakase.eumakase.domain.ExternalApiUsage;
import com.eumakase.eumakase.domain.User;
import com.eumakase.eumakase.dto.admin.AdminUsageDetailResponseDto;
import com.eumakase.eumakase.dto.admin.AdminUsageDetailResponseDto.Item;
import com.eumakase.eumakase.exception.AdminException;
import com.eumakase.eumakase.repository.DiaryEmotionInsightRepository;
import com.eumakase.eumakase.repository.DiaryRepository;
import com.eumakase.eumakase.repository.ExternalApiUsageRepository;
import com.eumakase.eumakase.repository.MusicRepository;
import com.eumakase.eumakase.repository.ShareUrlRepository;
import com.eumakase.eumakase.repository.UserRepository;
import com.eumakase.eumakase.util.AdminStatsUtil;
import com.eumakase.eumakase.util.enums.ExternalApiOperation;
import com.eumakase.eumakase.util.enums.UsageMetric;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 관리자 이용 현황 KPI 상세 목록 (GET /admin/usage/details). 일기 본문·가사는 내려주지 않는다
 */
@Service
@RequiredArgsConstructor
public class AdminUsageDetailService {

    static final int MAX_PAGE_SIZE = 50;

    private final UserRepository userRepository;
    private final DiaryRepository diaryRepository;
    private final DiaryEmotionInsightRepository diaryEmotionInsightRepository;
    private final MusicRepository musicRepository;
    private final ShareUrlRepository shareUrlRepository;
    private final ExternalApiUsageRepository externalApiUsageRepository;

    public AdminUsageDetailResponseDto getDetails(String metricValue, LocalDate from, LocalDate to, int page, int size) {
        UsageMetric metric = UsageMetric.fromValue(metricValue)
                .orElseThrow(() -> new AdminException(HttpStatus.BAD_REQUEST, "metric이 올바르지 않습니다."));
        AdminStatsUtil.validateRange(from, to);
        if (page < 0) {
            throw new AdminException(HttpStatus.BAD_REQUEST, "page는 0 이상이어야 합니다.");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new AdminException(HttpStatus.BAD_REQUEST, "size는 1~" + MAX_PAGE_SIZE + " 사이여야 합니다.");
        }
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();
        Pageable pageable = PageRequest.of(page, size);
        Pageable byCreated = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdDate"), Sort.Order.desc("id")));

        List<Item> content;
        long total;
        switch (metric) {
            case SIGNUPS -> {
                total = userRepository.countByCreatedDateGreaterThanEqualAndCreatedDateLessThan(start, end);
                content = userRepository.findByCreatedDateGreaterThanEqualAndCreatedDateLessThan(start, end, byCreated)
                        .stream()
                        .map(user -> new Item(user.getId(), user.getId(), user.getNickname(), user.getCreatedDate(), user.getEmail()))
                        .collect(Collectors.toList());
            }
            case ACTIVE_USERS -> {
                total = diaryRepository.countDistinctUsers(start, end);
                List<Object[]> rows = diaryRepository.findActiveUserRows(start, end, pageable);
                Map<Long, String> nicknames = nicknamesOf(rows.stream().map(row -> (Long) row[0]));
                content = rows.stream()
                        .map(row -> new Item((Long) row[0], (Long) row[0], nicknames.get((Long) row[0]),
                                (LocalDateTime) row[1], "일기 " + ((Number) row[2]).longValue() + "건"))
                        .collect(Collectors.toList());
            }
            case DIARIES -> {
                total = diaryRepository.countByCreatedDateGreaterThanEqualAndCreatedDateLessThan(start, end);
                List<Object[]> rows = diaryRepository.findDetailRows(start, end, pageable);
                Map<Long, List<String>> emotions = emotionsOf(rows);
                content = rows.stream()
                        .map(row -> new Item((Long) row[0], (Long) row[1], (String) row[2], (LocalDateTime) row[3],
                                diaryDetail((String) row[4], emotions.get((Long) row[0]))))
                        .collect(Collectors.toList());
            }
            case MUSICS -> {
                total = musicRepository.countByCreatedDateGreaterThanEqualAndCreatedDateLessThan(start, end);
                content = refRows(musicRepository.findDetailRows(start, end, pageable), "일기 #");
            }
            case SHARES -> {
                total = shareUrlRepository.countByCreatedDateGreaterThanEqualAndCreatedDateLessThan(start, end);
                content = refRows(shareUrlRepository.findDetailRows(start, end, pageable), "음악 #");
            }
            default -> {
                total = externalApiUsageRepository.countByOperationAndSuccessTrueAndCreatedDateGreaterThanEqualAndCreatedDateLessThan(
                        ExternalApiOperation.PUSH_SEND, start, end);
                List<ExternalApiUsage> usages = externalApiUsageRepository
                        .findByOperationAndSuccessTrueAndCreatedDateGreaterThanEqualAndCreatedDateLessThan(
                                ExternalApiOperation.PUSH_SEND, start, end, byCreated).getContent();
                Map<Long, String> nicknames = nicknamesOf(usages.stream().map(ExternalApiUsage::getUserId));
                content = usages.stream()
                        .map(usage -> new Item(usage.getId(), usage.getUserId(),
                                usage.getUserId() == null ? null : nicknames.get(usage.getUserId()),
                                usage.getCreatedDate(), pushDetail(usage.isSuccess(), usage.getErrorCode())))
                        .collect(Collectors.toList());
            }
        }
        return new AdminUsageDetailResponseDto(content, page, size, total);
    }

    // [id, userId, nickname, createdDate, refId] 행 → Item (detail = prefix + refId)
    private static List<Item> refRows(List<Object[]> rows, String prefix) {
        return rows.stream()
                .map(row -> new Item((Long) row[0], (Long) row[1], (String) row[2], (LocalDateTime) row[3],
                        row[4] == null ? null : prefix + row[4]))
                .collect(Collectors.toList());
    }

    private Map<Long, String> nicknamesOf(java.util.stream.Stream<Long> userIds) {
        List<Long> ids = userIds.filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return userRepository.findAllById(ids).stream()
                .filter(user -> user.getNickname() != null)
                .collect(Collectors.toMap(User::getId, User::getNickname));
    }

    private Map<Long, List<String>> emotionsOf(List<Object[]> diaryRows) {
        List<Long> diaryIds = diaryRows.stream().map(row -> (Long) row[0]).collect(Collectors.toList());
        Map<Long, List<String>> emotions = new HashMap<>();
        if (diaryIds.isEmpty()) {
            return emotions;
        }
        for (Object[] row : diaryEmotionInsightRepository.findEmotionsByDiaryIds(diaryIds)) {
            emotions.computeIfAbsent((Long) row[0], k -> new ArrayList<>()).add((String) row[1]);
        }
        return emotions;
    }

    // "카테고리 · 감정" (감정이 여러 개면 ", "로 연결). 카테고리가 없으면 "미지정", 감정이 없으면 카테고리만
    static String diaryDetail(String category, List<String> emotions) {
        String categoryText = category == null || category.isBlank() ? AdminDashboardService.CATEGORY_UNASSIGNED : category;
        if (emotions == null || emotions.isEmpty()) {
            return categoryText;
        }
        return categoryText + " · " + String.join(", ", emotions);
    }

    static String pushDetail(boolean success, String errorCode) {
        if (success) {
            return "성공";
        }
        return errorCode == null ? "실패" : "실패 · " + errorCode;
    }
}
