package model;

/**
 * Student user — lowest booking priority (4).
 */
public class Student extends User {
    private String studentId;
    private String department;
    private int year;

    public Student(int userId, String username, String passwordHash,
                   String email, String fullName,
                   String studentId, String department, int year) {
        super(userId, username, passwordHash, email, fullName, "STUDENT");
        this.studentId = studentId;
        this.department = department;
        this.year = year;
    }

    @Override
    public int getBookingPriority() {
        return 4; // Lowest priority
    }

    @Override
    public String getRoleLabel() {
        return "Student";
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
}
