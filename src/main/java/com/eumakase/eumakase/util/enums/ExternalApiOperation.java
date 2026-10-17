package com.eumakase.eumakase.util.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 미터링 기록 단위(기능). 제공사는 기능마다 고정이다
 */
@Getter
@RequiredArgsConstructor
public enum ExternalApiOperation {
    FOLLOW_UP_QUESTION(ExternalApiProvider.OPENAI),
    EMOTION_INSIGHT(ExternalApiProvider.OPENAI),
    LYRICS_GENERATE(ExternalApiProvider.OPENAI),
    MUSIC_GENERATE(ExternalApiProvider.SUNO),
    MUSIC_STATUS_POLL(ExternalApiProvider.SUNO), // 상태 조회. 과금 단위가 아니므로 생성 건수와 분리한다
    S3_UPLOAD(ExternalApiProvider.AWS_S3),
    STORAGE_UPLOAD(ExternalApiProvider.FIREBASE),
    PUSH_SEND(ExternalApiProvider.FIREBASE);

    private final ExternalApiProvider provider;
}
