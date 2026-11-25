package com.payroll.ui;

import com.payroll.dao.FileDao;
import com.payroll.model.TimeLog;
import com.payroll.model.User;
import com.payroll.service.PayrollService;
import com.payroll.util.UIHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 SupervisorPanel
 */
public class SupervisorPanel extends JPanel {
    private final DashboardFrame parentDashboard;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JButton btnResolve;
    private final JButton btnManual;
    private final JButton btnArchive;
    private final JButton btnDelete;
    private final JButton btnRestore;
    private final JButton btnToggleArchive;
    private boolean archivedView;

    public SupervisorPanel(DashboardFrame parentDashboard) {
        this.parentDashboard = parentDashboard;
        setLayout(new BorderLayout(10, 10));
        setBackground(UIHelper.CARD_BG);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel toolbars = new JPanel(new GridLayout(2, 1, 0, 4));
        toolbars.setOpaque(false);

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        row1.setOpaque(false);
        btnResolve = UIHelper.createButton("Resolve Missed Clock-Out", UIHelper.WARNING, Color.WHITE, e -> resolveMissedClockOut());
        btnManual = UIHelper.createButton("Add Manual Shift Entry", UIHelper.PURPLE, Color.WHITE, e -> addManualShift());
        JButton btnRefresh = UIHelper.createButton("Refresh Logs", new Color(148, 163, 184), Color.WHITE, e -> refreshData());
        row1.add(btnResolve);
        row1.add(btnManual);
        row1.add(btnRefresh);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        row2.setOpaque(false);
        btnArchive = UIHelper.createButton("Archive Selected Shift", UIHelper.WARNING, Color.WHITE, e -> archiveSelectedShift());
        btnDelete = UIHelper.createButton("Delete Selected Shift", UIHelper.DANGER, Color.WHITE, e -> deleteSelectedShift());
        btnRestore = UIHelper.createButton("Restore Selected Shift", UIHelper.SUCCESS, Color.WHITE, e -> restoreSelectedShift());
        btnToggleArchive = UIHelper.createButton("View Archived", new Color(100, 116, 139), Color.WHITE, e -> toggleArchivedView());
        row2.add(btnArchive);
        row2.add(btnDelete);
        row2.add(btnRestore);
        row2.add(btnToggleArchive);

        toolbars.add(row1);
        toolbars.add(row2);
        add(toolbars, BorderLayout.NORTH);

        String[] cols = {"Emp ID", "Employee Name", "Date", "Clock In Time", "Clock Out Time", "Reg Hrs", "OT Hrs", "Total Pay"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIHelper.styleTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JLabel lblHint = new JLabel("Tip: Select a shift with 'PENDING' clock-out and click 'Resolve Missed Clock-Out' to complete it.");
        lblHint.setFont(UIHelper.FONT_SMALL);
        lblHint.setForeground(UIHelper.TEXT_MUTED);
        add(lblHint, BorderLayout.SOUTH);
        updateActionAvailability();
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        List<TimeLog> logs = archivedView ? FileDao.loadArchivedLogs() : FileDao.loadLogs();
        for (TimeLog l : logs) {
            tableModel.addRow(new Object[]{
                    l.getEmployeeId(), l.getName(), l.getDate(), l.getClockInTime(), l.getClockOutTime(),
                    UIHelper.hours(l.getRegularHours()), UIHelper.hours(l.getOvertimeHours()),
                    UIHelper.currency(l.getTotalPay())
            });
        }
        updateActionAvailability();
    }

    private void updateActionAvailability() {
        btnResolve.setEnabled(!archivedView);
        btnManual.setEnabled(!archivedView);
        btnArchive.setEnabled(!archivedView);
        btnRestore.setEnabled(archivedView);
        btnToggleArchive.setText(archivedView ? "View Active" : "View Archived");
    }

    private void toggleArchivedView() {
        archivedView = !archivedView;
        refreshData();
    }

    private TimeLog getSelectedLog() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIHelper.showWarning(this, "Select a shift row first.", "No Shift Selected");
            return null;
        }
        String employeeId = (String) tableModel.getValueAt(row, 0);
        String clockInTime = (String) tableModel.getValueAt(row, 3);
        List<TimeLog> logs = archivedView ? FileDao.loadArchivedLogs() : FileDao.loadLogs();
        for (TimeLog log : logs) {
            if (log.getEmployeeId().equalsIgnoreCase(employeeId)
                    && log.getClockInTime().equals(clockInTime)) return log;
        }
        UIHelper.showError(this, "The selected shift could not be found. Refresh the table and try again.",
                "Shift Not Found");
        return null;
    }

    private void archiveSelectedShift() {
        TimeLog target = getSelectedLog();
        if (target == null) return;
        if (target.isPending()) {
            UIHelper.showWarning(this, "A pending clock-in cannot be archived until it is resolved or deleted.",
                    "Pending Shift");
            return;
        }
        if (!UIHelper.confirm(this, "Archive the shift for " + target.getName() + " on " + target.getDate()
                + "? It will be removed from active logs and payroll.", "Confirm Archive")) return;
        boolean archived = FileDao.archiveLog(target.getEmployeeId(), target.getClockInTime());
        parentDashboard.refreshAll();
        if (!archived) {
            UIHelper.showError(this, "Could not archive the selected shift.", "Archive Failed");
        }
    }

    private void deleteSelectedShift() {
        TimeLog target = getSelectedLog();
        if (target == null) return;
        if (!UIHelper.confirm(this, "Permanently delete the shift for " + target.getName() + " on "
                + target.getDate() + "?", "Confirm Delete")) return;
        boolean deleted = FileDao.deleteLog(target.getEmployeeId(), target.getClockInTime());
        parentDashboard.refreshAll();
        if (!deleted) {
            UIHelper.showError(this, "Could not delete the selected shift.", "Delete Failed");
        }
    }

    private void restoreSelectedShift() {
        TimeLog target = getSelectedLog();
        if (target == null) return;
        boolean restored = FileDao.restoreLog(target.getEmployeeId(), target.getClockInTime());
        parentDashboard.refreshAll();
        if (!restored) {
            UIHelper.showError(this, "Could not restore the selected shift.", "Restore Failed");
        }
    }

    private void resolveMissedClockOut() {
        List<TimeLog> logs = FileDao.loadLogs();
        TimeLog target = null;
        int row = table.getSelectedRow();
        if (row >= 0) {
            String empId = (String) tableModel.getValueAt(row, 0);
            String clockIn = (String) tableModel.getValueAt(row, 3);
            for (TimeLog l : logs) {
                if (l.getEmployeeId().equalsIgnoreCase(empId) && l.getClockInTime().equalsIgnoreCase(clockIn)) {
                    target = l; break;
                }
            }
        }

        if (target == null || !target.isPending()) {
            List<TimeLog> pending = new ArrayList<TimeLog>();
            for (TimeLog l : logs) if (l.isPending()) pending.add(l);
            if (pending.isEmpty()) {
                UIHelper.showInfo(this, "There are currently no pending clock-ins to resolve.", "No Pending Shifts");
                return;
            }
            String[] opts = new String[pending.size()];
            for (int i = 0; i < pending.size(); i++) {
                opts[i] = pending.get(i).getEmployeeId() + " - " + pending.get(i).getName() + " (" + pending.get(i).getClockInTime() + ")";
            }
            String sel = (String) JOptionPane.showInputDialog(this, "Select a pending shift:", "Resolve Shift",
                    JOptionPane.QUESTION_MESSAGE, null, opts, opts[0]);
            if (sel == null) return;
            for (int i = 0; i < opts.length; i++) if (opts[i].equals(sel)) { target = pending.get(i); break; }
        }
        if (target == null) return;

        String defOut = new SimpleDateFormat(PayrollService.DATE_TIME_FORMAT).format(new Date());
        String outTime = JOptionPane.showInputDialog(this, "Clock In was: " + target.getClockInTime() + "\nEnter Clock-Out (yyyy-MM-dd HH:mm:ss):", defOut);
        if (outTime == null || outTime.trim().isEmpty()) return;

        User emp = PayrollService.findUserById(target.getEmployeeId());
        double rate = emp != null ? emp.getHourlyRate() : 200.0;
        if (PayrollService.resolvePendingShift(target, outTime.trim(), rate)) {
            UIHelper.showInfo(this, "Missed clock-out resolved successfully!", "Success");
            parentDashboard.refreshAll();
        } else {
            UIHelper.showError(this, "Invalid timestamp or clock-out is before clock-in.", "Resolution Error");
        }
    }

    private void addManualShift() {
        List<User> users = FileDao.loadUsers();
        if (users.isEmpty()) return;
        String[] opts = new String[users.size()];
        for (int i = 0; i < users.size(); i++) opts[i] = users.get(i).getEmployeeId() + " - " + users.get(i).getName();

        String sel = (String) JOptionPane.showInputDialog(this, "Select Employee:", "Manual Shift",
                JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
        if (sel == null) return;

        User emp = users.get(0);
        for (User u : users) if (sel.startsWith(u.getEmployeeId())) { emp = u; break; }

        String today = new SimpleDateFormat(PayrollService.DATE_FORMAT).format(new Date());
        JTextField txtIn = UIHelper.createField(16);
        JTextField txtOut = UIHelper.createField(16);
        txtIn.setText(today + " 08:00:00");
        txtOut.setText(today + " 17:00:00");

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = UIHelper.insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        UIHelper.addGridRow(form, gbc, 0, "Clock-In:", txtIn);
        UIHelper.addGridRow(form, gbc, 1, "Clock-Out:", txtOut);

        if (JOptionPane.showConfirmDialog(this, form, "Enter Shift for " + emp.getName(), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            if (PayrollService.addManualShift(emp, txtIn.getText().trim(), txtOut.getText().trim())) {
                UIHelper.showInfo(this, "Manual shift saved successfully!", "Shift Saved");
                parentDashboard.refreshAll();
            } else {
                UIHelper.showError(this, "Invalid timestamps! Please check format (yyyy-MM-dd HH:mm:ss).", "Input Error");
            }
        }
    }
}
