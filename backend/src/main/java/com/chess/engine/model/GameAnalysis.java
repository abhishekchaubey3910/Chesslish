package com.chess.engine.model;

import java.util.List;
import java.util.Map;

public class GameAnalysis {
    private PlayerInfo white;
    private PlayerInfo black;
    private String result;
    private String opening;
    private List<AnalyzedMove> moves;
    private AnalysisSummary summary;
    private String startingFen; // FEN of starting position (before any moves)

    public GameAnalysis() {}

    // Getters and setters
    public PlayerInfo getWhite() { return white; }
    public void setWhite(PlayerInfo white) { this.white = white; }

    public PlayerInfo getBlack() { return black; }
    public void setBlack(PlayerInfo black) { this.black = black; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getOpening() { return opening; }
    public void setOpening(String opening) { this.opening = opening; }

    public List<AnalyzedMove> getMoves() { return moves; }
    public void setMoves(List<AnalyzedMove> moves) { this.moves = moves; }

    public AnalysisSummary getSummary() { return summary; }
    public void setSummary(AnalysisSummary summary) { this.summary = summary; }

    public String getStartingFen() { return startingFen; }
    public void setStartingFen(String startingFen) { this.startingFen = startingFen; }

    // Nested classes
    public static class PlayerInfo {
        private String name;
        private String rating;
        private double accuracy;

        public PlayerInfo() {}
        public PlayerInfo(String name, String rating, double accuracy) {
            this.name = name;
            this.rating = rating;
            this.accuracy = accuracy;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getRating() { return rating; }
        public void setRating(String rating) { this.rating = rating; }
        public double getAccuracy() { return accuracy; }
        public void setAccuracy(double accuracy) { this.accuracy = accuracy; }
    }

    public static class AnalysisSummary {
        private int totalMoves;
        private Map<String, Integer> whiteClassifications;
        private Map<String, Integer> blackClassifications;

        public AnalysisSummary() {}

        public int getTotalMoves() { return totalMoves; }
        public void setTotalMoves(int totalMoves) { this.totalMoves = totalMoves; }
        public Map<String, Integer> getWhiteClassifications() { return whiteClassifications; }
        public void setWhiteClassifications(Map<String, Integer> whiteClassifications) { this.whiteClassifications = whiteClassifications; }
        public Map<String, Integer> getBlackClassifications() { return blackClassifications; }
        public void setBlackClassifications(Map<String, Integer> blackClassifications) { this.blackClassifications = blackClassifications; }
    }
}
