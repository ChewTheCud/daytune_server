package com.eumakase.eumakase.dto.lyrics;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class LyricsCreateRequestDto {
    @NotNull
    private String mood;

    @NotNull
    private String diaryContent;
}
