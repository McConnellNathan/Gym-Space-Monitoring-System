package aihazardanalyzer.service;

import aihazardanalyzer.model.OccupancyAnalysisResult;
import java.util.*;

public class OccupancyResultStabilizer {
    private final Deque<OccupancyAnalysisResult> recentResults = new ArrayDeque<>();
    private final int maxHistorySize;

    public OccupancyResultStabilizer(int maxHistorySize) {
        if (maxHistorySize < 1) {
            throw new IllegalArgumentException("maxHistorySize must be at least 1.");
        }
        this.maxHistorySize = maxHistorySize;
    }

    public OccupancyAnalysisResult addAndStabilize(OccupancyAnalysisResult newResult) {
        recentResults.addLast(newResult);

        while (recentResults.size() > maxHistorySize) {
            recentResults.removeFirst();
        }

        int stabilizedPeopleCount = medianPeopleCount();
        String stabilizedSceneStatus = majoritySceneStatus();
        double stabilizedConfidence = averageConfidence();
        String notes = buildNotes(newResult);

        return new OccupancyAnalysisResult(
                stabilizedPeopleCount,
                stabilizedSceneStatus,
                stabilizedConfidence,
                notes
        );
    }

    private int medianPeopleCount() {
        List<Integer> counts = new ArrayList<>();
        for (OccupancyAnalysisResult result : recentResults) {
            counts.add(result.getPeopleCount());
        }

        counts.sort(Integer::compareTo);
        return counts.get(counts.size() / 2);
    }

    private String majoritySceneStatus() {
        Map<String, Integer> counts = new HashMap<>();

        for (OccupancyAnalysisResult result : recentResults) {
            counts.merge(result.getSceneStatus(), 1, Integer::sum);
        }

        return counts.entrySet()
                .stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse("NORMAL_ACTIVITY");
    }

    private double averageConfidence() {
        double total = 0.0;
        for (OccupancyAnalysisResult result : recentResults) {
            total += result.getConfidence();
        }
        return total / recentResults.size();
    }

    private String buildNotes(OccupancyAnalysisResult newestResult) {
        return "Stabilized over " + recentResults.size() + " frame(s). Latest note: " + newestResult.getNotes();
    }
}
