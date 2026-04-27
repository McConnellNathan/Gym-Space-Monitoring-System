package DataStore;

public class StartLogStore {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5000;

    public static void main(String[] args) throws InterruptedException {
        startLogStore(args);
    }

    public static void startLogStore(String[] args) throws InterruptedException {
        String host = args.length > 0 && !args[0].isBlank() ? args[0] : DEFAULT_HOST;
        int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;

        LogStore logStore = new LogStore(host, port);
        Runtime.getRuntime().addShutdownHook(new Thread(logStore::stopRunning));

        System.out.printf("[StartLogStore] Starting LogStore on %s:%d%n", host, port);
        logStore.start();
        logStore.awaitShutdown();
    }
}
