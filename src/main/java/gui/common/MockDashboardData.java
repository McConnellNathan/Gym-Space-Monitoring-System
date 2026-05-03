package gui.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Making mock data for now for testing purposes
 */

public class MockDashboardData implements DashboardGateway{
    @Override
    public int getCurrentOccupancy() {
        return 42;
    }

    @Override
    public int getMaxOccupancy() {
        return 100;
    }

    @Override
    public List<String> getClassSchedule() {
        return List.of(
                "Yoga - 10:00 AM - 8 spots left",
                "Spin - 12:00 PM - 3 spots left",
                "HIIT - 5:30 PM - Full",
                "Pilates - 7:00 PM - 5 spots left"
        );
    }

    @Override
    public boolean registerForClass(String className) {
        return !className.toLowerCase().contains("full");
    }

    @Override
    public List<String> getEmployeeAlerts() {
        return List.of(
                "CRITICAL: Fall detected in Free Weights",
                "WARNING: Treadmill 3 malfunction",
                "INFO: Noise threshold exceeded"
        );
    }

    @Override
    public List<DashboardAlert> getActiveAlerts() {
        return List.of(
                new DashboardAlert(
                        "A003",
                        DashboardAlert.Severity.CRITICAL,
                        "Fall Detected",
                        "Free Weights",
                        "2:45 PM",
                        "Camera detected a user down for longer than 10 seconds."
                ),
                new DashboardAlert(
                        "A002",
                        DashboardAlert.Severity.WARNING,
                        "Machine Malfunction",
                        "Cardio Area",
                        "2:31 PM",
                        "Treadmill 3 reported abnormal machine behavior."
                ),
                new DashboardAlert(
                        "A001",
                        DashboardAlert.Severity.INFO,
                        "Noise Event Logged",
                        "Free Weights",
                        "2:15 PM",
                        "Bulky Buzzer was triggered by excessive noise."
                )
        );
    }

    @Override
    public boolean resolveAlert(String alertId) {
        return true;
    }

    @Override
    public boolean resolveCriticalAlert(String alertId, String managerPin) {
        return "6789".equals(managerPin);
    }


    private final List<DashboardAlert> activeAlerts = new ArrayList<>(List.of(
            new DashboardAlert("A003", DashboardAlert.Severity.CRITICAL,
                    "Fall Detected", "Free Weights", "2:45 PM",
                    "Camera detected a user down for longer than 10 seconds."),
            new DashboardAlert("A002", DashboardAlert.Severity.WARNING,
                    "Machine Malfunction", "Cardio Area", "2:31 PM",
                    "Treadmill 3 reported abnormal machine behavior."),
            new DashboardAlert("A001", DashboardAlert.Severity.INFO,
                    "Noise Event Logged", "Free Weights", "2:15 PM",
                    "Bulky Buzzer was triggered by excessive noise.")
    ));

    private final List<DashboardAlert> resolvedAlertLogs = new ArrayList<>();

    private boolean moveAlertToLogs(String alertId) {
        for (DashboardAlert alert : new ArrayList<>(activeAlerts)) {
            if (alert.getId().equals(alertId)) {
                activeAlerts.remove(alert);
                resolvedAlertLogs.add(0, alert);
                return true;
            }
        }

        return false;
    }

    @Override
    public List<DashboardAlert> getResolvedAlertLogs() {
        return resolvedAlertLogs;
    }
}
