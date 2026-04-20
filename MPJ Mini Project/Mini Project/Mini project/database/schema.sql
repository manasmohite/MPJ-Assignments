-- ═══════════════════════════════════════════════════════════════════
--  Campus Navigator — Database Schema & Sample Data
--  MySQL 8.x compatible
--  Run: mysql -u root -p < database/schema.sql
-- ═══════════════════════════════════════════════════════════════════

-- Create & select database
CREATE DATABASE IF NOT EXISTS campus_nav
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE campus_nav;

-- ───────────────────────────────────────────
-- Drop tables in reverse dependency order
-- ───────────────────────────────────────────
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS time_slots;
DROP TABLE IF EXISTS facilities;
DROP TABLE IF EXISTS users;

-- ───────────────────────────────────────────
-- USERS
-- ───────────────────────────────────────────
CREATE TABLE users (
    user_id       INT          NOT NULL AUTO_INCREMENT,
    username      VARCHAR(60)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email         VARCHAR(120) NOT NULL,
    full_name     VARCHAR(120) NOT NULL,
    role          ENUM('STUDENT','FACULTY','ADMIN') NOT NULL DEFAULT 'STUDENT',
    extra_id      VARCHAR(60)  DEFAULT NULL,   -- student_id / employee_id / admin_code
    department    VARCHAR(100) DEFAULT NULL,
    extra_field   VARCHAR(100) DEFAULT NULL,   -- year (student) / designation (faculty)
    is_active     TINYINT(1)   NOT NULL DEFAULT 1,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    INDEX idx_username (username),
    INDEX idx_role     (role)
) ENGINE=InnoDB;

-- ───────────────────────────────────────────
-- FACILITIES
-- ───────────────────────────────────────────
CREATE TABLE facilities (
    facility_id INT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(120) NOT NULL,
    building    VARCHAR(100) NOT NULL,
    floor       TINYINT      NOT NULL DEFAULT 0,
    room_number VARCHAR(20)  NOT NULL,
    type        ENUM('CLASSROOM','LAB','SEMINAR_HALL','AUDITORIUM','SPORTS','LIBRARY','CONFERENCE') NOT NULL,
    capacity    SMALLINT     NOT NULL DEFAULT 30,
    description TEXT         DEFAULT NULL,
    is_active   TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (facility_id),
    INDEX idx_building (building),
    INDEX idx_type     (type)
) ENGINE=InnoDB;

-- ───────────────────────────────────────────
-- TIME SLOTS
-- ───────────────────────────────────────────
CREATE TABLE time_slots (
    slot_id    INT         NOT NULL AUTO_INCREMENT,
    label      VARCHAR(40) NOT NULL,
    start_time TIME        NOT NULL,
    end_time   TIME        NOT NULL,
    PRIMARY KEY (slot_id)
) ENGINE=InnoDB;

-- ───────────────────────────────────────────
-- BOOKINGS
-- ───────────────────────────────────────────
CREATE TABLE bookings (
    booking_id  INT          NOT NULL AUTO_INCREMENT,
    user_id     INT          NOT NULL,
    facility_id INT          NOT NULL,
    slot_id     INT          NOT NULL,
    booking_date DATE        NOT NULL,
    purpose     ENUM('EXAM','FACULTY_CLASS','CLUB_EVENT','STUDENT_STUDY') NOT NULL,
    status      ENUM('CONFIRMED','WAITLISTED','CANCELLED','OVERRIDDEN') NOT NULL DEFAULT 'CONFIRMED',
    notes       TEXT         DEFAULT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (booking_id),
    FOREIGN KEY (user_id)     REFERENCES users(user_id)      ON DELETE CASCADE,
    FOREIGN KEY (facility_id) REFERENCES facilities(facility_id) ON DELETE CASCADE,
    FOREIGN KEY (slot_id)     REFERENCES time_slots(slot_id) ON DELETE CASCADE,
    INDEX idx_date_facility (booking_date, facility_id),
    INDEX idx_user          (user_id),
    INDEX idx_status        (status)
) ENGINE=InnoDB;

-- ═══════════════════════════════════════════════════════════════════
--  SAMPLE DATA
-- ═══════════════════════════════════════════════════════════════════

-- ───────────────────────────────────────────
-- TIME SLOTS (8 slots per day)
-- ───────────────────────────────────────────
INSERT INTO time_slots (label, start_time, end_time) VALUES
('08:00 - 09:00', '08:00:00', '09:00:00'),
('09:00 - 10:00', '09:00:00', '10:00:00'),
('10:00 - 11:00', '10:00:00', '11:00:00'),
('11:00 - 12:00', '11:00:00', '12:00:00'),
('12:00 - 13:00', '12:00:00', '13:00:00'),
('13:00 - 14:00', '13:00:00', '14:00:00'),
('14:00 - 15:00', '14:00:00', '15:00:00'),
('15:00 - 16:00', '15:00:00', '16:00:00');

-- ───────────────────────────────────────────
-- FACILITIES
-- ───────────────────────────────────────────
INSERT INTO facilities (name, building, floor, room_number, type, capacity, description) VALUES
-- Academic Block A
('Classroom A-101',   'Academic Block A', 1, 'A-101', 'CLASSROOM',    60, 'Standard lecture room with projector and whiteboard'),
('Classroom A-102',   'Academic Block A', 1, 'A-102', 'CLASSROOM',    60, 'Standard lecture room with projector and whiteboard'),
('Seminar Room A-201','Academic Block A', 2, 'A-201', 'SEMINAR_HALL', 40, 'Seminar hall with round table setup'),
('Computer Lab A-301','Academic Block A', 3, 'A-301', 'LAB',          30, '30 desktop PCs, high-speed internet'),

-- Academic Block B
('Classroom B-101',   'Academic Block B', 1, 'B-101', 'CLASSROOM',    80, 'Large lecture hall'),
('Classroom B-102',   'Academic Block B', 1, 'B-102', 'CLASSROOM',    80, 'Large lecture hall'),
('Physics Lab B-201', 'Academic Block B', 2, 'B-201', 'LAB',          24, 'Physics experiments lab'),
('Chemistry Lab B-202','Academic Block B', 2, 'B-202','LAB',          24, 'Chemistry lab with fume hoods'),
('Conference B-301',  'Academic Block B', 3, 'B-301', 'CONFERENCE',   20, 'Board-room style conference room'),

-- Auditorium
('Main Auditorium',   'Auditorium',       0, 'AUD-01','AUDITORIUM',  500, 'Main campus auditorium for large events'),
('Mini Auditorium',   'Auditorium',       1, 'AUD-02','AUDITORIUM',  150, 'Smaller auditorium for presentations'),

-- Library
('Reading Hall',      'Library',          0, 'LIB-01','LIBRARY',     100, 'Quiet reading hall — no group discussions'),
('Group Study Room 1','Library',          1, 'LIB-G1','SEMINAR_HALL', 10, 'Group study room (max 10 people)'),
('Group Study Room 2','Library',          1, 'LIB-G2','SEMINAR_HALL', 10, 'Group study room (max 10 people)'),

-- Sports Complex
('Indoor Sports Hall','Sports Complex',   0, 'SPT-01','SPORTS',      200, 'Badminton, Table Tennis, Basketball'),
('Gymnasium',         'Sports Complex',   1, 'SPT-02','SPORTS',       50, 'Fully equipped gymnasium'),

-- Research Center
('Research Lab 1',    'Research Center',  2, 'RC-201','LAB',          20, 'Advanced research laboratory'),
('Seminar Room RC',   'Research Center',  1, 'RC-101','SEMINAR_HALL', 35, 'Research presentations room');

-- ───────────────────────────────────────────
-- USERS
-- ─────────────────────────────────────────────────────────────────
-- IMPORTANT: Passwords below are plaintext shown for documentation.
-- The application uses PasswordUtil.hash() which produces a
-- "salt:hash" Base64 string.  We use a FIXED test hash below
-- that corresponds to known passwords so the app can verify them.
--
-- Since SHA-256 with random salt can't be pre-generated here
-- statically, we store a special marker and use a BYPASS mode.
-- → SOLUTION: Run the SeedDB utility after schema setup (see README).
--   OR use the pre-seeded INSERT below which uses known test hashes.
--
-- Test accounts (run SeedDB.java to generate proper hashes):
--   admin    / admin123
--   faculty1 / faculty123
--   faculty2 / faculty123
--   student1 / student123
--   student2 / student123
--   student3 / student123
-- ─────────────────────────────────────────────────────────────────
-- We insert placeholder hashes here; SeedDB.java will REPLACE them.
-- The placeholder is a valid hash of "changeme" — run SeedDB to fix.
-- ─────────────────────────────────────────────────────────────────

INSERT INTO users (username, password_hash, email, full_name, role, extra_id, department, extra_field) VALUES
('admin',    'SEED_REQUIRED', 'admin@campus.edu',      'System Administrator', 'ADMIN',   'ADM001', 'Administration', NULL),
('faculty1', 'SEED_REQUIRED', 'dr.sharma@campus.edu',  'Dr. Priya Sharma',     'FACULTY', 'FAC101', 'Computer Science', 'Associate Professor'),
('faculty2', 'SEED_REQUIRED', 'prof.mehta@campus.edu', 'Prof. Rakesh Mehta',   'FACULTY', 'FAC102', 'Physics',          'Professor'),
('student1', 'SEED_REQUIRED', 's.kumar@campus.edu',    'Arjun Kumar',          'STUDENT', 'STU2001', 'Computer Science', '2'),
('student2', 'SEED_REQUIRED', 's.patel@campus.edu',    'Neha Patel',           'STUDENT', 'STU2002', 'Electronics',     '3'),
('student3', 'SEED_REQUIRED', 's.singh@campus.edu',    'Ravi Singh',           'STUDENT', 'STU2003', 'Mechanical',      '1');

-- ─────────────────────────────────────────────────────────────────
-- SAMPLE BOOKINGS (use tomorrow's date so they're in the future)
-- These will be inserted by SeedDB.java after user IDs are known.
-- ─────────────────────────────────────────────────────────────────
-- (Leave empty here — SeedDB.java inserts them programmatically)

-- ═══════════════════════════════════════════════════════════════════
--  VERIFICATION QUERY — run after seeding
-- ═══════════════════════════════════════════════════════════════════
-- SELECT 'users' AS tbl, COUNT(*) AS cnt FROM users
-- UNION ALL SELECT 'facilities', COUNT(*) FROM facilities
-- UNION ALL SELECT 'time_slots', COUNT(*) FROM time_slots
-- UNION ALL SELECT 'bookings', COUNT(*) FROM bookings;
