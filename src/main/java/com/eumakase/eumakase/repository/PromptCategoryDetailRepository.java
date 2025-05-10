package com.eumakase.eumakase.repository;

import com.eumakase.eumakase.domain.PromptCategoryDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromptCategoryDetailRepository extends JpaRepository<PromptCategoryDetail, Long> {
    List<PromptCategoryDetail> findByPromptCategoryId(Long promptCategoryId);
}

