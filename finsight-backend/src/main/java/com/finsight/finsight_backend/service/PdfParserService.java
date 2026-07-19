package com.finsight.finsight_backend.service;

import com.finsight.finsight_backend.entity.Category;
import com.finsight.finsight_backend.entity.Transaction;
import com.finsight.finsight_backend.entity.User;
import com.finsight.finsight_backend.repository.CategoryRepository;
import com.finsight.finsight_backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PdfParserService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    public int parseAndSave(MultipartFile file, User user) throws Exception {
        int count = 0;

        try (PDDocument document = PDDocument.load(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            String[] lines = text.split("\n");

            Pattern amountPattern = Pattern.compile("(\\d+[,\\d]*\\.\\d{2})");

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;

                Matcher matcher = amountPattern.matcher(line);
                if (!matcher.find()) continue;

                try {
                    String amountStr = matcher.group(1).replaceAll(",", "");
                    BigDecimal amount = new BigDecimal(amountStr);

                    if (amount.compareTo(BigDecimal.ONE) < 0) continue;

                    String type = line.toLowerCase().contains("credit") ? "INCOME" : "EXPENSE";
                    String title = line.substring(0, Math.min(line.length(), 50)).trim();

                    Category category = categoryRepository.findByName("Other").orElse(null);

                    Transaction transaction = Transaction.builder()
                            .user(user)
                            .title(title)
                            .amount(amount)
                            .type(type)
                            .transactionDate(LocalDate.now())
                            .category(category)
                            .source("PDF")
                            .build();

                    transactionRepository.save(transaction);
                    count++;
                } catch (Exception e) {
                    // skip unparseable lines
                }
            }
        }
        return count;
    }
}
