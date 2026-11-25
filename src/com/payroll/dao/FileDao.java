package com.payroll.dao;

import com.payroll.model.TimeLog;
import com.payroll.model.User;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
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
    public static final String ARCHIVED_USERS_FILE = DATA_DIR + File.separator + "archived_users.txt";
    public static final String ARCHIVED_TIMELOGS_FILE = DATA_DIR + File.separator + "archived_timelogs.txt";
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
        return loadUsersFrom(USERS_FILE, true);
    }

    public static List<User> loadArchivedUsers() {
        return loadUsersFrom(ARCHIVED_USERS_FILE, false);
    }

    private static List<User> loadUsersFrom(String path, boolean initializeIfMissing) {
        List<User> list = new ArrayList<User>();
        File file = new File(path);
        if (!file.exists() && initializeIfMissing) initStorage();
        if (!file.exists()) return list;

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
        return saveUsersTo(USERS_FILE, users);
    }

    private static boolean saveUsersTo(String path, List<User> users) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path, false))) {
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

    public static boolean archiveUser(String employeeId) {
        List<User> users = loadUsers();
        User target = findUser(users, employeeId);
        if (target == null) return false;

        List<User> archived = loadArchivedUsers();
        if (findUser(archived, employeeId) == null) {
            archived.add(target);
            if (!saveUsersTo(ARCHIVED_USERS_FILE, archived)) return false;
        }
        users.remove(target);
        return saveUsers(users);
    }

    public static boolean restoreUser(String employeeId) {
        List<User> archived = loadArchivedUsers();
        User target = findUser(archived, employeeId);
        if (target == null) return false;

        List<User> users = loadUsers();
        User activeWithId = findUser(users, employeeId);
        User activeWithUsername = findUserByUsername(users, target.getUsername());
        if (activeWithId == null && activeWithUsername != null) return false;
        if (activeWithId == null) {
            users.add(target);
            if (!saveUsers(users)) return false;
        }
        archived.remove(target);
        return saveUsersTo(ARCHIVED_USERS_FILE, archived);
    }

    public static boolean deleteUser(String employeeId) {
        List<User> users = loadUsers();
        List<User> archived = loadArchivedUsers();
        boolean removedActive = removeUser(users, employeeId);
        boolean removedArchived = removeUser(archived, employeeId);
        if (!removedActive && !removedArchived) return false;
        boolean usersSaved = saveUsers(users);
        boolean archivedSaved = saveUsersTo(ARCHIVED_USERS_FILE, archived);
        return usersSaved && archivedSaved;
    }

    private static User findUser(List<User> users, String employeeId) {
        for (User user : users) {
            if (user.getEmployeeId().equalsIgnoreCase(employeeId)) return user;
        }
        return null;
    }

    private static User findUserByUsername(List<User> users, String username) {
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) return user;
        }
        return null;
    }

    private static boolean removeUser(List<User> users, String employeeId) {
        User target = findUser(users, employeeId);
        return target != null && users.remove(target);
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
        return loadLogsFrom(TIMELOGS_FILE, true);
    }

    public static List<TimeLog> loadArchivedLogs() {
        return loadLogsFrom(ARCHIVED_TIMELOGS_FILE, false);
    }

    private static List<TimeLog> loadLogsFrom(String path, boolean initializeIfMissing) {
        List<TimeLog> list = new ArrayList<TimeLog>();
        File file = new File(path);
        if (!file.exists() && initializeIfMissing) initStorage();
        if (!file.exists()) return list;

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
        return saveLogsTo(TIMELOGS_FILE, logs);
    }

    private static boolean saveLogsTo(String path, List<TimeLog> logs) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path, false))) {
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

    public static boolean archiveLog(String employeeId, String clockInTime) {
        List<TimeLog> logs = loadLogs();
        TimeLog target = findLog(logs, employeeId, clockInTime);
        if (target == null) return false;

        List<TimeLog> archived = loadArchivedLogs();
        if (findLog(archived, employeeId, clockInTime) == null) {
            archived.add(target);
            if (!saveLogsTo(ARCHIVED_TIMELOGS_FILE, archived)) return false;
        }
        logs.remove(target);
        return saveLogs(logs);
    }

    public static boolean restoreLog(String employeeId, String clockInTime) {
        List<TimeLog> archived = loadArchivedLogs();
        TimeLog target = findLog(archived, employeeId, clockInTime);
        if (target == null) return false;

        List<TimeLog> logs = loadLogs();
        if (findLog(logs, employeeId, clockInTime) == null) {
            logs.add(target);
            if (!saveLogs(logs)) return false;
        }
        archived.remove(target);
        return saveLogsTo(ARCHIVED_TIMELOGS_FILE, archived);
    }

    public static boolean deleteLog(String employeeId, String clockInTime) {
        List<TimeLog> logs = loadLogs();
        List<TimeLog> archived = loadArchivedLogs();
        boolean removedActive = removeLog(logs, employeeId, clockInTime);
        boolean removedArchived = removeLog(archived, employeeId, clockInTime);
        if (!removedActive && !removedArchived) return false;
        boolean logsSaved = saveLogs(logs);
        boolean archivedSaved = saveLogsTo(ARCHIVED_TIMELOGS_FILE, archived);
        return logsSaved && archivedSaved;
    }

    private static TimeLog findLog(List<TimeLog> logs, String employeeId, String clockInTime) {
        for (TimeLog log : logs) {
            if (log.getEmployeeId().equalsIgnoreCase(employeeId)
                    && log.getClockInTime().equals(clockInTime)) return log;
        }
        return null;
    }

    private static boolean removeLog(List<TimeLog> logs, String employeeId, String clockInTime) {
        TimeLog target = findLog(logs, employeeId, clockInTime);
        return target != null && logs.remove(target);
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

    public static String savePayStubPdf(String employeeId, String payStubText) {
        File directory = new File(PAYSTUBS_DIR);
        if (!directory.exists() && !directory.mkdirs()) {
            System.err.println("Error creating pay stub directory: " + directory.getPath());
            return null;
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File pdfFile = new File(directory, "PayStub_" + employeeId + "_" + timestamp + ".pdf");
        Document document = new Document(PageSize.LETTER, 42, 42, 42, 42);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();
            Font font = new Font(Font.COURIER, 8);
            for (String line : payStubText.split("\\r?\\n", -1)) {
                document.add(new Paragraph(line.isEmpty() ? " " : line, font));
            }
            document.close();
            return pdfFile.getPath();
        } catch (DocumentException | IOException e) {
            if (document.isOpen()) {
                document.close();
            }
            if (pdfFile.exists() && !pdfFile.delete()) {
                System.err.println("Could not remove incomplete pay stub PDF: " + pdfFile.getPath());
            }
            System.err.println("Error creating pay stub PDF: " + e.getMessage());
            return null;
        }
    }
}
