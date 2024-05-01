package com.eumakase.eumakase.dto.music;

import lombok.*;

@Data
public class MusicUpdateFileUrlsResultDto {
    private int totalNullUrls;
    private int updatedUrlsCount;

    // 생성자
    public MusicUpdateFileUrlsResultDto(int totalNullUrls, int updatedUrlsCount) {
        this.totalNullUrls = totalNullUrls;
        this.updatedUrlsCount = updatedUrlsCount;
    }

    // Getter 메소드
    public int getTotalNullUrls() {
        return totalNullUrls;
    }

    public int getUpdatedUrlsCount() {
        return updatedUrlsCount;
    }

    // Setter 메소드 (필요한 경우)
    public void setTotalNullUrls(int totalNullUrls) {
        this.totalNullUrls = totalNullUrls;
    }

    public void setUpdatedUrlsCount(int updatedUrlsCount) {
        this.updatedUrlsCount = updatedUrlsCount;
    }
}
