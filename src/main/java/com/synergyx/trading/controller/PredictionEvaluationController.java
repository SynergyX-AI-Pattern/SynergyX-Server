package com.synergyx.trading.controller;

import com.synergyx.trading.service.predictionService.PredictionEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/predictions")
@RequiredArgsConstructor
public class PredictionEvaluationController {

    private final PredictionEvaluationService evaluationService;

    @GetMapping("/errors")
    public ResponseEntity<Map<String, Object>> getPredictionErrors() {
        return ResponseEntity.ok(evaluationService.getPredictionErrors());
    }
}
