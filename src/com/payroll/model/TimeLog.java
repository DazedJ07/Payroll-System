package com.payroll.model;

/**
 * TimeLog - Model representing an employee shift time punch and pay record.
 */
public class TimeLog {
    private String employeeId;
    private String name;
    private String date; // yyyy-MM-dd
    private String clockInTime;  // yyyy-MM-dd HH:mm:ss
    private String clockOutTime; // yyyy-MM-dd HH:mm:ss or "PENDING"
    private double regularHours;
    private double overtimeHours;
    private double totalPay;

    public TimeLog(String employeeId, String name, String date, String clockInTime,
                   String clockOutTime, double regularHours, double overtimeHours, double totalPay) {
        this.employeeId = employeeId != null ? employeeId.trim() : "";
        this.name = name != null ? name.trim() : "";
        this.date = date != null ? date.trim() : "";
        this.clockInTime = clockInTime != null ? clockInTime.trim() : "";
        this.clockOutTime = clockOutTime != null ? clockOutTime.trim() : "PENDING";
        this.regularHours = regularHours;
        this.overtimeHours = overtimeHours;
        this.totalPay = totalPay;
    }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getClockInTime() { return clockInTime; }
    public void setClockInTime(String clockInTime) { this.clockInTime = clockInTime; }

    public String getClockOutTime() { return clockOutTime; }
    public void setClockOutTime(String clockOutTime) { this.clockOutTime = clockOutTime; }

    public double getRegularHours() { return regularHours; }
    public void setRegularHours(double regularHours) { this.regularHours = regularHours; }

    public double getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(double overtimeHours) { this.overtimeHours = overtimeHours; }

    public double getTotalPay() { return totalPay; }
    public void setTotalPay(double totalPay) { this.totalPay = totalPay; }

    public boolean isPending() {
        return clockOutTime == null || clockOutTime.trim().isEmpty() || "PENDING".equalsIgnoreCase(clockOutTime.trim());
    }

    public String toCsv() {
        return String.format("%s,%s,%s,%s,%s,%.2f,%.2f,%.2f",
                employeeId, name, date, clockInTime, clockOutTime,
                regularHours, overtimeHours, totalPay);
    }

    public static TimeLog fromCsv(String line) {
        if (line == null || line.trim().isEmpty() || line.startsWith("#")) return null;
        String[] parts = line.split(",");
        if (parts.length >= 8) {
            try {
                return new TimeLog(parts[0].trim(), parts[1].trim(), parts[2].trim(),
                        parts[3].trim(), parts[4].trim(),
                        Double.parseDouble(parts[5].trim()),
                        Double.parseDouble(parts[6].trim()),
                        Double.parseDouble(parts[7].trim()));
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
