package gui.common;

import java.util.List;

/**
 * Interface to communicate with Dashboard class
 */

public interface DashboardGateway {
    int getCurrentOccupancy();
    int getMaxOccupancy();
    List<String> getClassSchedule();
    boolean registerForClass(String className);
    List<String> getEmployeeAlerts();
    List<DashboardAlert> getActiveAlerts();
    boolean resolveAlert(String alertId);
    boolean resolveCriticalAlert(String alertId, String managerPin);
}
