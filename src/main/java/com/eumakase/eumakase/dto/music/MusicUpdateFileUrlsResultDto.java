package com.eumakase.eumakase.dto.music;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
public class MusicUpdateFileUrlsResultDto {
    private List<MusicUpdateInfo> updatedMusicFiles;
    private List<MusicUpdateInfo> notUpdatedMusicFiles;
}

