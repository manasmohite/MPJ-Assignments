package model;

/**
 * Represents a predefined time slot for booking.
 */
public class TimeSlot {
    private int slotId;
    private String label;      // e.g. "09:00 - 10:00"
    private String startTime;  // "09:00"
    private String endTime;    // "10:00"

    public TimeSlot(int slotId, String label, String startTime, String endTime) {
        this.slotId = slotId;
        this.label = label;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getSlotId() { return slotId; }
    public void setSlotId(int slotId) { this.slotId = slotId; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    @Override
    public String toString() {
        return label;
    }
}
