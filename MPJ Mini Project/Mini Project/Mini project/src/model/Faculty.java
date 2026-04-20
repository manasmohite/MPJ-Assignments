package model;

/**
 * Faculty user — priority 2 (after Exam events).
 */
public class Faculty extends User {
    private String employeeId;
    private String department;
    private String designation;

    public Faculty(int userId, String username, String passwordHash,
                   String email, String fullName,
                   String employeeId, String department, String designation) {
        super(userId, username, passwordHash, email, fullName, "FACULTY");
        this.employeeId = employeeId;
        this.department = department;
        this.designation = designation;
    }

    @Override
    public int getBookingPriority() {
        return 2; // Second highest priority
    }

    @Override
    public String getRoleLabel() {
        return "Faculty";
    }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
}
