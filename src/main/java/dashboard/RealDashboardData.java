package dashboard;

import datastore.Employee;
import datastore.MachineData;
import gui.common.DashboardAlert;
import protocol.Msg;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class RealDashboardData implements DashboardGateway, AutoCloseable {
    private final Dashboard dashboard;
    private final DateTimeFormatter timeFormatter =
            DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault());

    public RealDashboardData() throws IOException {
        this.dashboard = new Dashboard();
    }

    @Override
    public String getCurrentEmployeeName() {
        Employee e = dashboard.getCurrentEmployee();
        return e != null ? e.employeeName() : "";
    }

    @Override
    public MachineData[] getMachineData() {
        try {
            return dashboard.requestMachineUsageData();
        } catch (Exception e) {
            System.err.println("[RealDashboardData] Failed to get machine data: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Employee.EmployeeStatus signIn(String username, String password) {
        try {
            return dashboard.signIn(username, password);
        } catch (Exception e) {
            System.err.println("[RealDashboardData] Sign-in failed: " + e.getMessage());
            return null;
        }
    }

    @Override
    public int getCurrentOccupancy() {
        try {
            Msg response = dashboard.requestFromLogStore(new Msg.RequestMemberData());
            if (response instanceof Msg.MemberDataResponseMsg r && r.success() && r.memberData() != null) {
                LocalDate today = LocalDate.now();
                return (int) Arrays.stream(r.memberData())
                        .filter(e -> e.month() == today.getMonthValue()
                                && e.day() == today.getDayOfMonth()
                                && e.year() == today.getYear())
                        .count();
            }
            return 0;
        } catch (Exception e) {
            System.err.println("Failed to get current occupancy: " + e.getMessage());
            return 0;
        }
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
        List<String> result = new ArrayList<>();
        for (DashboardAlert alert : getActiveAlerts()) {
            result.add(alert.getSeverity() + ": " + alert.getTitle() + " - " + alert.getLocation());
        }
        return result;
    }

    @Override
    public List<DashboardAlert> getActiveAlerts() {
        try {
            Map<String, Msg.AlertNotification> alerts = dashboard.getActiveAlerts();

            return alerts.values().stream()
                    .sorted(Comparator.comparingLong(Msg.AlertNotification::timestampEpochMillis).reversed())
                    .map(this::toDashboardAlert)
                    .toList();
        } catch (Exception e) {
            System.err.println("[RealDashboardData] Failed to get active alerts: " + e.getMessage());
            return List.of();
        }
    }

    @Override
    public boolean resolveAlert(String alertId) {
        try {
            Msg.AlertNotification alert = dashboard.getActiveAlerts().get(alertId);
            if (alert == null) {
                return false;
            }

            Employee current = dashboard.getCurrentEmployee();
            if (current == null) {
                return false;
            }

            return dashboard.acknowledgeAlert(alertId, current.employeeId());
        } catch (Exception e) {
            System.err.println("[RealDashboardData] Failed to resolve alert: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean resolveCriticalAlert(String alertId, String managerPin) {
        try {
            Msg.AlertNotification alert = dashboard.getActiveAlerts().get(alertId);
            if (alert == null) {
                return false;
            }

            if (!"6789".equals(managerPin)) {
                return false;
            }

            Employee current = dashboard.getCurrentEmployee();
            if (current == null) {
                return false;
            }

            return dashboard.acknowledgeAlert(alertId, current.employeeId());
        } catch (Exception e) {
            System.err.println("[RealDashboardData] Failed to resolve critical alert: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<DashboardAlert> getResolvedAlertLogs() {
        try {
            Msg response = dashboard.requestFromLogStore(
                    new Msg.LogReadRequestMsg("resolved-query", "ALL", 0, System.currentTimeMillis())
            );

            if (response instanceof Msg.LogReadResponseMsg r && r.success() && r.records() != null) {
                return Arrays.stream(r.records())
                        .filter(record -> record.logType() != null)
                        .filter(record -> record.logType().contains("ALERT_RESOLVED"))
                        .sorted(Comparator.comparingLong(Msg.LogRecord::timestampEpochMillis).reversed())
                        .map(this::logRecordToDashboardAlert)
                        .toList();
            }

            return List.of();
        } catch (Exception e) {
            System.err.println("[RealDashboardData] Failed to get resolved alert logs: " + e.getMessage());
            return List.of();
        }
    }

    private DashboardAlert logRecordToDashboardAlert(Msg.LogRecord record) {
        return new DashboardAlert(
                record.logId(),
                DashboardAlert.Severity.INFO,
                record.logType(),
                record.source(),
                timeFormatter.format(Instant.ofEpochMilli(record.timestampEpochMillis())),
                record.description()
        );
    }

    private DashboardAlert toDashboardAlert(Msg.AlertNotification alert) {
        return new DashboardAlert(
                alert.alertId(),
                mapSeverity(alert.severity()),
                prettyTitle(alert.type()),
                alert.location(),
                timeFormatter.format(Instant.ofEpochMilli(alert.timestampEpochMillis())),
                alert.description()
        );
    }

    private DashboardAlert.Severity mapSeverity(Msg.AlertSeverity severity) {
        return switch (severity) {
            case CRITICAL -> DashboardAlert.Severity.CRITICAL;
            case WARNING -> DashboardAlert.Severity.WARNING;
            case INFORMATIONAL -> DashboardAlert.Severity.INFO;
        };
    }

    private String prettyTitle(Msg.AlertType type) {
        return switch (type) {
            case FALL -> "Fall Detected";
            case AGGRESSION -> "Conflict Detected";
            case OVERCROWDING -> "Overcrowding";
            case WALKWAY_OBSTRUCTION -> "Walkway Obstruction";
            case SOUND_DISTURBANCE -> "Sound Event";
            case MACHINE_MALFUNCTION -> "Machine Malfunction";
            case IMPROPER_EQUIPMENT_USE -> "Improper Equipment Use";
            case INJURY -> "Injury Detected";
            case ENVIRONMENTAL_HAZARD -> "Environmental Hazard";
            case SYSTEM_ERROR -> "System Error";
        };
    }

    @Override
    public void close() {
        dashboard.close();
    }

    public boolean sendTestAlert() {
        return dashboard.sendTestAlert();
    }
}