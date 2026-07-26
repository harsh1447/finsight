package com.finsight.finsight_backend.controller;

import com.finsight.finsight_backend.dto.request.ChatRequest;
import com.finsight.finsight_backend.dto.response.AiInsightResponse;
import com.finsight.finsight_backend.service.GeminiAiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final GeminiAiService geminiAiService;

    @GetMapping("/insights")
    public ResponseEntity<AiInsightResponse> getInsights() {
        return ResponseEntity.ok(geminiAiService.getSpendingInsights());
    }

    @GetMapping("/summary")
    public ResponseEntity<AiInsightResponse> getSummary() {
        return ResponseEntity.ok(geminiAiService.getMonthlySummary());
    }

    @PostMapping("/chat")
    public ResponseEntity<AiInsightResponse> chat(
            @Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(geminiAiService.chat(request.getMessage()));
    }
}
