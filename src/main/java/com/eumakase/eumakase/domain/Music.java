package com.eumakase.eumakase.domain;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import com.eumakase.eumakase.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class Music extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id")
    private Diary diary;

    @Column
    private String generationPrompt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_category_id")
    private PromptCategory promptCategory;

    @Column(name = "suno_ai_music_id")
    private String sunoAiMusicId;

    @Column(name = "file_url")
    private String fileUrl;
}