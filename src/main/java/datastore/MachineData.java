package DataStore;

import java.io.Serializable;

/**
 * Demo machine usage summary returned by the Log Store.
 */
public record MachineData(
        String machineId,
        String machineType,
        int usageCount
) implements Serializable {}
