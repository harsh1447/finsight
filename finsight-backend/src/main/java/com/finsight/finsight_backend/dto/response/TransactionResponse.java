package com.finsight.finsight_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private String title;
    private BigDecimal amount;
    private String type;
    private LocalDate transactionDate;
    private String description;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;
    private String merchantName;
    private Boolean isRecurring;
    private String source;
    private LocalDateTime createdAt;
}
