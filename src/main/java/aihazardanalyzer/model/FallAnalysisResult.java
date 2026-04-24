package aihazardanalyzer.model;

public class FallAnalysisResult {
    private boolean possibleFall;
    private double confidence;
    private String notes;

    public FallAnalysisResult() {
    }

    public FallAnalysisResult(boolean possibleFall, double confidence, String notes) {
        this.possibleFall = possibleFall;
        this.confidence = confidence;
        this.notes = notes;
    }

    public boolean isPossibleFall() {
        return possibleFall;
    }

    public void setPossibleFall(boolean possibleFall) {
        this.possibleFall = possibleFall;
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
        return "FallAnalysisResult{" +
                "possibleFall=" + possibleFall +
                ", confidence=" + confidence +
                ", notes='" + notes + '\'' +
                '}';
    }
}
