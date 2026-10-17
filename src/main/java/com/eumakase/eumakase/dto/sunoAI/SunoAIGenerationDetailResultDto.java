package com.eumakase.eumakase.dto.sunoAI;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SunoAIGenerationDetailResultDto {
    private int code;
    private String msg;
    private DataNode data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataNode {
        @JsonProperty("taskId")
        private String taskId;

        @JsonProperty("parentMusicId")
        private String parentMusicId;

        private String param;

        // "response" 객체 아래에 트랙 정보가 있음
        private ResponseNode response;

        private String status;
        private String type;
        private String operationType;
        private String errorCode;
        private String errorMessage;
    }

    @Data
    public static class ResponseNode {
        @JsonProperty("taskId")
        private String taskId;

        @JsonProperty("sunoData")
        private List<TrackInfo> sunoData;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TrackInfo {
        private String id;

        @JsonProperty("audioUrl")
        @JsonAlias("audio_url")
        private String audioUrl;

        @JsonProperty("sourceAudioUrl")
        @JsonAlias("source_audio_url")
        private String sourceAudioUrl;

        @JsonProperty("streamAudioUrl")
        @JsonAlias("stream_audio_url")
        private String streamAudioUrl;

        @JsonProperty("sourceStreamAudioUrl")
        @JsonAlias("source_stream_audio_url")
        private String sourceStreamAudioUrl;

        @JsonProperty("imageUrl")
        @JsonAlias("image_url")
        private String imageUrl;

        @JsonProperty("sourceImageUrl")
        @JsonAlias("source_image_url")
        private String sourceImageUrl;

        private String prompt;

        @JsonProperty("modelName")
        @JsonAlias("model_name")
        private String modelName;

        private String title;
        private String tags;
        private Double duration;

        /**
         * 저장에 쓸 음원 URL. 유지되는 audioUrl을 우선하고, 비어 있으면 sourceAudioUrl을 쓴다.
         */
        public String playableAudioUrl() {
            if (hasText(audioUrl)) {
                return audioUrl;
            }
            if (hasText(sourceAudioUrl)) {
                return sourceAudioUrl;
            }
            return null;
        }

        private static boolean hasText(String value) {
            return value != null && !value.isBlank();
        }
    }
}
