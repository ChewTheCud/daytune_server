package com.eumakase.eumakase.dto.ChatGPT;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 응답 데이터 내 choices 키 값 내부
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public class Choice implements Serializable {
    private Integer index;
    private Message message;
}
