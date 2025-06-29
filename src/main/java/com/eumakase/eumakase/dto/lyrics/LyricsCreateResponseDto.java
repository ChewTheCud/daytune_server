package com.eumakase.eumakase.dto.lyrics;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class LyricsCreateResponseDto {
    private String content;
}
