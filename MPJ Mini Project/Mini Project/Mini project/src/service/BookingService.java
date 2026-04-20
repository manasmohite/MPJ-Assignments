package service;

import dao.BookingDAO;
import dao.FacilityDAO;
import dao.TimeSlotDAO;
import model.*;
import util.AppConstants;

import java.time.LocalDate;
import java.util.List;

/**
 * Service layer for booking operations.
 * Spawns a BookingThread per request and waits for result.
 */
public class BookingService {

    private final BookingDAO bookingDAO;
    private final FacilityDAO facilityDAO;
    private final TimeSlotDAO timeSlotDAO;

    public BookingService() {
        bookingDAO  = new BookingDAO();
        facilityDAO = new FacilityDAO();
        timeSlotDAO = new TimeSlotDAO();
    }

    /**
     * Submit a booking request — processes in a new thread.
     * Blocks caller until thread completes (with timeout).
     *
     * @return BookingRequest with result populated
     */
    public BookingRequest submitBooking(User user, int facilityId, int slotId,
                                        LocalDate date, Booking.Purpose purpose, String notes) {

        BookingRequest req = new BookingRequest(user, facilityId, slotId, date, purpose, notes);
        BookingThread task = new BookingThread(req);
        Thread thread = new Thread(task, "BookingThread-" + user.getUsername() + "-" + System.currentTimeMillis());
        thread.start();

        // Wait for the thread to complete (with timeout)
        try {
            thread.join(AppConstants.BOOKING_THREAD_TIMEOUT_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            req.setResultMessage("❌ Booking interrupted. Please try again.");
            req.setResultStatus(Booking.Status.CANCELLED);
        }

        if (!req.isProcessed()) {
            req.setResultMessage("❌ Booking timed out. Please try again.");
            req.setResultStatus(Booking.Status.CANCELLED);
        }

        return req;
    }

    /**
     * Get all bookings for a user.
     */
    public List<Booking> getMyBookings(int userId) {
        return bookingDAO.findByUser(userId);
    }

    /**
     * Get all bookings (admin only).
     */
    public List<Booking> getAllBookings() {
        return bookingDAO.findAll();
    }

    /**
     * Cancel a booking (only owner or admin).
     */
    public boolean cancelBooking(int bookingId, User requestingUser) {
        List<Booking> all = bookingDAO.findAll();
        for (Booking b : all) {
            if (b.getBookingId() == bookingId) {
                if (b.getUserId() == requestingUser.getUserId()
                    || "ADMIN".equals(requestingUser.getRole())) {
                    return bookingDAO.cancel(bookingId);
                }
                return false; // Not authorized
            }
        }
        return false;
    }

    /**
     * Get taken slot IDs for a facility on a date (for UI display).
     */
    public List<Integer> getTakenSlots(int facilityId, LocalDate date) {
        return bookingDAO.getTakenSlotIds(facilityId, date);
    }

    /**
     * Check if a specific slot is available.
     */
    public boolean isAvailable(int facilityId, int slotId, LocalDate date) {
        return !bookingDAO.isSlotTaken(facilityId, slotId, date);
    }
}
