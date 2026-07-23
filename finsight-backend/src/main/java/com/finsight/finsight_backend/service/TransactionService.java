package com.finsight.finsight_backend.service;

import com.finsight.finsight_backend.dto.request.TransactionRequest;
import com.finsight.finsight_backend.dto.response.SummaryResponse;
import com.finsight.finsight_backend.dto.response.TransactionResponse;
import com.finsight.finsight_backend.entity.Category;
import com.finsight.finsight_backend.entity.Transaction;
import com.finsight.finsight_backend.entity.User;
import com.finsight.finsight_backend.repository.CategoryRepository;
import com.finsight.finsight_backend.repository.TransactionRepository;
import com.finsight.finsight_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AutoCategorizationService autoCategorizationService;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .title(t.getTitle())
                .amount(t.getAmount())
                .type(t.getType())
                .transactionDate(t.getTransactionDate())
                .description(t.getDescription())
                .categoryName(t.getCategory() != null ? t.getCategory().getName() : null)
                .categoryIcon(t.getCategory() != null ? t.getCategory().getIcon() : null)
                .categoryColor(t.getCategory() != null ? t.getCategory().getColorHex() : null)
                .merchantName(t.getMerchantName())
                .isRecurring(t.getIsRecurring())
                .source(t.getSource())
                .createdAt(t.getCreatedAt())
                .build();
    }

    public TransactionResponse addTransaction(TransactionRequest request) {
        User user = getCurrentUser();

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId()).orElse(null);
        }

        if (category == null) {
            category = autoCategorizationService.categorize(
                request.getTitle(),
                request.getMerchantName()
            );
        }

        Transaction transaction = Transaction.builder()
                .user(user)
                .category(category)
                .title(request.getTitle())
                .amount(request.getAmount())
                .type(request.getType().toUpperCase())
                .transactionDate(request.getTransactionDate())
                .description(request.getDescription())
                .merchantName(request.getMerchantName())
                .isRecurring(request.getIsRecurring())
                .source("MANUAL")
                .build();

        return toResponse(transactionRepository.save(transaction));
    }

    public List<TransactionResponse> getAllTransactions() {
        User user = getCurrentUser();
        return transactionRepository
                .findByUserIdOrderByTransactionDateDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TransactionResponse getTransactionById(Long id) {
        User user = getCurrentUser();
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        return toResponse(transaction);
    }

    public TransactionResponse updateTransaction(Long id, TransactionRequest request) {
        User user = getCurrentUser();
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId()).orElse(null);
        }
        if (category == null) {
            category = autoCategorizationService.categorize(
                request.getTitle(),
                request.getMerchantName()
            );
        }

        transaction.setTitle(request.getTitle());
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType().toUpperCase());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setDescription(request.getDescription());
        transaction.setCategory(category);
        transaction.setMerchantName(request.getMerchantName());
        transaction.setIsRecurring(request.getIsRecurring());

        return toResponse(transactionRepository.save(transaction));
    }

    public void deleteTransaction(Long id) {
        User user = getCurrentUser();
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        transactionRepository.delete(transaction);
    }

    public SummaryResponse getSummary() {
        User user = getCurrentUser();
        BigDecimal totalIncome = transactionRepository
                .sumAmountByUserIdAndType(user.getId(), "INCOME");
        BigDecimal totalExpenses = transactionRepository
                .sumAmountByUserIdAndType(user.getId(), "EXPENSE");
        BigDecimal savings = totalIncome.subtract(totalExpenses);
        return SummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .savings(savings)
                .build();
    }
}
