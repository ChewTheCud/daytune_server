package com.eumakase.eumakase.repository;

import com.eumakase.eumakase.domain.Diary;
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
}