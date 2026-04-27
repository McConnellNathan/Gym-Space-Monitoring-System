package doorscanner;

import protocol.Msg;
import utility.RemoteMessageClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScanMessageHandler implements AutoCloseable {

    private static final String DEFAULT_LOG_STORE_HOST = "localhost";
    private static final int DEFAULT_LOG_STORE_PORT = 5000;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static ScanMessageHandler instance;

    private final RemoteMessageClient logStoreClient;

    private ScanMessageHandler() throws IOException {
        this(DEFAULT_LOG_STORE_HOST, DEFAULT_LOG_STORE_PORT);
    }

    private ScanMessageHandler(String host, int port) throws IOException {
        this.logStoreClient = new RemoteMessageClient(host, port);
    }

    public static synchronized ScanMessageHandler getInstance() throws IOException {
        return getInstance(DEFAULT_LOG_STORE_HOST, DEFAULT_LOG_STORE_PORT);
    }

    public static synchronized ScanMessageHandler getInstance(String host, int port) throws IOException {
        if (instance == null || instance.logStoreClient.isClosed()) {
            instance = new ScanMessageHandler(host, port);
        }
        return instance;
    }

    public synchronized Msg sendScanMessage(String description) throws IOException, ClassNotFoundException {
        LocalDateTime now = LocalDateTime.now();
        Msg.MemberEnterRecord record = new Msg.MemberEnterRecord(
                description,
                now.getMonthValue(),
                now.getDayOfMonth(),
                now.getYear(),
                now.toLocalTime().format(TIME_FORMATTER)
        );

        return logStoreClient.sendAndRead(new Msg.MemberEnter(record));
    }

    @Override
    public synchronized void close() throws IOException {
        logStoreClient.close();
        synchronized (ScanMessageHandler.class) {
            if (instance == this) {
                instance = null;
            }
        }
    }
}
