package main.java.aihazardanalyzer;

import main.java.utility.RemoteMessageClient;

import java.io.IOException;

public final class MessageHandler implements AutoCloseable {

    private static final String DEFAULT_ALERT_MANAGER_HOST = "localhost";
    private static final int DEFAULT_ALERT_MANAGER_PORT = 6000;

    private static MessageHandler instance;

    private final RemoteMessageClient alertManagerClient;

    private MessageHandler() throws IOException {
        this(DEFAULT_ALERT_MANAGER_HOST, DEFAULT_ALERT_MANAGER_PORT);
    }

    private MessageHandler(String host, int port) throws IOException {
        this.alertManagerClient = new RemoteMessageClient(host, port);
    }

    public static synchronized MessageHandler getInstance() throws IOException {
        return getInstance(DEFAULT_ALERT_MANAGER_HOST, DEFAULT_ALERT_MANAGER_PORT);
    }

    public static synchronized MessageHandler getInstance(String host, int port) throws IOException {
        if (instance == null || instance.alertManagerClient.isClosed()) {
            instance = new MessageHandler(host, port);
        }
        return instance;
    }

    public synchronized void sendFallDetectionMessage(String location, double confidence) throws IOException {
        alertManagerClient.send(HazardMessageFactory.buildFallDetectionMessage(location, confidence));
    }

    public synchronized void sendInjuryDetectionMessage(String location, double confidence) throws IOException {
        alertManagerClient.send(HazardMessageFactory.buildInjuryDetectionMessage(location, confidence));
    }

    public synchronized void sendAggressionDetectionMessage(String location, double confidence) throws IOException {
        alertManagerClient.send(HazardMessageFactory.buildAggressionDetectionMessage(location, confidence));
    }

    public synchronized void sendOvercrowdingMessage(
            String location,
            double confidence,
            int estimatedPeople
    ) throws IOException {
        alertManagerClient.send(HazardMessageFactory.buildOvercrowdingMessage(
                location,
                confidence,
                estimatedPeople
        ));
    }

    public synchronized void sendWalkwayObstructionMessage(String location, double confidence) throws IOException {
        alertManagerClient.send(HazardMessageFactory.buildWalkwayObstructionMessage(location, confidence));
    }

    public synchronized void sendImproperEquipmentUseMessage(String location, double confidence) throws IOException {
        alertManagerClient.send(HazardMessageFactory.buildImproperEquipmentUseMessage(location, confidence));
    }

    @Override
    public synchronized void close() throws IOException {
        alertManagerClient.close();
        synchronized (MessageHandler.class) {
            if (instance == this) {
                instance = null;
            }
        }
    }
}
