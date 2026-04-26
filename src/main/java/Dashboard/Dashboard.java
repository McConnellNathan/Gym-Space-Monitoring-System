package main.java.Dashboard;

import main.java.protocol.Msg;
import main.java.utility.RemoteMessageClient;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Dashboard implements AutoCloseable {

    private final RemoteMessageClient alertManagerClient;
    private final RemoteMessageClient logStoreClient;
    private final Map<String, Msg.AlertNotificationMsg> activeAlerts = new ConcurrentHashMap<>();
    private final AtomicBoolean listeningForAlerts = new AtomicBoolean(false);

    private Thread alertListenerThread;

    public Dashboard(
            String alertManagerHost,
            int alertManagerPort,
            String logStoreHost,
            int logStorePort
    ) throws IOException {
        this.alertManagerClient = new RemoteMessageClient(alertManagerHost, alertManagerPort);
        this.logStoreClient = new RemoteMessageClient(logStoreHost, logStorePort);
        startAlertListener();
    }

    public synchronized void sendToAlertManager(Msg msg) throws IOException {
        alertManagerClient.send(msg);
    }

    public synchronized void acknowledgeAlert(String alertId, String employeeId) throws IOException {
        sendToAlertManager(new Msg.AlertAcknowledgementMsg(
                alertId,
                employeeId,
                System.currentTimeMillis()
        ));
    }


    public synchronized void sendToLogStore(Msg msg) throws IOException {
        logStoreClient.send(msg);
    }

    public synchronized Msg readFromLogStore() throws IOException, ClassNotFoundException {
        return logStoreClient.read();
    }

    public synchronized Msg requestFromLogStore(Msg msg) throws IOException, ClassNotFoundException {
        return logStoreClient.sendAndRead(msg);
    }

    public Map<String, Msg.AlertNotificationMsg> getActiveAlerts() {
        return Map.copyOf(activeAlerts);
    }


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
                    Msg incoming = alertManagerClient.read();
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

    public void stopAlertListener() {
        listeningForAlerts.set(false);
        if (alertListenerThread != null) {
            alertListenerThread.interrupt();
        }
    }

    private void handleAlertManagerMessage(Msg incoming) {
        if (incoming instanceof Msg.AlertNotificationMsg notification) {
            if (notification.status() == Msg.AlertStatus.RESOLVED) {
                activeAlerts.remove(notification.alertId());
            } else {
                activeAlerts.put(notification.alertId(), notification);
            }
            System.out.printf(
                    "[Dashboard] Alert update received alertId=%s status=%s type=%s%n",
                    notification.alertId(),
                    notification.status(),
                    notification.type()
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
            alertManagerClient.close();
        } catch (IOException ignored) {
        }
        try {
            logStoreClient.close();
        } catch (IOException ignored) {
        }
    }
}
