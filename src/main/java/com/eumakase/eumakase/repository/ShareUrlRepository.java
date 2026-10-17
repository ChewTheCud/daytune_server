package com.eumakase.eumakase.repository;

import com.eumakase.eumakase.domain.ShareUrl;
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
}