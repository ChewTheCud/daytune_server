package com.eumakase.eumakase.dto.music;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 특정 일기의 Music 정보 조회 응답 DTO
 */
@Builder
@NoArgsConstructor
@Data
public class MusicReadResponseDto {
    private Long musicId;
    private String fileUrl;

    public MusicReadResponseDto(Long id, String fileUrl) {
        this.musicId = id;
        this.fileUrl = fileUrl;
    }
}
