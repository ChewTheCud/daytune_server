package com.eumakase.eumakase.dto.file;

import com.eumakase.eumakase.domain.Diary;
import lombok.*;

import java.io.Serializable;

/**
 * 파일업로드 응답 DTO
 */
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public class FileUploadResponseDto implements Serializable {
    private String fileUrl;

    public FileUploadResponseDto(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}