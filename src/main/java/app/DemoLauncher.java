package app;

import alertmanager.AlertManager;
import datastore.LogStore;

import java.io.File;
import java.io.IOException;

public class DemoLauncher {
    private static final String HOST = "localhost";
    private static final int LOG_STORE_PORT = 5000;
    private static final int ALERT_MANAGER_PORT = 6000;

    public static void main(String[] args) throws InterruptedException {
        LogStore logStore = null;
        AlertManager alertManager = null;
        Process scannerProcess = null;
        Process aiDashboardProcess = null;

        try {
            System.out.println("Creating LogStore...");
            logStore = new LogStore(HOST, LOG_STORE_PORT);

            System.out.println("Starting LogStore...");
            logStore.start();

            Thread.sleep(250);

            System.out.println("Creating AlertManager...");
            alertManager = new AlertManager(
                    HOST,
                    ALERT_MANAGER_PORT,
                    HOST,
                    LOG_STORE_PORT
            );

            System.out.println("Starting AlertManager...");
            alertManager.start();

            Thread.sleep(500);

            System.out.printf("LogStore started on %s:%d%n", HOST, LOG_STORE_PORT);
            System.out.printf("AlertManager started on %s:%d%n", HOST, ALERT_MANAGER_PORT);

            System.out.println("Launching DoorScannerDemo...");
            scannerProcess = launchJavaProcess("app.DoorScannerDemo");

            Thread.sleep(500);

            System.out.println("Launching DemoAiDashboard...");
            aiDashboardProcess = launchJavaProcess("app.DemoAiDashboard");

            Process finalScannerProcess = scannerProcess;
            Process finalAiDashboardProcess = aiDashboardProcess;
            LogStore finalLogStore = logStore;
            AlertManager finalAlertManager = alertManager;

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down demo...");

                stopProcess(finalScannerProcess, "DoorScannerDemo");
                stopProcess(finalAiDashboardProcess, "DemoAiDashboard");

                try {
                    finalAlertManager.stopRunning();
                } catch (Exception ignored) {
                }

                try {
                    finalLogStore.stopRunning();
                } catch (Exception ignored) {
                }
            }));

            System.out.println("Demo is running.");

            Thread.currentThread().join();
        } catch (Exception e) {
            System.err.println("Startup failed: " + e.getMessage());
            e.printStackTrace();

            stopProcess(scannerProcess, "DoorScannerDemo");
            stopProcess(aiDashboardProcess, "DemoAiDashboard");

            if (alertManager != null) {
                try {
                    alertManager.stopRunning();
                } catch (Exception ignored) {
                }
            }
            if (logStore != null) {
                try {
                    logStore.stopRunning();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static Process launchJavaProcess(String mainClass) throws IOException {
        String javaBin = System.getProperty("java.home")
                + File.separator + "bin"
                + File.separator + "java";

        String classpath = System.getProperty("java.class.path");

        ProcessBuilder builder = new ProcessBuilder(
                javaBin,
                "-cp",
                classpath,
                mainClass
        );

        builder.inheritIO();
        return builder.start();
    }

    private static void stopProcess(Process process, String name) {
        if (process == null || !process.isAlive()) {
            return;
        }

        System.out.println("Stopping " + name + "...");
        process.destroy();

        try {
            if (!process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                process.destroyForcibly();
            }
        } catch (InterruptedException ignored) {
            process.destroyForcibly();
            Thread.currentThread().interrupt();
        }
    }
}