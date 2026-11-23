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
 * SupervisorPanel - Team shift log overview, missed punch resolution, and manual shift entry.
 */
public class SupervisorPanel extends JPanel {
    private final DashboardFrame parentDashboard;
    private final DefaultTableModel tableModel;
    private final JTable table;

    public SupervisorPanel(DashboardFrame parentDashboard) {
        this.parentDashboard = parentDashboard;
        setLayout(new BorderLayout(10, 10));
        setBackground(UIHelper.CARD_BG);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.setOpaque(false);

        JButton btnResolve = UIHelper.createButton("Resolve Missed Clock-Out", UIHelper.WARNING, Color.WHITE, e -> resolveMissedClockOut());
        JButton btnManual = UIHelper.createButton("Add Manual Shift Entry", UIHelper.PURPLE, Color.WHITE, e -> addManualShift());
        JButton btnRefresh = UIHelper.createButton("Refresh Logs", new Color(148, 163, 184), Color.WHITE, e -> refreshData());

        toolbar.add(btnResolve);
        toolbar.add(btnManual);
        toolbar.add(btnRefresh);
        add(toolbar, BorderLayout.NORTH);

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
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        for (TimeLog l : FileDao.loadLogs()) {
            tableModel.addRow(new Object[]{
                    l.getEmployeeId(), l.getName(), l.getDate(), l.getClockInTime(), l.getClockOutTime(),
                    UIHelper.hours(l.getRegularHours()), UIHelper.hours(l.getOvertimeHours()),
                    UIHelper.currency(l.getTotalPay())
            });
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
