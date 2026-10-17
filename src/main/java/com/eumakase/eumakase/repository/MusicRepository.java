package com.eumakase.eumakase.repository;

import com.eumakase.eumakase.domain.Music;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MusicRepository extends JpaRepository<Music, Long> {
    List<Music> findBySunoAiMusicIdIsNotNullAndFileUrlIsNull();
    List<Music> findBySunoAiMusicIdIsNotNullAndFileUrlIsNullOrFileUrl(String emptyString);
    List<Music> findByDiaryIdAndIdNot(Long diaryId, Long musicId);
    List<Music> findByDiaryId(Long diaryId);

    // ---- 관리자 집계. 기간은 [from, to) ----

    long countByDiaryUserId(Long userId);

    List<Music> findTop20ByDiaryUserIdOrderByCreatedDateDescIdDesc(Long userId);

    // 일별: date, 음악 수
    @Query("select cast(m.createdDate as LocalDate), count(m) from Music m"
            + " where m.createdDate >= :from and m.createdDate < :to"
            + " group by cast(m.createdDate as LocalDate)")
    List<Object[]> countDaily(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    long countByCreatedDateGreaterThanEqualAndCreatedDateLessThan(LocalDateTime from, LocalDateTime to);

    // 상세 목록: musicId, userId, nickname, createdDate, diaryId
    @Query("select m.id, u.id, u.nickname, m.createdDate, d.id from Music m"
            + " left join m.diary d left join d.user u"
            + " where m.createdDate >= :from and m.createdDate < :to"
            + " order by m.createdDate desc, m.id desc")
    List<Object[]> findDetailRows(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);
}