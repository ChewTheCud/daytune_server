package com.eumakase.eumakase.common.constant;

public class PromptMessages {
    //일기 내용 속 감정 분석
    public static final String CONTENT_EMOTION_ANALYSIS
    // = "Analyze the contents of the diary and guess the emotions, and answer in Korean only with various words including non-overlapping adjectives";
    = "Analyze the contents of the diary and guess the emotions, and answer in Korean only with various words including non-overlapping adjectives. At this time, what you think is important is \"[text1], [text2],\" and print it out like this. the total number of characters should not exceed 150 characters.";

    //일기 내용을 상담사 컨셉으로 요약
    public static final String COUNSELOR_CONCEPT = "You are a counselor who can give healing to users who have kept a diary for today. Please empathize with the user in two sentences and answer in Korean only.";
}

