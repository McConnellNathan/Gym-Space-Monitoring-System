package AlertManager;

import protocol.Msg;

/**
 * Internal Alert Manager model created from incoming hazard detections.
 *
 * <p>This is separate from protocol messages so the manager can track status and severity
 * while still sending compact dashboard snapshots.</p>
 */
public record Alert(
        String alertId,
        Msg.AlertType type,
        Msg.AlertSeverity severity,
        Msg.AlertStatus status,
        String location,
        String description,
        double confidence,
        long timestampEpochMillis
) {

    /**
     * Converts an incoming hazard detection into a newly active alert.
     *
     * @param alertId generated alert identifier
     * @param detection hazard message received from the AI analyzer
     * @param severity severity assigned by the Alert Manager
     * @return active alert ready to be stored and broadcast
     */
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
                detection.location(),
                detection.description(),
                detection.confidence(),
                detection.timestampEpochMillis()
        );
    }
}
