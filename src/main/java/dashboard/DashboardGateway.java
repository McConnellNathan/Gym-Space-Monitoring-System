package dashboard;

import datastore.Employee;
import datastore.MachineData;
import gui.common.DashboardAlert;

import java.util.List;

/**
 * Interface to communicate with Dashboard class
 */

public interface DashboardGateway {
    default Employee.EmployeeStatus signIn(String username, String password) { return null; }
    default String getCurrentEmployeeName() { return ""; }
    default MachineData[] getMachineData() { return null; }
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
