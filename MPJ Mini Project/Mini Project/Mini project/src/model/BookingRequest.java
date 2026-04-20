package model;

import java.time.LocalDate;

/**
 * Encapsulates a booking request passed to the booking thread engine.
 */
public class BookingRequest {
    private User requester;
    private int facilityId;
    private int slotId;
    private LocalDate date;
    private Booking.Purpose purpose;
    private String notes;

    // Result fields — populated after thread processes the request
    private volatile String resultMessage;
    private volatile Booking.Status resultStatus;
    private volatile boolean processed = false;

    public BookingRequest(User requester, int facilityId, int slotId,
                          LocalDate date, Booking.Purpose purpose, String notes) {
        this.requester = requester;
        this.facilityId = facilityId;
        this.slotId = slotId;
        this.date = date;
        this.purpose = purpose;
        this.notes = notes;
    }

    public User getRequester() { return requester; }
    public int getFacilityId() { return facilityId; }
    public int getSlotId() { return slotId; }
    public LocalDate getDate() { return date; }
    public Booking.Purpose getPurpose() { return purpose; }
    public String getNotes() { return notes; }

    public synchronized String getResultMessage() { return resultMessage; }
    public synchronized void setResultMessage(String msg) { this.resultMessage = msg; }

    public synchronized Booking.Status getResultStatus() { return resultStatus; }
    public synchronized void setResultStatus(Booking.Status s) { this.resultStatus = s; }

    public synchronized boolean isProcessed() { return processed; }
    public synchronized void setProcessed(boolean p) { this.processed = p; }
}
