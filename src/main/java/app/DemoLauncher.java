package app;

import alertmanager.AlertManager;
import datastore.LogStore;
import datastore.MembershipStore;

import java.io.File;
import java.io.IOException;

public class DemoLauncher {
    private static final String HOST = "localhost";
    private static final int LOG_STORE_PORT = 5000;
    private static final int ALERT_MANAGER_PORT = 6000;
    private static final int MEMBERSHIP_STORE_PORT = 5001;

    public static void main(String[] args) throws InterruptedException {
        LogStore logStore = null;
        MembershipStore membershipStore = null;
        AlertManager alertManager = null;
        Process scannerProcess = null;
        Process aiDashboardProcess = null;
        Process guiProcess = null;

        try {
            System.out.println("Creating LogStore...");
            logStore = new LogStore(HOST, LOG_STORE_PORT);

            System.out.println("Starting LogStore...");
            logStore.start();

            Thread.sleep(250);

            System.out.println("Creating MembershipStore...");
            membershipStore = new MembershipStore(HOST, MEMBERSHIP_STORE_PORT);

            System.out.println("Starting MembershipStore...");
            membershipStore.start();

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
            System.out.printf("MembershipStore started on %s:%d%n", HOST, MEMBERSHIP_STORE_PORT);
            System.out.printf("AlertManager started on %s:%d%n", HOST, ALERT_MANAGER_PORT);

            System.out.println("Launching DoorScannerDemo...");
            scannerProcess = launchJavaProcess("app.DoorScannerDemo");

            Thread.sleep(500);

            System.out.println("Launching DemoAiDashboard...");
            aiDashboardProcess = launchJavaProcess("app.DemoAiDashboard");

            Thread.sleep(500);

            System.out.println("Launching MainGuiApp...");
            guiProcess = launchJavaProcess("gui.MainGuiApp");

            Process finalScannerProcess = scannerProcess;
            Process finalAiDashboardProcess = aiDashboardProcess;
            Process finalGuiProcess = guiProcess;
            LogStore finalLogStore = logStore;
            MembershipStore finalMembershipStore = membershipStore;
            AlertManager finalAlertManager = alertManager;

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down demo...");

                stopProcess(finalScannerProcess, "DoorScannerDemo");
                stopProcess(finalAiDashboardProcess, "DemoAiDashboard");
                stopProcess(finalGuiProcess, "MainGuiApp");

                try {
                    finalAlertManager.stopRunning();
                } catch (Exception ignored) {
                }

                try {
                    finalMembershipStore.stopRunning();
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
            stopProcess(guiProcess, "MainGuiApp");

            if (alertManager != null) {
                try {
                    alertManager.stopRunning();
                } catch (Exception ignored) {
                }
            }
            if (membershipStore != null) {
                try {
                    membershipStore.stopRunning();
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