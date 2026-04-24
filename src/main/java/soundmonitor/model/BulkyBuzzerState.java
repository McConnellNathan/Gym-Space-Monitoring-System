package soundmonitor.model;

public class BulkyBuzzerState {
    private boolean alarmActive;
    private boolean armed;
    private double currentLevel;
    private String reason;
    private BuzzerTriggerSource triggerSource;

    public BulkyBuzzerState() {
        this.triggerSource = BuzzerTriggerSource.NONE;
    }

    public BulkyBuzzerState(boolean alarmActive, boolean armed, double currentLevel,
                            String reason, BuzzerTriggerSource triggerSource) {
        this.alarmActive = alarmActive;
        this.armed = armed;
        this.currentLevel = currentLevel;
        this.reason = reason;
        this.triggerSource = triggerSource;
    }

    public boolean isAlarmActive() {
        return alarmActive;
    }

    public void setAlarmActive(boolean alarmActive) {
        this.alarmActive = alarmActive;
    }

    public boolean isArmed() {
        return armed;
    }

    public void setArmed(boolean armed) {
        this.armed = armed;
    }

    public double getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(double currentLevel) {
        this.currentLevel = currentLevel;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public BuzzerTriggerSource getTriggerSource() {
        return triggerSource;
    }

    public void setTriggerSource(BuzzerTriggerSource triggerSource) {
        this.triggerSource = triggerSource;
    }

    @Override
    public String toString() {
        return "BulkyBuzzerState{" +
                "alarmActive=" + alarmActive +
                ", armed=" + armed +
                ", currentLevel=" + currentLevel +
                ", reason='" + reason + '\'' +
                ", triggerSource=" + triggerSource +
                '}';
    }
}