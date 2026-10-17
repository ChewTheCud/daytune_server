package com.eumakase.eumakase.dto.sunoAI;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SunoAIGenerationDetailResultDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void 완료_응답의_audio_url을_파일_URL로_읽는다() throws Exception {
        String json = """
                {
                  "code": 200,
                  "msg": "success",
                  "data": {
                    "taskId": "5c79be8e",
                    "status": "SUCCESS",
                    "response": {
                      "taskId": "5c79be8e",
                      "sunoData": [
                        {
                          "id": "track-1",
                          "audio_url": "https://cdn.example/track-1.mp3",
                          "source_audio_url": "",
                          "stream_audio_url": "https://cdn.example/track-1",
                          "createTime": "2025-01-01 00:00:00",
                          "duration": 198.44
                        },
                        {
                          "id": "track-2",
                          "audio_url": "https://cdn.example/track-2.mp3",
                          "source_audio_url": "",
                          "createTime": "2025-01-01 00:00:00",
                          "duration": 210.0
                        }
                      ]
                    }
                  }
                }
                """;

        SunoAIGenerationDetailResultDto result = objectMapper.readValue(json, SunoAIGenerationDetailResultDto.class);
        List<SunoAIGenerationDetailResultDto.TrackInfo> tracks = result.getData().getResponse().getSunoData();

        assertThat(tracks).extracting(SunoAIGenerationDetailResultDto.TrackInfo::playableAudioUrl)
                .containsExactly(
                        "https://cdn.example/track-1.mp3",
                        "https://cdn.example/track-2.mp3"
                );
    }

    @Test
    void audioUrl이_없으면_sourceAudioUrl을_쓴다() {
        SunoAIGenerationDetailResultDto.TrackInfo track = new SunoAIGenerationDetailResultDto.TrackInfo();
        track.setSourceAudioUrl("https://cdn1.suno.ai/track.mp3");

        assertThat(track.playableAudioUrl()).isEqualTo("https://cdn1.suno.ai/track.mp3");
    }

    @Test
    void 음원_URL이_없으면_null이다() {
        SunoAIGenerationDetailResultDto.TrackInfo track = new SunoAIGenerationDetailResultDto.TrackInfo();
        track.setAudioUrl("  ");
        track.setSourceAudioUrl("");

        assertThat(track.playableAudioUrl()).isNull();
    }
}
