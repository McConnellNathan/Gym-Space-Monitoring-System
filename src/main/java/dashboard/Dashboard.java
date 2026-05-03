package dashboard;

import datastore.Employee;
import datastore.MachineData;
import protocol.Msg;
import utility.RemoteMessageClient;

import java.io.Console;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Client-side coordinator for dashboard interactions with the backend services.
 *
 * <p>The Dashboard maintains socket clients for Alert Manager, Log Store, and Membership Store.
 * It listens for alert snapshots, sends acknowledgement messages, and gates manager-only
 * statistics behind the signed-in employee's role.</p>
 */
public class Dashboard implements AutoCloseable {

    private static final String DEFAULT_ALERT_MANAGER_HOST = "localhost";
    private static final int DEFAULT_ALERT_MANAGER_PORT = 6000;
    private static final String DEFAULT_LOG_STORE_HOST = "localhost";
    private static final int DEFAULT_LOG_STORE_PORT = 5000;
    private static final String DEFAULT_MEMBERSHIP_STORE_HOST = "localhost";
    private static final int DEFAULT_MEMBERSHIP_STORE_PORT = 5001;
    private static final Scanner TERMINAL_SCANNER = new Scanner(System.in);

    private final RemoteMessageClient alertListenerClient;
    private final RemoteMessageClient alertCommandClient;
    private final RemoteMessageClient logStoreClient;
    private final RemoteMessageClient membershipStoreClient;
    private final Map<String, Msg.AlertNotification> activeAlerts = new ConcurrentHashMap<>();
    private final AtomicBoolean listeningForAlerts = new AtomicBoolean(false);

    private Thread alertListenerThread;
    private Employee currentEmployee;
    private Employee.EmployeeStatus currentEmployeeStatus;

    /**
     * Creates a dashboard client using all default backend service endpoints.
     */
    public Dashboard() throws IOException {
        this(
                DEFAULT_ALERT_MANAGER_HOST,
                DEFAULT_ALERT_MANAGER_PORT,
                DEFAULT_LOG_STORE_HOST,
                DEFAULT_LOG_STORE_PORT,
                DEFAULT_MEMBERSHIP_STORE_HOST,
                DEFAULT_MEMBERSHIP_STORE_PORT
        );
    }

    /**
     * Creates a dashboard client using the default Membership Store endpoint.
     */
    public Dashboard(
            String alertManagerHost,
            int alertManagerPort,
            String logStoreHost,
            int logStorePort
    ) throws IOException {
        this(
                alertManagerHost,
                alertManagerPort,
                logStoreHost,
                logStorePort,
                DEFAULT_MEMBERSHIP_STORE_HOST,
                DEFAULT_MEMBERSHIP_STORE_PORT
        );
    }

    /**
     * Creates a dashboard client with explicit service endpoints.
     */
    public Dashboard(
            String alertManagerHost,
            int alertManagerPort,
            String logStoreHost,
            int logStorePort,
            String membershipStoreHost,
            int membershipStorePort
    ) throws IOException {
        this.alertListenerClient = new RemoteMessageClient(
                alertManagerHost,
                alertManagerPort,
                new Msg.DashboardConnected()
        );

        this.alertCommandClient = new RemoteMessageClient(
                alertManagerHost,
                alertManagerPort
        );
        this.logStoreClient = new RemoteMessageClient(logStoreHost, logStorePort);
        this.membershipStoreClient = new RemoteMessageClient(membershipStoreHost, membershipStorePort);
        startAlertListener();
    }

    /**
     * Sends a one-way protocol message to the Alert Manager.
     */
    public synchronized void sendToAlertManager(Msg msg) throws IOException {
        alertCommandClient.send(msg);
    }

    /**
     * Sends a direct alert acknowledgement with a known employee id.
     *
     * @return
     */
    public synchronized boolean acknowledgeAlert(String alertId, String employeeId) throws IOException {
        sendToAlertManager(new Msg.AlertAcknowledgementMsg(
                alertId,
                employeeId,
                System.currentTimeMillis()
        ));
        return true;
    }

    /**
     * Acknowledges an alert using the current employee or verified manager credentials.
     *
     * <p>Non-critical alerts use the currently signed-in employee. Critical alerts prompt for
     * manager credentials and only acknowledge after Membership Store verification.</p>
     *
     * @return true when an acknowledgement message was sent
     */
    public synchronized boolean acknowledgeAlert(Msg.AlertNotification alert)
            throws IOException, ClassNotFoundException {
        if (alert == null) {
            return false;
        }

        if (alert.severity() != Msg.AlertSeverity.CRITICAL) {
            if (currentEmployee == null) {
                return false;
            }
            acknowledgeAlert(alert.alertId(), currentEmployee.employeeId());
            return true;
        }

        // todo here we should request manager credentials from the GUI not terminal
        ManagerCredentials managerCredentials = requestManagerCredentialsFromTerminal();
        Msg response = requestFromMembershipStore(new Msg.VerifyManager(
                managerCredentials.username(),
                managerCredentials.password()
        ));

        if (response instanceof Msg.VerifyManagerResponse managerResponse
                && managerResponse.validManager()
                && managerResponse.employeeId() != null) {
            acknowledgeAlert(alert.alertId(), managerResponse.employeeId());
            return true;
        }

        return false;
    }


    /**
     * Sends a one-way protocol message to the Log Store.
     */
    public synchronized void sendToLogStore(Msg msg) throws IOException {
        logStoreClient.send(msg);
    }

    /**
     * Reads the next message from the Log Store connection.
     */
    public synchronized Msg readFromLogStore() throws IOException, ClassNotFoundException {
        return logStoreClient.read();
    }

    /**
     * Sends a request to the Log Store and waits for its response.
     */
    public synchronized Msg requestFromLogStore(Msg msg) throws IOException, ClassNotFoundException {
        return logStoreClient.sendAndRead(msg);
    }

    /**
     * Requests machine usage data for managers.
     *
     * @return machine usage records, or null when the current user is not a manager
     */
    public synchronized MachineData[] requestMachineUsageData() throws IOException, ClassNotFoundException {
        if (!isCurrentUserManager()) {
            return null;
        }

        Msg response = requestFromLogStore(new Msg.RequestMachineData());
        if (response instanceof Msg.MachineDataResponseMsg machineDataResponse && machineDataResponse.success()) {
            return machineDataResponse.machineData();
        }

        return null;
    }

    /**
     * Requests member entry history for managers.
     *
     * @return member entry records, or null when the current user is not a manager
     */
    public synchronized Msg.MemberEnterRecord[] requestMemberUsageData() throws IOException, ClassNotFoundException {
        if (!isCurrentUserManager()) {
            return null;
        }

        Msg response = requestFromLogStore(new Msg.RequestMemberData());
        if (response instanceof Msg.MemberDataResponseMsg memberDataResponse && memberDataResponse.success()) {
            return memberDataResponse.memberData();
        }

        return null;
    }

    /**
     * Sends a request to the Membership Store and waits for its response.
     */
    public synchronized Msg requestFromMembershipStore(Msg msg) throws IOException, ClassNotFoundException {
        return membershipStoreClient.sendAndRead(msg);
    }

    /**
     * Authenticates an employee by username and password through the Membership Store.
     */
    public synchronized Msg requestEmployee(String employeeUsername, String password)
            throws IOException, ClassNotFoundException {
        return requestFromMembershipStore(new Msg.RequestEmployee(employeeUsername, password));
    }

    /**
     * Signs in an employee and stores their current dashboard session state.
     *
     * @return employee status on success, or null on failed authentication
     */
    public synchronized Employee.EmployeeStatus signIn(String employeeUsername, String password)
            throws IOException, ClassNotFoundException {
        Msg response = requestEmployee(employeeUsername, password);
        if (response instanceof Msg.EmployeeResponseMsg employeeResponse && employeeResponse.employee() != null) {
            currentEmployee = employeeResponse.employee();
            currentEmployeeStatus = currentEmployee.employeeStatus();
            return currentEmployeeStatus;
        }

        currentEmployee = null;
        currentEmployeeStatus = null;
        return null;
    }

    /**
     * Clears the current dashboard employee session.
     */
    public synchronized void signOut() {
        currentEmployee = null;
        currentEmployeeStatus = null;
        // TODO: Return to the sign-in page.
    }

    /**
     * Returns the currently signed-in employee, if any.
     */
    public synchronized Employee getCurrentEmployee() {
        return currentEmployee;
    }

    /**
     * Returns the role for the currently signed-in employee, if any.
     */
    public synchronized Employee.EmployeeStatus getCurrentEmployeeStatus() {
        return currentEmployeeStatus;
    }

    private boolean isCurrentUserManager() {
        return currentEmployeeStatus == Employee.EmployeeStatus.MANAGER;
    }

    private ManagerCredentials requestManagerCredentialsFromTerminal() {
        Console console = System.console();
        if (console != null) {
            String username = console.readLine("Manager username: ");
            char[] passwordChars = console.readPassword("Manager password: ");
            String password = passwordChars == null ? "" : new String(passwordChars);
            return new ManagerCredentials(username, password);
        }

        System.out.print("Manager username: ");
        String username = TERMINAL_SCANNER.nextLine();
        System.out.print("Manager password: ");
        String password = TERMINAL_SCANNER.nextLine();
        return new ManagerCredentials(username, password);
    }

    private record ManagerCredentials(String username, String password) {}

    /**
     * Returns a snapshot of the alerts most recently pushed by the Alert Manager.
     */
    public Map<String, Msg.AlertNotification> getActiveAlerts() {
        return Map.copyOf(activeAlerts);
    }


    /**
     * Starts the background listener that receives alert snapshots from Alert Manager.
     */
    public void startAlertListener() {
        if (!listeningForAlerts.compareAndSet(false, true)) {
            return;
        }

        alertListenerThread = Thread.ofVirtual().name("dashboard-alert-listener").start(() -> {
            // This listener currently watches Alert Manager traffic only.
            // It will eventually need to refresh the active alert list by reading from LogStore,
            // which means this task will need coordinated access to the LogStore read socket/client too.
            while (listeningForAlerts.get()) {
                try {
                    Msg incoming = alertListenerClient.read();
                    handleAlertManagerMessage(incoming);
                } catch (IOException | ClassNotFoundException e) {
                    if (listeningForAlerts.get()) {
                        System.err.println("[Dashboard] Alert listener stopped: " + e.getMessage());
                    }
                    break;
                }
            }
        });
    }

    /**
     * Stops the background alert listener.
     */
    public void stopAlertListener() {
        listeningForAlerts.set(false);
        if (alertListenerThread != null) {
            alertListenerThread.interrupt();
        }
    }

    /**
     * Applies an incoming Alert Manager message to the dashboard's local state.
     */
    private void handleAlertManagerMessage(Msg incoming) {
        if (incoming instanceof Msg.AlertNotificationMsg notification) {
            Map<String, Msg.AlertNotification> latestAlerts = new HashMap<>();
            for (Msg.AlertNotification alert : notification.alerts()) {
                latestAlerts.put(alert.alertId(), alert);
            }

            activeAlerts.clear();
            activeAlerts.putAll(latestAlerts);
            System.out.printf(
                    "[Dashboard] Alert snapshot received count=%d%n",
                    notification.alerts().length
            );
            return;
        }

        if (incoming instanceof Msg.ErrorMsg errorMsg) {
            System.err.printf(
                    "[Dashboard] Alert Manager error code=%d message=%s%n",
                    errorMsg.code(),
                    errorMsg.message()
            );
            return;
        }

        System.out.println("[Dashboard] Received message from Alert Manager: " + incoming);
    }

    @Override
    public void close() {
        stopAlertListener();

        try {
            alertListenerClient.send(new Msg.DisconnectMsg("Dashboard disconnecting"));
        } catch (IOException ignored) {
        }

        try {
            alertListenerClient.close();
        } catch (IOException ignored) {
        }

        try {
            alertCommandClient.close();
        } catch (IOException ignored) {
        }

        try {
            logStoreClient.close();
        } catch (IOException ignored) {
        }

        try {
            membershipStoreClient.close();
        } catch (IOException ignored) {
        }
    }

    public synchronized boolean sendTestAlert() {
        try {
            sendToAlertManager(new Msg.HazardDetectionMsg(
                    Msg.AlertType.FALL,
                    "Free Weights",
                    0.95,
                    "Manual test fall alert from GUI",
                    System.currentTimeMillis()
            ));
            return true;
        } catch (IOException e) {
            System.err.println("[Dashboard] Failed to send test alert: " + e.getMessage());
            return false;
        }
    }
}
