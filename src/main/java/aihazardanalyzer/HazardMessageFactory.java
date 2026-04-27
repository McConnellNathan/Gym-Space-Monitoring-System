package main.java.aihazardanalyzer;

import main.java.protocol.Msg;

import java.util.UUID;

/**
 * Builds hazard detection protocol messages for the Alert Manager.
 */
public final class HazardMessageFactory {

    private HazardMessageFactory() {
    }

    public static Msg.HazardDetectionMsg buildFallDetectionMessage(int zoneId, String location, double confidence) {
        return buildMessage(
                Msg.AlertType.FALL,
                zoneId,
                location,
                confidence,
                "Possible fall detected in zone " + zoneId
        );
    }

    public static Msg.HazardDetectionMsg buildInjuryDetectionMessage(int zoneId, String location, double confidence) {
        return buildMessage(
                Msg.AlertType.INJURY,
                zoneId,
                location,
                confidence,
                "Possible injury detected in zone " + zoneId
        );
    }

    public static Msg.HazardDetectionMsg buildAggressionDetectionMessage(int zoneId, String location, double confidence) {
        return buildMessage(
                Msg.AlertType.AGGRESSION,
                zoneId,
                location,
                confidence,
                "Possible aggression detected in zone " + zoneId
        );
    }

    public static Msg.HazardDetectionMsg buildOvercrowdingMessage(
            int zoneId,
            String location,
            double confidence,
            int estimatedPeople
    ) {
        return buildMessage(
                Msg.AlertType.OVERCROWDING,
                zoneId,
                location,
                confidence,
                "Possible overcrowding detected in zone " + zoneId + ". Estimated people: " + estimatedPeople
        );
    }

    public static Msg.HazardDetectionMsg buildWalkwayObstructionMessage(int zoneId, String location, double confidence) {
        return buildMessage(
                Msg.AlertType.WALKWAY_OBSTRUCTION,
                zoneId,
                location,
                confidence,
                "Possible walkway obstruction detected in zone " + zoneId
        );
    }

    public static Msg.HazardDetectionMsg buildImproperEquipmentUseMessage(
            int zoneId,
            String location,
            double confidence
    ) {
        return buildMessage(
                Msg.AlertType.IMPROPER_EQUIPMENT_USE,
                zoneId,
                location,
                confidence,
                "Possible improper equipment use detected in zone " + zoneId
        );
    }

    private static Msg.HazardDetectionMsg buildMessage(
            Msg.AlertType type,
            int zoneId,
            String location,
            double confidence,
            String description
    ) {
        return new Msg.HazardDetectionMsg(
                UUID.randomUUID().toString(),
                type,
                zoneId,
                location,
                confidence,
                description,
                System.currentTimeMillis()
        );
    }
}
