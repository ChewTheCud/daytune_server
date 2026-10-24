package com.eumakase.eumakase.dto.chatGPT;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChatGPTRequestDtoTest {

    @Test
    void gpt_5_6_요청은_완료_토큰_상한과_추론_없음을_보낸다() throws Exception {
        ChatGPTRequestDto request = new ChatGPTRequestDto(
                "gpt-5.6-terra",
                List.of(new Message("user", "안녕")),
                1.0,
                300,
                1.0,
                1,
                0.6,
                "none"
        );

        JsonNode json = new ObjectMapper().valueToTree(request);

        assertThat(json.get("model").asText()).isEqualTo("gpt-5.6-terra");
        assertThat(json.get("max_completion_tokens").asInt()).isEqualTo(300);
        assertThat(json.get("reasoning_effort").asText()).isEqualTo("none");
        assertThat(json.has("max_tokens")).isFalse();
    }
}
