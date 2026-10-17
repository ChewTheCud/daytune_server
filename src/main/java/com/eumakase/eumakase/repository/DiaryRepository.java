package com.eumakase.eumakase.repository;

import com.eumakase.eumakase.domain.Diary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
    List<Diary> findByUserId(Long userId);

    // ---- 관리자 집계. 기간은 [from, to) ----

    long countByUserId(Long userId);

    List<Diary> findTop20ByUserIdOrderByCreatedDateDescIdDesc(Long userId);

    // 일별: date, 일기 수, 작성 유저 수(중복 제거)
    @Query("select cast(d.createdDate as LocalDate), count(d), count(distinct d.user.id) from Diary d"
            + " where d.createdDate >= :from and d.createdDate < :to"
            + " group by cast(d.createdDate as LocalDate)")
    List<Object[]> countDaily(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // 기간 전체 작성 유저 수(중복 제거)
    @Query("select count(distinct d.user.id) from Diary d where d.createdDate >= :from and d.createdDate < :to")
    long countDistinctUsers(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // 카테고리별: categoryId(null 가능), 이름, 일기 수 — 일기 수 내림차순 → 카테고리 없는 일기는 맨 뒤
    @Query("select c.id, c.mainPrompt, count(d) from Diary d left join d.promptCategory c"
            + " where d.createdDate >= :from and d.createdDate < :to"
            + " group by c.id, c.mainPrompt order by count(d) desc, c.id asc nulls last")
    List<Object[]> countByCategory(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // 히트맵용 작성 시각
    @Query("select d.createdDate from Diary d where d.createdDate >= :from and d.createdDate < :to")
    List<LocalDateTime> findCreatedDates(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // ---- 상세 목록 (KPI 모달) ----

    long countByCreatedDateGreaterThanEqualAndCreatedDateLessThan(LocalDateTime from, LocalDateTime to);

    // 일기 상세: diaryId, userId, nickname, createdDate, 카테고리 이름 (정렬은 쿼리 고정)
    @Query("select d.id, u.id, u.nickname, d.createdDate, c.mainPrompt from Diary d"
            + " left join d.user u left join d.promptCategory c"
            + " where d.createdDate >= :from and d.createdDate < :to"
            + " order by d.createdDate desc, d.id desc")
    List<Object[]> findDetailRows(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

    // 활동 유저 상세: userId, 마지막 일기 시각, 일기 수 — 마지막 일기 시각 내림차순
    @Query("select d.user.id, max(d.createdDate), count(d) from Diary d"
            + " where d.createdDate >= :from and d.createdDate < :to"
            + " group by d.user.id order by max(d.createdDate) desc, d.user.id desc")
    List<Object[]> findActiveUserRows(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);
}