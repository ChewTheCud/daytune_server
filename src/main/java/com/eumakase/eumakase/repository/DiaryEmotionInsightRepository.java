package com.eumakase.eumakase.repository;

import com.eumakase.eumakase.domain.DiaryEmotionInsight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaryEmotionInsightRepository extends JpaRepository<DiaryEmotionInsight, Long> {
    List<DiaryEmotionInsight> findByDiaryId(Long diaryId);
}