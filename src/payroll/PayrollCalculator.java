package payroll;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * PayrollCalculator - Dedicated mathematical calculation engine
 * Handles regular hours, overtime hours (1.5x multiplier), and gross pay.
 */
public class PayrollCalculator {

    public static final double STANDARD_WORK_HOURS = 8.0;
    public static final double OVERTIME_RATE_MULTIPLIER = 1.5;
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * Calculates the elapsed time in fractional hours between clock-in and clock-out.
     *
     * @param clockInStr  formatted string (yyyy-MM-dd HH:mm:ss)
     * @param clockOutStr formatted string (yyyy-MM-dd HH:mm:ss)
     * @return elapsed hours as double (e.g., 8.5)
     */
    public static double calculateTotalHours(String clockInStr, String clockOutStr) {
        if (clockInStr == null || clockOutStr == null || 
            clockInStr.trim().isEmpty() || clockOutStr.trim().isEmpty() ||
            "PENDING".equalsIgnoreCase(clockOutStr.trim())) {
            return 0.0;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);
        try {
            Date in = sdf.parse(clockInStr.trim());
            Date out = sdf.parse(clockOutStr.trim());
            long diffMillis = out.getTime() - in.getTime();
            if (diffMillis < 0) {
                return 0.0;
            }
            return (double) diffMillis / (1000.0 * 60.0 * 60.0);
        } catch (ParseException e) {
            System.err.println("Error parsing timestamps: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Regular hours: Up to 8.0 hours per shift.
     *
     * @param totalHours total elapsed shift hours
     * @return regular hours (0.0 to 8.0)
     */
    public static double calculateRegularHours(double totalHours) {
        if (totalHours <= 0) {
            return 0.0;
        }
        return Math.min(STANDARD_WORK_HOURS, totalHours);
    }

    /**
     * Overtime hours: Any hours worked beyond 8.0 hours in a single shift.
     *
     * @param totalHours total elapsed shift hours
     * @return overtime hours (totalHours - 8.0, or 0.0 if <= 8.0)
     */
    public static double calculateOvertimeHours(double totalHours) {
        if (totalHours <= STANDARD_WORK_HOURS) {
            return 0.0;
        }
        return totalHours - STANDARD_WORK_HOURS;
    }

    /**
     * Gross pay calculation: (regularHours * hourlyRate) + (overtimeHours * hourlyRate * 1.5).
     *
     * @param regularHours  regular hours worked (up to 8.0)
     * @param overtimeHours overtime hours worked (beyond 8.0)
     * @param hourlyRate    base employee hourly pay rate
     * @return total gross pay
     */
    public static double calculateGrossPay(double regularHours, double overtimeHours, double hourlyRate) {
        double regularPay = regularHours * hourlyRate;
        double overtimePay = overtimeHours * (hourlyRate * OVERTIME_RATE_MULTIPLIER);
        return regularPay + overtimePay;
    }

    /**
     * Formats currency cleanly with two decimal places.
     */
    public static String formatCurrency(double amount) {
        return String.format("$%.2f", amount);
    }

    /**
     * Formats hours cleanly with two decimal places.
     */
    public static String formatHours(double hours) {
        return String.format("%.2f", hours);
    }
}
