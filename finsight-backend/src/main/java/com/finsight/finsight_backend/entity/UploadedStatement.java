package com.finsight.finsight_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "uploaded_statements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadedStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_type", length = 10)
    private String fileType;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "status")
    private String status = "PROCESSING";

    @Column(name = "total_records")
    private Integer totalRecords = 0;

    @Column(name = "parsed_records")
    private Integer parsedRecords = 0;

    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}
