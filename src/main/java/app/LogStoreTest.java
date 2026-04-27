package app;

import DataStore.LogStore;

// This is just for testing LogStore locally. will need to spin it up a better way for the demo
public class LogStoreTest {
    public static void main(String[] args) throws InterruptedException {
        int port = 5001;

        LogStore logStore = new LogStore(port);
        logStore.start();

        System.out.println("[LogStoreTestRunner] LogStore started on port " + port);
        System.out.println("[LogStoreTestRunner] Waiting for log requests...");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[LogStoreTestRunner] Shutting down LogStore...");
            logStore.stopRunning();
        }));

        logStore.awaitShutdown();
    }
}
