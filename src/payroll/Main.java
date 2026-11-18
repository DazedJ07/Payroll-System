package payroll;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main - Entry point for Staff Timekeeping and Payroll Desktop Application.
 * Configures system look-and-feel, initializes data storage files, and launches LoginFrame.
 */
public class Main {

    public static void main(String[] args) {
        // Apply System Look and Feel for native OS appearance (Windows / macOS)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set system look and feel: " + e.getMessage());
        }

        // Auto-create data directory, default users.txt, timelogs.txt, and paystubs folder
        FileHelper.initializeDataDirectoryAndFiles();

        // Launch Login GUI on Swing Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            }
        });
    }
}
