package app;

import aihazardanalyzer.service.MessageHandler;
import java.io.IOException;
import java.util.Map;

public class AlertSender {

    private final MessageHandler messageHandler;
    private final Map<String, Long> lastSentAlertTimes;

    public AlertSender(MessageHandler messageHandler, Map<String, Long> lastSentAlertTimes) {
        this.messageHandler = messageHandler;
        this.lastSentAlertTimes = lastSentAlertTimes;
    }

    public boolean shouldSendAlert(String key, long cooldownMs) {
        long now = System.currentTimeMillis();
        Long lastSent = lastSentAlertTimes.get(key);
        if (lastSent != null && now - lastSent < cooldownMs) {
            return false;
        }
        lastSentAlertTimes.put(key, now);
        return true;
    }

    public void sendAggressionAlertAsync(String cooldownKey, String location, double confidence) {
        if (messageHandler == null) {
            System.err.println("MessageHandler is not connected.");
            lastSentAlertTimes.remove(cooldownKey);
            return;
        }
        Thread.ofVirtual().name("hazard-send-aggression").start(() -> {
            try {
                messageHandler.sendAggressionDetectionMessage(location, confidence);
                System.out.printf("Sent aggression alert location=%s confidence=%.2f%n", location, confidence);
            } catch (IOException e) {
                lastSentAlertTimes.remove(cooldownKey);
                System.err.println("Failed to send aggression alert: " + e.getMessage());
            }
        });
    }

    public void sendFallAlertAsync(String cooldownKey, String location, double confidence) {
        if (messageHandler == null) {
            System.err.println("MessageHandler is not connected.");
            lastSentAlertTimes.remove(cooldownKey);
            return;
        }
        Thread.ofVirtual().name("hazard-send-fall").start(() -> {
            try {
                messageHandler.sendFallDetectionMessage(location, confidence);
                System.out.printf("Sent fall alert location=%s confidence=%.2f%n", location, confidence);
            } catch (IOException e) {
                lastSentAlertTimes.remove(cooldownKey);
                System.err.println("Failed to send fall alert: " + e.getMessage());
            }
        });
    }

    public void sendOvercrowdingAlertAsync(String cooldownKey, String location, double confidence, int estimatedPeople) {
        if (messageHandler == null) {
            System.err.println("MessageHandler is not connected.");
            lastSentAlertTimes.remove(cooldownKey);
            return;
        }
        Thread.ofVirtual().name("hazard-send-overcrowding").start(() -> {
            try {
                messageHandler.sendOvercrowdingMessage(location, confidence, estimatedPeople);
                System.out.printf("Sent overcrowding alert location=%s confidence=%.2f people=%d%n",
                        location, confidence, estimatedPeople);
            } catch (IOException e) {
                lastSentAlertTimes.remove(cooldownKey);
                System.err.println("Failed to send overcrowding alert: " + e.getMessage());
            }
        });
    }

    public void sendWalkwayAlertAsync(String cooldownKey, String location, double confidence) {
        if (messageHandler == null) {
            System.err.println("MessageHandler is not connected.");
            lastSentAlertTimes.remove(cooldownKey);
            return;
        }
        Thread.ofVirtual().name("hazard-send-walkway").start(() -> {
            try {
                messageHandler.sendWalkwayObstructionMessage(location, confidence);
                System.out.printf("Sent walkway alert location=%s confidence=%.2f%n", location, confidence);
            } catch (IOException e) {
                lastSentAlertTimes.remove(cooldownKey);
                System.err.println("Failed to send walkway alert: " + e.getMessage());
            }
        });
    }

    public void sendSoundAlertAsync(String cooldownKey, String location, double confidence) {
        if (messageHandler == null) {
            System.err.println("MessageHandler is not connected.");
            lastSentAlertTimes.remove(cooldownKey);
            return;
        }
        Thread.ofVirtual().name("hazard-send-sound").start(() -> {
            try {
                messageHandler.sendSoundDisturbanceMessage(location, confidence);
                System.out.printf("Sent sound alert location=%s confidence=%.2f%n", location, confidence);
            } catch (IOException e) {
                lastSentAlertTimes.remove(cooldownKey);
                System.err.println("Failed to send sound alert: " + e.getMessage());
            }
        });
    }
}