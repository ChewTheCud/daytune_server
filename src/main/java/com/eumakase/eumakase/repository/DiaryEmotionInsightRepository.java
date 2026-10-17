package com.eumakase.eumakase.repository;

import com.eumakase.eumakase.domain.DiaryEmotionInsight;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface DiaryEmotionInsightRepository extends JpaRepository<DiaryEmotionInsight, Long> {
    List<DiaryEmotionInsight> findByDiaryId(Long diaryId);

    // ---- 관리자 집계. 기간은 일기 작성 시각 기준 [from, to) ----

    // 감정별: emotion, count — count 내림차순 → emotion 오름차순 (상한은 Pageable)
    @Query("select i.emotion, count(i) from DiaryEmotionInsight i"
            + " where i.diary.createdDate >= :from and i.diary.createdDate < :to"
            + " group by i.emotion order by count(i) desc, i.emotion asc")
    List<Object[]> countByEmotion(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

    // 상세 목록용: 일기별 감정 (diaryId, emotion) 입력 순서
    @Query("select i.diary.id, i.emotion from DiaryEmotionInsight i where i.diary.id in :diaryIds order by i.id asc")
    List<Object[]> findEmotionsByDiaryIds(@Param("diaryIds") Collection<Long> diaryIds);
}