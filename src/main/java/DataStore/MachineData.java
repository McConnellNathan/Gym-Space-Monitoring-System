package main.java.DataStore;

import java.io.Serializable;

public record MachineData(
        String machineId,
        String machineType,
        int usageCount
) implements Serializable {}
