package soundmonitor.model;

public class AudioAnalysisResult {
    private boolean tooLoud;
    private boolean triggerAlarm;
    private String soundType;
    private double confidence;
    private String notes;

    public AudioAnalysisResult() {
    }

    public AudioAnalysisResult(boolean tooLoud, boolean triggerAlarm, String soundType, double confidence, String notes) {
        this.tooLoud = tooLoud;
        this.triggerAlarm = triggerAlarm;
        this.soundType = soundType;
        this.confidence = confidence;
        this.notes = notes;
    }

    public boolean isTooLoud() {
        return tooLoud;
    }

    public void setTooLoud(boolean tooLoud) {
        this.tooLoud = tooLoud;
    }

    public boolean isTriggerAlarm() {
        return triggerAlarm;
    }

    public void setTriggerAlarm(boolean triggerAlarm) {
        this.triggerAlarm = triggerAlarm;
    }

    public String getSoundType() {
        return soundType;
    }

    public void setSoundType(String soundType) {
        this.soundType = soundType;
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
        return "AudioAnalysisResult{" +
                "tooLoud=" + tooLoud +
                ", triggerAlarm=" + triggerAlarm +
                ", soundType='" + soundType + '\'' +
                ", confidence=" + confidence +
                ", notes='" + notes + '\'' +
                '}';
    }
}