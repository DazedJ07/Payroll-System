package com.payroll.ui;

import com.payroll.model.User;
import com.payroll.service.PayrollService;
import com.payroll.util.UIHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 LoginFrame
 */
public class LoginFrame extends JFrame {
    private final JTextField txtUsername;
    private final JPasswordField[] pfHolder = new JPasswordField[1];

    public LoginFrame() {
        setTitle("Staff Timekeeping & Payroll System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 340);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBackground(UIHelper.BG_LIGHT);
        main.setBorder(BorderFactory.createEmptyBorder(25, 30, 20, 30));

        // Header
        JPanel header = new JPanel(new BorderLayout(4, 4));
        header.setOpaque(false);
        JLabel lblTitle = new JLabel("Staff Timekeeping & Payroll", SwingConstants.CENTER);
        lblTitle.setFont(UIHelper.FONT_TITLE);
        lblTitle.setForeground(UIHelper.TEXT_DARK);
        JLabel lblSubtitle = new JLabel("Enter your credentials to access your account", SwingConstants.CENTER);
        lblSubtitle.setFont(UIHelper.FONT_SUBTITLE);
        lblSubtitle.setForeground(UIHelper.TEXT_MUTED);
        header.add(lblTitle, BorderLayout.NORTH);
        header.add(lblSubtitle, BorderLayout.SOUTH);
        main.add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = UIHelper.insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtUsername = UIHelper.createField(18);
        JPanel passPanel = UIHelper.createPasswordPanel(18, pfHolder);

        KeyAdapter enterListener = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) processLogin();
            }
        };
        txtUsername.addKeyListener(enterListener);
        pfHolder[0].addKeyListener(enterListener);

        UIHelper.addGridRow(form, gbc, 0, "Username:", txtUsername);
        UIHelper.addGridRow(form, gbc, 1, "Password:", passPanel);

        // Buttons
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);
        JButton btnExit = UIHelper.createButton("Exit", new Color(148, 163, 184), Color.WHITE, e -> System.exit(0));
        JButton btnLogin = UIHelper.createButton("Login", UIHelper.PRIMARY, Color.WHITE, e -> processLogin());
        btnBar.add(btnExit);
        btnBar.add(btnLogin);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.insets = UIHelper.insets(16, 8, 0, 8);
        form.add(btnBar, gbc);

        main.add(form, BorderLayout.CENTER);
        setContentPane(main);
    }

    private void processLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(pfHolder[0].getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            UIHelper.showWarning(this, "Please enter both username and password.", "Input Required");
            return;
        }

        User user = PayrollService.authenticate(username, password);
        if (user != null) {
            UIHelper.showInfo(this, "Welcome, " + user.getName() + "!\nRole: " + user.getRole(), "Login Successful");
            new DashboardFrame(user).setVisible(true);
            dispose();
        } else {
            UIHelper.showError(this, "Invalid username or password.\nPlease check your credentials.", "Login Failed");
            pfHolder[0].setText("");
            pfHolder[0].requestFocus();
        }
    }
}
