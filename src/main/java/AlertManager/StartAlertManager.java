package AlertManager;

/**
 * Command-line entry point for running the Alert Manager server.
 *
 * <p>Defaults to {@code localhost:6000}. Pass {@code host port} to override.</p>
 */
public class StartAlertManager {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 6000;

    public static void main(String[] args) throws InterruptedException {
        startAlertManager(args);
    }

    /**
     * Starts the Alert Manager and blocks until the server is shut down.
     *
     * @param args optional host and port arguments
     */
    public static void startAlertManager(String[] args) throws InterruptedException {
        String host = args.length > 0 && !args[0].isBlank() ? args[0] : DEFAULT_HOST;
        int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;

        AlertManager alertManager = new AlertManager(host, port);
        Runtime.getRuntime().addShutdownHook(new Thread(alertManager::stopRunning));

        System.out.printf("[StartAlertManager] Starting AlertManager on %s:%d%n", host, port);
        alertManager.start();
        alertManager.awaitShutdown();
    }
}
