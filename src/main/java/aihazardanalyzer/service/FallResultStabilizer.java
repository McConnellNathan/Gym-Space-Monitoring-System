package aihazardanalyzer.service;

import aihazardanalyzer.model.FallAnalysisResult;

import java.util.ArrayDeque;
import java.util.Deque;

public class FallResultStabilizer {
    private final Deque<FallAnalysisResult> recentResults = new ArrayDeque<>();
    private final int maxHistorySize;

    public FallResultStabilizer(int maxHistorySize) {
        if (maxHistorySize < 1) {
            throw new IllegalArgumentException("maxHistorySize must be at least 1.");
        }
        this.maxHistorySize = maxHistorySize;
    }

    public FallAnalysisResult addAndStabilize(FallAnalysisResult newResult) {
        recentResults.addLast(newResult);

        while (recentResults.size() > maxHistorySize) {
            recentResults.removeFirst();
        }

        boolean stabilizedFall = majorityFall();
        double stabilizedConfidence = averageConfidence();
        String notes = buildNotes(newResult);

        return new FallAnalysisResult(
                stabilizedFall,
                stabilizedConfidence,
                notes
        );
    }

    private boolean majorityFall() {
        int trueCount = 0;

        for (FallAnalysisResult result : recentResults) {
            if (result.isPossibleFall()) {
                trueCount++;
            }
        }

        return trueCount >= 2;
    }

    private double averageConfidence() {
        double total = 0.0;
        for (FallAnalysisResult result : recentResults) {
            total += result.getConfidence();
        }
        return total / recentResults.size();
    }

    private String buildNotes(FallAnalysisResult newestResult) {
        return "Stabilized over " + recentResults.size() + " frame(s). Latest note: " + newestResult.getNotes();
    }
}
