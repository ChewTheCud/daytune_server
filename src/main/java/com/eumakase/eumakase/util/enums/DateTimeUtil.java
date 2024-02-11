package com.eumakase.eumakase.util.enums;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {

    // 공통으로 사용할 DateTimeFormatter 정의
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * LocalDateTime 객체를 "yyyy-MM-dd HH:mm:ss" 포맷의 문자열로 변환
     *
     * @param dateTime LocalDateTime 객체
     * @return 포맷된 날짜 및 시간 문자열
     */
    public static LocalDateTime format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }

        // "yyyy-MM-dd HH:mm:ss" 형태로 날짜와 시간을 포맷
        String formattedDateTime = dateTime.format(FORMATTER);

        // 포맷된 문자열을 다시 LocalDateTime으로 파싱
        return LocalDateTime.parse(formattedDateTime, FORMATTER);
    }
}

