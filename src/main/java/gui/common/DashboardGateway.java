package gui.common;

import datastore.Employee;
import java.util.List;

/**
 * Interface to communicate with Dashboard class
 */

public interface DashboardGateway {
    default Employee.EmployeeStatus signIn(String username, String password) { return null; }
    int getCurrentOccupancy();
    int getMaxOccupancy();
    List<String> getClassSchedule();
    boolean registerForClass(String className);
    List<String> getEmployeeAlerts();
    List<DashboardAlert> getActiveAlerts();
    boolean resolveAlert(String alertId);
    boolean resolveCriticalAlert(String alertId, String managerPin);
    List<DashboardAlert> getResolvedAlertLogs();
}
