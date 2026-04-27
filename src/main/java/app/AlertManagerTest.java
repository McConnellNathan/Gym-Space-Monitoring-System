package app;

import AlertManager.AlertManager;


// This is just for testing AlertManager locally. will need to spin it up a better way for the demo
public class AlertManagerTest {
    public static void main(String[] args) throws InterruptedException {
        int alertManagerPort = 5000;
        int logStorePort = 5001;

        AlertManager alertManager = new AlertManager(
                alertManagerPort,
                "localhost",
                logStorePort
        );
        alertManager.start();

        System.out.println("[AlertManagerTestRunner] AlertManager started on port " + alertManagerPort);
        System.out.println("[AlertManagerTestRunner] Connected to LogStore on port " + logStorePort);
        System.out.println("[AlertManagerTestRunner] Waiting for hazard messages...");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[AlertManagerTestRunner] Shutting down AlertManager...");
            alertManager.stopRunning();
        }));

        alertManager.awaitShutdown();
    }
}
