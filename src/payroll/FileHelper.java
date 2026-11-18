package payroll;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * FileHelper - Handles file persistence for users, time logs, and pay stubs.
 * Strictly implements java.io.FileReader (wrapped in BufferedReader) and
 * java.io.FileWriter (wrapped in BufferedWriter).
 */
public class FileHelper {

    public static final String DATA_DIR = "data";
    public static final String USERS_FILE = "data" + File.separator + "users.txt";
    public static final String TIMELOGS_FILE = "data" + File.separator + "timelogs.txt";
    public static final String PAYSTUBS_DIR = "paystubs";

    // ==========================================
    // NESTED MODEL: User
    // ==========================================
    public static class User {
        private String employeeId;
        private String name;
        private String username;
        private String password;
        private String role; // "Admin", "Supervisor", "Staff"
        private double hourlyRate;

        public User(String employeeId, String name, String username, String password, String role, double hourlyRate) {
            this.employeeId = employeeId;
            this.name = name;
            this.username = username;
            this.password = password;
            this.role = role;
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

        public String toCsvLine() {
            return String.format("%s,%s,%s,%s,%s,%.2f",
                    employeeId, name, username, password, role, hourlyRate);
        }

        @Override
        public String toString() {
            return String.format("%s - %s (%s)", employeeId, name, role);
        }
    }

    // ==========================================
    // NESTED MODEL: TimeLog
    // ==========================================
    public static class TimeLog {
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
            this.employeeId = employeeId;
            this.name = name;
            this.date = date;
            this.clockInTime = clockInTime;
            this.clockOutTime = clockOutTime;
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
            return "PENDING".equalsIgnoreCase(clockOutTime) || clockOutTime == null || clockOutTime.trim().isEmpty();
        }

        public String toCsvLine() {
            return String.format("%s,%s,%s,%s,%s,%.2f,%.2f,%.2f",
                    employeeId, name, date, clockInTime, clockOutTime,
                    regularHours, overtimeHours, totalPay);
        }
    }

    // ==========================================
    // INITIALIZATION & DIRECTORY SETUP
    // ==========================================

    /**
     * Ensures data/ and paystubs/ folders exist and seeds users.txt and timelogs.txt.
     */
    public static void initializeDataDirectoryAndFiles() {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        File paystubsDir = new File(PAYSTUBS_DIR);
        if (!paystubsDir.exists()) {
            paystubsDir.mkdirs();
        }

        File usersFile = new File(USERS_FILE);
        if (!usersFile.exists()) {
            seedDefaultUsers(usersFile);
        }

        File logsFile = new File(TIMELOGS_FILE);
        if (!logsFile.exists()) {
            try {
                logsFile.createNewFile();
            } catch (IOException e) {
                System.err.println("Could not create timelogs.txt: " + e.getMessage());
            }
        }
    }

    /**
     * Seeds default users required by specifications.
     */
    private static void seedDefaultUsers(File usersFile) {
        BufferedWriter writer = null;
        try {
            // Explicitly using FileWriter wrapped in BufferedWriter
            writer = new BufferedWriter(new FileWriter(usersFile));
            writer.write("EMP001,Admin User,admin,admin123,Admin,500.00");
            writer.newLine();
            writer.write("EMP002,Jane Supervisor,super,super123,Supervisor,350.00");
            writer.newLine();
            writer.write("EMP003,John Staff,staff,staff123,Staff,200.00");
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error seeding users.txt: " + e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {}
            }
        }
    }

    // ==========================================
    // USER PERSISTENCE (FileReader & FileWriter)
    // ==========================================

    /**
     * Loads all users from data/users.txt using java.io.FileReader wrapped in BufferedReader.
     */
    public static List<User> loadUsersFromFile() {
        List<User> list = new ArrayList<User>();
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            initializeDataDirectoryAndFiles();
        }

        BufferedReader reader = null;
        try {
            // Explicitly using FileReader wrapped in BufferedReader
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    String empId = parts[0].trim();
                    String name = parts[1].trim();
                    String username = parts[2].trim();
                    String password = parts[3].trim();
                    String role = parts[4].trim();
                    double rate = Double.parseDouble(parts[5].trim());
                    list.add(new User(empId, name, username, password, role, rate));
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading users from file: " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {}
            }
        }
        return list;
    }

    /**
     * Saves the entire list of users to data/users.txt using java.io.FileWriter.
     */
    public static void saveUsersToFile(List<User> users) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(new File(USERS_FILE), false));
            for (User u : users) {
                writer.write(u.toCsvLine());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving users to file: " + e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {}
            }
        }
    }

    /**
     * Appends a single new user to data/users.txt.
     */
    public static void appendUser(User user) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(new File(USERS_FILE), true));
            writer.write(user.toCsvLine());
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error appending user: " + e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {}
            }
        }
    }

    // ==========================================
    // TIME LOG PERSISTENCE (FileReader & FileWriter)
    // ==========================================

    /**
     * Loads all time logs from data/timelogs.txt using java.io.FileReader wrapped in BufferedReader.
     */
    public static List<TimeLog> loadLogsFromFile() {
        List<TimeLog> logs = new ArrayList<TimeLog>();
        File file = new File(TIMELOGS_FILE);
        if (!file.exists()) {
            initializeDataDirectoryAndFiles();
            return logs;
        }

        BufferedReader reader = null;
        try {
            // Explicitly using FileReader wrapped in BufferedReader
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length >= 8) {
                    String empId = parts[0].trim();
                    String name = parts[1].trim();
                    String date = parts[2].trim();
                    String clockIn = parts[3].trim();
                    String clockOut = parts[4].trim();
                    double regHours = Double.parseDouble(parts[5].trim());
                    double otHours = Double.parseDouble(parts[6].trim());
                    double totalPay = Double.parseDouble(parts[7].trim());

                    logs.add(new TimeLog(empId, name, date, clockIn, clockOut, regHours, otHours, totalPay));
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading time logs: " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {}
            }
        }
        return logs;
    }

    /**
     * Saves all time logs back to data/timelogs.txt.
     */
    public static void saveLogsToFile(List<TimeLog> logs) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(new File(TIMELOGS_FILE), false));
            for (TimeLog log : logs) {
                writer.write(log.toCsvLine());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving logs: " + e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {}
            }
        }
    }

    /**
     * Appends a new TimeLog record to data/timelogs.txt.
     */
    public static void appendLogToFile(TimeLog log) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(new File(TIMELOGS_FILE), true));
            writer.write(log.toCsvLine());
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error appending time log: " + e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {}
            }
        }
    }

    // ==========================================
    // PAY STUB EXPORT (FileWriter)
    // ==========================================

    /**
     * Exports a pay stub text receipt backup into paystubs/ folder.
     *
     * @param employeeId   ID of the employee
     * @param payStubText  formatted receipt content
     * @return Path string of the saved file
     */
    public static String exportPayStub(String employeeId, String payStubText) {
        File dir = new File(PAYSTUBS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = PAYSTUBS_DIR + File.separator + "PayStub_" + employeeId + "_" + timestamp + ".txt";
        BufferedWriter writer = null;
        try {
            // Explicitly using FileWriter wrapped in BufferedWriter
            writer = new BufferedWriter(new FileWriter(new File(fileName)));
            writer.write(payStubText);
            return fileName;
        } catch (IOException e) {
            System.err.println("Error exporting pay stub: " + e.getMessage());
            return null;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {}
            }
        }
    }
}
