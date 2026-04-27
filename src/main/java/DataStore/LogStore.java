package main.java.DataStore;

import main.java.protocol.Envelope;
import main.java.protocol.Msg;
import main.java.utility.Server;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.CopyOnWriteArrayList;

public class LogStore extends Server {

    private static final int UNKNOWN_MESSAGE_ERROR_CODE = -200;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String[] MACHINE_TYPES = {
            "TreadMill",
            "StairMaster",
            "RowMachine",
            "Elliptical",
            "ExerciseBike",
            "CableMachine",
            "SmithMachine",
            "LegPress",
            "ChestPress",
            "LatPulldown"
    };

    private final List<Msg.LogRecord> records = new CopyOnWriteArrayList<>();
    private final List<Msg.MemberEnterRecord> memberEnterRecords = new CopyOnWriteArrayList<>();
    private final ArrayList<MachineData> machineData = new ArrayList<>();

    public LogStore(int port) {
        super(port);
        initializeMachineData();
        initializeMemberEnterData();
    }

    public LogStore(String host, int port) {
        super(host, port);
        initializeMachineData();
        initializeMemberEnterData();
    }

    @Override
    public void processMessage(Envelope env) {
        Msg msg = env.msg();

        try {
            switch (msg) {
                case Msg.LogWriteRequestMsg writeRequestMsg -> {
                    Msg.LogRecord storedRecord = normalizeRecord(writeRequestMsg.record());
                    records.add(storedRecord);
                    env.replyTo().send(new Msg.LogWriteResponseMsg(
                            storedRecord.logId(),
                            true,
                            "Log written successfully"
                    ));
                }
                case Msg.LogReadRequestMsg readRequestMsg -> {
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
                }
                case Msg.LogDeleteRequestMsg deleteRequestMsg -> {
                    int deletedCount = deleteRecordsForAlert(deleteRequestMsg.relatedAlertId());
                    env.replyTo().send(new Msg.LogDeleteResponseMsg(
                            deleteRequestMsg.requestId(),
                            deletedCount,
                            true,
                            "Deleted " + deletedCount + " log record(s)"
                    ));
                }
                case Msg.MemberEnter memberEnter -> {
                    Msg.MemberEnterRecord record = normalizeMemberEnterRecord(memberEnter.record());
                    memberEnterRecords.add(record);
                    env.replyTo().send(new Msg.LogWriteResponseMsg(
                            null,
                            true,
                            "Member entry recorded successfully"
                    ));
                }
                case Msg.RequestMachineData ignored -> env.replyTo().send(new Msg.MachineDataResponseMsg(
                        machineData.toArray(MachineData[]::new),
                        true,
                        "Machine data retrieved successfully"
                ));
                case Msg.Ping ignored -> env.replyTo().send(new Msg.Pong());
                default -> env.replyTo().send(new Msg.ErrorMsg(
                        UNKNOWN_MESSAGE_ERROR_CODE,
                        -1,
                        "Unsupported message type: " + msg.getClass().getSimpleName()
                ));
            }
        } catch (IOException e) {
            System.err.println("[LogStore] Failed to reply to client: " + e.getMessage());
            env.replyTo().close();
        }
    }

    private void initializeMachineData() {
        if (!machineData.isEmpty()) {
            return;
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < MACHINE_TYPES.length; i++) {
            machineData.add(new MachineData(
                    "M-" + String.format("%03d", i + 1),
                    MACHINE_TYPES[i],
                    random.nextInt(5, 250)
            ));
        }
    }

    private void initializeMemberEnterData() {
        if (!memberEnterRecords.isEmpty()) {
            return;
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        LocalDate today = LocalDate.now();

        for (int daysBack = 56; daysBack >= 0; daysBack--) {
            LocalDate date = today.minusDays(daysBack);
            int entriesForDay = sampleEntriesForDay(date.getDayOfWeek(), random);

            for (int i = 0; i < entriesForDay; i++) {
                LocalTime entryTime = sampleGymEntryTime(random);
                memberEnterRecords.add(new Msg.MemberEnterRecord(
                        "Member entered gym",
                        date.getMonthValue(),
                        date.getDayOfMonth(),
                        date.getYear(),
                        entryTime.format(TIME_FORMATTER)
                ));
            }
        }
    }

    private int sampleEntriesForDay(DayOfWeek dayOfWeek, ThreadLocalRandom random) {
        return switch (dayOfWeek) {
            case MONDAY, TUESDAY, WEDNESDAY, THURSDAY -> random.nextInt(26, 41);
            case FRIDAY -> random.nextInt(20, 33);
            case SATURDAY -> random.nextInt(10, 19);
            case SUNDAY -> random.nextInt(8, 16);
        };
    }

    private LocalTime sampleGymEntryTime(ThreadLocalRandom random) {
        double meanHour = 17.5;
        double standardDeviation = 2.2;
        double sampledHour = meanHour + random.nextGaussian() * standardDeviation;
        double clampedHour = Math.max(5.5, Math.min(21.5, sampledHour));

        int hour = (int) clampedHour;
        int minute = (int) Math.round((clampedHour - hour) * 60);
        if (minute == 60) {
            hour++;
            minute = 0;
        }

        return LocalTime.of(Math.min(hour, 23), minute, random.nextInt(0, 60));
    }

    private Msg.MemberEnterRecord normalizeMemberEnterRecord(Msg.MemberEnterRecord record) {
        if (record == null) {
            return new Msg.MemberEnterRecord(
                    "Unknown member entry",
                    0,
                    0,
                    0,
                    "00:00:00"
            );
        }

        return record;
    }

    private Msg.LogRecord normalizeRecord(Msg.LogRecord record) {
        if (record == null) {
            return new Msg.LogRecord(
                    UUID.randomUUID().toString(),
                    "UNKNOWN",
                    "LogStore",
                    "Empty log write request received",
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
