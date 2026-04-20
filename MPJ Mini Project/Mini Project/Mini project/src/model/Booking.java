package model;

import java.time.LocalDate;

/**
 * Represents a facility booking record.
 * Purpose type determines priority resolution.
 */
public class Booking {
    // Purpose types — used for priority resolution
    public enum Purpose {
        EXAM(1),
        FACULTY_CLASS(2),
        CLUB_EVENT(3),
        STUDENT_STUDY(4);

        private final int priority;
        Purpose(int priority) { this.priority = priority; }
        public int getPriority() { return priority; }
    }

    public enum Status {
        CONFIRMED, WAITLISTED, CANCELLED, OVERRIDDEN
    }

    private int bookingId;
    private int userId;
    private int facilityId;
    private int slotId;
    private LocalDate bookingDate;
    private Purpose purpose;
    private Status status;
    private String notes;
    private String createdAt;

    // For display purposes (joined data)
    private String userName;
    private String facilityName;
    private String slotLabel;

    public Booking(int bookingId, int userId, int facilityId, int slotId,
                   LocalDate bookingDate, Purpose purpose, Status status, String notes) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.facilityId = facilityId;
        this.slotId = slotId;
        this.bookingDate = bookingDate;
        this.purpose = purpose;
        this.status = status;
        this.notes = notes;
    }

    // Getters & Setters
    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getFacilityId() { return facilityId; }
    public void setFacilityId(int facilityId) { this.facilityId = facilityId; }

    public int getSlotId() { return slotId; }
    public void setSlotId(int slotId) { this.slotId = slotId; }

    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }

    public Purpose getPurpose() { return purpose; }
    public void setPurpose(Purpose purpose) { this.purpose = purpose; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getFacilityName() { return facilityName; }
    public void setFacilityName(String facilityName) { this.facilityName = facilityName; }

    public String getSlotLabel() { return slotLabel; }
    public void setSlotLabel(String slotLabel) { this.slotLabel = slotLabel; }

    public int getPriority() {
        return purpose.getPriority();
    }

    @Override
    public String toString() {
        return "Booking #" + bookingId + " | " + facilityName + " | " +
               bookingDate + " | " + slotLabel + " | " + purpose + " | " + status;
    }
}
