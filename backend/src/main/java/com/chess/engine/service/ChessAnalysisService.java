package com.chess.engine.service;

import com.chess.engine.model.*;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Parses PGN into positions, runs Stockfish evaluation on each,
 * calculates centipawn loss, and classifies every move.
 */
@Service
public class ChessAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(ChessAnalysisService.class);

    private final StockfishEngineService stockfish;

    @Value("${stockfish.default-depth:20}")
    private int defaultDepth;

    // Book moves: skip classification for first N half-moves (approximate opening theory)
    private static final int BOOK_MOVES_THRESHOLD = 8;

    public ChessAnalysisService(StockfishEngineService stockfish) {
        this.stockfish = stockfish;
    }

    /**
     * Full game analysis: parse PGN → evaluate every position → classify moves.
     */
    @Async("analysisExecutor")
    public CompletableFuture<GameAnalysis> analyzeGame(String pgn, int depth) {
        try {
            int searchDepth = depth > 0 ? depth : defaultDepth;
            log.info("Starting game analysis at depth {}", searchDepth);

            // 1. Parse PGN
            ParsedGame parsed = parsePgn(pgn);
            log.info("Parsed {} half-moves from PGN", parsed.sans.size());

            // 2. Evaluate all positions
            List<EvalResult> evals = evaluateAllPositions(parsed.fens, searchDepth);

            // 3. Classify moves
            List<AnalyzedMove> analyzedMoves = classifyMoves(parsed, evals);

            // 4. Calculate accuracy
            double whiteAccuracy = calculateAccuracy(analyzedMoves, "white");
            double blackAccuracy = calculateAccuracy(analyzedMoves, "black");

            // 5. Build summary
            Map<String, Integer> whiteClassCount = countClassifications(analyzedMoves, "white");
            Map<String, Integer> blackClassCount = countClassifications(analyzedMoves, "black");

            // 6. Assemble response
            GameAnalysis analysis = new GameAnalysis();
            analysis.setWhite(new GameAnalysis.PlayerInfo(
                    parsed.whiteName, parsed.whiteElo, whiteAccuracy));
            analysis.setBlack(new GameAnalysis.PlayerInfo(
                    parsed.blackName, parsed.blackElo, blackAccuracy));
            analysis.setResult(parsed.result);
            analysis.setOpening(parsed.opening);
            analysis.setStartingFen(parsed.fens.get(0));
            analysis.setMoves(analyzedMoves);

            GameAnalysis.AnalysisSummary summary = new GameAnalysis.AnalysisSummary();
            summary.setTotalMoves(analyzedMoves.size());
            summary.setWhiteClassifications(whiteClassCount);
            summary.setBlackClassifications(blackClassCount);
            analysis.setSummary(summary);

            log.info("Analysis complete: {} moves, White accuracy: {}%, Black accuracy: {}%",
                    analyzedMoves.size(), whiteAccuracy, blackAccuracy);

            return CompletableFuture.completedFuture(analysis);

        } catch (Exception e) {
            log.error("Game analysis failed", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    // ---- PGN Parsing ----

    private static class ParsedGame {
        List<String> fens = new ArrayList<>();      // FEN before each half-move (index 0 = starting pos)
        List<String> sans = new ArrayList<>();       // SAN notation for each half-move
        List<String> ucis = new ArrayList<>();       // UCI notation for each half-move
        List<String> colors = new ArrayList<>();     // "white" or "black" for each half-move
        List<Integer> moveNumbers = new ArrayList<>();
        List<Piece> capturedPieces = new ArrayList<>(); // what was captured (NONE if no capture)
        List<Piece> movedPieces = new ArrayList<>();    // what piece moved
        String whiteName = "White";
        String whiteElo = "?";
        String blackName = "Black";
        String blackElo = "?";
        String result = "*";
        String opening = "";
    }

    private ParsedGame parsePgn(String pgn) throws Exception {
        ParsedGame parsed = new ParsedGame();

        // Extract headers from PGN
        for (String line : pgn.split("\n")) {
            line = line.trim();
            if (line.startsWith("[White \"")) parsed.whiteName = extractHeader(line);
            else if (line.startsWith("[Black \"")) parsed.blackName = extractHeader(line);
            else if (line.startsWith("[WhiteElo \"")) parsed.whiteElo = extractHeader(line);
            else if (line.startsWith("[BlackElo \"")) parsed.blackElo = extractHeader(line);
            else if (line.startsWith("[Result \"")) parsed.result = extractHeader(line);
            else if (line.startsWith("[Opening \"")) parsed.opening = extractHeader(line);
            else if (line.startsWith("[ECO \"") && parsed.opening.isEmpty()) parsed.opening = extractHeader(line);
        }

        // Extract movetext (everything after the headers)
        String moveText = extractMoveText(pgn);

        // Parse moves using chesslib
        MoveList moveList = new MoveList();
        moveList.loadFromSan(moveText);

        Board board = new Board();

        // Starting position
        parsed.fens.add(board.getFen());

        int halfMoveIndex = 0;
        for (Move move : moveList) {
            // Record pre-move info
            String color = (halfMoveIndex % 2 == 0) ? "white" : "black";
            int moveNum = (halfMoveIndex / 2) + 1;

            // What piece is on the destination? (capture detection)
            Piece capturedPiece = board.getPiece(move.getTo());
            Piece movedPiece = board.getPiece(move.getFrom());

            // SAN representation
            String san = move.toString();

            // UCI representation
            String uci = move.getFrom().toString().toLowerCase() + move.getTo().toString().toLowerCase();
            if (move.getPromotion() != Piece.NONE) {
                uci += move.getPromotion().getFenSymbol().toLowerCase();
            }

            // Execute move
            board.doMove(move);

            // Record
            parsed.colors.add(color);
            parsed.moveNumbers.add(moveNum);
            parsed.sans.add(san);
            parsed.ucis.add(uci);
            parsed.fens.add(board.getFen());
            parsed.capturedPieces.add(capturedPiece);
            parsed.movedPieces.add(movedPiece);

            halfMoveIndex++;
        }

        return parsed;
    }

    private String extractHeader(String line) {
        int start = line.indexOf('"') + 1;
        int end = line.lastIndexOf('"');
        return (start > 0 && end > start) ? line.substring(start, end) : "";
    }

    private String extractMoveText(String pgn) {
        StringBuilder moveText = new StringBuilder();
        boolean inHeaders = true;

        for (String line : pgn.split("\n")) {
            line = line.trim();
            if (line.isEmpty()) {
                inHeaders = false;
                continue;
            }
            if (line.startsWith("[")) continue; // Skip headers
            if (!inHeaders || !line.startsWith("[")) {
                inHeaders = false;
                moveText.append(line).append(" ");
            }
        }

        // Remove result from movetext
        String mt = moveText.toString().trim();
        mt = mt.replaceAll("\\s*(1-0|0-1|1/2-1/2|\\*)\\s*$", "").trim();
        return mt;
    }

    // ---- Stockfish Evaluation ----

    private List<EvalResult> evaluateAllPositions(List<String> fens, int depth) {
        log.info("Evaluating {} positions using engine pool...", fens.size());
        // IMPORTANT: Use indexed parallel evaluation to preserve order.
        // parallelStream().map().toList() does NOT guarantee ordering!
        EvalResult[] results = new EvalResult[fens.size()];
        java.util.stream.IntStream.range(0, fens.size())
                .parallel()
                .forEach(i -> results[i] = stockfish.getEvaluation(fens.get(i), depth));
        return java.util.Arrays.asList(results);
    }

    // ---- Move Classification ----

    private List<AnalyzedMove> classifyMoves(ParsedGame parsed, List<EvalResult> evals) {
        List<AnalyzedMove> result = new ArrayList<>();

        for (int i = 0; i < parsed.sans.size(); i++) {
            AnalyzedMove am = new AnalyzedMove();
            am.setMoveNumber(parsed.moveNumbers.get(i));
            am.setColor(parsed.colors.get(i));
            am.setSan(parsed.sans.get(i));
            am.setUci(parsed.ucis.get(i));
            am.setFen(parsed.fens.get(i + 1)); // FEN after the move

            // Evals: index i = position before move i, index i+1 = position after move i
            EvalResult evalBefore = evals.get(i);
            EvalResult evalAfter = evals.get(i + 1);

            boolean isWhite = "white".equals(parsed.colors.get(i));

            // Fix for final checkmate move: Stockfish often outputs nothing for a mated board, so we manually set it.
            if (parsed.sans.get(i).endsWith("#")) {
                evalAfter.setMate(true);
                evalAfter.setMateIn(0);
                evalAfter.setScoreCp(isWhite ? 100000 : -100000);
            }

            // Set eval (after the move, from white's perspective)
            if (evalAfter.isMate()) {
                am.setEvalType("mate");
                am.setMateIn(evalAfter.getMateIn());
                am.setEval(evalAfter.getMateIn() > 0 ? 999.0 : -999.0);
            } else {
                am.setEvalType("cp");
                am.setEval(evalAfter.getScoreCp() / 100.0);
            }

            am.setBestMove(evalBefore.getBestMove());
            am.setBestLine(evalBefore.getPvLine());

            // Calculate centipawn loss
            // cpLoss = how much worse was the played move vs the best move
            // From the perspective of the moving side
            int cpLoss = calculateCpLoss(evalBefore, evalAfter, isWhite);
            am.setCpLoss(Math.max(0, cpLoss));

            // Classify the move
            boolean isBook = (i < BOOK_MOVES_THRESHOLD);
            am.setBookMove(isBook);

            boolean isSacrifice = isMaterialSacrifice(parsed.capturedPieces.get(i), parsed.movedPieces.get(i));
            String classification = classify(cpLoss, isBook, isSacrifice, evalBefore, evalAfter, isWhite);
            am.setClassification(classification);

            result.add(am);
        }

        return result;
    }

    /**
     * Calculate centipawn loss:
     * The difference between the best eval (before) and actual eval (after),
     * from the moving player's perspective.
     */
    private int calculateCpLoss(EvalResult evalBefore, EvalResult evalAfter, boolean isWhiteMoving) {
        int bestScore = evalBefore.getScoreCp(); // already white-normalized
        int actualScore = evalAfter.getScoreCp(); // already white-normalized

        if (isWhiteMoving) {
            // White wants the score to stay high
            return bestScore - actualScore;
        } else {
            // Black wants the score to stay low (negative)
            return actualScore - bestScore;
        }
    }

    /**
     * Detect if the move was a material sacrifice:
     * We moved a valuable piece to a square where it can be captured,
     * OR we captured something worth less than our piece.
     */
    private boolean isMaterialSacrifice(Piece capturedPiece, Piece movedPiece) {
        if (capturedPiece == null || capturedPiece == Piece.NONE) {
            // No capture — could still be a sacrifice if a piece was left hanging
            // For simplicity, only count captures for now
            return false;
        }

        int movedValue = pieceValue(movedPiece);
        int capturedValue = pieceValue(capturedPiece);

        // Sacrifice = we traded down (gave up more material than we captured)
        return movedValue > capturedValue + 100; // buffer of 1 pawn
    }

    private int pieceValue(Piece piece) {
        if (piece == null || piece == Piece.NONE) return 0;
        String name = piece.name().toUpperCase();
        if (name.contains("PAWN")) return 100;
        if (name.contains("KNIGHT")) return 300;
        if (name.contains("BISHOP")) return 320;
        if (name.contains("ROOK")) return 500;
        if (name.contains("QUEEN")) return 900;
        if (name.contains("KING")) return 0; // king captures aren't really sacrifices
        return 0;
    }

    /**
     * Classify a move based on centipawn loss and context.
     */
    private String classify(int cpLoss, boolean isBook, boolean isSacrifice,
                           EvalResult evalBefore, EvalResult evalAfter, boolean isWhite) {
        if (isBook) return "BOOK";

        // Brilliant: sacrifice + near-best + position wasn't already winning big
        int absBefore = Math.abs(evalBefore.getScoreCp());
        if (isSacrifice && cpLoss <= 10 && absBefore < 300) {
            return "BRILLIANT";
        }

        // Great: position was bad or equal, and this move was critical (only good option)
        // Approximation: eval swung positively for the mover and cpLoss is small
        if (cpLoss <= 10 && isPositionTurningMove(evalBefore, evalAfter, isWhite)) {
            return "GREAT";
        }

        // Standard classifications by centipawn loss
        if (cpLoss <= 0) return "BEST";
        if (cpLoss <= 10) return "EXCELLENT";
        if (cpLoss <= 50) return "GOOD";
        if (cpLoss <= 100) return "INACCURACY";
        if (cpLoss <= 300) return "MISTAKE";
        return "BLUNDER";
    }

    /**
     * A "turning move" is one where the position changes character significantly
     * in favor of the moving side (e.g. from losing/equal to winning).
     */
    private boolean isPositionTurningMove(EvalResult evalBefore, EvalResult evalAfter, boolean isWhite) {
        int before = evalBefore.getScoreCp();
        int after = evalAfter.getScoreCp();

        if (isWhite) {
            // White's move: position went from <= 0 to >= 100 (turned around)
            return before <= 0 && after >= 100;
        } else {
            // Black's move: position went from >= 0 to <= -100
            return before >= 0 && after <= -100;
        }
    }

    // ---- Accuracy Calculation ----

    /**
     * Convert centipawn evaluation to win probability (0-100 scale).
     * Uses the standard logistic curve: winP = 50 + 50 * (2/(1+10^(-cp/400)) - 1)
     * This maps cp=0 → 50%, cp=100 → ~64%, cp=300 → ~85%, cp=±∞ → 0/100%
     */
    private double cpToWinProbability(int cp) {
        return 50.0 + 50.0 * (2.0 / (1.0 + Math.pow(10.0, -cp / 400.0)) - 1.0);
    }

    /**
     * Calculate accuracy using per-move win probability loss (CAPS2-inspired).
     *
     * Algorithm:
     * 1. For each move, convert evalBefore and evalAfter to win probabilities
     *    from the moving player's perspective.
     * 2. Compute winProbLoss = winP_before - winP_after (how many win% points lost).
     * 3. Apply the exponential formula to each move's win% loss (NOT raw centipawns).
     *    moveAccuracy = 103.1668 * exp(-0.04354 * winProbLoss) - 3.1668
     * 4. Average all per-move accuracies.
     *
     * This gives realistic 50-95% ranges because win% losses are small numbers (0-20)
     * even when centipawn losses are large (50-300+).
     */
    private double calculateAccuracy(List<AnalyzedMove> moves, String color) {
        // We need pairs of (evalBefore, evalAfter) → but we only stored cpLoss.
        // Reconstruct win% loss using the raw eval values stored in the moves.
        // We'll use the eval chain: for move i, evalBefore = eval of move i-1 (or starting eval),
        // evalAfter = eval of move i.

        List<Double> moveAccuracies = new ArrayList<>();

        // Filter to only this color's non-book moves
        List<AnalyzedMove> colorMoves = moves.stream()
                .filter(m -> m.getColor().equals(color) && !m.isBookMove())
                .toList();

        for (AnalyzedMove m : colorMoves) {
            // Reconstruct evalBefore from evalAfter + cpLoss
            // cpLoss = evalBefore - evalAfter (from mover's perspective, in centipawns)
            // evalAfter is stored in m.getEval() (in pawns, white-relative)
            double evalAfterCp = m.getEval() * 100.0; // convert pawns → centipawns
            boolean isWhite = "white".equals(m.getColor());

            // cpLoss was computed as: (evalBefore - evalAfter) from mover's perspective
            // For white: cpLoss = evalBefore_white - evalAfter_white → evalBefore_white = evalAfter_white + cpLoss
            // For black: cpLoss = evalAfter_white - evalBefore_white → evalBefore_white = evalAfter_white - cpLoss
            double evalBeforeCp;
            if (isWhite) {
                evalBeforeCp = evalAfterCp + m.getCpLoss();
            } else {
                evalBeforeCp = evalAfterCp - m.getCpLoss();
            }

            // Convert both to win probability from the MOVING player's perspective
            double winBefore, winAfter;
            if (isWhite) {
                winBefore = cpToWinProbability((int) evalBeforeCp);
                winAfter = cpToWinProbability((int) evalAfterCp);
            } else {
                // For black, negate the cp values to get black's win probability
                winBefore = cpToWinProbability((int) -evalBeforeCp);
                winAfter = cpToWinProbability((int) -evalAfterCp);
            }

            // Win probability loss (how many win% points this move cost)
            double winProbLoss = Math.max(0, winBefore - winAfter);

            // Apply the exponential formula to the WIN% LOSS (not raw centipawns)
            // This gives sensible results because winProbLoss is typically 0-20
            double moveAcc = 103.1668 * Math.exp(-0.04354 * winProbLoss) - 3.1668;
            moveAcc = Math.max(0, Math.min(100, moveAcc));

            moveAccuracies.add(moveAcc);
        }

        if (moveAccuracies.isEmpty()) return 100.0;

        double avg = moveAccuracies.stream().mapToDouble(Double::doubleValue).average().orElse(100.0);
        return Math.round(avg * 10.0) / 10.0;
    }

    // ---- Summary ----

    private Map<String, Integer> countClassifications(List<AnalyzedMove> moves, String color) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("brilliant", 0);
        counts.put("great", 0);
        counts.put("best", 0);
        counts.put("excellent", 0);
        counts.put("good", 0);
        counts.put("inaccuracy", 0);
        counts.put("mistake", 0);
        counts.put("blunder", 0);
        counts.put("book", 0);

        for (AnalyzedMove m : moves) {
            if (m.getColor().equals(color)) {
                String key = m.getClassification().toLowerCase();
                counts.merge(key, 1, Integer::sum);
            }
        }

        return counts;
    }
}
