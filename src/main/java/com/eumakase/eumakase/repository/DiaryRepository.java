package com.eumakase.eumakase.repository;

import com.eumakase.eumakase.domain.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
    List<Diary> findByUserId(Long userId);
}