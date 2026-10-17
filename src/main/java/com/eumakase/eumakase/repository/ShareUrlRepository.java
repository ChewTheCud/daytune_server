package com.eumakase.eumakase.repository;

import com.eumakase.eumakase.domain.ShareUrl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShareUrlRepository extends JpaRepository<ShareUrl, Long> {

    // ---- 관리자 집계. 기간은 [from, to) ----

    long countByMusicDiaryUserId(Long userId);

    // 일별: date, 공유 수
    @Query("select cast(s.createdDate as LocalDate), count(s) from ShareUrl s"
            + " where s.createdDate >= :from and s.createdDate < :to"
            + " group by cast(s.createdDate as LocalDate)")
    List<Object[]> countDaily(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    long countByCreatedDateGreaterThanEqualAndCreatedDateLessThan(LocalDateTime from, LocalDateTime to);

    // 상세 목록: shareUrlId, userId, nickname, createdDate, musicId
    @Query("select s.id, u.id, u.nickname, s.createdDate, m.id from ShareUrl s"
            + " left join s.music m left join m.diary d left join d.user u"
            + " where s.createdDate >= :from and s.createdDate < :to"
            + " order by s.createdDate desc, s.id desc")
    List<Object[]> findDetailRows(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);
}