package AlertManager;

import protocol.Msg;

/**
 * Internal Alert Manager model created from incoming hazard detections.
 */
public record Alert(
        String alertId,
        Msg.AlertType type,
        Msg.AlertSeverity severity,
        Msg.AlertStatus status,
        int zoneId,
        String location,
        String description,
        double confidence,
        long timestampEpochMillis
) {

    public static Alert fromHazardDetection(
            String alertId,
            Msg.HazardDetectionMsg detection,
            Msg.AlertSeverity severity
    ) {
        return new Alert(
                alertId,
                detection.type(),
                severity,
                Msg.AlertStatus.ACTIVE,
                detection.zoneId(),
                detection.location(),
                detection.description(),
                detection.confidence(),
                detection.timestampEpochMillis()
        );
    }
}
