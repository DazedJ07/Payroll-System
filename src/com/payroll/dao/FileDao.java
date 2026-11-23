package com.payroll.dao;

import com.payroll.model.TimeLog;
import com.payroll.model.User;
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
 * FileDao - Manages persistence for users, shift timelogs, and exported pay stubs.
 */
public class FileDao {
    public static final String DATA_DIR = "data";
    public static final String USERS_FILE = DATA_DIR + File.separator + "users.txt";
    public static final String TIMELOGS_FILE = DATA_DIR + File.separator + "timelogs.txt";
    public static final String PAYSTUBS_DIR = "paystubs";

    public static void initStorage() {
        new File(DATA_DIR).mkdirs();
        new File(PAYSTUBS_DIR).mkdirs();

        File usersFile = new File(USERS_FILE);
        if (!usersFile.exists()) {
            List<User> defaults = new ArrayList<User>();
            defaults.add(new User("EMP001", "Admin User", "admin", "admin123", "Admin", 500.00));
            defaults.add(new User("EMP002", "Jane Supervisor", "super", "super123", "Supervisor", 350.00));
            defaults.add(new User("EMP003", "John Staff", "staff", "staff123", "Staff", 200.00));
            saveUsers(defaults);
        }

        File logsFile = new File(TIMELOGS_FILE);
        if (!logsFile.exists()) {
            try {
                logsFile.createNewFile();
            } catch (IOException ignored) {}
        }
    }

    public static List<User> loadUsers() {
        List<User> list = new ArrayList<User>();
        File file = new File(USERS_FILE);
        if (!file.exists()) initStorage();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                User u = User.fromCsv(line);
                if (u != null) list.add(u);
            }
        } catch (IOException e) {
            System.err.println("Error reading users: " + e.getMessage());
        }
        return list;
    }

    public static boolean saveUsers(List<User> users) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE, false))) {
            for (User u : users) {
                writer.write(u.toCsv());
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error writing users: " + e.getMessage());
            return false;
        }
    }

    public static boolean appendUser(User user) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE, true))) {
            writer.write(user.toCsv());
            writer.newLine();
            return true;
        } catch (IOException e) {
            System.err.println("Error appending user: " + e.getMessage());
            return false;
        }
    }

    public static List<TimeLog> loadLogs() {
        List<TimeLog> list = new ArrayList<TimeLog>();
        File file = new File(TIMELOGS_FILE);
        if (!file.exists()) initStorage();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                TimeLog log = TimeLog.fromCsv(line);
                if (log != null) list.add(log);
            }
        } catch (IOException e) {
            System.err.println("Error reading timelogs: " + e.getMessage());
        }
        return list;
    }

    public static boolean saveLogs(List<TimeLog> logs) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TIMELOGS_FILE, false))) {
            for (TimeLog log : logs) {
                writer.write(log.toCsv());
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error writing timelogs: " + e.getMessage());
            return false;
        }
    }

    public static boolean appendLog(TimeLog log) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TIMELOGS_FILE, true))) {
            writer.write(log.toCsv());
            writer.newLine();
            return true;
        } catch (IOException e) {
            System.err.println("Error appending timelog: " + e.getMessage());
            return false;
        }
    }

    public static String savePayStubFile(String employeeId, String payStubText) {
        new File(PAYSTUBS_DIR).mkdirs();
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = PAYSTUBS_DIR + File.separator + "PayStub_" + employeeId + "_" + timestamp + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(payStubText);
            return fileName;
        } catch (IOException e) {
            System.err.println("Error exporting pay stub: " + e.getMessage());
            return null;
        }
    }
}
