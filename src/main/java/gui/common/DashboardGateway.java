package gui.common;

import java.util.List;

/**
 * Interface to communicate with Dashboard class
 */

public interface DashboardGateway {
    int getCurrentOccupancy();
    int getMaxOccupancy();
    List<String> getClassSchedule();
    boolean registerForClass(String className);
}
