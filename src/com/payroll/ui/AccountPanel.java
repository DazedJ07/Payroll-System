package com.payroll.ui;

import com.payroll.model.User;
import com.payroll.service.PayrollService;
import com.payroll.util.UIHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

/**
 * AccountPanel - User profile details viewer and self-service password changer.
 */
public class AccountPanel extends JPanel {
    private final User user;
    private final DashboardFrame parentDashboard;

    public AccountPanel(User user, DashboardFrame parentDashboard) {
        this.user = user;
        this.parentDashboard = parentDashboard;
        setLayout(new BorderLayout(15, 15));
        setBackground(UIHelper.CARD_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Profile Details Card
        JPanel profileCard = new JPanel(new GridLayout(0, 2, 10, 8));
        profileCard.setOpaque(false);
        profileCard.setBorder(BorderFactory.createTitledBorder("Your Profile"));
        addProfileRow(profileCard, "Full Name:", user.getName());
        addProfileRow(profileCard, "Employee ID:", user.getEmployeeId());
        addProfileRow(profileCard, "Username:", user.getUsername());
        addProfileRow(profileCard, "Role:", user.getRole());
        addProfileRow(profileCard, "Base Hourly Rate:", UIHelper.currency(user.getHourlyRate()) + " / hr");
        add(profileCard, BorderLayout.NORTH);

        // Change Password Card
        JPanel passCard = new JPanel(new GridBagLayout());
        passCard.setOpaque(false);
        passCard.setBorder(BorderFactory.createTitledBorder("Change Password"));

        JPasswordField[] curHolder = new JPasswordField[1];
        JPasswordField[] newHolder = new JPasswordField[1];
        JPasswordField[] confHolder = new JPasswordField[1];
        JPanel curPanel = UIHelper.createPasswordPanel(16, curHolder);
        JPanel newPanel = UIHelper.createPasswordPanel(16, newHolder);
        JPanel confPanel = UIHelper.createPasswordPanel(16, confHolder);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = UIHelper.insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        UIHelper.addGridRow(passCard, gbc, 0, "Current Password:", curPanel);
        UIHelper.addGridRow(passCard, gbc, 1, "New Password:", newPanel);
        UIHelper.addGridRow(passCard, gbc, 2, "Confirm Password:", confPanel);

        JButton btnChange = UIHelper.createButton("Update Password", UIHelper.PRIMARY, Color.WHITE, e -> {
            String cur = new String(curHolder[0].getPassword());
            String nw = new String(newHolder[0].getPassword());
            String cf = new String(confHolder[0].getPassword());

            if (!user.getPassword().equals(cur)) {
                UIHelper.showError(this, "Current password is incorrect.", "Error"); return;
            }
            if (!UIHelper.isValidCsv(nw)) {
                UIHelper.showError(this, "New password cannot contain commas or linebreaks.", "Error"); return;
            }
            if (!nw.equals(cf)) {
                UIHelper.showError(this, "New passwords do not match.", "Error"); return;
            }
            user.setPassword(nw);
            PayrollService.updateUser(user);
            curHolder[0].setText(""); newHolder[0].setText(""); confHolder[0].setText("");
            UIHelper.showInfo(this, "Your password has been updated successfully!", "Success");
        });

        gbc.gridx = 1; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        passCard.add(btnChange, gbc);
        add(passCard, BorderLayout.CENTER);
    }

    private void addProfileRow(JPanel p, String title, String val) {
        JLabel t = new JLabel(title);
        t.setFont(UIHelper.FONT_BOLD);
        p.add(t);
        JLabel v = new JLabel(val);
        v.setFont(UIHelper.FONT_PLAIN);
        p.add(v);
    }
}
