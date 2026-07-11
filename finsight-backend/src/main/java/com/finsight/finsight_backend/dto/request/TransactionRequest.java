package com.finsight.finsight_backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Type is required")
    private String type;

    @NotNull(message = "Date is required")
    private LocalDate transactionDate;

    private String description;
    private Long categoryId;
    private String merchantName;
    private Boolean isRecurring = false;
}
