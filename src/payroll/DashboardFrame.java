package payroll;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/*
DashboardFrame - Main user interface accommodating Staff, Supervisor, and Admin views.
Features live time tracking, shift history, role-based controls, and one-click pay stub generation.
*/
public class DashboardFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final FileHelper.User currentUser;

    // Header Components
    private JLabel lblLiveClock;
    private JLabel lblStatusBadge;
    private JLabel lblUserTag;
    private JLabel lblAccountName;
    private JLabel lblAccountUsername;
    private JLabel lblAccountRole;

    // Staff Timekeeping Panel Components
    private JButton btnClockIn;
    private JButton btnClockOut;
    private JButton btnGenerateMyPayStub;
    private JTable tblPersonalLogs;
    private DefaultTableModel modelPersonalLogs;
    private JLabel lblPersonalSummary;

    // Supervisor Team Overview Components
    private JTable tblTeamLogs;
    private DefaultTableModel modelTeamLogs;
    private JButton btnResolveMissedClockOut;
    private JButton btnAddManualShift;
    private JButton btnRefreshTeam;

    // Admin Management Components
    private JTable tblEmployees;
    private DefaultTableModel modelEmployees;
    private JButton btnAddEmployee;
    private JButton btnAdjustRate;
    private JButton btnAdminGeneratePayStub;

    public DashboardFrame(FileHelper.User user) {
        this.currentUser = user;

        setTitle("Staff Timekeeping & Payroll System - [" + user.getRole().toUpperCase() + " DASHBOARD]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 680);
        setLocationRelativeTo(null);

        initComponents();
        startLiveClock();
        refreshAllData();
    }

    // UI INITIALIZATION
    private void initComponents() {
        JPanel rootPanel = new JPanel(new BorderLayout(0, 10));
        rootPanel.setBackground(new Color(241, 245, 249));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));

        // 1. Top Header Bar
        rootPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Center Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Personal Timekeeping Tab 
        tabbedPane.addTab("My Timekeeping", createPersonalTimekeepingPanel());
        tabbedPane.addTab("Account Details", createAccountDetailsPanel());

        // Supervisor View 
        if ("Supervisor".equalsIgnoreCase(currentUser.getRole()) || "Admin".equalsIgnoreCase(currentUser.getRole())) {
            tabbedPane.addTab("Team Overview & Logs", createSupervisorPanel());
        }

        // Admin View
        if ("Admin".equalsIgnoreCase(currentUser.getRole())) {
            tabbedPane.addTab("Employee & Payroll Admin", createAdminPanel());
        }

        rootPanel.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(rootPanel);
    }

    /*
    Creates top header banner showing user info, live clock, and logout button.
    */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 10));
        panel.setBackground(new Color(30, 41, 59));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        // Left info
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Timekeeping & Payroll");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);

        lblUserTag = new JLabel(userTagText());
        lblUserTag.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUserTag.setForeground(new Color(203, 213, 225));

        leftPanel.add(lblTitle);
        leftPanel.add(lblUserTag);

        //(Live Clock, Status Badge, Logout)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setOpaque(false);

        lblLiveClock = new JLabel("yyyy-MM-dd HH:mm:ss");
        lblLiveClock.setFont(new Font("Monospaced", Font.BOLD, 13));
        lblLiveClock.setForeground(new Color(254, 240, 138));

        lblStatusBadge = new JLabel("Status: Unknown");
        lblStatusBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatusBadge.setForeground(Color.WHITE);
        lblStatusBadge.setOpaque(true);
        lblStatusBadge.setBackground(new Color(71, 85, 105));
        lblStatusBadge.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setBackground(new Color(225, 29, 72));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(DashboardFrame.this,
                        "Are you sure you want to log out?",
                        "Confirm Logout",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    new LoginFrame().setVisible(true);
                    DashboardFrame.this.dispose();
                }
            }
        });

        rightPanel.add(lblLiveClock);
        rightPanel.add(lblStatusBadge);
        rightPanel.add(btnLogout);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);
        return panel;
    }

    // 1. STAFF VIEW: PERSONAL TIMEKEEPING
    private JPanel createPersonalTimekeepingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Action Buttons Bar
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        actionBar.setOpaque(false);

        btnClockIn = new JButton("Clock In");
        btnClockIn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnClockIn.setBackground(new Color(22, 163, 74));
        btnClockIn.setForeground(Color.WHITE);
        btnClockIn.setPreferredSize(new Dimension(130, 36));
        btnClockIn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClockIn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clockIn();
            }
        });

        btnClockOut = new JButton("Clock Out");
        btnClockOut.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnClockOut.setBackground(new Color(220, 38, 38));
        btnClockOut.setForeground(Color.WHITE);
        btnClockOut.setPreferredSize(new Dimension(130, 36));
        btnClockOut.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClockOut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clockOut();
            }
        });

        btnGenerateMyPayStub = new JButton("Generate My Pay Stub");
        btnGenerateMyPayStub.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnGenerateMyPayStub.setBackground(new Color(37, 99, 235));
        btnGenerateMyPayStub.setForeground(Color.WHITE);
        btnGenerateMyPayStub.setPreferredSize(new Dimension(190, 36));
        btnGenerateMyPayStub.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGenerateMyPayStub.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportPayStub(currentUser.getEmployeeId());
            }
        });

        JButton btnRefreshPersonal = new JButton("Refresh");
        btnRefreshPersonal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefreshPersonal.setPreferredSize(new Dimension(90, 36));
        btnRefreshPersonal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshAllData();
            }
        });

        actionBar.add(btnClockIn);
        actionBar.add(btnClockOut);
        actionBar.add(Box.createHorizontalStrut(15));
        actionBar.add(btnGenerateMyPayStub);
        actionBar.add(btnRefreshPersonal);

        panel.add(actionBar, BorderLayout.NORTH);

        // Personal Shift Table
        String[] columns = {"Date", "Clock In Time", "Clock Out Time", "Regular Hours", "Overtime Hours", "Shift Total Pay"};
        modelPersonalLogs = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblPersonalLogs = new JTable(modelPersonalLogs);
        tblPersonalLogs.setRowHeight(26);
        tblPersonalLogs.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblPersonalLogs.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblPersonalLogs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tblPersonalLogs.getColumnCount(); i++) {
            tblPersonalLogs.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(tblPersonalLogs);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Summary Bar
        lblPersonalSummary = new JLabel("Summary: Total Shifts: 0 | Regular Hours: 0.00 | Overtime Hours: 0.00 | Estimated Total Pay: $0.00");
        lblPersonalSummary.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPersonalSummary.setForeground(new Color(15, 23, 42));
        lblPersonalSummary.setBorder(BorderFactory.createEmptyBorder(8, 5, 5, 5));
        panel.add(lblPersonalSummary, BorderLayout.SOUTH);

        return panel;
    }

    // 2. SUPERVISOR VIEW: TEAM OVERVIEW
    private JPanel createSupervisorPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.setOpaque(false);

        btnResolveMissedClockOut = new JButton("Resolve Missed Clock-Out");
        btnResolveMissedClockOut.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnResolveMissedClockOut.setBackground(new Color(217, 119, 6));
        btnResolveMissedClockOut.setForeground(Color.WHITE);
        btnResolveMissedClockOut.setPreferredSize(new Dimension(210, 34));
        btnResolveMissedClockOut.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnResolveMissedClockOut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resolveMissedClockOut();
            }
        });

        btnAddManualShift = new JButton("Add Manual Shift Entry");
        btnAddManualShift.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAddManualShift.setBackground(new Color(79, 70, 229));
        btnAddManualShift.setForeground(Color.WHITE);
        btnAddManualShift.setPreferredSize(new Dimension(190, 34));
        btnAddManualShift.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAddManualShift.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addManualShiftEntry();
            }
        });

        btnRefreshTeam = new JButton("Refresh Logs");
        btnRefreshTeam.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefreshTeam.setPreferredSize(new Dimension(110, 34));
        btnRefreshTeam.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshAllData();
            }
        });

        toolbar.add(btnResolveMissedClockOut);
        toolbar.add(btnAddManualShift);
        toolbar.add(btnRefreshTeam);

        panel.add(toolbar, BorderLayout.NORTH);

        // Team Table
        String[] teamColumns = {"Emp ID", "Employee Name", "Date", "Clock In Time", "Clock Out Time", "Reg Hrs", "OT Hrs", "Total Pay"};
        modelTeamLogs = new DefaultTableModel(teamColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblTeamLogs = new JTable(modelTeamLogs);
        tblTeamLogs.setRowHeight(26);
        tblTeamLogs.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblTeamLogs.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblTeamLogs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tblTeamLogs.getColumnCount(); i++) {
            tblTeamLogs.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(tblTeamLogs);
        panel.add(scrollPane, BorderLayout.CENTER);

        JLabel lblHint = new JLabel("Tip: Select a shift with 'PENDING' clock-out and click 'Resolve Missed Clock-Out' to complete the punch.");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHint.setForeground(new Color(100, 116, 139));
        lblHint.setBorder(BorderFactory.createEmptyBorder(6, 4, 4, 4));
        panel.add(lblHint, BorderLayout.SOUTH);

        return panel;
    }

    // 3. ADMIN VIEW: EMPLOYEE & PAYROLL ADMIN
    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Admin Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.setOpaque(false);

        btnAddEmployee = new JButton("+ Add New Employee");
        btnAddEmployee.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAddEmployee.setBackground(new Color(16, 185, 129));
        btnAddEmployee.setForeground(Color.WHITE);
        btnAddEmployee.setPreferredSize(new Dimension(175, 34));
        btnAddEmployee.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAddEmployee.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewEmployee();
            }
        });

        btnAdjustRate = new JButton("Adjust Hourly Rate");
        btnAdjustRate.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAdjustRate.setBackground(new Color(14, 165, 233));
        btnAdjustRate.setForeground(Color.WHITE);
        btnAdjustRate.setPreferredSize(new Dimension(165, 34));
        btnAdjustRate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdjustRate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adjustHourlyRate();
            }
        });

        btnAdminGeneratePayStub = new JButton("Generate Employee Pay Stub");
        btnAdminGeneratePayStub.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAdminGeneratePayStub.setBackground(new Color(124, 58, 237));
        btnAdminGeneratePayStub.setForeground(Color.WHITE);
        btnAdminGeneratePayStub.setPreferredSize(new Dimension(220, 34));
        btnAdminGeneratePayStub.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdminGeneratePayStub.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generatePayStubForSelectedEmployee();
            }
        });

        JButton btnEditCredentials = new JButton("Edit Credentials");
        btnEditCredentials.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnEditCredentials.setPreferredSize(new Dimension(150, 34));
        btnEditCredentials.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editEmployeeCredentials();
            }
        });

        JButton btnUpdateEmployeePassword = new JButton("Update Password");
        btnUpdateEmployeePassword.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnUpdateEmployeePassword.setPreferredSize(new Dimension(160, 34));
        btnUpdateEmployeePassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateEmployeePassword();
            }
        });

        JButton btnRefreshEmp = new JButton("Refresh");
        btnRefreshEmp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefreshEmp.setPreferredSize(new Dimension(90, 34));
        btnRefreshEmp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshAllData();
            }
        });

        toolbar.add(btnAddEmployee);
        toolbar.add(btnAdjustRate);
        toolbar.add(btnAdminGeneratePayStub);
        toolbar.add(btnRefreshEmp);

        JPanel accountToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        accountToolbar.setOpaque(false);
        accountToolbar.add(btnEditCredentials);
        accountToolbar.add(btnUpdateEmployeePassword);

        JPanel toolbarRows = new JPanel(new GridLayout(2, 1, 0, 2));
        toolbarRows.setOpaque(false);
        toolbarRows.add(toolbar);
        toolbarRows.add(accountToolbar);
        panel.add(toolbarRows, BorderLayout.NORTH);

        // Employee Table
        String[] empColumns = {"Employee ID", "Full Name", "Username", "Role", "Hourly Rate ($/hr)"};
        modelEmployees = new DefaultTableModel(empColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblEmployees = new JTable(modelEmployees);
        tblEmployees.setRowHeight(26);
        tblEmployees.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblEmployees.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblEmployees.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tblEmployees.getColumnCount(); i++) {
            tblEmployees.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(tblEmployees);
        panel.add(scrollPane, BorderLayout.CENTER);

        JLabel lblNotice = new JLabel("Select an employee to edit account credentials or update their password.");
        lblNotice.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNotice.setForeground(new Color(100, 116, 139));
        lblNotice.setBorder(BorderFactory.createEmptyBorder(6, 4, 4, 4));
        panel.add(lblNotice, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createAccountDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));

        JLabel title = new JLabel("Account Details");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(30, 41, 59));
        panel.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setOpaque(false);

        JPanel details = new JPanel(new GridLayout(0, 2, 12, 10));
        details.setOpaque(false);
        details.setBorder(BorderFactory.createTitledBorder("Your Profile"));
        lblAccountName = addAccountDetail(details, "Full name:", currentUser.getName());
        addAccountDetail(details, "Employee ID:", currentUser.getEmployeeId());
        lblAccountUsername = addAccountDetail(details, "Username:", currentUser.getUsername());
        lblAccountRole = addAccountDetail(details, "Role:", currentUser.getRole());
        content.add(details, BorderLayout.NORTH);

        JPanel passwordForm = new JPanel(new GridBagLayout());
        passwordForm.setOpaque(false);
        passwordForm.setBorder(BorderFactory.createTitledBorder("Change Password"));

        PasswordFieldWithToggle currentPassword = new PasswordFieldWithToggle(18);
        PasswordFieldWithToggle newPassword = new PasswordFieldWithToggle(18);
        PasswordFieldWithToggle confirmPassword = new PasswordFieldWithToggle(18);
        setPasswordFieldSize(currentPassword);
        setPasswordFieldSize(newPassword);
        setPasswordFieldSize(confirmPassword);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 8, 7, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.3;
        gbc.gridx = 0; gbc.gridy = 0;
        passwordForm.add(new JLabel("Current password:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        passwordForm.add(currentPassword, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        passwordForm.add(new JLabel("New password:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        passwordForm.add(newPassword, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        passwordForm.add(new JLabel("Confirm password:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        passwordForm.add(confirmPassword, gbc);

        JButton btnChangePassword = new JButton("Change Password");
        btnChangePassword.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnChangePassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeOwnPassword(currentPassword, newPassword, confirmPassword);
            }
        });
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 0.7;
        gbc.anchor = GridBagConstraints.EAST;
        passwordForm.add(btnChangePassword, gbc);

        content.add(passwordForm, BorderLayout.CENTER);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JLabel addAccountDetail(JPanel details, String label, String value) {
        JLabel name = new JLabel(label);
        name.setFont(new Font("Segoe UI", Font.BOLD, 13));
        details.add(name);
        JLabel detail = new JLabel(value);
        detail.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        details.add(detail);
        return detail;
    }

    private void setPasswordFieldSize(PasswordFieldWithToggle input) {
        input.getPasswordField().setFont(new Font("Segoe UI", Font.PLAIN, 13));
        input.getPasswordField().setPreferredSize(new Dimension(220, 32));
    }

    private String userTagText() {
        return " |  " + currentUser.getName() + " (" + currentUser.getEmployeeId() + ") - "
                + currentUser.getRole() + "  |  Rate: "
                + PayrollCalculator.formatCurrency(currentUser.getHourlyRate()) + "/hr";
    }

    // LIVE CLOCK & DATA REFRESH ENGINE
    private void startLiveClock() {
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String now = new SimpleDateFormat(PayrollCalculator.DATE_TIME_FORMAT).format(new Date());
                lblLiveClock.setText(now);
            }
        });
        timer.start();
    }

    /*
    Refreshes all tables and status badges from local data files.
    */
    private void refreshAllData() {
        List<FileHelper.TimeLog> allLogs = FileHelper.loadLogsFromFile();
        List<FileHelper.User> allUsers = FileHelper.loadUsersFromFile();

        // 1. Refresh Personal Table & Status
        modelPersonalLogs.setRowCount(0);
        double totalReg = 0.0;
        double totalOt = 0.0;
        double totalPay = 0.0;
        int completedShifts = 0;
        boolean currentlyClockedIn = false;
        String activeClockInTime = "";

        for (FileHelper.TimeLog log : allLogs) {
            if (log.getEmployeeId().equalsIgnoreCase(currentUser.getEmployeeId())) {
                modelPersonalLogs.addRow(new Object[]{
                        log.getDate(),
                        log.getClockInTime(),
                        log.getClockOutTime(),
                        PayrollCalculator.formatHours(log.getRegularHours()),
                        PayrollCalculator.formatHours(log.getOvertimeHours()),
                        PayrollCalculator.formatCurrency(log.getTotalPay())
                });

                if (log.isPending()) {
                    currentlyClockedIn = true;
                    activeClockInTime = log.getClockInTime();
                } else {
                    completedShifts++;
                    totalReg += log.getRegularHours();
                    totalOt += log.getOvertimeHours();
                    totalPay += log.getTotalPay();
                }
            }
        }

        // Update Personal Summary
        lblPersonalSummary.setText(String.format(
                "Personal Summary: Completed Shifts: %d | Regular Hours: %.2f | Overtime Hours: %.2f | Estimated Total Pay: %s",
                completedShifts, totalReg, totalOt, PayrollCalculator.formatCurrency(totalPay)
        ));

        // Update Status Badge and Clock In/Out button states
        if (currentlyClockedIn) {
            lblStatusBadge.setText("Status: Clocked In since " + activeClockInTime);
            lblStatusBadge.setBackground(new Color(22, 163, 74));
            btnClockIn.setEnabled(false);
            btnClockOut.setEnabled(true);
        } else {
            lblStatusBadge.setText("Status: Clocked Out");
            lblStatusBadge.setBackground(new Color(100, 116, 139));
            btnClockIn.setEnabled(true);
            btnClockOut.setEnabled(false);
        }

        // 2. Refresh Supervisor Team Table if available
        if (modelTeamLogs != null) {
            modelTeamLogs.setRowCount(0);
            for (FileHelper.TimeLog log : allLogs) {
                modelTeamLogs.addRow(new Object[]{
                        log.getEmployeeId(),
                        log.getName(),
                        log.getDate(),
                        log.getClockInTime(),
                        log.getClockOutTime(),
                        PayrollCalculator.formatHours(log.getRegularHours()),
                        PayrollCalculator.formatHours(log.getOvertimeHours()),
                        PayrollCalculator.formatCurrency(log.getTotalPay())
                });
            }
        }

        // 3. Refresh Admin Employee Table if available
        if (modelEmployees != null) {
            modelEmployees.setRowCount(0);
            for (FileHelper.User u : allUsers) {
                modelEmployees.addRow(new Object[]{
                        u.getEmployeeId(),
                        u.getName(),
                        u.getUsername(),
                        u.getRole(),
                        String.format("%.2f", u.getHourlyRate())
                });
            }
        }
    }

    // USER-DEFINED METHODS: CLOCK IN & CLOCK OUT

    /*
    User-defined method: Clocks in the current user.
    Prevents double clock-in if an active shift is already pending.
    */
    public void clockIn() {
        List<FileHelper.TimeLog> logs = FileHelper.loadLogsFromFile();

        // Check for double clock-in
        for (FileHelper.TimeLog log : logs) {
            if (log.getEmployeeId().equalsIgnoreCase(currentUser.getEmployeeId()) && log.isPending()) {
                JOptionPane.showMessageDialog(this,
                        "You are already clocked in!\nActive shift started at: " + log.getClockInTime() +
                                "\nPlease clock out before clocking in again.",
                        "Clock In Warning",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        Date now = new Date();
        String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(now);
        String timeStr = new SimpleDateFormat(PayrollCalculator.DATE_TIME_FORMAT).format(now);

        FileHelper.TimeLog newLog = new FileHelper.TimeLog(
                currentUser.getEmployeeId(),
                currentUser.getName(),
                dateStr,
                timeStr,
                "PENDING",
                0.0,
                0.0,
                0.0
        );

        FileHelper.appendLogToFile(newLog);
        refreshAllData();

        JOptionPane.showMessageDialog(this,
                "Clock In recorded successfully!\nTimestamp: " + timeStr,
                "Clocked In",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /*
    User-defined method: Clocks out the current user, calculates regular hours,
    overtime hours, and total gross pay, and updates timelogs.txt.
    */
    public void clockOut() {
        List<FileHelper.TimeLog> logs = FileHelper.loadLogsFromFile();
        FileHelper.TimeLog activeLog = null;

        for (FileHelper.TimeLog log : logs) {
            if (log.getEmployeeId().equalsIgnoreCase(currentUser.getEmployeeId()) && log.isPending()) {
                activeLog = log;
                break;
            }
        }

        if (activeLog == null) {
            JOptionPane.showMessageDialog(this,
                    "No active clock-in found! Please clock in first.",
                    "Clock Out Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String clockOutTime = new SimpleDateFormat(PayrollCalculator.DATE_TIME_FORMAT).format(new Date());
        activeLog.setClockOutTime(clockOutTime);

        // Perform payroll calculations via dedicated methods
        double totalHours = PayrollCalculator.calculateTotalHours(activeLog.getClockInTime(), clockOutTime);
        double regHours = PayrollCalculator.calculateRegularHours(totalHours);
        double otHours = PayrollCalculator.calculateOvertimeHours(totalHours);
        double totalPay = PayrollCalculator.calculateGrossPay(regHours, otHours, currentUser.getHourlyRate());

        activeLog.setRegularHours(regHours);
        activeLog.setOvertimeHours(otHours);
        activeLog.setTotalPay(totalPay);

        FileHelper.saveLogsToFile(logs);
        refreshAllData();

        String message = String.format(
                "Clock Out recorded successfully!\n" +
                        "-----------------------------------------\n" +
                        "Clock In:        %s\n" +
                        "Clock Out:       %s\n" +
                        "Total Hours:     %.2f hrs\n" +
                        "Regular Hours:   %.2f hrs\n" +
                        "Overtime Hours:  %.2f hrs\n" +
                        "Hourly Rate:     %s/hr\n" +
                        "Shift Earnings:  %s",
                activeLog.getClockInTime(),
                clockOutTime,
                totalHours,
                regHours,
                otHours,
                PayrollCalculator.formatCurrency(currentUser.getHourlyRate()),
                PayrollCalculator.formatCurrency(totalPay)
        );

        JOptionPane.showMessageDialog(this, message, "Shift Summary", JOptionPane.INFORMATION_MESSAGE);
    }

    // SUPERVISOR LOGIC: RESOLVE MISSED PUNCHES

    /*
    Resolves a missed clock-out for a pending shift or allows editing.
    */
    private void resolveMissedClockOut() {
        List<FileHelper.TimeLog> logs = FileHelper.loadLogsFromFile();
        List<FileHelper.User> users = FileHelper.loadUsersFromFile();

        // Check if a row is selected in the team table
        int selectedRow = tblTeamLogs.getSelectedRow();
        FileHelper.TimeLog targetLog = null;

        if (selectedRow >= 0) {
            String empId = (String) modelTeamLogs.getValueAt(selectedRow, 0);
            String clockIn = (String) modelTeamLogs.getValueAt(selectedRow, 3);
            for (FileHelper.TimeLog log : logs) {
                if (log.getEmployeeId().equalsIgnoreCase(empId) && log.getClockInTime().equalsIgnoreCase(clockIn)) {
                    targetLog = log;
                    break;
                }
            }
        }

        // If no row selected or row is not pending, let user select from pending logs
        if (targetLog == null || !targetLog.isPending()) {
            List<FileHelper.TimeLog> pendingLogs = new ArrayList<FileHelper.TimeLog>();
            for (FileHelper.TimeLog log : logs) {
                if (log.isPending()) {
                    pendingLogs.add(log);
                }
            }

            if (pendingLogs.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "There are currently no pending clock-ins to resolve.",
                        "No Pending Shifts",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String[] options = new String[pendingLogs.size()];
            for (int i = 0; i < pendingLogs.size(); i++) {
                FileHelper.TimeLog l = pendingLogs.get(i);
                options[i] = String.format("%s - %s (%s)", l.getEmployeeId(), l.getName(), l.getClockInTime());
            }

            String selected = (String) JOptionPane.showInputDialog(
                    this,
                    "Select a pending shift to resolve:",
                    "Resolve Missed Clock-Out",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (selected == null) return;

            for (int i = 0; i < options.length; i++) {
                if (options[i].equals(selected)) {
                    targetLog = pendingLogs.get(i);
                    break;
                }
            }
        }

        if (targetLog == null) return;

        // Prompt for clock-out timestamp
        String defaultOut = new SimpleDateFormat(PayrollCalculator.DATE_TIME_FORMAT).format(new Date());
        String inputOut = JOptionPane.showInputDialog(
                this,
                "Shift Details:\n" +
                        "Employee: " + targetLog.getName() + " (" + targetLog.getEmployeeId() + ")\n" +
                        "Clock In: " + targetLog.getClockInTime() + "\n\n" +
                        "Enter Clock-Out Timestamp (yyyy-MM-dd HH:mm:ss):",
                defaultOut
        );

        if (inputOut == null || inputOut.trim().isEmpty()) {
            return;
        }

        inputOut = inputOut.trim();

        // Validate format
        SimpleDateFormat sdf = new SimpleDateFormat(PayrollCalculator.DATE_TIME_FORMAT);
        sdf.setLenient(false);
        try {
            Date outDate = sdf.parse(inputOut);
            Date inDate = sdf.parse(targetLog.getClockInTime());
            if (outDate.before(inDate)) {
                JOptionPane.showMessageDialog(this,
                        "Clock-out time cannot be before clock-in time!",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid date format! Please use: yyyy-MM-dd HH:mm:ss",
                    "Format Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Find employee's hourly rate
        double hourlyRate = 200.0;
        for (FileHelper.User u : users) {
            if (u.getEmployeeId().equalsIgnoreCase(targetLog.getEmployeeId())) {
                hourlyRate = u.getHourlyRate();
                break;
            }
        }

        double totalHours = PayrollCalculator.calculateTotalHours(targetLog.getClockInTime(), inputOut);
        double regHours = PayrollCalculator.calculateRegularHours(totalHours);
        double otHours = PayrollCalculator.calculateOvertimeHours(totalHours);
        double totalPay = PayrollCalculator.calculateGrossPay(regHours, otHours, hourlyRate);

        targetLog.setClockOutTime(inputOut);
        targetLog.setRegularHours(regHours);
        targetLog.setOvertimeHours(otHours);
        targetLog.setTotalPay(totalPay);

        FileHelper.saveLogsToFile(logs);
        refreshAllData();

        JOptionPane.showMessageDialog(this,
                "Missed clock-out resolved successfully!\n" +
                        "Total Hours: " + String.format("%.2f", totalHours) + "\n" +
                        "Regular Hours: " + String.format("%.2f", regHours) + "\n" +
                        "Overtime Hours: " + String.format("%.2f", otHours) + "\n" +
                        "Total Calculated Pay: " + PayrollCalculator.formatCurrency(totalPay),
                "Resolution Successful",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /*
    Allows supervisor/admin to add a completely manual shift entry.
    */
    private void addManualShiftEntry() {
        List<FileHelper.User> users = FileHelper.loadUsersFromFile();
        if (users.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No employees found in the system.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] userOptions = new String[users.size()];
        for (int i = 0; i < users.size(); i++) {
            FileHelper.User u = users.get(i);
            userOptions[i] = u.getEmployeeId() + " - " + u.getName();
        }

        String selectedUserStr = (String) JOptionPane.showInputDialog(
                this,
                "Select Employee:",
                "Add Manual Shift",
                JOptionPane.PLAIN_MESSAGE,
                null,
                userOptions,
                userOptions[0]
        );
        if (selectedUserStr == null) return;

        FileHelper.User selectedUser = null;
        for (FileHelper.User u : users) {
            if (selectedUserStr.startsWith(u.getEmployeeId())) {
                selectedUser = u;
                break;
            }
        }
        if (selectedUser == null) return;

        String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String defaultIn = dateStr + " 08:00:00";
        String defaultOut = dateStr + " 17:00:00";

        JTextField txtIn = new JTextField(defaultIn, 18);
        JTextField txtOut = new JTextField(defaultOut, 18);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Employee:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        formPanel.add(new JLabel(selectedUser.getName() + " (" + selectedUser.getEmployeeId() + ")"), gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Clock-In (yyyy-MM-dd HH:mm:ss):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        formPanel.add(txtIn, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Clock-Out (yyyy-MM-dd HH:mm:ss):"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        formPanel.add(txtOut, gbc);

        int result = JOptionPane.showConfirmDialog(this, formPanel, "Enter Shift Times", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        String inTime = txtIn.getText().trim();
        String outTime = txtOut.getText().trim();

        SimpleDateFormat sdf = new SimpleDateFormat(PayrollCalculator.DATE_TIME_FORMAT);
        sdf.setLenient(false);
        try {
            Date dIn = sdf.parse(inTime);
            Date dOut = sdf.parse(outTime);
            if (dOut.before(dIn)) {
                JOptionPane.showMessageDialog(this, "Clock-out must be after clock-in!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String shiftDate = inTime.substring(0, 10);

            double totalHours = PayrollCalculator.calculateTotalHours(inTime, outTime);
            double regHours = PayrollCalculator.calculateRegularHours(totalHours);
            double otHours = PayrollCalculator.calculateOvertimeHours(totalHours);
            double totalPay = PayrollCalculator.calculateGrossPay(regHours, otHours, selectedUser.getHourlyRate());

            FileHelper.TimeLog manualLog = new FileHelper.TimeLog(
                    selectedUser.getEmployeeId(),
                    selectedUser.getName(),
                    shiftDate,
                    inTime,
                    outTime,
                    regHours,
                    otHours,
                    totalPay
            );

            FileHelper.appendLogToFile(manualLog);
            refreshAllData();

            JOptionPane.showMessageDialog(this,
                    "Manual shift entry saved successfully!\n" +
                            "Regular Hours: " + String.format("%.2f", regHours) + "\n" +
                            "Overtime Hours: " + String.format("%.2f", otHours) + "\n" +
                            "Calculated Total Pay: " + PayrollCalculator.formatCurrency(totalPay),
                    "Shift Saved",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid date format! Use: yyyy-MM-dd HH:mm:ss",
                    "Format Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ADMIN ACTIONS: ADD EMPLOYEES & ADJUST RATES

    private FileHelper.User selectEmployeeForAccountAction(String prompt, List<FileHelper.User> users) {
        if (users.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No employees found.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        int selectedRow = tblEmployees.getSelectedRow();
        if (selectedRow >= 0) {
            String employeeId = (String) modelEmployees.getValueAt(selectedRow, 0);
            for (FileHelper.User user : users) {
                if (user.getEmployeeId().equalsIgnoreCase(employeeId)) {
                    return user;
                }
            }
        }

        String[] options = new String[users.size()];
        for (int i = 0; i < users.size(); i++) {
            FileHelper.User user = users.get(i);
            options[i] = user.getEmployeeId() + " - " + user.getName() + " (" + user.getRole() + ")";
        }

        String selected = (String) JOptionPane.showInputDialog(
                this, prompt, "Select Employee", JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        if (selected == null) {
            return null;
        }

        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(selected)) {
                return users.get(i);
            }
        }
        return null;
    }

    private boolean isValidCsvField(String value) {
        return value != null && !value.trim().isEmpty()
                && value.indexOf(',') < 0 && value.indexOf('\n') < 0 && value.indexOf('\r') < 0;
    }

    private void editEmployeeCredentials() {
        List<FileHelper.User> users = FileHelper.loadUsersFromFile();
        final FileHelper.User targetUser = selectEmployeeForAccountAction(
                "Select the employee whose credentials you want to edit:", users);
        if (targetUser == null) {
            return;
        }

        JTextField txtName = new JTextField(targetUser.getName(), 18);
        JTextField txtUsername = new JTextField(targetUser.getUsername(), 18);
        JComboBox<String> cmbRole = new JComboBox<String>(new String[]{"Staff", "Supervisor", "Admin"});
        cmbRole.setSelectedItem(targetUser.getRole());

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.add(new JLabel("Full name:"));
        form.add(txtName);
        form.add(new JLabel("Username:"));
        form.add(txtUsername);
        form.add(new JLabel("Role:"));
        form.add(cmbRole);

        int result = JOptionPane.showConfirmDialog(
                this, form, "Edit Employee Credentials",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String name = txtName.getText().trim();
        String username = txtUsername.getText().trim();
        String role = (String) cmbRole.getSelectedItem();
        if (!isValidCsvField(name) || !isValidCsvField(username)) {
            JOptionPane.showMessageDialog(this,
                    "Full name and username are required and cannot contain commas or line breaks.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (FileHelper.User user : users) {
            if (!user.getEmployeeId().equalsIgnoreCase(targetUser.getEmployeeId())
                    && user.getUsername().equalsIgnoreCase(username)) {
                JOptionPane.showMessageDialog(this, "Username already exists!",
                        "Duplicate Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String oldRole = targetUser.getRole();
        targetUser.setName(name);
        targetUser.setUsername(username);
        targetUser.setRole(role);
        if (!FileHelper.saveUsersToFile(users)) {
            JOptionPane.showMessageDialog(this, "Could not save the updated employee credentials.",
                    "Save Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean editingCurrentAccount = targetUser.getEmployeeId().equalsIgnoreCase(currentUser.getEmployeeId());
        if (editingCurrentAccount) {
            currentUser.setName(name);
            currentUser.setUsername(username);
            currentUser.setRole(role);
            lblUserTag.setText(userTagText());
            lblAccountName.setText(name);
            lblAccountUsername.setText(username);
            lblAccountRole.setText(role);
        }
        refreshAllData();

        if (editingCurrentAccount && !oldRole.equalsIgnoreCase(role)) {
            JOptionPane.showMessageDialog(this,
                    "Your role was updated. Please log in again for the new permissions to take effect.",
                    "Credentials Updated", JOptionPane.INFORMATION_MESSAGE);
            new LoginFrame().setVisible(true);
            dispose();
            return;
        }

        JOptionPane.showMessageDialog(this, "Credentials for " + name + " were updated.",
                "Credentials Updated", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateEmployeePassword() {
        List<FileHelper.User> users = FileHelper.loadUsersFromFile();
        final FileHelper.User targetUser = selectEmployeeForAccountAction(
                "Select the employee whose password you want to update:", users);
        if (targetUser == null) {
            return;
        }

        PasswordFieldWithToggle newPassword = new PasswordFieldWithToggle(18);
        PasswordFieldWithToggle confirmPassword = new PasswordFieldWithToggle(18);
        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("New password:"));
        form.add(newPassword);
        form.add(new JLabel("Confirm password:"));
        form.add(confirmPassword);

        int result = JOptionPane.showConfirmDialog(
                this, form, "Update Employee Password",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String password = new String(newPassword.getPassword());
        String confirmation = new String(confirmPassword.getPassword());
        if (!isValidCsvField(password)) {
            JOptionPane.showMessageDialog(this,
                    "Password is required and cannot contain commas or line breaks.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!password.equals(confirmation)) {
            JOptionPane.showMessageDialog(this, "The passwords do not match.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        targetUser.setPassword(password);
        if (!FileHelper.saveUsersToFile(users)) {
            JOptionPane.showMessageDialog(this, "Could not save the updated password.",
                    "Save Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (targetUser.getEmployeeId().equalsIgnoreCase(currentUser.getEmployeeId())) {
            currentUser.setPassword(password);
        }
        JOptionPane.showMessageDialog(this,
                "Password for " + targetUser.getName() + " was updated.",
                "Password Updated", JOptionPane.INFORMATION_MESSAGE);
    }

    private void changeOwnPassword(PasswordFieldWithToggle currentPassword,
                                   PasswordFieldWithToggle newPassword,
                                   PasswordFieldWithToggle confirmPassword) {
        String oldValue = new String(currentPassword.getPassword());
        String newValue = new String(newPassword.getPassword());
        String confirmation = new String(confirmPassword.getPassword());

        if (oldValue.isEmpty() || newValue.isEmpty() || confirmation.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete all password fields.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!isValidCsvField(newValue)) {
            JOptionPane.showMessageDialog(this,
                    "Password cannot contain commas or line breaks.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!newValue.equals(confirmation)) {
            JOptionPane.showMessageDialog(this, "The new passwords do not match.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<FileHelper.User> users = FileHelper.loadUsersFromFile();
        FileHelper.User account = null;
        for (FileHelper.User user : users) {
            if (user.getEmployeeId().equalsIgnoreCase(currentUser.getEmployeeId())) {
                account = user;
                break;
            }
        }
        if (account == null || !account.getPassword().equals(oldValue)) {
            JOptionPane.showMessageDialog(this, "The current password is incorrect.",
                    "Password Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        account.setPassword(newValue);
        if (!FileHelper.saveUsersToFile(users)) {
            JOptionPane.showMessageDialog(this, "Could not save the new password.",
                    "Save Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        currentUser.setPassword(newValue);
        currentPassword.getPasswordField().setText("");
        newPassword.getPasswordField().setText("");
        confirmPassword.getPasswordField().setText("");
        JOptionPane.showMessageDialog(this, "Your password was updated.",
                "Password Updated", JOptionPane.INFORMATION_MESSAGE);
    }

    /*
    Adds a new employee and saves to users.txt.
    */
    private void addNewEmployee() {
        JTextField txtId = new JTextField(15);
        JTextField txtName = new JTextField(15);
        JTextField txtUsername = new JTextField(15);
        PasswordFieldWithToggle txtPass = new PasswordFieldWithToggle(15);
        JComboBox<String> cmbRole = new JComboBox<String>(new String[]{"Staff", "Supervisor", "Admin"});
        JTextField txtRate = new JTextField("200.00", 15);

        // Pre-fill next ID recommendation
        List<FileHelper.User> existing = FileHelper.loadUsersFromFile();
        txtId.setText(String.format("EMP%03d", existing.size() + 1));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Employee ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; form.add(txtId, gbc);

        gbc.gridx = 0; gbc.gridy = 1; form.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; form.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy = 2; form.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; form.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 3; form.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;         form.add(txtPass, gbc);

        gbc.gridx = 0; gbc.gridy = 4; form.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; form.add(cmbRole, gbc);

        gbc.gridx = 0; gbc.gridy = 5; form.add(new JLabel("Hourly Rate ($):"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; form.add(txtRate, gbc);

        int result = JOptionPane.showConfirmDialog(this, form, "Add New Employee", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String empId = txtId.getText().trim();
        String name = txtName.getText().trim();
        String username = txtUsername.getText().trim();
        String pass = new String(txtPass.getPassword());
        String role = (String) cmbRole.getSelectedItem();
        String rateStr = txtRate.getText().trim();

        if (!isValidCsvField(empId) || !isValidCsvField(name) || !isValidCsvField(username)
                || !isValidCsvField(pass) || rateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "All fields are required. Values cannot contain commas or line breaks.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check for duplicates
        for (FileHelper.User u : existing) {
            if (u.getEmployeeId().equalsIgnoreCase(empId)) {
                JOptionPane.showMessageDialog(this, "Employee ID already exists!", "Duplicate Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (u.getUsername().equalsIgnoreCase(username)) {
                JOptionPane.showMessageDialog(this, "Username already exists!", "Duplicate Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        double rate;
        try {
            rate = Double.parseDouble(rateStr);
            if (rate <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Hourly rate must be a valid positive number!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        FileHelper.User newUser = new FileHelper.User(empId, name, username, pass, role, rate);
        FileHelper.appendUser(newUser);
        refreshAllData();

        JOptionPane.showMessageDialog(this,
                "Employee " + name + " (" + empId + ") added successfully!",
                "Employee Added",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /*
    Adjusts the hourly rate of an employee.
    */
    private void adjustHourlyRate() {
        List<FileHelper.User> users = FileHelper.loadUsersFromFile();
        if (users.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No employees found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int selectedRow = tblEmployees.getSelectedRow();
        FileHelper.User targetUser = null;

        if (selectedRow >= 0) {
            String empId = (String) modelEmployees.getValueAt(selectedRow, 0);
            for (FileHelper.User u : users) {
                if (u.getEmployeeId().equalsIgnoreCase(empId)) {
                    targetUser = u;
                    break;
                }
            }
        }

        if (targetUser == null) {
            String[] userOptions = new String[users.size()];
            for (int i = 0; i < users.size(); i++) {
                FileHelper.User u = users.get(i);
                userOptions[i] = u.getEmployeeId() + " - " + u.getName() + " (Current: " + PayrollCalculator.formatCurrency(u.getHourlyRate()) + ")";
            }

            String selected = (String) JOptionPane.showInputDialog(
                    this,
                    "Select Employee to adjust rate:",
                    "Adjust Hourly Rate",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    userOptions,
                    userOptions[0]
            );
            if (selected == null) return;

            for (int i = 0; i < userOptions.length; i++) {
                if (userOptions[i].equals(selected)) {
                    targetUser = users.get(i);
                    break;
                }
            }
        }

        if (targetUser == null) return;

        String inputRate = JOptionPane.showInputDialog(
                this,
                "Employee: " + targetUser.getName() + " (" + targetUser.getEmployeeId() + ")\n" +
                        "Current Rate: " + PayrollCalculator.formatCurrency(targetUser.getHourlyRate()) + "\n\n" +
                        "Enter new hourly rate ($):",
                String.format("%.2f", targetUser.getHourlyRate())
        );

        if (inputRate == null || inputRate.trim().isEmpty()) return;

        try {
            double newRate = Double.parseDouble(inputRate.trim());
            if (newRate <= 0) throw new NumberFormatException();

            targetUser.setHourlyRate(newRate);
            if (!FileHelper.saveUsersToFile(users)) {
                JOptionPane.showMessageDialog(this, "Could not save the updated hourly rate.",
                        "Save Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            refreshAllData();

            JOptionPane.showMessageDialog(this,
                    "Hourly rate for " + targetUser.getName() + " updated to " + PayrollCalculator.formatCurrency(newRate) + "/hr!",
                    "Rate Updated",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid rate! Please enter a positive number.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generatePayStubForSelectedEmployee() {
        List<FileHelper.User> users = FileHelper.loadUsersFromFile();
        if (users.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No employees found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int selectedRow = tblEmployees.getSelectedRow();
        String selectedEmpId = null;

        if (selectedRow >= 0) {
            selectedEmpId = (String) modelEmployees.getValueAt(selectedRow, 0);
        } else {
            String[] userOptions = new String[users.size()];
            for (int i = 0; i < users.size(); i++) {
                FileHelper.User u = users.get(i);
                userOptions[i] = u.getEmployeeId() + " - " + u.getName();
            }

            String selected = (String) JOptionPane.showInputDialog(
                    this,
                    "Select employee to generate pay stub:",
                    "Generate Pay Stub",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    userOptions,
                    userOptions[0]
            );
            if (selected == null) return;

            for (int i = 0; i < userOptions.length; i++) {
                if (userOptions[i].equals(selected)) {
                    selectedEmpId = users.get(i).getEmployeeId();
                    break;
                }
            }
        }

        if (selectedEmpId != null) {
            exportPayStub(selectedEmpId);
        }
    }

    // 4. ONE-CLICK PAY STUB GENERATION & PRINT/PDF

    /**
    User-defined method: Generates a cleanly formatted pay stub, displays it
    in a preview dialog with Print/PDF capabilities, and saves a .txt backup in paystubs/.
    @param targetEmpId Employee ID whose pay stub is generated
    */
    public void exportPayStub(String targetEmpId) {
        List<FileHelper.User> users = FileHelper.loadUsersFromFile();
        FileHelper.User emp = null;
        for (FileHelper.User u : users) {
            if (u.getEmployeeId().equalsIgnoreCase(targetEmpId)) {
                emp = u;
                break;
            }
        }

        if (emp == null) {
            JOptionPane.showMessageDialog(this, "Employee record not found for ID: " + targetEmpId, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<FileHelper.TimeLog> logs = FileHelper.loadLogsFromFile();
        List<FileHelper.TimeLog> empLogs = new ArrayList<FileHelper.TimeLog>();
        double totalRegHours = 0.0;
        double totalOtHours = 0.0;
        double totalGrossPay = 0.0;

        for (FileHelper.TimeLog log : logs) {
            if (log.getEmployeeId().equalsIgnoreCase(targetEmpId) && !log.isPending()) {
                empLogs.add(log);
                totalRegHours += log.getRegularHours();
                totalOtHours += log.getOvertimeHours();
                totalGrossPay += log.getTotalPay();
            }
        }

        if (empLogs.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No completed shift records found for " + emp.getName() + " (" + emp.getEmployeeId() + ").\n" +
                            "Please clock out active shifts before generating pay stub.",
                    "No Shift Data",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nowStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        double regularPay = totalRegHours * emp.getHourlyRate();
        double overtimeRate = emp.getHourlyRate() * PayrollCalculator.OVERTIME_RATE_MULTIPLIER;
        double overtimePay = totalOtHours * overtimeRate;

        // Build cleanly formatted text receipt
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================================\n");
        sb.append("                       STAFF TIMEKEEPING & PAYROLL SYSTEM\n");
        sb.append("                               OFFICIAL PAY STUB\n");
        sb.append("========================================================================================\n");
        sb.append(String.format("Generated On:       %s\n", nowStr));
        sb.append(String.format("Employee ID:        %s\n", emp.getEmployeeId()));
        sb.append(String.format("Employee Name:      %s\n", emp.getName()));
        sb.append(String.format("Role / Position:    %s\n", emp.getRole()));
        sb.append(String.format("Base Hourly Rate:   %s / hr\n", PayrollCalculator.formatCurrency(emp.getHourlyRate())));
        sb.append(String.format("Overtime Rate (1.5x): %s / hr\n", PayrollCalculator.formatCurrency(overtimeRate)));
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format("%-12s %-20s %-20s %-10s %-10s %-12s\n",
                "Date", "Clock-In", "Clock-Out", "Reg Hrs", "OT Hrs", "Shift Total"));
        sb.append("----------------------------------------------------------------------------------------\n");

        for (FileHelper.TimeLog l : empLogs) {
            sb.append(String.format("%-12s %-20s %-20s %-10.2f %-10.2f %-12s\n",
                    l.getDate(),
                    l.getClockInTime(),
                    l.getClockOutTime(),
                    l.getRegularHours(),
                    l.getOvertimeHours(),
                    PayrollCalculator.formatCurrency(l.getTotalPay())));
        }

        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append("PAYROLL SUMMARY BREAKDOWN:\n");
        sb.append(String.format("  Total Completed Shifts:   %d\n", empLogs.size()));
        sb.append(String.format("  Total Regular Hours:      %-8.2f hrs  @ %s/hr   =  %s\n",
                totalRegHours, PayrollCalculator.formatCurrency(emp.getHourlyRate()), PayrollCalculator.formatCurrency(regularPay)));
        sb.append(String.format("  Total Overtime Hours:     %-8.2f hrs  @ %s/hr   =  %s\n",
                totalOtHours, PayrollCalculator.formatCurrency(overtimeRate), PayrollCalculator.formatCurrency(overtimePay)));
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format("  GROSS TOTAL PAYOUT:                                                 %s\n",
                PayrollCalculator.formatCurrency(totalGrossPay)));
        sb.append("========================================================================================\n");
        sb.append("                      Thank you for your service and dedication!\n");
        sb.append("========================================================================================\n");

        String payStubText = sb.toString();

        // Save .txt receipt backup in paystubs/ folder
        String savedFilePath = FileHelper.exportPayStub(emp.getEmployeeId(), payStubText);

        // Display Pay Stub Preview Dialog with Print / PDF Export
        showPayStubDialog(emp, payStubText, savedFilePath);
    }

    /*
    Displays pay stub dialog with built-in printing (PDF export) and receipt backup notification.
    */
    private void showPayStubDialog(final FileHelper.User emp, final String payStubText, final String savedFilePath) {
        final JDialog dialog = new JDialog(this, "Official Pay Stub - " + emp.getName(), true);
        dialog.setSize(760, 560);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.setBackground(new Color(248, 250, 252));

        final JTextArea txtReceipt = new JTextArea(payStubText);
        txtReceipt.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtReceipt.setEditable(false);
        txtReceipt.setBackground(Color.WHITE);
        txtReceipt.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(txtReceipt);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Controls
        JPanel bottomBar = new JPanel(new BorderLayout(10, 5));
        bottomBar.setOpaque(false);

        JLabel lblSavedInfo = new JLabel("Backup saved: " + (savedFilePath != null ? savedFilePath : "paystubs/"));
        lblSavedInfo.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblSavedInfo.setForeground(new Color(100, 116, 139));
        bottomBar.add(lblSavedInfo, BorderLayout.WEST);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setOpaque(false);

        JButton btnPrintPdf = new JButton("Print / Save as PDF");
        btnPrintPdf.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPrintPdf.setBackground(new Color(37, 99, 235));
        btnPrintPdf.setForeground(Color.WHITE);
        btnPrintPdf.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPrintPdf.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    MessageFormat header = new MessageFormat("Pay Stub - " + emp.getName() + " (" + emp.getEmployeeId() + ")");
                    MessageFormat footer = new MessageFormat("Page {0}");
                    boolean complete = txtReceipt.print(header, footer, true, null, null, true);
                    if (complete) {
                        JOptionPane.showMessageDialog(dialog,
                                "Pay stub print/PDF job sent successfully!",
                                "Print Success",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (PrinterException pe) {
                    JOptionPane.showMessageDialog(dialog,
                            "Printing error: " + pe.getMessage(),
                            "Print Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton btnClose = new JButton("Close");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        buttonsPanel.add(btnPrintPdf);
        buttonsPanel.add(btnClose);
        bottomBar.add(buttonsPanel, BorderLayout.EAST);

        panel.add(bottomBar, BorderLayout.SOUTH);

        dialog.setContentPane(panel);

        // Notify user about generation and backup
        JOptionPane.showMessageDialog(this,
                "Pay stub successfully generated!\nBackup receipt saved to: " + savedFilePath,
                "Pay Stub Generated",
                JOptionPane.INFORMATION_MESSAGE);

        dialog.setVisible(true);
    }
}
