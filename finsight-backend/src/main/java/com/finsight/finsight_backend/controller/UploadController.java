package com.finsight.finsight_backend.controller;

import com.finsight.finsight_backend.dto.response.UploadResponse;
import com.finsight.finsight_backend.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @PostMapping("/csv")
    public ResponseEntity<UploadResponse> uploadCsv(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(uploadService.uploadCsv(file));
    }

    @PostMapping("/pdf")
    public ResponseEntity<UploadResponse> uploadPdf(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(uploadService.uploadPdf(file));
    }

    @GetMapping("/history")
    public ResponseEntity<List<UploadResponse>> getHistory() {
        return ResponseEntity.ok(uploadService.getUploadHistory());
    }
}
