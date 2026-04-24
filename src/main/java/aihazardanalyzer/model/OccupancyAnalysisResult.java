package aihazardanalyzer.model;

public class OccupancyAnalysisResult {
    private int peopleCount;
    private String sceneStatus;
    private double confidence;
    private String notes;

    public OccupancyAnalysisResult() {
    }

    public OccupancyAnalysisResult(int peopleCount, String sceneStatus, double confidence, String notes) {
        this.peopleCount = peopleCount;
        this.sceneStatus = sceneStatus;
        this.confidence = confidence;
        this.notes = notes;
    }

    public int getPeopleCount() {
        return peopleCount;
    }

    public void setPeopleCount(int peopleCount) {
        this.peopleCount = peopleCount;
    }

    public String getSceneStatus() {
        return sceneStatus;
    }

    public void setSceneStatus(String sceneStatus) {
        this.sceneStatus = sceneStatus;
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
        return "OccupancyAnalysisResult{" +
                "peopleCount=" + peopleCount +
                ", sceneStatus='" + sceneStatus + '\'' +
                ", confidence=" + confidence +
                ", notes='" + notes + '\'' +
                '}';
    }
}
