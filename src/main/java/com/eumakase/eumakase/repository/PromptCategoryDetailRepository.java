package com.eumakase.eumakase.repository;

import com.eumakase.eumakase.domain.PromptCategoryDetail;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromptCategoryDetailRepository extends JpaRepository<PromptCategoryDetail, Long> {
    @Query("SELECT d.prompt FROM PromptCategoryDetail d " +
            "JOIN d.promptCategory c " +
            "WHERE c.mainPrompt = :mainPrompt")
    List<String> findPromptsByMainPrompt(@Param("mainPrompt") String mainPrompt);
}

