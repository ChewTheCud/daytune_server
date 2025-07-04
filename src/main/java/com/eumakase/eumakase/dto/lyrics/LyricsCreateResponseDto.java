package com.eumakase.eumakase.dto.lyrics;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class LyricsCreateResponseDto {
    @NotNull
    private Long lyricsId;

    @NotNull
    private String content;
}
