package com.eumakase.eumakase.dto.sunoAI;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SunoAIGenerationDetailResultDto {
    private int code;
    private String msg;
    private DataNode data;

    @Data
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
        private long createTime;
    }

    @Data
    public static class ResponseNode {
        @JsonProperty("taskId")
        private String taskId;

        @JsonProperty("sunoData")
        private List<TrackInfo> sunoData;
    }

    @Data
    public static class TrackInfo {
        private String id;

        @JsonProperty("audioUrl")
        private String audioUrl;

        @JsonProperty("sourceAudioUrl")
        private String sourceAudioUrl;

        @JsonProperty("streamAudioUrl")
        private String streamAudioUrl;

        @JsonProperty("sourceStreamAudioUrl")
        private String sourceStreamAudioUrl;

        @JsonProperty("imageUrl")
        private String imageUrl;

        @JsonProperty("sourceImageUrl")
        private String sourceImageUrl;

        private String prompt;

        @JsonProperty("modelName")
        private String modelName;

        private String title;
        private String tags;
        private Long createTime;
        private Double duration;
    }
}
