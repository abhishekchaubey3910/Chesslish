package com.chess.engine.model;

public class AnalysisRequest {
    private String pgn;
    private int depth = 20;

    public AnalysisRequest() {}

    public AnalysisRequest(String pgn, int depth) {
        this.pgn = pgn;
        this.depth = depth;
    }

    public String getPgn() { return pgn; }
    public void setPgn(String pgn) { this.pgn = pgn; }
    public int getDepth() { return depth; }
    public void setDepth(int depth) { this.depth = depth; }
}
