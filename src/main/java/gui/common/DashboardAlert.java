package gui.common;

public class DashboardAlert {

    public enum Severity {
        INFO, WARNING, CRITICAL
    }

    private final String id;
    private final Severity severity;
    private final String title;
    private final String location;
    private final String time;
    private final String description;

    public DashboardAlert(String id, Severity severity, String title,
                          String location, String time, String description) {
        this.id = id;
        this.severity = severity;
        this.title = title;
        this.location = location;
        this.time = time;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public String getTime() {
        return time;
    }

    public String getDescription() {
        return description;
    }
}