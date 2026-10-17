package com.eumakase.eumakase.service;

import com.eumakase.eumakase.domain.Music;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiaryServiceMusicUrlTest {

    @Test
    void suno_audio_url도_일기_재생_주소로_쓴다() {
        Music music = Music.builder().fileUrl("https://cdn.example/track.mp3").build();

        assertThat(DiaryService.resolveMusicUrl(List.of(music)))
                .isEqualTo("https://cdn.example/track.mp3");
    }

    @Test
    void 선택된_firebase_주소를_우선한다() {
        Music suno = Music.builder().fileUrl("https://cdn.example/track.mp3").build();
        Music firebase = Music.builder()
                .fileUrl("https://storage.googleapis.com/bucket/music/diaryId_1.mp3")
                .build();

        assertThat(DiaryService.resolveMusicUrl(List.of(suno, firebase)))
                .isEqualTo("https://storage.googleapis.com/bucket/music/diaryId_1.mp3");
    }

    @Test
    void 빈_주소는_건너뛰고_있는_주소를_쓴다() {
        Music empty = Music.builder().fileUrl(null).build();
        Music ready = Music.builder().fileUrl("https://cdn1.suno.ai/track.mp3").build();

        assertThat(DiaryService.resolveMusicUrl(List.of(empty, ready)))
                .isEqualTo("https://cdn1.suno.ai/track.mp3");
    }

    @Test
    void 재생_주소가_없으면_null이다() {
        assertThat(DiaryService.resolveMusicUrl(List.of())).isNull();
        assertThat(DiaryService.resolveMusicUrl(List.of(Music.builder().fileUrl("").build()))).isNull();
    }
}
