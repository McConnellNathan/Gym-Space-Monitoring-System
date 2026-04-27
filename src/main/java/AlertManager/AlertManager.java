package AlertManager;

import protocol.Envelope;
import protocol.Msg;
import protocol.ReplyChannel;
import utility.RemoteMessageClient;
import utility.Server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AlertManager extends Server {

    private static final int UNKNOWN_MESSAGE_ERROR_CODE = -100;
    private static final int ALERT_NOT_FOUND_ERROR_CODE = -101;

    private final ConcurrentHashMap<String, Alert> activeAlerts = new ConcurrentHashMap<>();
    private final ArrayList<ReplyChannel> dashboardClients = new ArrayList<>();
    private RemoteMessageClient logStoreClient;

    /**
     * Create the inital set up of the server for the Processor waiting to be started
     *
     * @param port give server listening port
     */
    public AlertManager(int port) {
        super(port);
    }

    public AlertManager(String host, int port) {
        super(host, port);
    }

    public AlertManager(int port, String logStoreHost, int logStorePort) {
        this(port);
        connectToLogStore(logStoreHost, logStorePort);
    }

    public AlertManager(String host, int port, String logStoreHost, int logStorePort) {
        this(host, port);
        connectToLogStore(logStoreHost, logStorePort);
    }

    @Override
    public void processMessage(Envelope env) {
        Msg msg = env.msg();

        try {
            switch (msg) {
                case Msg.DashboardConnected ignored -> {
                    registerDashboard(env.replyTo());
                    NotifyDashboards();
                }
                case Msg.DisconnectMsg ignored -> unregisterDashboard(env.replyTo());
                case Msg.HazardDetectionMsg hazardDetectionMsg -> handleHazardDetection(env, hazardDetectionMsg);
                case Msg.AlertAcknowledgementMsg acknowledgementMsg -> handleAlertAcknowledgement(env, acknowledgementMsg);
                case Msg.AlertStatusUpdateMsg statusUpdateMsg -> {
                    Alert existing = activeAlerts.get(statusUpdateMsg.alertId());
                    if (existing == null) {
                        env.replyTo().send(new Msg.ErrorMsg(
                                ALERT_NOT_FOUND_ERROR_CODE,
                                -1,
                                "Alert not found: " + statusUpdateMsg.alertId()
                        ));
                        return;
                    }

                    Alert updated = withStatus(existing, statusUpdateMsg.newStatus());
                    applyAlertStatusChange(updated, statusUpdateMsg.employeeId());
                    NotifyDashboards();
                }
                case Msg.Ping ignored -> env.replyTo().send(new Msg.Pong());
                default -> env.replyTo().send(new Msg.ErrorMsg(
                        UNKNOWN_MESSAGE_ERROR_CODE,
                        -1,
                        "Unsupported message type: " + msg.getClass().getSimpleName()
                ));
            }
        } catch (IOException e) {
            System.err.println("[AlertManager] Failed to reply to client: " + e.getMessage());
            env.replyTo().close();
        }
    }

    private void handleAlertAcknowledgement(Envelope env, Msg.AlertAcknowledgementMsg acknowledgementMsg) throws IOException {
        Alert existing = activeAlerts.get(acknowledgementMsg.alertId());
        if (existing == null) {
            env.replyTo().send(new Msg.ErrorMsg(
                    ALERT_NOT_FOUND_ERROR_CODE,
                    -1,
                    "Alert not found: " + acknowledgementMsg.alertId()
            ));
            return;
        }

        Alert resolvedAlert = withStatus(existing, Msg.AlertStatus.RESOLVED);
        applyAlertStatusChange(resolvedAlert, acknowledgementMsg.employeeId());
        NotifyDashboards();
    }

    private void handleHazardDetection(Envelope env, Msg.HazardDetectionMsg hazardDetectionMsg) throws IOException {
        Alert alert = createAlert(hazardDetectionMsg);

        activeAlerts.put(alert.alertId(), alert);
        writeToLogs(alert, "CREATED");

        System.out.printf(
                "[AlertManager] Hazard detection received id=%s type=%s -> alertId=%s severity=%s%n",
                hazardDetectionMsg.type(),
                alert.alertId(),
                alert.severity()
        );

        NotifyDashboards();
    }

    private void applyAlertStatusChange(Alert alert, String employeeId) {
        if (checkAlertStatus(alert)) {
            activeAlerts.put(alert.alertId(), alert);
            writeToLogs(alert, "STATUS_UPDATED by " + employeeId);
            return;
        }

        closeAlert(alert);
        removeAlertLogs(alert.alertId());
        writeToLogs(alert, "RESOLVED by " + employeeId);
    }

    private Alert createAlert(Msg.HazardDetectionMsg msg) {
        String alertId = UUID.randomUUID().toString();
        Msg.AlertSeverity severity = classifySeverity(msg.type(), msg.confidence());
        return Alert.fromHazardDetection(alertId, msg, severity);
    }

    private Alert withStatus(Alert alert, Msg.AlertStatus status) {
        return new Alert(
                alert.alertId(),
                alert.type(),
                alert.severity(),
                status,
                alert.location(),
                alert.description(),
                alert.confidence(),
                alert.timestampEpochMillis()
        );
    }

    private Msg.AlertSeverity classifySeverity(Msg.AlertType type, double confidence) {
        return switch (type) {
            case FALL, INJURY, AGGRESSION, ENVIRONMENTAL_HAZARD -> Msg.AlertSeverity.CRITICAL;
            case OVERCROWDING,
                 WALKWAY_OBSTRUCTION,
                 IMPROPER_EQUIPMENT_USE,
                 MACHINE_MALFUNCTION,
                 SOUND_DISTURBANCE -> Msg.AlertSeverity.WARNING;
            case SYSTEM_ERROR -> confidence >= 0.9
                    ? Msg.AlertSeverity.CRITICAL
                    : Msg.AlertSeverity.WARNING;
            default -> Msg.AlertSeverity.INFORMATIONAL;
        };
    }

    private void writeToLogs(Alert alert, String action) {
        Msg.LogRecord record = new Msg.LogRecord(
                UUID.randomUUID().toString(),
                "ALERT_" + action,
                "AlertManager",
                action + ": " + alert.description() + " [status=" + alert.status() + ", severity=" + alert.severity() + "]",
                alert.alertId(),
                System.currentTimeMillis()
        );

        Msg.LogWriteRequestMsg logWriteRequestMsg = new Msg.LogWriteRequestMsg(record);
        try {
            Msg response = sendToLogStore(logWriteRequestMsg);
            System.out.printf(
                    "[AlertManager] Log write request created for alert %s action=%s payload=%s response=%s%n",
                    alert.alertId(),
                    action,
                    logWriteRequestMsg,
                    response
            );
        } catch (IOException | ClassNotFoundException e) {
            System.err.printf(
                    "[AlertManager] Failed to write alert %s to LogStore: %s%n",
                    alert.alertId(),
                    e.getMessage()
            );
        }
    }

    private void removeAlertLogs(String alertId) {
        Msg.LogDeleteRequestMsg deleteRequestMsg = new Msg.LogDeleteRequestMsg(
                UUID.randomUUID().toString(),
                alertId
        );

        try {
            Msg response = sendToLogStore(deleteRequestMsg);
            System.out.printf(
                    "[AlertManager] Log delete request created for alert %s payload=%s response=%s%n",
                    alertId,
                    deleteRequestMsg,
                    response
            );
        } catch (IOException | ClassNotFoundException e) {
            System.err.printf(
                    "[AlertManager] Failed to delete old logs for alert %s: %s%n",
                    alertId,
                    e.getMessage()
            );
        }
    }

    private boolean checkAlertStatus(Alert alert) {
        return alert.status() != Msg.AlertStatus.RESOLVED;
    }

    private void closeAlert(Alert alert) {
        activeAlerts.remove(alert.alertId());
        System.out.printf("[AlertManager] Closed alert %s status=%s%n", alert.alertId(), alert.status());
    }

    private Msg.AlertNotification toNotification(Alert alert) {
        return new Msg.AlertNotification(
                alert.alertId(),
                alert.type(),
                alert.severity(),
                alert.status(),
                alert.location(),
                alert.description(),
                alert.timestampEpochMillis()
        );
    }

    private void registerDashboard(ReplyChannel replyChannel) {
        if (dashboardClients.contains(replyChannel)) {
            return;
        }

        dashboardClients.add(replyChannel);
        System.out.printf("[AlertManager] Dashboard connected. totalDashboards=%d%n", dashboardClients.size());
    }

    private void unregisterDashboard(ReplyChannel replyChannel) {
        if (dashboardClients.remove(replyChannel)) {
            System.out.printf("[AlertManager] Dashboard disconnected. totalDashboards=%d%n", dashboardClients.size());
        }
    }

    private void NotifyDashboards() {
        ArrayList<Alert> activeAlertsSnapshot = new ArrayList<>(activeAlerts.values());
        Msg.AlertNotification[] notifications = activeAlertsSnapshot.stream()
                .map(this::toNotification)
                .toArray(Msg.AlertNotification[]::new);
        Msg.AlertNotificationMsg alertNotificationMsg = new Msg.AlertNotificationMsg(
                notifications,
                System.currentTimeMillis()
        );

        Iterator<ReplyChannel> iterator = dashboardClients.iterator();
        while (iterator.hasNext()) {
            ReplyChannel dashboardClient = iterator.next();
            try {
                dashboardClient.send(alertNotificationMsg);
            } catch (Exception e) {
                iterator.remove();
                dashboardClient.close();
                System.err.printf(
                        "[AlertManager] Removed disconnected dashboard while notifying: %s%n",
                        e.getMessage()
                );
            }
        }
    }

    public synchronized void connectToLogStore(String host, int port) {
        try {
            if (logStoreClient != null) {
                logStoreClient.close();
            }
            logStoreClient = new RemoteMessageClient(host, port);
            System.out.printf("[AlertManager] Connected to LogStore at %s:%d%n", host, port);
        } catch (IOException e) {
            throw new RuntimeException("Unable to connect AlertManager to LogStore", e);
        }
    }

    public synchronized void sendToLogStoreNoReply(Msg msg) throws IOException {
        if (logStoreClient == null) {
            throw new IOException("LogStore client is not connected");
        }
        logStoreClient.send(msg);
    }

    public synchronized Msg readFromLogStore() throws IOException, ClassNotFoundException {
        if (logStoreClient == null) {
            throw new IOException("LogStore client is not connected");
        }
        return logStoreClient.read();
    }

    public synchronized Msg sendToLogStore(Msg msg) throws IOException, ClassNotFoundException {
        if (logStoreClient == null) {
            throw new IOException("LogStore client is not connected");
        }
        return logStoreClient.sendAndRead(msg);
    }

    @Override
    public void close() {
        super.close();
        if (logStoreClient != null) {
            try {
                logStoreClient.close();
            } catch (IOException ignored) {
            }
        }
    }
}
