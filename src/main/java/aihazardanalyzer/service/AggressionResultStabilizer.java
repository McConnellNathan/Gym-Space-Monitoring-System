package aihazardanalyzer.service;

import aihazardanalyzer.model.AggressionAnalysisResult;
import java.util.ArrayDeque;
import java.util.Deque;

public class AggressionResultStabilizer {
    private final Deque<AggressionAnalysisResult> recentResults = new ArrayDeque<>();
    private final int maxHistorySize;

    public AggressionResultStabilizer(int maxHistorySize) {
        if (maxHistorySize < 1) {
            throw new IllegalArgumentException("maxHistorySize must be at least 1.");
        }
        this.maxHistorySize = maxHistorySize;
    }

    public AggressionAnalysisResult addAndStabilize(AggressionAnalysisResult newResult) {
        recentResults.addLast(newResult);

        while (recentResults.size() > maxHistorySize) {
            recentResults.removeFirst();
        }

        boolean stabilizedConflict = majorityConflict();
        double stabilizedConfidence = averageConfidence();
        String notes = buildNotes(newResult);

        return new AggressionAnalysisResult(
                stabilizedConflict,
                stabilizedConfidence,
                notes
        );
    }

    private boolean majorityConflict() {
        int trueCount = 0;

        for (AggressionAnalysisResult result : recentResults) {
            if (result.isPossibleConflict()) {
                trueCount++;
            }
        }

        return trueCount >= 2;
    }

    private double averageConfidence() {
        double total = 0.0;
        for (AggressionAnalysisResult result : recentResults) {
            total += result.getConfidence();
        }
        return total / recentResults.size();
    }

    private String buildNotes(AggressionAnalysisResult newestResult) {
        return "Stabilized over " + recentResults.size() + " frame(s). Latest note: " + newestResult.getNotes();
    }
}
