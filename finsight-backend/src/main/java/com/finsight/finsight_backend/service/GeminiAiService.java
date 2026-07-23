package com.finsight.finsight_backend.service;

import com.finsight.finsight_backend.dto.response.AiInsightResponse;
import com.finsight.finsight_backend.entity.Transaction;
import com.finsight.finsight_backend.entity.User;
import com.finsight.finsight_backend.repository.TransactionRepository;
import com.finsight.finsight_backend.repository.UserRepository;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GeminiAiService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final Gson gson = new Gson();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private String callGemini(String prompt) throws IOException, InterruptedException {
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);

        JsonArray parts = new JsonArray();
        parts.add(textPart);

        JsonObject content = new JsonObject();
        content.add("parts", parts);

        JsonArray contents = new JsonArray();
        contents.add(content);

        JsonObject requestBody = new JsonObject();
        requestBody.add("contents", contents);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        JsonObject responseJson = gson.fromJson(response.body(), JsonObject.class);

        return responseJson
                .getAsJsonArray("candidates")
                .get(0).getAsJsonObject()
                .getAsJsonObject("content")
                .getAsJsonArray("parts")
                .get(0).getAsJsonObject()
                .get("text").getAsString();
    }

    public AiInsightResponse getSpendingInsights() {
        User user = getCurrentUser();
        List<Transaction> transactions = transactionRepository
                .findByUserIdOrderByTransactionDateDesc(user.getId());

        if (transactions.isEmpty()) {
            return AiInsightResponse.builder()
                    .insight("No transactions found. Start adding your expenses to get AI insights!")
                    .type("INSIGHTS")
                    .build();
        }

        Map<String, BigDecimal> categoryTotals = transactions.stream()
                .filter(t -> t.getType().equals("EXPENSE"))
                .collect(Collectors.groupingBy(
                        t -> t.getCategory() != null ? t.getCategory().getName() : "Other",
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getType().equals("INCOME"))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.stream()
                .filter(t -> t.getType().equals("EXPENSE"))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a personal finance advisor for an Indian user. ");
        prompt.append("Analyze this spending data and give 3-4 specific, actionable insights in simple English.\n\n");
        prompt.append("Total Income: ₹").append(totalIncome).append("\n");
        prompt.append("Total Expenses: ₹").append(totalExpense).append("\n");
        prompt.append("Savings: ₹").append(totalIncome.subtract(totalExpense)).append("\n\n");
        prompt.append("Spending by Category:\n");
        categoryTotals.forEach((cat, amount) ->
                prompt.append("- ").append(cat).append(": ₹").append(amount).append("\n"));
        prompt.append("\nRecent transactions:\n");
        transactions.stream().limit(10).forEach(t ->
                prompt.append("- ").append(t.getTitle())
                        .append(" ₹").append(t.getAmount())
                        .append(" (").append(t.getType()).append(")\n"));
        prompt.append("\nProvide specific advice about their spending patterns, ");
        prompt.append("where they can save money, and what they're doing well. ");
        prompt.append("Keep it friendly and practical for an Indian student/professional.");

        try {
            String insight = callGemini(prompt.toString());
            return AiInsightResponse.builder()
                    .insight(insight)
                    .type("INSIGHTS")
                    .build();
        } catch (Exception e) {
            return AiInsightResponse.builder()
                    .insight("Unable to generate insights at this time. Please try again later.")
                    .type("ERROR")
                    .build();
        }
    }

    public AiInsightResponse getMonthlySummary() {
        User user = getCurrentUser();
        List<Transaction> transactions = transactionRepository
                .findByUserIdOrderByTransactionDateDesc(user.getId());

        if (transactions.isEmpty()) {
            return AiInsightResponse.builder()
                    .insight("No transactions found yet!")
                    .type("SUMMARY")
                    .build();
        }

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getType().equals("INCOME"))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.stream()
                .filter(t -> t.getType().equals("EXPENSE"))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String prompt = "You are a friendly financial assistant. " +
                "Create a brief, encouraging financial summary for an Indian user.\n\n" +
                "Their data:\n" +
                "Total Income: ₹" + totalIncome + "\n" +
                "Total Expenses: ₹" + totalExpense + "\n" +
                "Net Savings: ₹" + totalIncome.subtract(totalExpense) + "\n" +
                "Number of transactions: " + transactions.size() + "\n\n" +
                "Write a 2-3 sentence friendly summary of their financial health. " +
                "Be encouraging and mention one specific thing they can improve.";

        try {
            String summary = callGemini(prompt);
            return AiInsightResponse.builder()
                    .insight(summary)
                    .type("SUMMARY")
                    .build();
        } catch (Exception e) {
            return AiInsightResponse.builder()
                    .insight("Unable to generate summary at this time.")
                    .type("ERROR")
                    .build();
        }
    }
}
