package DataStore;

import protocol.Envelope;
import protocol.Msg;
import utility.Server;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Authentication server for dashboard employee sign-in and manager verification.
 *
 * <p>The store keeps a small in-memory employee list for the demonstration and responds
 * to employee lookup and manager-verification protocol messages.</p>
 */
public class MembershipStore extends Server {

    private static final int UNKNOWN_MESSAGE_ERROR_CODE = -300;

    private final List<Employee> employees = new CopyOnWriteArrayList<>();

    /**
     * Creates a Membership Store listening on all local interfaces for the given port.
     */
    public MembershipStore(int port) {
        super(port);
        initializeEmployees();
    }

    /**
     * Creates a Membership Store bound to a specific host and port.
     */
    public MembershipStore(String host, int port) {
        super(host, port);
        initializeEmployees();
    }

    /**
     * Handles employee sign-in requests, manager verification, and ping health checks.
     */
    @Override
    public void processMessage(Envelope env) {
        Msg msg = env.msg();

        try {
            switch (msg) {
                case Msg.RequestEmployee requestEmployee -> env.replyTo().send(findEmployee(requestEmployee));
                case Msg.VerifyManager verifyManagerRequest -> env.replyTo().send(verifyManager(verifyManagerRequest));
                case Msg.Ping ignored -> env.replyTo().send(new Msg.Pong());
                default -> env.replyTo().send(new Msg.ErrorMsg(
                        UNKNOWN_MESSAGE_ERROR_CODE,
                        -1,
                        "Unsupported message type: " + msg.getClass().getSimpleName()
                ));
            }
        } catch (IOException e) {
            System.err.println("[MembershipStore] Failed to reply to client: " + e.getMessage());
            env.replyTo().close();
        }
    }

    /**
     * Seeds employee accounts used for dashboard sign-in demonstrations.
     */
    private void initializeEmployees() {
        if (!employees.isEmpty()) {
            return;
        }

        employees.add(new Employee("E-001", "Nathan McConnell", "nmcconnell", Employee.EmployeeStatus.MANAGER, "1234"));
        employees.add(new Employee("E-002", "Kevin Rouzand", "krouzand", Employee.EmployeeStatus.EMPLOYEE, "1234"));
        employees.add(new Employee("E-003", "Asthon Langen", "alangen", Employee.EmployeeStatus.EMPLOYEE, "1234"));
        employees.add(new Employee("E-004", "Julia Marsh", "jmarsh", Employee.EmployeeStatus.MANAGER, "1234"));
        employees.add(new Employee("E-005", "Mckenzie Johnston", "mjohnston", Employee.EmployeeStatus.EMPLOYEE, "1234"));
    }

    /**
     * Authenticates an employee by username and password.
     */
    private Msg.EmployeeResponseMsg findEmployee(Msg.RequestEmployee requestEmployee) {
        for (Employee employee : employees) {
            if (Objects.equals(employee.employeeUsername(), requestEmployee.employeeUsername())
                    && Objects.equals(employee.password(), requestEmployee.password())) {
                return new Msg.EmployeeResponseMsg(
                        employee,
                        true,
                        "Employee authenticated successfully"
                );
            }
        }

        return new Msg.EmployeeResponseMsg(
                null,
                false,
                "Employee username or password was incorrect"
        );
    }

    /**
     * Verifies that the supplied credentials belong to a manager account.
     */
    private Msg.VerifyManagerResponse verifyManager(Msg.VerifyManager verifyManager) {
        for (Employee employee : employees) {
            if (Objects.equals(employee.employeeUsername(), verifyManager.employeeUsername())
                    && Objects.equals(employee.password(), verifyManager.password())
                    && employee.employeeStatus() == Employee.EmployeeStatus.MANAGER) {
                return new Msg.VerifyManagerResponse(
                        true,
                        employee.employeeId(),
                        "Manager verified successfully"
                );
            }
        }

        return new Msg.VerifyManagerResponse(
                false,
                null,
                "Manager username or password was incorrect"
        );
    }
}
