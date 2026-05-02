package gui.common;

import java.util.List;

/**
 * Making mock data for now for testing purposes
 */

public class MockDashboardData implements DashboardGateway{
    @Override
    public int getCurrentOccupancy() {
        return 42;
    }

    @Override
    public int getMaxOccupancy() {
        return 100;
    }

    @Override
    public List<String> getClassSchedule() {
        return List.of(
                "Yoga - 10:00 AM - 8 spots left",
                "Spin - 12:00 PM - 3 spots left",
                "HIIT - 5:30 PM - Full",
                "Pilates - 7:00 PM - 5 spots left"
        );
    }

    @Override
    public boolean registerForClass(String className) {
        return !className.toLowerCase().contains("full");
    }
}
