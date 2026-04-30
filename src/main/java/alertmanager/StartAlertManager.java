package alertmanager;

/**
 * Command-line entry point for running the Alert Manager server.
 *
 * <p>Defaults to Alert Manager {@code localhost:6000} and Log Store
 * {@code localhost:5000}. Pass {@code host alertPort logStoreHost logStorePort}
 * to override.</p>
 */
public class StartAlertManager {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 6000;
    private static final String DEFAULT_LOG_STORE_HOST = "localhost";
    private static final int DEFAULT_LOG_STORE_PORT = 5000;

    public static void main(String[] args) throws InterruptedException {
        startAlertManager(args);
    }

    /**
     * Starts the Alert Manager and blocks until the server is shut down.
     *
     * @param args optional host, alert-manager port, log-store host, and log-store port
     */
    public static void startAlertManager(String[] args) throws InterruptedException {
        String host = args.length > 0 && !args[0].isBlank() ? args[0] : DEFAULT_HOST;
        int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;
        String logStoreHost = args.length > 2 && !args[2].isBlank() ? args[2] : DEFAULT_LOG_STORE_HOST;
        int logStorePort = args.length > 3 ? Integer.parseInt(args[3]) : DEFAULT_LOG_STORE_PORT;

        AlertManager alertManager = new AlertManager(host, port, logStoreHost, logStorePort);
        Runtime.getRuntime().addShutdownHook(new Thread(alertManager::stopRunning));

        System.out.printf("[StartAlertManager] Starting AlertManager on %s:%d%n", host, port);
        System.out.printf("[StartAlertManager] Connecting to LogStore at %s:%d%n", logStoreHost, logStorePort);
        alertManager.start();
        alertManager.awaitShutdown();
    }
}
