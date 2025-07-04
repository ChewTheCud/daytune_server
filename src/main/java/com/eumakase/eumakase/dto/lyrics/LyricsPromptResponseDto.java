package com.eumakase.eumakase.dto.lyrics;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class LyricsPromptResponseDto {
    private String content;
}
