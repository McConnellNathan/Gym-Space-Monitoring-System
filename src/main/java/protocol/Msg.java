package main.java.protocol;

import main.java.DataStore.Employee;
import main.java.DataStore.MachineData;

import java.io.Serializable;

/**
 * The Message class will have enums to send messages between the different lays of a system.
 * The messages need to be serializable objects so ideally only contain ints and strings.
 * Message information should be organized in the following form for different messages
 *
 * creating a message:
 *  Msg.FloorInformation info =
 *         new Msg.FloorInformation(10, 20, new int[]{1, 2, 3});
 * Msg msg =
 *         new Msg.FloorUpdateMsg(5, 42, info);
 *
 * Sending Messages:
 * ObjectOutPutStream.writeObject(msg)
 * ObjectOutPutStream.flush()
 *
 * Reading Messages:
 *
 * Object obj = in.readObject()
 * if (!(obj instanceof Message msg)) {
 *     throw new IllegalStateException("Not a valid message");
 * }
 *
 * Msg msg = (msg) obj
 *
 * switch(msg){
 *     case Msg.FloorUpdateMsg m -> {
 *         System.out.println("Update for id " + m.id());
 *     }
 *     case Msg.ErrorMsg e -> {
 *         System.out.println("Error: " + e.message());
 *     }
 * }
 *
 * Author: Nathan McConnell
 */




public interface Msg extends Serializable {


    enum AlertSeverity {
        INFORMATIONAL,
        WARNING,
        CRITICAL
    }

    enum AlertType {
        FALL,
        INJURY,
        AGGRESSION,
        OVERCROWDING,
        WALKWAY_OBSTRUCTION,
        IMPROPER_EQUIPMENT_USE,
        MACHINE_MALFUNCTION,
        ENVIRONMENTAL_HAZARD,
        SOUND_DISTURBANCE,
        SYSTEM_ERROR
    }

    enum AlertStatus {
        ACTIVE,
        RESOLVED
    }

    record Ping() implements Msg {}

    record Pong() implements Msg {}

    record DashboardConnected() implements Msg {}

    // Generic error response used when a request cannot be processed.
    record ErrorMsg(int code, int spotID, String message) implements Msg {}

//    this disconnected message is for disconnecting sockets/streamn
//    NOT for light or sensor errors
    record DisconnectMsg(String disconnectMsg) implements Msg {}

    // Sent from the AI Hazard Analyzer to report a newly detected hazard.
    record HazardDetectionMsg(
            AlertType type,
            String location,
            double confidence,
            String description,
            long timestampEpochMillis
    ) implements Msg {}

    // Serializable dashboard alert payload shared inside dashboard notification snapshots.
    record AlertNotification(
            String alertId,
            AlertType type,
            AlertSeverity severity,
            AlertStatus status,
            String location,
            String description,
            long timestampEpochMillis
    ) implements Serializable {}

    // Sent from the Alert Manager to dashboards as a full alert snapshot.
    record AlertNotificationMsg(
            AlertNotification[] alerts,
            long timestampEpochMillis
    ) implements Msg {}

//    // Sent from the Employee Dashboard when an alert is acknowledged or resolved.
//    record AlertStatusUpdateMsg(
//            String alertId,
//            AlertStatus newStatus,
//            String employeeId,
//            long timestampEpochMillis
//    ) implements Msg {}

    // Sent from the Employee Dashboard when staff acknowledge an alert.
    record AlertAcknowledgementMsg(
            String alertId,
            String employeeId,
            long timestampEpochMillis
    ) implements Msg {}

    // Serializable log entry shared with the Log Store.
    record LogRecord(
            String logId,
            String logType,
            String source,
            String description,
            String relatedAlertId,
            long timestampEpochMillis
    ) implements Serializable {}

    // Sent to the Log Store to persist a log record.
    record LogWriteRequestMsg(LogRecord record) implements Msg {}

    // Returned by the Log Store after attempting to persist a log record.
    record LogWriteResponseMsg(String logId, boolean success, String message) implements Msg {}

    // Sent to the Log Store to query logs for a type and time range.
    record LogReadRequestMsg(
            String requestId,
            String logType,
            long startTimeEpochMillis,
            long endTimeEpochMillis
    ) implements Msg {}

    // Returned by the Log Store with log query results.
    record LogReadResponseMsg(
            String requestId,
            LogRecord[] records,
            boolean success,
            String message
    ) implements Msg {}

    // Sent to the Log Store to delete records related to a specific alert.
    record LogDeleteRequestMsg(
            String requestId,
            String relatedAlertId
    ) implements Msg {}

    // Returned by the Log Store after deleting records related to a specific alert.
    record LogDeleteResponseMsg(
            String requestId,
            int deletedCount,
            boolean success,
            String message
    ) implements Msg {}

    // Serializable member entry payload written to the Log Store by the door scanner.
    record MemberEnterRecord(
            String description,
            int month,
            int day,
            int year,
            String time
    ) implements Serializable {}

    // Sent to the Log Store when a member enters through the door scanner.
    record MemberEnter(MemberEnterRecord record) implements Msg {}

    // Sent to the Log Store to retrieve the current machine data list.
    record RequestMachineData() implements Msg {}

    // Returned by the Log Store with the current machine data list.
    record MachineDataResponseMsg(
            MachineData[] machineData,
            boolean success,
            String message
    ) implements Msg {}

    // Sent to the Log Store to retrieve the current member entry list.
    record RequestMemberData() implements Msg {}

    // Returned by the Log Store with the current member entry list.
    record MemberDataResponseMsg(
            MemberEnterRecord[] memberData,
            boolean success,
            String message
    ) implements Msg {}

    // Sent to the Membership Store to authenticate and retrieve an employee by username.
    record RequestEmployee(
            String employeeUsername,
            String password
    ) implements Msg {}

    // Returned by the Membership Store after an employee lookup/authentication attempt.
    record EmployeeResponseMsg(
            Employee employee,
            boolean success,
            String message
    ) implements Msg {}

    // Sent to the Membership Store to verify manager credentials for restricted actions.
    record VerifyManager(
            String employeeUsername,
            String password
    ) implements Msg {}

    // Returned by the Membership Store after manager credential verification.
    record VerifyManagerResponse(
            boolean validManager,
            String employeeId,
            String message
    ) implements Msg {}
}
