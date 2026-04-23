package com.chess.engine.controller;

import com.chess.engine.model.AnalysisRequest;
import com.chess.engine.model.GameAnalysis;
import com.chess.engine.service.ChessAnalysisService;
import com.chess.engine.service.StockfishEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
public class AnalysisController {

    private static final Logger log = LoggerFactory.getLogger(AnalysisController.class);

    private final ChessAnalysisService analysisService;
    private final StockfishEngineService stockfishService;

    public AnalysisController(ChessAnalysisService analysisService, StockfishEngineService stockfishService) {
        this.analysisService = analysisService;
        this.stockfishService = stockfishService;
    }

    /**
     * Analyze a chess game from PGN.
     * Returns the full analysis with move classifications.
     */
    @PostMapping("/analyze")
    public CompletableFuture<ResponseEntity<GameAnalysis>> analyzeGame(@RequestBody AnalysisRequest request) {
        log.info("Received analysis request (depth={})", request.getDepth());

        if (request.getPgn() == null || request.getPgn().isBlank()) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().build());
        }

        if (!stockfishService.isRunning()) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.internalServerError().build());
        }

        return analysisService.analyzeGame(request.getPgn(), request.getDepth())
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> {
                    log.error("Analysis failed", ex);
                    return ResponseEntity.internalServerError().build();
                });
    }

    /**
     * Health check — is the engine running?
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        boolean engineRunning = stockfishService.isRunning();
        return ResponseEntity.ok(Map.of(
                "status", engineRunning ? "ok" : "engine_down",
                "engine", engineRunning
        ));
    }
}
