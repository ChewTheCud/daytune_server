package com.eumakase.eumakase.repository;

import com.eumakase.eumakase.domain.DiaryQuestionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaryQuestionAnswerRepository extends JpaRepository<DiaryQuestionAnswer, Long> {
    List<DiaryQuestionAnswer> findByDiaryId(Long diaryId);
}