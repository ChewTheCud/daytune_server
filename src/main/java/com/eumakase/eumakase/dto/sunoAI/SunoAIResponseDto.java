package com.eumakase.eumakase.dto.sunoAI;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

/**
 * SunoAI 생성 응답 DTO
 */
@NoArgsConstructor
@Data
public class SunoAIResponseDto implements Serializable {
    private String id;
    private String title;

    @JsonProperty("image_url")
    private String imageUrl;
    private String lyric;

    @JsonProperty("audio_url")
    private String audioUrl;

    @JsonProperty("video_url")
    private String videoUrl;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("model_name")
    private String modelName;

    private String status;

    @JsonProperty("gpt_description_prompt")
    private String gptDescriptionPrompt;

    private String prompt;
    private String type;
    private String tags;
}
