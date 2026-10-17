package com.eumakase.eumakase.service;

import com.eumakase.eumakase.domain.User;
import com.eumakase.eumakase.dto.admin.AdminUserDetailResponseDto;
import com.eumakase.eumakase.dto.admin.AdminUserPageResponseDto;
import com.eumakase.eumakase.dto.admin.AdminUserSummaryDto;
import com.eumakase.eumakase.exception.AdminException;
import com.eumakase.eumakase.repository.DiaryRepository;
import com.eumakase.eumakase.repository.ExternalApiUsageRepository;
import com.eumakase.eumakase.repository.MusicRepository;
import com.eumakase.eumakase.repository.ShareUrlRepository;
import com.eumakase.eumakase.repository.UserRepository;
import com.eumakase.eumakase.util.AdminStatsUtil;
import com.eumakase.eumakase.util.enums.ExternalApiProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 관리자 유저 검색·상세
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    static final int MAX_PAGE_SIZE = 50;
    private static final int RECENT_ACTIVITY_LIMIT = 20;
    private static final long NO_MATCH_ID = -1L;

    private final UserRepository userRepository;
    private final DiaryRepository diaryRepository;
    private final MusicRepository musicRepository;
    private final ShareUrlRepository shareUrlRepository;
    private final ExternalApiUsageRepository externalApiUsageRepository;

    /**
     * 유저 검색. query가 숫자면 id 일치도 포함, 이메일·닉네임은 부분 일치(대소문자 무시). 최신 가입순
     */
    public AdminUserPageResponseDto search(String query, int page, int size) {
        if (page < 0) {
            throw new AdminException(HttpStatus.BAD_REQUEST, "page는 0 이상이어야 합니다.");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new AdminException(HttpStatus.BAD_REQUEST, "size는 1~" + MAX_PAGE_SIZE + " 사이여야 합니다.");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdDate"), Sort.Order.desc("id")));

        Page<User> result;
        if (!StringUtils.hasText(query)) {
            result = userRepository.findAll(pageable);
        } else {
            String keyword = query.trim();
            long id = keyword.matches("\\d{1,18}") ? Long.parseLong(keyword) : NO_MATCH_ID;
            result = userRepository.searchForAdmin(id, "%" + escapeLike(keyword.toLowerCase(Locale.ROOT)) + "%", pageable);
        }

        List<AdminUserSummaryDto> content = result.getContent().stream()
                .map(AdminUserSummaryDto::of)
                .collect(Collectors.toList());
        return new AdminUserPageResponseDto(content, page, size, result.getTotalElements());
    }

    /**
     * 유저 상세. 카운트는 전체 기간, 비용은 from~to
     */
    public AdminUserDetailResponseDto getDetail(Long userId, LocalDate from, LocalDate to) {
        AdminStatsUtil.validateRange(from, to);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AdminException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        AdminUserDetailResponseDto.Counts counts = new AdminUserDetailResponseDto.Counts(
                diaryRepository.countByUserId(userId),
                musicRepository.countByDiaryUserId(userId),
                shareUrlRepository.countByMusicDiaryUserId(userId));

        List<AdminUserDetailResponseDto.Activity> recentActivity = Stream.concat(
                        diaryRepository.findTop20ByUserIdOrderByCreatedDateDescIdDesc(userId).stream()
                                .map(diary -> new AdminUserDetailResponseDto.Activity("DIARY", diary.getId(), diary.getCreatedDate())),
                        musicRepository.findTop20ByDiaryUserIdOrderByCreatedDateDescIdDesc(userId).stream()
                                .map(music -> new AdminUserDetailResponseDto.Activity("MUSIC", music.getId(), music.getCreatedDate())))
                .sorted(Comparator.comparing(AdminUserDetailResponseDto.Activity::getCreatedDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(RECENT_ACTIVITY_LIMIT)
                .collect(Collectors.toList());

        Map<ExternalApiProvider, Object[]> byProvider = new HashMap<>();
        for (Object[] row : externalApiUsageRepository.summarizeUserByProvider(
                userId, from.atStartOfDay(), to.plusDays(1).atStartOfDay())) {
            byProvider.put((ExternalApiProvider) row[0], row);
        }
        List<AdminUserDetailResponseDto.ProviderCost> costs = new ArrayList<>();
        for (ExternalApiProvider provider : ExternalApiProvider.values()) {
            Object[] row = byProvider.get(provider);
            long calls = row == null ? 0 : ((Number) row[1]).longValue();
            // OPENAI는 비용이 없으면 0, 나머지는 비용이 기록된 경우(SUNO)만 값
            BigDecimal cost = row == null || row[2] == null ? null : (BigDecimal) row[2];
            if (cost == null && provider == ExternalApiProvider.OPENAI) {
                cost = BigDecimal.ZERO;
            }
            if (cost != null) {
                cost = cost.setScale(6, RoundingMode.HALF_UP);
            }
            costs.add(new AdminUserDetailResponseDto.ProviderCost(provider, calls, cost));
        }

        return new AdminUserDetailResponseDto(user.getId(), user.getNickname(), user.getEmail(), user.getRole(),
                user.getCreatedDate(), user.getLastLoginDate(), user.getDeletedDate(),
                counts, recentActivity, costs);
    }

    // LIKE 와일드카드를 글자 그대로 찾도록 '!'로 이스케이프 (UserRepository.searchForAdmin의 escape '!')
    static String escapeLike(String keyword) {
        return keyword.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }
}
