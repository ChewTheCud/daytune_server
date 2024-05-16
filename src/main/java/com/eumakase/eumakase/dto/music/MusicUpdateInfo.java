package com.eumakase.eumakase.dto.music;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MusicUpdateInfo {
    private Long diaryId;
    private Long musicId;
    private String audioUrl;
}
