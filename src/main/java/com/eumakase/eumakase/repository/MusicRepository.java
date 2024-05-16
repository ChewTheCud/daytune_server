package com.eumakase.eumakase.repository;

import com.eumakase.eumakase.domain.Music;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MusicRepository extends JpaRepository<Music, Long> {
    List<Music> findBySunoAiMusicIdIsNotNullAndFileUrlIsNull();
    List<Music> findByDiaryIdAndIdNot(Long diaryId, Long musicId);
}