package com.eumakase.eumakase.controller;

import com.eumakase.eumakase.common.dto.ApiResponse;
import com.eumakase.eumakase.dto.file.FileUploadResponseDto;
import com.eumakase.eumakase.service.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/file")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileUploadResponseDto>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = fileService.uploadFile(file);
            FileUploadResponseDto fileUploadResponseDto = new FileUploadResponseDto(fileUrl);
            return ResponseEntity.ok(ApiResponse.success("파일 업로드에 성공했습니다.", fileUploadResponseDto));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("파일 업로드에 실패했습니다."));
        }
    }

    @GetMapping("/download/urls/{diaryId}")
    public ResponseEntity<List<String>> getDownloadUrlsByDiaryId(@PathVariable String diaryId) {
        List<String> downloadUrls = fileService.getFileDownloadUrlsByDiaryId(diaryId);
        if (downloadUrls.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(downloadUrls);
    }
}
