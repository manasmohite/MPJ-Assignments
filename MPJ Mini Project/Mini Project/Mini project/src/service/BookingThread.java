package service;

import dao.BookingDAO;
import model.*;

import java.util.List;

/**
 * Each booking request is processed in its own thread.
 * Demonstrates: Runnable, Thread, synchronized, race condition prevention.
 *
 * PRIORITY ORDER (lower number = higher priority):
 *   1 = EXAM
 *   2 = FACULTY_CLASS
 *   3 = CLUB_EVENT
 *   4 = STUDENT_STUDY
 *
 * If a slot is taken by a LOWER priority booking, a HIGHER priority
 * request will OVERRIDE it (move lower to WAITLIST).
 */
public class BookingThread implements Runnable {

    private final BookingRequest request;
    private final BookingDAO bookingDAO;

    // Static lock object — shared across ALL BookingThread instances.
    // This ensures only one thread modifies booking state at a time.
    private static final Object BOOKING_LOCK = new Object();

    public BookingThread(BookingRequest request) {
        this.request = request;
        this.bookingDAO = new BookingDAO();
    }

    @Override
    public void run() {
        System.out.println("[BookingThread] Processing request from: "
            + request.getRequester().getUsername()
            + " | Thread: " + Thread.currentThread().getName());

        try {
            // Simulate slight processing delay (realistic async behavior)
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // === CRITICAL SECTION — synchronized on shared lock ===
        synchronized (BOOKING_LOCK) {
            processBooking();
        }

        request.setProcessed(true);
        System.out.println("[BookingThread] Done. Result: " + request.getResultMessage());
    }

    /**
     * Core booking logic — runs inside synchronized block.
     */
    private void processBooking() {
        int facilityId = request.getFacilityId();
        int slotId     = request.getSlotId();

        // Check if slot is currently taken
        boolean taken = bookingDAO.isSlotTaken(facilityId, slotId, request.getDate());

        if (!taken) {
            // === SLOT IS FREE — book it directly ===
            Booking newBooking = new Booking(
                0,
                request.getRequester().getUserId(),
                facilityId,
                slotId,
                request.getDate(),
                request.getPurpose(),
                Booking.Status.CONFIRMED,
                request.getNotes()
            );
            int id = bookingDAO.insert(newBooking);
            if (id > 0) {
                request.setResultStatus(Booking.Status.CONFIRMED);
                request.setResultMessage("✅ Booking CONFIRMED! Booking ID: " + id);
            } else {
                request.setResultStatus(Booking.Status.CANCELLED);
                request.setResultMessage("❌ Database error during booking. Please try again.");
            }

        } else {
            // === SLOT IS TAKEN — check priority ===
            Booking existing = bookingDAO.getConfirmedBooking(facilityId, slotId, request.getDate());

            if (existing != null && request.getPurpose().getPriority() < existing.getPriority()) {
                // New request has HIGHER priority — override existing booking
                System.out.println("[BookingThread] Priority override: "
                    + request.getPurpose() + "(" + request.getPurpose().getPriority() + ")"
                    + " > " + existing.getPurpose() + "(" + existing.getPriority() + ")");

                // Move existing to WAITLISTED
                bookingDAO.updateStatus(existing.getBookingId(), Booking.Status.WAITLISTED);

                // Insert new booking as CONFIRMED
                Booking override = new Booking(
                    0,
                    request.getRequester().getUserId(),
                    facilityId,
                    slotId,
                    request.getDate(),
                    request.getPurpose(),
                    Booking.Status.CONFIRMED,
                    request.getNotes()
                );
                int id = bookingDAO.insert(override);

                // Also mark original as OVERRIDDEN
                bookingDAO.updateStatus(existing.getBookingId(), Booking.Status.OVERRIDDEN);

                if (id > 0) {
                    request.setResultStatus(Booking.Status.CONFIRMED);
                    request.setResultMessage(
                        "✅ Booking CONFIRMED (Priority Override)! Booking ID: " + id + "\n"
                        + "⚠️ Previous booking #" + existing.getBookingId()
                        + " by " + existing.getUserName() + " was moved to OVERRIDDEN."
                    );
                } else {
                    request.setResultStatus(Booking.Status.CANCELLED);
                    request.setResultMessage("❌ Override failed. Please try again.");
                }

            } else {
                // New request has LOWER or EQUAL priority — add to WAITLIST
                Booking waitlisted = new Booking(
                    0,
                    request.getRequester().getUserId(),
                    facilityId,
                    slotId,
                    request.getDate(),
                    request.getPurpose(),
                    Booking.Status.WAITLISTED,
                    request.getNotes()
                );
                int id = bookingDAO.insert(waitlisted);

                // Suggest alternate slots
                List<Integer> taken2 = bookingDAO.getTakenSlotIds(facilityId, request.getDate());
                StringBuilder alts = new StringBuilder();
                for (int s = 1; s <= 8; s++) {
                    if (!taken2.contains(s) && s != slotId) {
                        alts.append("Slot #").append(s).append(", ");
                        if (alts.length() > 60) break;
                    }
                }
                String altMsg = alts.length() > 0
                    ? "\n💡 Alternate slots available: " + alts.toString().replaceAll(", $", "")
                    : "\n💡 No alternate slots available for this date.";

                request.setResultStatus(Booking.Status.WAITLISTED);
                request.setResultMessage(
                    "⏳ Slot unavailable. Added to WAITLIST (ID: " + id + ")." + altMsg
                );
            }
        }
    }
}
