package com.eumakase.eumakase.dto.music;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 일기 내 Music 선택 응답 DTO
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MusicSelectionResponseDto {
    private String shareUrl;
}
