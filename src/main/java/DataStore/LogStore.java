package DataStore;

import protocol.Envelope;
import protocol.Msg;
import utility.Server;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class LogStore extends Server {

    private static final int UNKNOWN_MESSAGE_ERROR_CODE = -200;

    private final List<Msg.LogRecord> records = new CopyOnWriteArrayList<>();

    public LogStore(int port) {
        super(port);
    }

    @Override
    public void processMessage(Envelope env) {
        Msg msg = env.msg();

        try {
            if (msg instanceof Msg.LogWriteRequestMsg writeRequestMsg) {
                Msg.LogRecord storedRecord = normalizeRecord(writeRequestMsg.record());
                records.add(storedRecord);
                env.replyTo().send(new Msg.LogWriteResponseMsg(
                        storedRecord.logId(),
                        true,
                        "Log written successfully"
                ));
                return;
            }

            if (msg instanceof Msg.LogReadRequestMsg readRequestMsg) {
                Msg.LogRecord[] matchingRecords = records.stream()
                        .filter(record -> matchesLogType(record, readRequestMsg.logType()))
                        .filter(record -> matchesTimeRange(
                                record,
                                readRequestMsg.startTimeEpochMillis(),
                                readRequestMsg.endTimeEpochMillis()
                        ))
                        .toArray(Msg.LogRecord[]::new);

                env.replyTo().send(new Msg.LogReadResponseMsg(
                        readRequestMsg.requestId(),
                        matchingRecords,
                        true,
                        "Logs retrieved successfully"
                ));
                return;
            }

            if (msg instanceof Msg.LogDeleteRequestMsg deleteRequestMsg) {
                int deletedCount = deleteRecordsForAlert(deleteRequestMsg.relatedAlertId());
                env.replyTo().send(new Msg.LogDeleteResponseMsg(
                        deleteRequestMsg.requestId(),
                        deletedCount,
                        true,
                        "Deleted " + deletedCount + " log record(s)"
                ));
                return;
            }

            if (msg instanceof Msg.Ping) {
                env.replyTo().send(new Msg.Pong());
                return;
            }

            env.replyTo().send(new Msg.ErrorMsg(
                    UNKNOWN_MESSAGE_ERROR_CODE,
                    -1,
                    "Unsupported message type: " + msg.getClass().getSimpleName()
            ));
        } catch (IOException e) {
            System.err.println("[LogStore] Failed to reply to client: " + e.getMessage());
            env.replyTo().close();
        }
    }

    private Msg.LogRecord normalizeRecord(Msg.LogRecord record) {
        if (record == null) {
            return new Msg.LogRecord(
                    UUID.randomUUID().toString(),
                    "UNKNOWN",
                    "LogStore",
                    "Empty log write request received",
                    -1,
                    null,
                    System.currentTimeMillis()
            );
        }

        if (record.logId() != null && !record.logId().isBlank()) {
            return record;
        }

        return new Msg.LogRecord(
                UUID.randomUUID().toString(),
                record.logType(),
                record.source(),
                record.description(),
                record.zoneId(),
                record.relatedAlertId(),
                record.timestampEpochMillis()
        );
    }

    private boolean matchesLogType(Msg.LogRecord record, String requestedLogType) {
        return requestedLogType == null
                || requestedLogType.isBlank()
                || "ALL".equalsIgnoreCase(requestedLogType)
                || requestedLogType.equals(record.logType());
    }

    private boolean matchesTimeRange(Msg.LogRecord record, long startTimeEpochMillis, long endTimeEpochMillis) {
        long timestamp = record.timestampEpochMillis();
        return timestamp >= startTimeEpochMillis && timestamp <= endTimeEpochMillis;
    }

    private int deleteRecordsForAlert(String relatedAlertId) {
        int deletedCount = 0;
        for (Msg.LogRecord record : records) {
            if (Objects.equals(record.relatedAlertId(), relatedAlertId) && records.remove(record)) {
                deletedCount++;
            }
        }
        return deletedCount;
    }
}
