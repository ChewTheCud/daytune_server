package com.eumakase.eumakase.controller;

import com.eumakase.eumakase.common.dto.ApiResponse;
import com.eumakase.eumakase.dto.file.FileUploadResponseDto;
import com.eumakase.eumakase.service.FirebaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v2/file")
public class FileController {
    private final FirebaseService firebaseService;

    public FileController(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    /**
     * 파일 업로드
     * @param file 업로드할 MultipartFile
     * @return 업로드 결과를 포함한 ApiResponse와 HTTP 상태 코드
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileUploadResponseDto>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 파일을 Firebase에 업로드하고 URL을 얻음
            String fileUrl = firebaseService.uploadFile(file);

            // 파일 업로드 응답 DTO 생성
            FileUploadResponseDto fileUploadResponseDto = new FileUploadResponseDto(fileUrl);

            // 성공 응답 반환
            return ResponseEntity.ok(ApiResponse.success("파일 업로드에 성공했습니다.", fileUploadResponseDto));
        } catch (Exception e) {
            // 오류 발생 시 오류 응답 반환
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("파일 업로드에 실패했습니다."));
        }
    }

    /**
     * 특정 다이어리 ID에 대한 파일 다운로드 URL 목록 반환
     * @param diaryId 파일 다운로드 URL을 가져올 다이어리 ID
     * @return 파일 다운로드 URL 목록과 HTTP 상태 코드
     */
    @GetMapping("/download/urls/{diaryId}")
    public ResponseEntity<List<String>> getDownloadUrlsByDiaryId(@PathVariable String diaryId) {
        // 다이어리 ID로 파일 다운로드 URL 목록을 가져옴
        List<String> downloadUrls = firebaseService.getFileDownloadUrlsByDiaryId(diaryId);

        // 다운로드 URL 목록이 비어 있는지 확인
        if (downloadUrls.isEmpty()) {
            // 목록이 비어 있으면 404 Not Found 응답 반환
            return ResponseEntity.notFound().build();
        }

        // 성공적으로 URL 목록을 가져왔으면 200 OK 응답 반환
        return ResponseEntity.ok(downloadUrls);
    }
}
