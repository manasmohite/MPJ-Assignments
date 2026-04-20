# 🏫 Intelligent Campus Navigation & Facility Booking System
### Built with Core Java · JDBC · Java Swing · MySQL · Multithreading

---

## 📋 Table of Contents
1. [Project Overview](#1-project-overview)
2. [Architecture](#2-architecture)
3. [Module Summary](#3-module-summary)
4. [Prerequisites](#4-prerequisites)
5. [Step-by-Step Setup (macOS)](#5-step-by-step-setup-macos)
6. [How to Run](#6-how-to-run)
7. [Test Accounts](#7-test-accounts)
8. [Project Structure](#8-project-structure)
9. [Key Design Decisions](#9-key-design-decisions)
10. [Troubleshooting](#10-troubleshooting)

---

## 1. Project Overview

A desktop application for managing campus facility bookings with:

- **Role-based access control** (Admin / Faculty / Student)
- **Real-time availability checking** per facility, date, and time slot
- **Priority-based booking resolution** with automatic override and waitlisting
- **Campus navigation** with BFS shortest-path algorithm between buildings
- **Multithreaded booking engine** — each request processed in its own thread with synchronization

---

## 2. Architecture

```
┌──────────────────────────────────────────────────────────┐
│                    Java Swing UI Layer                     │
│  LoginPanel │ FacilityListPanel │ BookingFormPanel         │
│  MyBookingsPanel │ AdminDashboardPanel │ MainFrame          │
└────────────────────────┬─────────────────────────────────┘
                         │ calls
┌────────────────────────▼─────────────────────────────────┐
│                   Service Layer                            │
│  AuthService │ BookingService │ NavigationService          │
│                                                            │
│  BookingThread (implements Runnable)                       │
│  ├── Each booking → new Thread                             │
│  ├── synchronized(BOOKING_LOCK) { ... }                    │
│  └── Priority resolution inside critical section           │
└────────────────────────┬─────────────────────────────────┘
                         │ calls
┌────────────────────────▼─────────────────────────────────┐
│                     DAO Layer                              │
│  UserDAO │ FacilityDAO │ BookingDAO │ TimeSlotDAO          │
│  (all use JDBC — no ORM)                                   │
└────────────────────────┬─────────────────────────────────┘
                         │
┌────────────────────────▼─────────────────────────────────┐
│              MySQL Database (campus_nav)                   │
│  users │ facilities │ bookings │ time_slots                │
└──────────────────────────────────────────────────────────┘
```

---

## 3. Module Summary

| Module | Classes | Description |
|--------|---------|-------------|
| **User Management** | `User`, `Student`, `Faculty`, `Admin`, `UserDAO`, `AuthService` | Inheritance hierarchy, role-based login |
| **Campus Navigation** | `NavigationService`, `FacilityDAO` | BFS path-finding, facility search |
| **Facility Booking** | `BookingService`, `BookingDAO`, `BookingFormPanel` | Date/slot/purpose booking with availability check |
| **Multithreading** | `BookingThread`, `BookingRequest` | `Runnable` + `synchronized` + `Thread.join()` |
| **Priority Resolution** | `BookingThread.processBooking()` | Enum-based priority, override logic, waitlist |
| **Database** | All DAO classes, `DBConnection` | JDBC with `PreparedStatement`, connection per call |
| **UI** | `MainFrame`, `*Panel` classes, `UITheme` | Swing with CardLayout, dark theme |

### Priority Order
```
1 (highest) = EXAM
2           = FACULTY_CLASS
3           = CLUB_EVENT
4 (lowest)  = STUDENT_STUDY
```
A higher-priority booking **overrides** a lower-priority one for the same slot, moving the lower to `OVERRIDDEN` status.

---

## 4. Prerequisites

### Required Software

| Software | Version | Install Command (macOS) |
|----------|---------|------------------------|
| Java JDK | 17 or higher | `brew install openjdk@17` |
| MySQL | 8.x | `brew install mysql` |
| MySQL JDBC Driver | 8.x | Download manually (see below) |

### Check existing installations
```bash
java -version        # Should show 17+
javac -version       # Should show 17+
mysql --version      # Should show 8.x
```

---

## 5. Step-by-Step Setup (macOS)

### Step 1 — Install Java (if not installed)
```bash
brew install openjdk@17

# Add to PATH (add to ~/.zshrc or ~/.bash_profile):
export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"
export JAVA_HOME="/opt/homebrew/opt/openjdk@17"

# Reload shell:
source ~/.zshrc
```

### Step 2 — Install & Start MySQL
```bash
brew install mysql
brew services start mysql

# Secure installation (set root password):
mysql_secure_installation
# → Follow prompts. Remember your root password!

# Test connection:
mysql -u root -p
```

### Step 3 — Download MySQL JDBC Connector

1. Visit: https://dev.mysql.com/downloads/connector/j/
2. Select **Platform Independent** → Download the `.zip`
3. Extract and find `mysql-connector-j-*.jar`
4. Place the `.jar` file into the project's `lib/` directory:
   ```
   seminar_project/
   └── lib/
       └── mysql-connector-j-8.x.x.jar    ← place here
   ```

### Step 4 — Configure Database Credentials
Edit `src/db.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/campus_nav?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.username=root
db.password=YOUR_MYSQL_ROOT_PASSWORD
```
> If your MySQL root has no password (fresh install), leave `db.password=` blank.

### Step 5 — Create Database Schema
```bash
# From inside the seminar_project/ directory:
mysql -u root -p < database/schema.sql

# Verify tables were created:
mysql -u root -p -e "USE campus_nav; SHOW TABLES;"
```
Expected output:
```
+----------------------+
| Tables_in_campus_nav |
+----------------------+
| bookings             |
| facilities           |
| time_slots           |
| users                |
+----------------------+
```

### Step 6 — Make run script executable
```bash
chmod +x run.sh
```

---

## 6. How to Run

### Option A — One Command (Recommended for first run)
```bash
# From the seminar_project/ directory:
./run.sh all
```
This will:
1. Compile all Java files
2. Run SeedDB (sets hashed passwords + inserts sample bookings)
3. Launch the application

### Option B — Step by Step
```bash
# Step 1: Compile
./run.sh compile

# Step 2: Seed database (REQUIRED before first login)
./run.sh seed

# Step 3: Run the app
./run.sh run
```

### Option C — Manual (if run.sh doesn't work)
```bash
cd seminar_project

# Create output directory
mkdir -p out

# Compile (adjust jar name to your version)
javac -cp .:lib/mysql-connector-j-8.4.0.jar \
      -sourcepath src \
      -d out \
      $(find src -name "*.java")

# Copy config to classpath
cp src/db.properties out/

# Seed the database
java -cp out:lib/mysql-connector-j-8.4.0.jar SeedDB

# Run the application
java -cp out:lib/mysql-connector-j-8.4.0.jar Main
```

### Running with IntelliJ IDEA
1. Open the `seminar_project/` folder as a project
2. Go to **File → Project Structure → Libraries** → Add the MySQL JDBC `.jar`
3. Mark `src/` as **Sources Root** (right-click → Mark Directory as → Sources Root)
4. Run `SeedDB.main()` once to seed the database
5. Run `Main.main()` to launch the app

### Running with VS Code
1. Install the **Extension Pack for Java** extension
2. Open `seminar_project/` folder
3. Add the MySQL JDBC `.jar` to `.vscode/settings.json`:
   ```json
   {
     "java.project.referencedLibraries": ["lib/*.jar"]
   }
   ```
4. Run `SeedDB.java` first, then `Main.java`

---

## 7. Test Accounts

| Username | Password | Role | Priority |
|----------|----------|------|----------|
| `admin` | `admin123` | Admin | 1 (highest) |
| `faculty1` | `faculty123` | Faculty | 2 |
| `faculty2` | `faculty123` | Faculty | 2 |
| `student1` | `student123` | Student | 4 (lowest) |
| `student2` | `student123` | Student | 4 |
| `student3` | `student123` | Student | 4 |

---

## 8. Project Structure

```
seminar_project/
├── src/
│   ├── Main.java                    ← Application entry point
│   ├── SeedDB.java                  ← One-time DB seeder
│   ├── db.properties                ← Database configuration
│   │
│   ├── model/                       ← Data model (OOP hierarchy)
│   │   ├── User.java                ← Abstract base class
│   │   ├── Student.java             ← Extends User (priority 4)
│   │   ├── Faculty.java             ← Extends User (priority 2)
│   │   ├── Admin.java               ← Extends User (priority 1)
│   │   ├── Facility.java            ← Campus facility
│   │   ├── TimeSlot.java            ← Predefined time slots
│   │   ├── Booking.java             ← Booking record + enums
│   │   └── BookingRequest.java      ← Thread-safe request wrapper
│   │
│   ├── dao/                         ← JDBC data access
│   │   ├── UserDAO.java
│   │   ├── FacilityDAO.java
│   │   ├── BookingDAO.java
│   │   └── TimeSlotDAO.java
│   │
│   ├── service/                     ← Business logic + threading
│   │   ├── AuthService.java         ← Login/logout singleton
│   │   ├── BookingService.java      ← Booking orchestration
│   │   ├── BookingThread.java       ← Runnable + synchronized
│   │   └── NavigationService.java   ← BFS path-finding
│   │
│   ├── ui/                          ← Java Swing UI
│   │   ├── UITheme.java             ← Colors, fonts, component factories
│   │   ├── MainFrame.java           ← Application window + CardLayout
│   │   ├── LoginPanel.java          ← Login screen
│   │   ├── FacilityListPanel.java   ← Browse + navigation
│   │   ├── BookingFormPanel.java    ← Booking form
│   │   ├── MyBookingsPanel.java     ← User's booking history
│   │   └── AdminDashboardPanel.java ← Admin view
│   │
│   └── util/                        ← Utilities
│       ├── DBConnection.java        ← JDBC connection manager
│       ├── PasswordUtil.java        ← SHA-256 + salt hashing
│       └── AppConstants.java        ← App-wide constants
│
├── database/
│   └── schema.sql                   ← MySQL schema + sample data DDL
│
├── lib/                             ← Place MySQL JDBC .jar here
│   └── (mysql-connector-j-*.jar)
│
├── out/                             ← Compiled .class files (auto-created)
│
├── run.sh                           ← macOS build & run script
└── README.md                        ← This file
```

---

## 9. Key Design Decisions

### OOP Principles Used
- **Inheritance**: `User → Student / Faculty / Admin` — each overrides `getBookingPriority()` and `getRoleLabel()`
- **Encapsulation**: All model fields are private with getters/setters
- **Polymorphism**: `user.getBookingPriority()` works for any user type without casting
- **Abstraction**: `User` is abstract — cannot be instantiated directly

### Multithreading Design
```
UI Thread (EDT)                    BookingThread
    │                                   │
    ├─ SwingWorker.doInBackground() ──► BookingThread.run()
    │                                   │
    │                              synchronized(BOOKING_LOCK) {
    │                                   ├─ Check slot availability
    │                                   ├─ Priority comparison
    │                                   ├─ Override / Waitlist / Confirm
    │                                   └─ DB write
    │                              }
    │                                   │
    ├─ SwingWorker.done() ◄─────────────┤
    └─ Update UI                        │
```

- Each booking = **1 new Thread** (`BookingThread implements Runnable`)
- A **static lock object** (`BOOKING_LOCK`) prevents race conditions across concurrent requests
- `Thread.join(timeout)` in `BookingService` waits for result before returning to UI
- `volatile` fields in `BookingRequest` ensure visibility across threads
- UI never blocks — `SwingWorker` keeps booking off the Event Dispatch Thread

### Priority Resolution Algorithm
```
if slot is FREE:
    → CONFIRM booking

else if slot is TAKEN:
    if new.priority < existing.priority:    (lower number = higher priority)
        → OVERRIDE: move existing to OVERRIDDEN
        → CONFIRM new booking
    else:
        → WAITLIST new booking
        → Suggest alternate free slots
```

### JDBC Pattern
- **Connection-per-call**: Each DAO method opens and closes its own connection using try-with-resources
- **PreparedStatement**: Used for all parameterized queries (prevents SQL injection)
- **No connection pooling**: Kept simple for educational purposes (HikariCP would be next step)

---

## 10. Troubleshooting

| Problem | Solution |
|---------|----------|
| `Communications link failure` | MySQL not running. Run: `brew services start mysql` |
| `Access denied for user 'root'` | Wrong password in `db.properties`. Verify with `mysql -u root -p` |
| `Table 'campus_nav.users' doesn't exist` | Run schema: `mysql -u root -p < database/schema.sql` |
| `SEED_REQUIRED` in password field | Run `./run.sh seed` to set proper hashed passwords |
| `ClassNotFoundException: com.mysql.cj.jdbc.Driver` | JDBC `.jar` not in `lib/` or not on classpath |
| `cannot find symbol: Main` | Compile first: `./run.sh compile` |
| Login fails for all accounts | SeedDB not run yet. Run `./run.sh seed` |
| UI looks wrong on macOS Retina | Add JVM flag: `java -Dsun.java2d.uiScale=1 ...` |
| `Error: Could not find or load main class` | Ensure you're running from `seminar_project/` directory |

### Resetting Everything
```bash
# Drop and recreate DB:
mysql -u root -p -e "DROP DATABASE IF EXISTS campus_nav;"
mysql -u root -p < database/schema.sql

# Recompile and reseed:
./run.sh all
```

---

## Academic Notes

This project demonstrates the following Java concepts for academic evaluation:

| Concept | Where Implemented |
|---------|-------------------|
| Abstract Classes | `User.java` |
| Inheritance | `Student`, `Faculty`, `Admin` extend `User` |
| Polymorphism | `getBookingPriority()`, `getRoleLabel()` overridden in each subclass |
| Encapsulation | All model fields are `private` |
| Enums | `Booking.Purpose`, `Booking.Status` |
| `implements Runnable` | `BookingThread.java` |
| `synchronized` keyword | `BookingThread.processBooking()` — static lock |
| `volatile` | `BookingRequest` result fields |
| `Thread.join()` | `BookingService.submitBooking()` |
| `SwingWorker` | Background task execution in `LoginPanel`, `BookingFormPanel` |
| JDBC `Connection` | `DBConnection.java` + all DAO classes |
| `PreparedStatement` | All parameterized SQL queries |
| Design Patterns | Singleton (`AuthService`), DAO pattern, CardLayout navigation |

---

*Campus Navigator — Java Seminar Project · Core Java · No frameworks used*
