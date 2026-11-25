package com.payroll.app;

import com.payroll.dao.FileDao;
import com.payroll.ui.LoginFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 Main - Entry point for the Staff Timekeeping and Payroll Application.
 */
public class Main {
    public static void main(String[] args) {
        // Apply FlatLaf modern theme with graceful native fallback
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        // Initialize local storage files and sample datasets
        FileDao.initStorage();

        // Launch UI on Swing Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
