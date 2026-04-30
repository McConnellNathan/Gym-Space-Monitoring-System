package datastore;

import java.io.Serializable;

/**
 * Employee account used by the Membership Store and Dashboard sign-in flow.
 */
public record Employee(
        String employeeId,
        String employeeName,
        String employeeUsername,
        EmployeeStatus employeeStatus,
        String password
) implements Serializable {

    /**
     * Coarse authorization level used by the Dashboard.
     */
    public enum EmployeeStatus {
        MANAGER,
        EMPLOYEE
    }
}
