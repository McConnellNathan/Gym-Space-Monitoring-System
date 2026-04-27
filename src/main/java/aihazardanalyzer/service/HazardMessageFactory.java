package aihazardanalyzer.service;

import protocol.Msg;

import java.util.UUID;

/**
 * Builds hazard detection protocol messages for the Alert Manager.
 */
public final class HazardMessageFactory {

    private HazardMessageFactory() {
    }

    public static Msg.HazardDetectionMsg buildFallDetectionMessage(String location, double confidence) {
        return buildMessage(
                Msg.AlertType.FALL,
                location,
                confidence,
                "Possible fall detected"
        );
    }

    public static Msg.HazardDetectionMsg buildInjuryDetectionMessage(String location, double confidence) {
        return buildMessage(
                Msg.AlertType.INJURY,
                location,
                confidence,
                "Possible injury detected"
        );
    }

    public static Msg.HazardDetectionMsg buildAggressionDetectionMessage(String location, double confidence) {
        return buildMessage(
                Msg.AlertType.AGGRESSION,
                location,
                confidence,
                "Possible aggression detected"
        );
    }

    public static Msg.HazardDetectionMsg buildOvercrowdingMessage(
            String location,
            double confidence,
            int estimatedPeople
    ) {
        return buildMessage(
                Msg.AlertType.OVERCROWDING,
                location,
                confidence,
                "Possible overcrowding detected. Estimated people: " + estimatedPeople
        );
    }

    public static Msg.HazardDetectionMsg buildWalkwayObstructionMessage(String location, double confidence) {
        return buildMessage(
                Msg.AlertType.WALKWAY_OBSTRUCTION,
                location,
                confidence,
                "Possible walkway obstruction detected"
        );
    }

    public static Msg.HazardDetectionMsg buildImproperEquipmentUseMessage(
            String location,
            double confidence
    ) {
        return buildMessage(
                Msg.AlertType.IMPROPER_EQUIPMENT_USE,
                location,
                confidence,
                "Possible improper equipment use detected"
        );
    }

    private static Msg.HazardDetectionMsg buildMessage(
            Msg.AlertType type,
            String location,
            double confidence,
            String description
    ) {
        return new Msg.HazardDetectionMsg(
                type,
                location,
                confidence,
                description,
                System.currentTimeMillis()
        );
    }
}
