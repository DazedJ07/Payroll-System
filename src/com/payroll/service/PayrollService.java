package com.payroll.service;

import com.payroll.dao.FileDao;
import com.payroll.model.TimeLog;
import com.payroll.model.User;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * PayrollService
 */
public class PayrollService {
    public static final double STANDARD_HOURS = 8.0;
    public static final double OT_MULTIPLIER = 1.5;
    public static final double TAX_RATE = 0.10; // 10% Withholding Tax
    public static final double BENEFIT_RATE = 0.04; // 4% Social Security / Health / Pag-IBIG
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    // MATHEMATICAL CALCULATIONS
    public static double calcTotalHours(String clockInStr, String clockOutStr) {
        if (clockInStr == null || clockOutStr == null || "PENDING".equalsIgnoreCase(clockOutStr.trim())) {
            return 0.0;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);
            Date in = sdf.parse(clockInStr.trim());
            Date out = sdf.parse(clockOutStr.trim());
            long diff = out.getTime() - in.getTime();
            return diff > 0 ? (double) diff / (1000.0 * 60.0 * 60.0) : 0.0;
        } catch (ParseException e) {
            return 0.0;
        }
    }

    public static double calcRegularHours(double totalHours) {
        return totalHours <= 0 ? 0.0 : Math.min(STANDARD_HOURS, totalHours);
    }

    public static double calcOvertimeHours(double totalHours) {
        return totalHours <= STANDARD_HOURS ? 0.0 : totalHours - STANDARD_HOURS;
    }

    public static double calcGrossPay(double regHours, double otHours, double hourlyRate) {
        return (regHours * hourlyRate) + (otHours * (hourlyRate * OT_MULTIPLIER));
    }

    public static double calcTax(double grossPay) {
        return grossPay * TAX_RATE;
    }

    public static double calcBenefits(double grossPay) {
        return grossPay * BENEFIT_RATE;
    }

    public static double calcTotalDeductions(double grossPay) {
        return calcTax(grossPay) + calcBenefits(grossPay);
    }

    public static double calcNetPay(double grossPay) {
        return grossPay - calcTotalDeductions(grossPay);
    }

    // AUTHENTICATION & USER MANAGEMENT
    public static User authenticate(String username, String password) {
        if (username == null || password == null) return null;
        for (User u : FileDao.loadUsers()) {
            if (u.getUsername().equalsIgnoreCase(username.trim()) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    public static boolean addUser(User newUser) {
        List<User> users = FileDao.loadUsers();
        for (User u : users) {
            if (u.getEmployeeId().equalsIgnoreCase(newUser.getEmployeeId())
                    || u.getUsername().equalsIgnoreCase(newUser.getUsername())) {
                return false;
            }
        }
        for (User u : FileDao.loadArchivedUsers()) {
            if (u.getEmployeeId().equalsIgnoreCase(newUser.getEmployeeId())
                    || u.getUsername().equalsIgnoreCase(newUser.getUsername())) {
                return false;
            }
        }
        return FileDao.appendUser(newUser);
    }

    public static boolean updateUser(User updated) {
        List<User> users = FileDao.loadUsers();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getEmployeeId().equalsIgnoreCase(updated.getEmployeeId())) {
                users.set(i, updated);
                return FileDao.saveUsers(users);
            }
        }
        return false;
    }

    public static User findUserById(String empId) {
        for (User u : FileDao.loadUsers()) {
            if (u.getEmployeeId().equalsIgnoreCase(empId)) return u;
        }
        return null;
    }

    // TIMEKEEPING ACTIONS
    public static String clockIn(User user) {
        List<TimeLog> logs = FileDao.loadLogs();
        for (TimeLog l : logs) {
            if (l.getEmployeeId().equalsIgnoreCase(user.getEmployeeId()) && l.isPending()) {
                return "ALREADY_CLOCKED_IN:" + l.getClockInTime();
            }
        }
        Date now = new Date();
        String dateStr = new SimpleDateFormat(DATE_FORMAT).format(now);
        String timeStr = new SimpleDateFormat(DATE_TIME_FORMAT).format(now);
        TimeLog newLog = new TimeLog(user.getEmployeeId(), user.getName(), dateStr, timeStr, "PENDING", 0, 0, 0);
        FileDao.appendLog(newLog);
        return timeStr;
    }

    public static TimeLog clockOut(User user) {
        List<TimeLog> logs = FileDao.loadLogs();
        TimeLog activeLog = null;
        for (TimeLog l : logs) {
            if (l.getEmployeeId().equalsIgnoreCase(user.getEmployeeId()) && l.isPending()) {
                activeLog = l;
                break;
            }
        }
        if (activeLog == null) return null;

        String outTime = new SimpleDateFormat(DATE_TIME_FORMAT).format(new Date());
        activeLog.setClockOutTime(outTime);
        double totalH = calcTotalHours(activeLog.getClockInTime(), outTime);
        double regH = calcRegularHours(totalH);
        double otH = calcOvertimeHours(totalH);
        double pay = calcGrossPay(regH, otH, user.getHourlyRate());

        activeLog.setRegularHours(regH);
        activeLog.setOvertimeHours(otH);
        activeLog.setTotalPay(pay);

        FileDao.saveLogs(logs);
        return activeLog;
    }

    public static boolean resolvePendingShift(TimeLog log, String outTime, double hourlyRate) {
        double totalH = calcTotalHours(log.getClockInTime(), outTime);
        if (totalH <= 0) return false;
        double regH = calcRegularHours(totalH);
        double otH = calcOvertimeHours(totalH);
        double pay = calcGrossPay(regH, otH, hourlyRate);

        log.setClockOutTime(outTime);
        log.setRegularHours(regH);
        log.setOvertimeHours(otH);
        log.setTotalPay(pay);

        List<TimeLog> logs = FileDao.loadLogs();
        for (int i = 0; i < logs.size(); i++) {
            TimeLog l = logs.get(i);
            if (l.getEmployeeId().equalsIgnoreCase(log.getEmployeeId())
                    && l.getClockInTime().equalsIgnoreCase(log.getClockInTime())) {
                logs.set(i, log);
                return FileDao.saveLogs(logs);
            }
        }
        return false;
    }

    public static boolean addManualShift(User user, String inTime, String outTime) {
        double totalH = calcTotalHours(inTime, outTime);
        if (totalH <= 0) return false;
        double regH = calcRegularHours(totalH);
        double otH = calcOvertimeHours(totalH);
        double pay = calcGrossPay(regH, otH, user.getHourlyRate());
        String shiftDate = inTime.length() >= 10 ? inTime.substring(0, 10) : "";

        TimeLog log = new TimeLog(user.getEmployeeId(), user.getName(), shiftDate, inTime, outTime, regH, otH, pay);
        return FileDao.appendLog(log);
    }

    // PAYSLIP GENERATION
    public static String[] generatePayStub(User emp) {
        List<TimeLog> completedLogs = new ArrayList<TimeLog>();
        double totalReg = 0, totalOt = 0, grossPay = 0;

        for (TimeLog log : FileDao.loadLogs()) {
            if (log.getEmployeeId().equalsIgnoreCase(emp.getEmployeeId()) && !log.isPending()) {
                completedLogs.add(log);
                totalReg += log.getRegularHours();
                totalOt += log.getOvertimeHours();
                grossPay += log.getTotalPay();
            }
        }
        if (completedLogs.isEmpty()) return null;

        double otRate = emp.getHourlyRate() * OT_MULTIPLIER;
        double tax = calcTax(grossPay);
        double benefits = calcBenefits(grossPay);
        double totalDeduct = tax + benefits;
        double netPay = grossPay - totalDeduct;

        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================================\n");
        sb.append("                       STAFF TIMEKEEPING & PAYROLL SYSTEM\n");
        sb.append("                               OFFICIAL PAYSLIP\n");
        sb.append("========================================================================================\n");
        sb.append(String.format("Generated On:       %s\n", new SimpleDateFormat(DATE_TIME_FORMAT).format(new Date())));
        sb.append(String.format("Employee ID:        %s\n", emp.getEmployeeId()));
        sb.append(String.format("Employee Name:      %s\n", emp.getName()));
        sb.append(String.format("Role / Position:    %s\n", emp.getRole()));
        sb.append(String.format("Base Hourly Rate:   $%.2f / hr\n", emp.getHourlyRate()));
        sb.append(String.format("Overtime Rate (1.5x): $%.2f / hr\n", otRate));
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format("%-12s %-20s %-20s %-10s %-10s %-12s\n", "Date", "Clock-In", "Clock-Out", "Reg Hrs", "OT Hrs", "Shift Total"));
        sb.append("----------------------------------------------------------------------------------------\n");

        for (TimeLog l : completedLogs) {
            sb.append(String.format("%-12s %-20s %-20s %-10.2f %-10.2f $%-11.2f\n",
                    l.getDate(), l.getClockInTime(), l.getClockOutTime(),
                    l.getRegularHours(), l.getOvertimeHours(), l.getTotalPay()));
        }

        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append("EARNINGS SUMMARY:\n");
        sb.append(String.format("  Total Completed Shifts:   %d\n", completedLogs.size()));
        sb.append(String.format("  Regular Earnings:         %-8.2f hrs  @ $%.2f/hr   =  $%.2f\n",
                totalReg, emp.getHourlyRate(), totalReg * emp.getHourlyRate()));
        sb.append(String.format("  Overtime Earnings:        %-8.2f hrs  @ $%.2f/hr   =  $%.2f\n",
                totalOt, otRate, totalOt * otRate));
        sb.append(String.format("  GROSS PAY:                                                          $%.2f\n", grossPay));
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append("DEDUCTIONS BREAKDOWN:\n");
        sb.append(String.format("  Withholding Tax (10.0%%):                                            -$%.2f\n", tax));
        sb.append(String.format("  Social Security & Benefits (4.0%%):                                 -$%.2f\n", benefits));
        sb.append(String.format("  TOTAL DEDUCTIONS:                                                  -$%.2f\n", totalDeduct));
        sb.append("========================================================================================\n");
        sb.append(String.format("  NET TAKE-HOME PAY:                                                  $%.2f\n", netPay));
        sb.append("========================================================================================\n");
        sb.append("                      Thank you for your dedicated service!\n");
        sb.append("========================================================================================\n");

        String text = sb.toString();
        String path = FileDao.savePayStubFile(emp.getEmployeeId(), text);
        return new String[]{text, path};
    }
}
