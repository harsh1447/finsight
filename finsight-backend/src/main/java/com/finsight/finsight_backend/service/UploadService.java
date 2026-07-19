package com.finsight.finsight_backend.service;

import com.finsight.finsight_backend.dto.response.UploadResponse;
import com.finsight.finsight_backend.entity.UploadedStatement;
import com.finsight.finsight_backend.entity.User;
import com.finsight.finsight_backend.repository.UploadedStatementRepository;
import com.finsight.finsight_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UploadService {

    private final UploadedStatementRepository uploadedStatementRepository;
    private final UserRepository userRepository;
    private final CsvParserService csvParserService;
    private final PdfParserService pdfParserService;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private UploadResponse toResponse(UploadedStatement s) {
        return UploadResponse.builder()
                .id(s.getId())
                .fileName(s.getFileName())
                .fileType(s.getFileType())
                .status(s.getStatus())
                .totalRecords(s.getTotalRecords())
                .parsedRecords(s.getParsedRecords())
                .uploadedAt(s.getUploadedAt())
                .build();
    }

    public UploadResponse uploadCsv(MultipartFile file) {
        User user = getCurrentUser();

        UploadedStatement statement = UploadedStatement.builder()
                .user(user)
                .fileName(file.getOriginalFilename())
                .fileType("CSV")
                .status("PROCESSING")
                .build();
        statement = uploadedStatementRepository.save(statement);

        try {
            int parsed = csvParserService.parseAndSave(file, user);
            statement.setStatus("COMPLETED");
            statement.setParsedRecords(parsed);
            statement.setTotalRecords(parsed);
            uploadedStatementRepository.save(statement);
            return UploadResponse.builder()
                    .id(statement.getId())
                    .fileName(statement.getFileName())
                    .fileType("CSV")
                    .status("COMPLETED")
                    .parsedRecords(parsed)
                    .totalRecords(parsed)
                    .uploadedAt(statement.getUploadedAt())
                    .message(parsed + " transactions imported successfully")
                    .build();
        } catch (Exception e) {
            statement.setStatus("FAILED");
            uploadedStatementRepository.save(statement);
            throw new RuntimeException("Failed to parse CSV: " + e.getMessage());
        }
    }

    public UploadResponse uploadPdf(MultipartFile file) {
        User user = getCurrentUser();

        UploadedStatement statement = UploadedStatement.builder()
                .user(user)
                .fileName(file.getOriginalFilename())
                .fileType("PDF")
                .status("PROCESSING")
                .build();
        statement = uploadedStatementRepository.save(statement);

        try {
            int parsed = pdfParserService.parseAndSave(file, user);
            statement.setStatus("COMPLETED");
            statement.setParsedRecords(parsed);
            statement.setTotalRecords(parsed);
            uploadedStatementRepository.save(statement);
            return UploadResponse.builder()
                    .id(statement.getId())
                    .fileName(statement.getFileName())
                    .fileType("PDF")
                    .status("COMPLETED")
                    .parsedRecords(parsed)
                    .totalRecords(parsed)
                    .uploadedAt(statement.getUploadedAt())
                    .message(parsed + " transactions imported successfully")
                    .build();
        } catch (Exception e) {
            statement.setStatus("FAILED");
            uploadedStatementRepository.save(statement);
            throw new RuntimeException("Failed to parse PDF: " + e.getMessage());
        }
    }

    public List<UploadResponse> getUploadHistory() {
        User user = getCurrentUser();
        return uploadedStatementRepository
                .findByUserIdOrderByUploadedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
