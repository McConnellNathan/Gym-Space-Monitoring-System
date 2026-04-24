package aihazardanalyzer.service;

import aihazardanalyzer.model.WalkwayAnalysisResult;
import java.util.ArrayDeque;
import java.util.Deque;

public class WalkwayResultStabilizer {
    private final Deque<WalkwayAnalysisResult> recentResults = new ArrayDeque<>();
    private final int maxHistorySize;

    public WalkwayResultStabilizer(int maxHistorySize) {
        if (maxHistorySize < 1) {
            throw new IllegalArgumentException("maxHistorySize must be at least 1.");
        }
        this.maxHistorySize = maxHistorySize;
    }

    public WalkwayAnalysisResult addAndStabilize(WalkwayAnalysisResult newResult) {
        recentResults.addLast(newResult);

        while (recentResults.size() > maxHistorySize) {
            recentResults.removeFirst();
        }

        boolean stabilizedWalkway = majorityWalkwayObstructed();
        double stabilizedConfidence = averageConfidence();
        String notes = buildNotes(newResult);

        return new WalkwayAnalysisResult(
                stabilizedWalkway,
                stabilizedConfidence,
                notes
        );
    }

    private boolean majorityWalkwayObstructed() {
        int trueCount = 0;

        for (WalkwayAnalysisResult result : recentResults) {
            if (result.isWalkwayObstructed()) {
                trueCount++;
            }
        }

        return trueCount >= 2;
    }

    private double averageConfidence() {
        double total = 0.0;
        for (WalkwayAnalysisResult result : recentResults) {
            total += result.getConfidence();
        }
        return total / recentResults.size();
    }

    private String buildNotes(WalkwayAnalysisResult newestResult) {
        return "Stabilized over " + recentResults.size() + " frame(s). Latest note: " + newestResult.getNotes();
    }
}
