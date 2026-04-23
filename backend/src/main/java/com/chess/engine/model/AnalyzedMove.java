package com.chess.engine.model;

import java.util.List;

public class AnalyzedMove {
    private int moveNumber;
    private String color;        // "white" or "black"
    private String san;          // e.g. "Nf3"
    private String uci;          // e.g. "g1f3"
    private String fen;          // position AFTER this move
    private double eval;         // evaluation in pawns (e.g. 0.45)
    private String evalType;     // "cp" or "mate"
    private int mateIn;          // only if evalType == "mate"
    private String bestMove;     // engine's best move (UCI)
    private List<String> bestLine; // principal variation
    private int cpLoss;          // centipawn loss from this move
    private String classification; // BRILLIANT, GREAT, BEST, EXCELLENT, GOOD, INACCURACY, MISTAKE, BLUNDER, BOOK
    private boolean isBookMove;

    public AnalyzedMove() {}

    // Getters and setters
    public int getMoveNumber() { return moveNumber; }
    public void setMoveNumber(int moveNumber) { this.moveNumber = moveNumber; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getSan() { return san; }
    public void setSan(String san) { this.san = san; }

    public String getUci() { return uci; }
    public void setUci(String uci) { this.uci = uci; }

    public String getFen() { return fen; }
    public void setFen(String fen) { this.fen = fen; }

    public double getEval() { return eval; }
    public void setEval(double eval) { this.eval = eval; }

    public String getEvalType() { return evalType; }
    public void setEvalType(String evalType) { this.evalType = evalType; }

    public int getMateIn() { return mateIn; }
    public void setMateIn(int mateIn) { this.mateIn = mateIn; }

    public String getBestMove() { return bestMove; }
    public void setBestMove(String bestMove) { this.bestMove = bestMove; }

    public List<String> getBestLine() { return bestLine; }
    public void setBestLine(List<String> bestLine) { this.bestLine = bestLine; }

    public int getCpLoss() { return cpLoss; }
    public void setCpLoss(int cpLoss) { this.cpLoss = cpLoss; }

    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }

    public boolean isBookMove() { return isBookMove; }
    public void setBookMove(boolean bookMove) { isBookMove = bookMove; }
}
