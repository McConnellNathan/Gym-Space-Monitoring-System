package aihazardanalyzer.model;

public class AggressionAnalysisResult {
    private boolean possibleConflict;
    private double confidence;
    private String notes;

    public AggressionAnalysisResult() {
    }

    public AggressionAnalysisResult(boolean possibleConflict, double confidence, String notes) {
        this.possibleConflict = possibleConflict;
        this.confidence = confidence;
        this.notes = notes;
    }

    public boolean isPossibleConflict() {
        return possibleConflict;
    }

    public void setPossibleConflict(boolean possibleConflict) {
        this.possibleConflict = possibleConflict;
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
        return "AggressionAnalysisResult{" +
                "possibleConflict=" + possibleConflict +
                ", confidence=" + confidence +
                ", notes='" + notes + '\'' +
                '}';
    }
}
