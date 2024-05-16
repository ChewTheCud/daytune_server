package com.eumakase.eumakase.util;

import java.io.*;
import java.net.URL;
import java.net.HttpURLConnection;

/**
 * 파일 다운로드 유틸리티
 */
public class FileUtil {
    /**
     * 주어진 URL에서 파일을 다운로드하여 임시 파일로 저장.
     * @param fileUrl 파일 URL
     * @return 다운로드한 파일
     * @throws IOException 파일 다운로드 중 오류가 발생한 경우
     */
    public static File downloadFile(String fileUrl) throws IOException {
        // URL 연결
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to download file: " + connection.getResponseMessage());
        }

        // 입력 스트림을 열어 파일 데이터 읽어들임
        InputStream inputStream = connection.getInputStream();

        // 임시 파일 생성
        File tempFile = File.createTempFile("temp", ".mp3");
        FileOutputStream outputStream = new FileOutputStream(tempFile);

        // 버퍼를 사용하여 데이터를 읽고 임시 파일에 씀
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        outputStream.close();
        inputStream.close();

        return tempFile;
    }
}