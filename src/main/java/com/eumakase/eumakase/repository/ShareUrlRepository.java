package com.eumakase.eumakase.repository;

import com.eumakase.eumakase.domain.ShareUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShareUrlRepository extends JpaRepository<ShareUrl, Long> {
}