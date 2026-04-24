package aihazardanalyzer.model;

public class WalkwayAnalysisResult {
    private boolean walkwayObstructed;
    private double confidence;
    private String notes;

    public WalkwayAnalysisResult() {
    }

    public WalkwayAnalysisResult(boolean walkwayObstructed, double confidence, String notes) {
        this.walkwayObstructed = walkwayObstructed;
        this.confidence = confidence;
        this.notes = notes;
    }

    public boolean isWalkwayObstructed() {
        return walkwayObstructed;
    }

    public void setWalkwayObstructed(boolean walkwayObstructed) {
        this.walkwayObstructed = walkwayObstructed;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "WalkwayAnalysisResult{" +
                "walkwayObstructed=" + walkwayObstructed +
                ", confidence=" + confidence +
                ", notes='" + notes + '\'' +
                '}';
    }
}
