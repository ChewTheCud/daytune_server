package com.eumakase.eumakase.dto.lyrics;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class LyricsReadResponseDto {
    private Long lyricsId;
    private Long musicId;
    private String content;
}
