package com.finsight.finsight_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {
    private Long id;
    private String fileName;
    private String fileType;
    private String status;
    private Integer totalRecords;
    private Integer parsedRecords;
    private LocalDateTime uploadedAt;
    private String message;
}
