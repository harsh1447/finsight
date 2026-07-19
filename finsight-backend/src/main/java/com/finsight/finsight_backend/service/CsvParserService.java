package com.finsight.finsight_backend.service;

import com.finsight.finsight_backend.entity.Category;
import com.finsight.finsight_backend.entity.Transaction;
import com.finsight.finsight_backend.entity.User;
import com.finsight.finsight_backend.repository.CategoryRepository;
import com.finsight.finsight_backend.repository.TransactionRepository;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CsvParserService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    private static final List<DateTimeFormatter> DATE_FORMATS = Arrays.asList(
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy")
    );

    public int parseAndSave(MultipartFile file, User user) throws Exception {
        int count = 0;
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] headers = reader.readNext();
            if (headers == null) return 0;

            String[] row;
            while ((row = reader.readNext()) != null) {
                try {
                    if (row.length < 3) continue;

                    String title = row[0].trim();
                    String amountStr = row[1].trim().replaceAll("[^0-9.-]", "");
                    String type = row[2].trim().toUpperCase();
                    String dateStr = row.length > 3 ? row[3].trim() : LocalDate.now().toString();
                    String categoryName = row.length > 4 ? row[4].trim() : "Other";
                    String merchant = row.length > 5 ? row[5].trim() : "";

                    if (title.isEmpty() || amountStr.isEmpty()) continue;
                    if (!type.equals("INCOME") && !type.equals("EXPENSE")) type = "EXPENSE";

                    BigDecimal amount = new BigDecimal(amountStr);
                    LocalDate date = parseDate(dateStr);

                    Category category = categoryRepository.findByName(categoryName)
                            .orElse(categoryRepository.findByName("Other").orElse(null));

                    Transaction transaction = Transaction.builder()
                            .user(user)
                            .title(title)
                            .amount(amount)
                            .type(type)
                            .transactionDate(date)
                            .category(category)
                            .merchantName(merchant)
                            .source("CSV")
                            .build();

                    transactionRepository.save(transaction);
                    count++;
                } catch (Exception e) {
                    // skip bad rows
                }
            }
        }
        return count;
    }

    private LocalDate parseDate(String dateStr) {
        for (DateTimeFormatter fmt : DATE_FORMATS) {
            try {
                return LocalDate.parse(dateStr, fmt);
            } catch (DateTimeParseException ignored) {}
        }
        return LocalDate.now();
    }
}
