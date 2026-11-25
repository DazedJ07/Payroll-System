package com.payroll.ui;

import com.payroll.dao.FileDao;
import com.payroll.model.TimeLog;
import com.payroll.model.User;
import com.payroll.service.PayrollService;
import com.payroll.util.UIHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 TimekeepingPanel
 */
public class TimekeepingPanel extends JPanel {
    private final User user;
    private final DashboardFrame parentDashboard;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JLabel lblSummary;
    private final JButton btnClockIn;
    private final JButton btnClockOut;

    public TimekeepingPanel(User user, DashboardFrame parentDashboard) {
        this.user = user;
        this.parentDashboard = parentDashboard;
        setLayout(new BorderLayout(10, 10));
        setBackground(UIHelper.CARD_BG);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Action Toolbar
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        bar.setOpaque(false);

        btnClockIn = UIHelper.createButton("Clock In", UIHelper.SUCCESS, Color.WHITE, e -> handleClockIn());
        btnClockOut = UIHelper.createButton("Clock Out", UIHelper.DANGER, Color.WHITE, e -> handleClockOut());
        JButton btnPayStub = UIHelper.createButton("Generate My Pay Stub", UIHelper.PRIMARY, Color.WHITE, e -> handleGeneratePayStub());
        JButton btnRefresh = UIHelper.createButton("Refresh", new Color(148, 163, 184), Color.WHITE, e -> refreshData());

        bar.add(btnClockIn);
        bar.add(btnClockOut);
        bar.add(Box.createHorizontalStrut(12));
        bar.add(btnPayStub);
        bar.add(btnRefresh);
        add(bar, BorderLayout.NORTH);

        // Logs Table
        String[] cols = {"Date", "Clock In Time", "Clock Out Time", "Regular Hours", "Overtime Hours", "Shift Total Pay"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIHelper.styleTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom Summary Bar
        lblSummary = new JLabel("Summary: Initializing...");
        lblSummary.setFont(UIHelper.FONT_BOLD);
        lblSummary.setForeground(UIHelper.TEXT_DARK);
        lblSummary.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));
        add(lblSummary, BorderLayout.SOUTH);
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        List<TimeLog> logs = FileDao.loadLogs();
        double totalReg = 0.0, totalOt = 0.0, totalPay = 0.0;
        int shifts = 0;
        boolean clockedIn = false;
        String clockInTime = "";

        for (TimeLog l : logs) {
            if (l.getEmployeeId().equalsIgnoreCase(user.getEmployeeId())) {
                tableModel.addRow(new Object[]{
                        l.getDate(), l.getClockInTime(), l.getClockOutTime(),
                        UIHelper.hours(l.getRegularHours()), UIHelper.hours(l.getOvertimeHours()),
                        UIHelper.currency(l.getTotalPay())
                });
                if (l.isPending()) {
                    clockedIn = true;
                    clockInTime = l.getClockInTime();
                } else {
                    shifts++;
                    totalReg += l.getRegularHours();
                    totalOt += l.getOvertimeHours();
                    totalPay += l.getTotalPay();
                }
            }
        }

        btnClockIn.setEnabled(!clockedIn);
        btnClockOut.setEnabled(clockedIn);
        lblSummary.setText(String.format("Personal Summary: Shifts: %d | Regular: %.2f hrs | Overtime: %.2f hrs | Total Pay: %s",
                shifts, totalReg, totalOt, UIHelper.currency(totalPay)));
        parentDashboard.updateStatusBadge(clockedIn, clockInTime);
    }

    private void handleClockIn() {
        String res = PayrollService.clockIn(user);
        if (res.startsWith("ALREADY_CLOCKED_IN:")) {
            UIHelper.showWarning(this, "You are already clocked in since " + res.substring(19), "Clock In Warning");
        } else {
            UIHelper.showInfo(this, "Clock In recorded successfully!\nTimestamp: " + res, "Clocked In");
            parentDashboard.refreshAll();
        }
    }

    private void handleClockOut() {
        TimeLog log = PayrollService.clockOut(user);
        if (log == null) {
            UIHelper.showWarning(this, "No active clock-in found. Please clock in first.", "Clock Out Warning");
            return;
        }
        parentDashboard.refreshAll();
        String msg = String.format("Clock Out successful!\nClock In: %s\nClock Out: %s\nRegular: %.2f hrs | Overtime: %.2f hrs\nShift Pay: %s",
                log.getClockInTime(), log.getClockOutTime(), log.getRegularHours(), log.getOvertimeHours(), UIHelper.currency(log.getTotalPay()));
        UIHelper.showInfo(this, msg, "Shift Completed");
    }

    private void handleGeneratePayStub() {
        String[] result = PayrollService.generatePayStub(user);
        if (result == null) {
            UIHelper.showWarning(this, "No completed shifts available to generate a pay stub.", "No Data");
            return;
        }
        new PayStubDialog(parentDashboard, user.getName(), user.getEmployeeId(), result[0], result[1]).setVisible(true);
    }
}
