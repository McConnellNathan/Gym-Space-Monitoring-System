package app;

import AlertManager.AlertManager;
import DataStore.LogStore;

public class TestBackendLauncher {
    private static final String HOST = "localhost";
    private static final int LOG_STORE_PORT = 5000;
    private static final int ALERT_MANAGER_PORT = 6000;

    public static void main(String[] args) throws InterruptedException {
        try {
            System.out.println("[TestBackendLauncher] Creating LogStore...");
            LogStore logStore = new LogStore(HOST, LOG_STORE_PORT);

            System.out.println("[TestBackendLauncher] Starting LogStore...");
            logStore.start();

            // Small pause just to make startup sequencing obvious and reliable for demo/testing
            Thread.sleep(250);

            System.out.println("[TestBackendLauncher] Creating AlertManager...");
            AlertManager alertManager = new AlertManager(
                    HOST,
                    ALERT_MANAGER_PORT,
                    HOST,
                    LOG_STORE_PORT
            );

            System.out.println("[TestBackendLauncher] Starting AlertManager...");
            alertManager.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("[TestBackendLauncher] Shutting down backend...");
                try {
                    alertManager.stopRunning();
                } catch (Exception ignored) {
                }
                try {
                    logStore.stopRunning();
                } catch (Exception ignored) {
                }
            }));

            System.out.printf("[TestBackendLauncher] LogStore started on %s:%d%n", HOST, LOG_STORE_PORT);
            System.out.printf("[TestBackendLauncher] AlertManager started on %s:%d%n", HOST, ALERT_MANAGER_PORT);
            System.out.println("[TestBackendLauncher] Backend is running. Launch GUI apps separately.");

            Thread.currentThread().join();
        } catch (Exception e) {
            System.err.println("[TestBackendLauncher] Backend startup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}