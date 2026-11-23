package com.payroll.model;

/**
 * User - Model representing system user and employee credentials.
 */
public class User {
    private String employeeId;
    private String name;
    private String username;
    private String password;
    private String role; // "Admin", "Supervisor", "Staff"
    private double hourlyRate;

    public User(String employeeId, String name, String username, String password, String role, double hourlyRate) {
        this.employeeId = employeeId != null ? employeeId.trim() : "";
        this.name = name != null ? name.trim() : "";
        this.username = username != null ? username.trim() : "";
        this.password = password != null ? password : "";
        this.role = role != null ? role.trim() : "Staff";
        this.hourlyRate = hourlyRate;
    }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(double hourlyRate) { this.hourlyRate = hourlyRate; }

    public boolean isAdmin() {
        return "Admin".equalsIgnoreCase(role);
    }

    public boolean isSupervisor() {
        return "Supervisor".equalsIgnoreCase(role);
    }

    public boolean hasSupervisorAccess() {
        return isAdmin() || isSupervisor();
    }

    public String toCsv() {
        return String.format("%s,%s,%s,%s,%s,%.2f",
                employeeId, name, username, password, role, hourlyRate);
    }

    public static User fromCsv(String line) {
        if (line == null || line.trim().isEmpty() || line.startsWith("#")) return null;
        String[] parts = line.split(",");
        if (parts.length >= 6) {
            try {
                return new User(parts[0].trim(), parts[1].trim(), parts[2].trim(),
                        parts[3], parts[4].trim(), Double.parseDouble(parts[5].trim()));
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("%s - %s (%s)", employeeId, name, role);
    }
}
