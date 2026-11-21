package payroll;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

public class PasswordFieldWithToggle extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JPasswordField passwordField;
    private final char defaultEchoChar;
    private final JButton visibilityButton;

    public PasswordFieldWithToggle(int columns) {
        super(new BorderLayout(4, 0));
        setOpaque(false);

        passwordField = new JPasswordField(columns);
        defaultEchoChar = passwordField.getEchoChar();
        visibilityButton = new JButton("\u25c9");
        visibilityButton.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
        visibilityButton.setFocusable(false);
        visibilityButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
        visibilityButton.setPreferredSize(new Dimension(38, 30));
        visibilityButton.setToolTipText("Show password");
        visibilityButton.getAccessibleContext().setAccessibleName("Toggle password visibility");
        visibilityButton.addActionListener(e -> toggleVisibility());

        add(passwordField, BorderLayout.CENTER);
        add(visibilityButton, BorderLayout.EAST);
    }

    public JPasswordField getPasswordField() {
        return passwordField;
    }

    public char[] getPassword() {
        return passwordField.getPassword();
    }

    private void toggleVisibility() {
        boolean visible = passwordField.getEchoChar() != 0;
        passwordField.setEchoChar(visible ? (char) 0 : defaultEchoChar);
        visibilityButton.setToolTipText(visible ? "Hide password" : "Show password");
    }
}
