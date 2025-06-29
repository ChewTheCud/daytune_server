package com.eumakase.eumakase.repository;

import com.eumakase.eumakase.domain.Lyrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LyricsRepository extends JpaRepository<Lyrics, Long> {
    List<Lyrics> findByMusicId(Long musicId);
}
