package com.eumakase.eumakase.repository;

import com.eumakase.eumakase.domain.Music;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MusicRepository extends JpaRepository<Music, Long> {

}