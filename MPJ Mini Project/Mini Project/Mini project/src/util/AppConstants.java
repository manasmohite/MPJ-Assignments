package util;

/**
 * Application-wide constants.
 */
public final class AppConstants {

    private AppConstants() {} // Prevent instantiation

    // Application info
    public static final String APP_NAME    = "Campus Navigator";
    public static final String APP_VERSION = "1.0.0";

    // Roles
    public static final String ROLE_ADMIN   = "ADMIN";
    public static final String ROLE_FACULTY = "FACULTY";
    public static final String ROLE_STUDENT = "STUDENT";

    // Booking status labels
    public static final String STATUS_CONFIRMED  = "CONFIRMED";
    public static final String STATUS_WAITLISTED = "WAITLISTED";
    public static final String STATUS_CANCELLED  = "CANCELLED";
    public static final String STATUS_OVERRIDDEN = "OVERRIDDEN";

    // Priority labels
    public static final String PURPOSE_EXAM         = "EXAM";
    public static final String PURPOSE_FACULTY_CLASS = "FACULTY_CLASS";
    public static final String PURPOSE_CLUB_EVENT   = "CLUB_EVENT";
    public static final String PURPOSE_STUDENT_STUDY = "STUDENT_STUDY";

    // Thread settings
    public static final int BOOKING_THREAD_TIMEOUT_MS = 5000;

    // UI dimensions
    public static final int WINDOW_WIDTH  = 1100;
    public static final int WINDOW_HEIGHT = 720;
}
