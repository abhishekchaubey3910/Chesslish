package com.chess.engine.model;

import java.util.List;

/**
 * Result from a single Stockfish position evaluation.
 */
public class EvalResult {
    private int scoreCp;           // centipawn score (from white's perspective)
    private boolean isMate;        // true if mate detected
    private int mateIn;            // moves to mate (positive = white mates, negative = black mates)
    private String bestMove;       // best move in UCI format (e.g. "e2e4")
    private List<String> pvLine;   // principal variation (best line)
    private int depth;             // depth reached

    public EvalResult() {}

    public EvalResult(int scoreCp, boolean isMate, int mateIn, String bestMove, List<String> pvLine, int depth) {
        this.scoreCp = scoreCp;
        this.isMate = isMate;
        this.mateIn = mateIn;
        this.bestMove = bestMove;
        this.pvLine = pvLine;
        this.depth = depth;
    }

    // Getters and setters
    public int getScoreCp() { return scoreCp; }
    public void setScoreCp(int scoreCp) { this.scoreCp = scoreCp; }

    public boolean isMate() { return isMate; }
    public void setMate(boolean mate) { isMate = mate; }

    public int getMateIn() { return mateIn; }
    public void setMateIn(int mateIn) { this.mateIn = mateIn; }

    public String getBestMove() { return bestMove; }
    public void setBestMove(String bestMove) { this.bestMove = bestMove; }

    public List<String> getPvLine() { return pvLine; }
    public void setPvLine(List<String> pvLine) { this.pvLine = pvLine; }

    public int getDepth() { return depth; }
    public void setDepth(int depth) { this.depth = depth; }
}
