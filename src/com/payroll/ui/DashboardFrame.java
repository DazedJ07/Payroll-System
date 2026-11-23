package com.payroll.ui;

import com.payroll.model.User;
import com.payroll.service.PayrollService;
import com.payroll.util.UIHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.Timer;

/**
 * DashboardFrame - Modern application frame hosting role-based operational tabs.
 */
public class DashboardFrame extends JFrame {
    private final User currentUser;
    private final JLabel lblClock;
    private final JLabel lblStatus;
    private final JLabel lblUserTag;
    private final TimekeepingPanel timekeepingPanel;
    private SupervisorPanel supervisorPanel;
    private AdminPanel adminPanel;

    public DashboardFrame(User user) {
        this.currentUser = user;
        setTitle("Staff Timekeeping & Payroll System - [" + user.getRole().toUpperCase() + " DASHBOARD]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setMinimumSize(new Dimension(860, 580));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(UIHelper.BG_LIGHT);
        root.setBorder(BorderFactory.createEmptyBorder(12, 14, 14, 14));

        // Top Modern Header Bar
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(UIHelper.HEADER_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JLabel lblTitle = new JLabel("Timekeeping & Payroll");
        lblTitle.setFont(UIHelper.FONT_TITLE);
        lblTitle.setForeground(Color.WHITE);
        lblUserTag = new JLabel(getUserTagText());
        lblUserTag.setFont(UIHelper.FONT_PLAIN);
        lblUserTag.setForeground(new Color(203, 213, 225));
        left.add(lblTitle);
        left.add(lblUserTag);
        header.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        lblClock = new JLabel();
        lblClock.setFont(UIHelper.FONT_MONO);
        lblClock.setForeground(new Color(254, 240, 138));

        lblStatus = new JLabel("Status: Initializing");
        lblStatus.setFont(UIHelper.FONT_BOLD);
        lblStatus.setForeground(Color.WHITE);
        lblStatus.setOpaque(true);
        lblStatus.setBackground(new Color(71, 85, 105));
        lblStatus.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JButton btnLogout = UIHelper.createButton("Logout", UIHelper.DANGER, Color.WHITE, e -> {
            if (UIHelper.confirm(this, "Are you sure you want to log out?", "Confirm Logout")) {
                new LoginFrame().setVisible(true);
                dispose();
            }
        });

        right.add(lblClock);
        right.add(lblStatus);
        right.add(btnLogout);
        header.add(right, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // Center Role-Based Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIHelper.FONT_BOLD);

        timekeepingPanel = new TimekeepingPanel(currentUser, this);
        tabs.addTab("My Timekeeping", timekeepingPanel);
        tabs.addTab("Account Details", new AccountPanel(currentUser, this));

        if (currentUser.hasSupervisorAccess()) {
            supervisorPanel = new SupervisorPanel(this);
            tabs.addTab("Team Overview & Logs", supervisorPanel);
        }
        if (currentUser.isAdmin()) {
            adminPanel = new AdminPanel(this);
            tabs.addTab("Employee & Payroll Admin", adminPanel);
        }

        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);

        // Live Clock Timer
        new Timer(1000, e -> lblClock.setText(new SimpleDateFormat(PayrollService.DATE_TIME_FORMAT).format(new Date()))).start();
        refreshAll();
    }

    public void updateStatusBadge(boolean clockedIn, String clockInTime) {
        if (clockedIn) {
            lblStatus.setText("Status: Clocked In (" + (clockInTime.length() > 11 ? clockInTime.substring(11) : clockInTime) + ")");
            lblStatus.setBackground(UIHelper.SUCCESS);
        } else {
            lblStatus.setText("Status: Clocked Out");
            lblStatus.setBackground(new Color(100, 116, 139));
        }
    }

    public void refreshAll() {
        lblUserTag.setText(getUserTagText());
        if (timekeepingPanel != null) timekeepingPanel.refreshData();
        if (supervisorPanel != null) supervisorPanel.refreshData();
        if (adminPanel != null) adminPanel.refreshData();
    }

    private String getUserTagText() {
        return " |  " + currentUser.getName() + " (" + currentUser.getEmployeeId() + ") - "
                + currentUser.getRole() + "  |  Rate: " + UIHelper.currency(currentUser.getHourlyRate()) + "/hr";
    }
}
