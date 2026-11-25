package com.payroll.ui;

import com.payroll.dao.FileDao;
import com.payroll.model.User;
import com.payroll.service.PayrollService;
import com.payroll.util.UIHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 AdminPanel
 */
public class AdminPanel extends JPanel {
    private final DashboardFrame parentDashboard;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JButton btnAddEmployee;
    private final JButton btnAdjustRate;
    private final JButton btnGeneratePayStub;
    private final JButton btnEditCredentials;
    private final JButton btnResetPassword;
    private final JButton btnArchive;
    private final JButton btnDelete;
    private final JButton btnRestore;
    private final JButton btnToggleArchive;
    private boolean archivedView;

    public AdminPanel(DashboardFrame parentDashboard) {
        this.parentDashboard = parentDashboard;
        setLayout(new BorderLayout(10, 10));
        setBackground(UIHelper.CARD_BG);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel toolbars = new JPanel(new GridLayout(3, 1, 0, 4));
        toolbars.setOpaque(false);

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        row1.setOpaque(false);
        btnAddEmployee = UIHelper.createButton("+ Add Employee", UIHelper.SUCCESS, Color.WHITE, e -> addEmployee());
        btnAdjustRate = UIHelper.createButton("Adjust Rate", new Color(14, 165, 233), Color.WHITE, e -> adjustRate());
        btnGeneratePayStub = UIHelper.createButton("Generate Pay Stub", UIHelper.PURPLE, Color.WHITE, e -> generatePayStub());
        row1.add(btnAddEmployee);
        row1.add(btnAdjustRate);
        row1.add(btnGeneratePayStub);
        row1.add(UIHelper.createButton("Refresh", new Color(148, 163, 184), Color.WHITE, e -> refreshData()));

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        row2.setOpaque(false);
        btnEditCredentials = UIHelper.createButton("Edit Credentials", UIHelper.PRIMARY, Color.WHITE, e -> editCredentials());
        btnResetPassword = UIHelper.createButton("Reset Password", UIHelper.WARNING, Color.WHITE, e -> resetPassword());
        row2.add(btnEditCredentials);
        row2.add(btnResetPassword);

        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        row3.setOpaque(false);
        btnArchive = UIHelper.createButton("Archive Employee", UIHelper.WARNING, Color.WHITE, e -> archiveSelectedEmployee());
        btnDelete = UIHelper.createButton("Delete Employee", UIHelper.DANGER, Color.WHITE, e -> deleteSelectedEmployee());
        btnRestore = UIHelper.createButton("Restore Employee", UIHelper.SUCCESS, Color.WHITE, e -> restoreSelectedEmployee());
        btnToggleArchive = UIHelper.createButton("View Archived", new Color(100, 116, 139), Color.WHITE, e -> toggleArchivedView());
        row3.add(btnArchive);
        row3.add(btnDelete);
        row3.add(btnRestore);
        row3.add(btnToggleArchive);

        toolbars.add(row1);
        toolbars.add(row2);
        toolbars.add(row3);
        add(toolbars, BorderLayout.NORTH);

        String[] cols = {"Employee ID", "Full Name", "Username", "Role", "Hourly Rate ($/hr)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIHelper.styleTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JLabel lblHint = new JLabel("Select an employee row to edit credentials, reset password, or adjust pay rate.");
        lblHint.setFont(UIHelper.FONT_SMALL);
        lblHint.setForeground(UIHelper.TEXT_MUTED);
        add(lblHint, BorderLayout.SOUTH);
        updateActionAvailability();
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        List<User> users = archivedView ? FileDao.loadArchivedUsers() : FileDao.loadUsers();
        for (User u : users) {
            tableModel.addRow(new Object[]{
                    u.getEmployeeId(), u.getName(), u.getUsername(), u.getRole(), UIHelper.currency(u.getHourlyRate())
            });
        }
        updateActionAvailability();
    }

    private void updateActionAvailability() {
        btnAddEmployee.setEnabled(!archivedView);
        btnAdjustRate.setEnabled(!archivedView);
        btnGeneratePayStub.setEnabled(!archivedView);
        btnEditCredentials.setEnabled(!archivedView);
        btnResetPassword.setEnabled(!archivedView);
        btnArchive.setEnabled(!archivedView);
        btnRestore.setEnabled(archivedView);
        btnToggleArchive.setText(archivedView ? "View Active" : "View Archived");
    }

    private void toggleArchivedView() {
        archivedView = !archivedView;
        refreshData();
    }

    private String getSelectedEmployeeId() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIHelper.showWarning(this, "Select an employee row first.", "No Employee Selected");
            return null;
        }
        return (String) tableModel.getValueAt(row, 0);
    }

    private void archiveSelectedEmployee() {
        String employeeId = getSelectedEmployeeId();
        if (employeeId == null) return;
        String name = (String) tableModel.getValueAt(table.getSelectedRow(), 1);
        if (!UIHelper.confirm(this, "Archive " + name + "? The account will be disabled, but its shift logs will be kept.",
                "Confirm Archive")) return;
        boolean archived = FileDao.archiveUser(employeeId);
        parentDashboard.refreshAll();
        if (!archived) {
            UIHelper.showError(this, "Could not archive the selected employee.", "Archive Failed");
        }
    }

    private void deleteSelectedEmployee() {
        String employeeId = getSelectedEmployeeId();
        if (employeeId == null) return;
        String name = (String) tableModel.getValueAt(table.getSelectedRow(), 1);
        if (!UIHelper.confirm(this, "Permanently delete " + name + "? Associated shift logs will be retained.",
                "Confirm Delete")) return;
        boolean deleted = FileDao.deleteUser(employeeId);
        parentDashboard.refreshAll();
        if (!deleted) {
            UIHelper.showError(this, "Could not delete the selected employee.", "Delete Failed");
        }
    }

    private void restoreSelectedEmployee() {
        String employeeId = getSelectedEmployeeId();
        if (employeeId == null) return;
        boolean restored = FileDao.restoreUser(employeeId);
        parentDashboard.refreshAll();
        if (!restored) {
            UIHelper.showError(this, "Could not restore the selected employee. Check for an active account with the same ID or username.",
                    "Restore Failed");
        }
    }

    private User getSelectedUser() {
        if (archivedView) {
            UIHelper.showWarning(this, "Switch to active employees to edit or generate a pay stub.", "Archived Employee");
            return null;
        }
        int row = table.getSelectedRow();
        if (row >= 0) {
            String empId = (String) tableModel.getValueAt(row, 0);
            return PayrollService.findUserById(empId);
        }
        List<User> list = FileDao.loadUsers();
        if (list.isEmpty()) return null;
        String[] opts = new String[list.size()];
        for (int i = 0; i < list.size(); i++) opts[i] = list.get(i).toString();
        String sel = (String) JOptionPane.showInputDialog(this, "Select Employee:", "Employee",
                JOptionPane.QUESTION_MESSAGE, null, opts, opts[0]);
        if (sel == null) return null;
        for (User u : list) if (sel.startsWith(u.getEmployeeId())) return u;
        return null;
    }

    private void addEmployee() {
        List<User> users = FileDao.loadUsers();
        JTextField txtId = UIHelper.createField(15);
        txtId.setText(nextEmployeeId(users));
        JTextField txtName = UIHelper.createField(15);
        JTextField txtUser = UIHelper.createField(15);
        JPasswordField[] pfHolder = new JPasswordField[1];
        JPanel passPanel = UIHelper.createPasswordPanel(15, pfHolder);
        JComboBox<String> cmbRole = new JComboBox<String>(new String[]{"Staff", "Supervisor", "Admin"});
        JTextField txtRate = UIHelper.createField(15);
        txtRate.setText("200.00");

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = UIHelper.insets(4, 5, 4, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        UIHelper.addGridRow(form, gbc, 0, "Employee ID:", txtId);
        UIHelper.addGridRow(form, gbc, 1, "Full Name:", txtName);
        UIHelper.addGridRow(form, gbc, 2, "Username:", txtUser);
        UIHelper.addGridRow(form, gbc, 3, "Password:", passPanel);
        UIHelper.addGridRow(form, gbc, 4, "Role:", cmbRole);
        UIHelper.addGridRow(form, gbc, 5, "Hourly Rate ($):", txtRate);

        if (JOptionPane.showConfirmDialog(this, form, "Add New Employee", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        String id = txtId.getText().trim(), name = txtName.getText().trim(), user = txtUser.getText().trim();
        String pass = new String(pfHolder[0].getPassword()), role = (String) cmbRole.getSelectedItem();
        if (!UIHelper.isValidCsv(id) || !UIHelper.isValidCsv(name) || !UIHelper.isValidCsv(user) || !UIHelper.isValidCsv(pass)) {
            UIHelper.showError(this, "All fields required without commas or linebreaks.", "Validation Error");
            return;
        }
        try {
            double rate = Double.parseDouble(txtRate.getText().trim());
            if (rate <= 0) throw new Exception();
            if (PayrollService.addUser(new User(id, name, user, pass, role, rate))) {
                UIHelper.showInfo(this, "Employee " + name + " added successfully!", "Success");
                parentDashboard.refreshAll();
            } else {
                UIHelper.showError(this, "Employee ID or Username already exists!", "Duplicate Error");
            }
        } catch (Exception ex) {
            UIHelper.showError(this, "Please enter a valid positive hourly rate.", "Input Error");
        }
    }

    private String nextEmployeeId(List<User> activeUsers) {
        List<User> allUsers = new ArrayList<User>(activeUsers);
        allUsers.addAll(FileDao.loadArchivedUsers());
        int nextId = 1;
        while (containsEmployeeId(allUsers, String.format("EMP%03d", nextId))) {
            nextId++;
        }
        return String.format("EMP%03d", nextId);
    }

    private boolean containsEmployeeId(List<User> users, String employeeId) {
        for (User user : users) {
            if (user.getEmployeeId().equalsIgnoreCase(employeeId)) return true;
        }
        return false;
    }

    private void editCredentials() {
        User target = getSelectedUser();
        if (target == null) return;
        JTextField txtName = new JTextField(target.getName(), 15);
        JTextField txtUser = new JTextField(target.getUsername(), 15);
        JComboBox<String> cmbRole = new JComboBox<String>(new String[]{"Staff", "Supervisor", "Admin"});
        cmbRole.setSelectedItem(target.getRole());

        JPanel form = new JPanel(new GridLayout(3, 2, 6, 6));
        form.add(new JLabel("Full Name:")); form.add(txtName);
        form.add(new JLabel("Username:"));  form.add(txtUser);
        form.add(new JLabel("Role:"));      form.add(cmbRole);

        if (JOptionPane.showConfirmDialog(this, form, "Edit Credentials", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String name = txtName.getText().trim(), user = txtUser.getText().trim(), role = (String) cmbRole.getSelectedItem();
            if (!UIHelper.isValidCsv(name) || !UIHelper.isValidCsv(user)) {
                UIHelper.showError(this, "Name and username are required.", "Error"); return;
            }
            target.setName(name); target.setUsername(user); target.setRole(role);
            PayrollService.updateUser(target);
            UIHelper.showInfo(this, "Credentials updated for " + name, "Success");
            parentDashboard.refreshAll();
        }
    }

    private void resetPassword() {
        User target = getSelectedUser();
        if (target == null) return;
        JPasswordField[] pfHolder = new JPasswordField[1];
        JPanel passPanel = UIHelper.createPasswordPanel(15, pfHolder);

        if (JOptionPane.showConfirmDialog(this, passPanel, "Reset Password for " + target.getName(), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String pass = new String(pfHolder[0].getPassword());
            if (!UIHelper.isValidCsv(pass)) {
                UIHelper.showError(this, "Valid password required without commas.", "Error"); return;
            }
            target.setPassword(pass);
            PayrollService.updateUser(target);
            UIHelper.showInfo(this, "Password updated successfully for " + target.getName(), "Success");
        }
    }

    private void adjustRate() {
        User target = getSelectedUser();
        if (target == null) return;
        String input = JOptionPane.showInputDialog(this, "Current: " + UIHelper.currency(target.getHourlyRate()) + "\nEnter new rate ($):", target.getHourlyRate());
        if (input == null || input.trim().isEmpty()) return;
        try {
            double rate = Double.parseDouble(input.trim());
            if (rate <= 0) throw new Exception();
            target.setHourlyRate(rate);
            PayrollService.updateUser(target);
            UIHelper.showInfo(this, "Hourly rate updated to " + UIHelper.currency(rate) + "/hr", "Rate Updated");
            parentDashboard.refreshAll();
        } catch (Exception e) {
            UIHelper.showError(this, "Please enter a valid positive number.", "Input Error");
        }
    }

    private void generatePayStub() {
        User target = getSelectedUser();
        if (target == null) return;
        String[] result = PayrollService.generatePayStub(target);
        if (result == null) {
            UIHelper.showWarning(this, "No completed shifts found for " + target.getName(), "No Data");
            return;
        }
        new PayStubDialog(parentDashboard, target.getName(), target.getEmployeeId(), result[0], result[1]).setVisible(true);
    }
}
