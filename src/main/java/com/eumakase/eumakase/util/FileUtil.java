package com.eumakase.eumakase.util;

import java.io.*;
import java.net.URL;
import java.net.HttpURLConnection;

/**
 * 파일 다운로드 유틸리티
 * 주어진 URL에서 파일을 다운로드하여 임시 파일로 저장하는 기능
 */
public class FileUtil {
    /**
     * 주어진 URL에서 파일을 다운로드하여 임시 파일로 저장
     * @param fileUrl 다운로드할 파일의 URL
     * @return 다운로드한 임시 파일
     * @throws IOException 파일 다운로드 중 오류가 발생한 경우
     */
    public static File downloadFile(String fileUrl) throws IOException {
        // URL 객체 생성
        URL url = new URL(fileUrl);

        // URL에 대한 HttpURLConnection 객체 생성
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // HTTP 요청 메서드를 GET으로 설정
        connection.setRequestMethod("GET");

        // 연결 시도
        connection.connect();

        // 서버 응답 코드가 HTTP OK(200)인지 확인
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            // 응답 코드가 HTTP OK가 아닌 경우 예외 발생
            throw new IOException("파일 다운로드 실패: " + connection.getResponseMessage());
        }

        // 서버로부터 파일 데이터를 읽기 위한 입력 스트림 열기
        InputStream inputStream = connection.getInputStream();

        // 임시 파일 생성 (접두사 "temp", 접미사 ".mp3" 사용)
        File tempFile = File.createTempFile("temp", ".mp3");

        // 임시 파일에 데이터를 쓰기 위한 파일 출력 스트림 생성
        FileOutputStream outputStream = new FileOutputStream(tempFile);

        // 데이터 읽기 및 쓰기를 위한 버퍼 생성 (버퍼 크기: 4096 바이트)
        byte[] buffer = new byte[4096];
        int bytesRead;

        // 입력 스트림으로부터 데이터를 읽어와 버퍼에 저장하고, 출력 스트림으로 씀
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        // 출력 스트림 닫기 (자원 해제)
        outputStream.close();

        // 입력 스트림 닫기 (자원 해제)
        inputStream.close();

        // 다운로드한 임시 파일 반환
        return tempFile;
    }
}
