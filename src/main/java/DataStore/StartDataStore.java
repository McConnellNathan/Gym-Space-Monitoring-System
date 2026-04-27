package main.java.DataStore;

/**
 * Command-line entry point for running the data-store services together.
 *
 * <p>Starts the Log Store on {@code localhost:5000} and the Membership Store on
 * {@code localhost:5001} by default. Pass {@code host logPort membershipPort} to override.</p>
 */
public class StartDataStore {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_LOG_STORE_PORT = 5000;
    private static final int DEFAULT_MEMBERSHIP_STORE_PORT = 5001;

    public static void main(String[] args) throws InterruptedException {
        startDataStore(args);
    }

    /**
     * Starts both data-store servers and blocks until they are shut down.
     *
     * @param args optional host, log-store port, and membership-store port
     */
    public static void startDataStore(String[] args) throws InterruptedException {
        String host = args.length > 0 && !args[0].isBlank() ? args[0] : DEFAULT_HOST;
        int logStorePort = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_LOG_STORE_PORT;
        int membershipStorePort = args.length > 2 ? Integer.parseInt(args[2]) : DEFAULT_MEMBERSHIP_STORE_PORT;

        LogStore logStore = new LogStore(host, logStorePort);
        MembershipStore membershipStore = new MembershipStore(host, membershipStorePort);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logStore.stopRunning();
            membershipStore.stopRunning();
        }));

        System.out.printf("[StartDataStore] Starting LogStore on %s:%d%n", host, logStorePort);
        System.out.printf("[StartDataStore] Starting MembershipStore on %s:%d%n", host, membershipStorePort);
        logStore.start();
        membershipStore.start();
        logStore.awaitShutdown();
        membershipStore.awaitShutdown();
    }
}
