package com.eumakase.eumakase.repository;

import com.eumakase.eumakase.domain.PromptCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromptCategoryRepository extends JpaRepository<PromptCategory, Long> {
    PromptCategory findByMainPrompt(String mainPrompt);
}
