package com.payroll.ui;

import com.payroll.util.UIHelper;
import com.payroll.dao.FileDao;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 PayStubDialog
 */
public class PayStubDialog extends JDialog {
    public PayStubDialog(Frame parent, String employeeName, String employeeId, String payStubText, String filePath) {
        super(parent, "Official Pay Stub - " + employeeName, true);
        setSize(760, 560);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.setBackground(UIHelper.BG_LIGHT);

        final JTextArea txtReceipt = new JTextArea(payStubText);
        txtReceipt.setFont(UIHelper.FONT_MONO);
        txtReceipt.setEditable(false);
        txtReceipt.setBackground(Color.WHITE);
        txtReceipt.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        panel.add(new JScrollPane(txtReceipt), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new BorderLayout(10, 5));
        bottomBar.setOpaque(false);

        JLabel lblSaved = new JLabel("Backup: " + (filePath != null ? filePath : "paystubs/"));
        lblSaved.setFont(UIHelper.FONT_SMALL);
        lblSaved.setForeground(UIHelper.TEXT_MUTED);
        bottomBar.add(lblSaved, BorderLayout.WEST);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);

        JButton btnPdf = UIHelper.createButton("Create PDF & Open in Browser", UIHelper.PRIMARY, Color.WHITE, e -> {
            String pdfPath = FileDao.savePayStubPdf(employeeId, payStubText);
            if (pdfPath == null) {
                UIHelper.showError(this, "Could not create the pay stub PDF. Check the paystubs folder and permissions.",
                        "PDF Creation Failed");
                return;
            }
            lblSaved.setText("PDF: " + pdfPath);

            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                UIHelper.showError(this, "PDF created at " + pdfPath + ", but this computer cannot open a web browser automatically.",
                        "Browser Unavailable");
                return;
            }
            try {
                Desktop.getDesktop().browse(new File(pdfPath).toURI());
            } catch (IOException | UnsupportedOperationException ex) {
                UIHelper.showError(this, "PDF created at " + pdfPath + ", but the browser could not be opened: "
                        + ex.getMessage(), "Unable to Open PDF");
            }
        });

        JButton btnClose = UIHelper.createButton("Close", new Color(148, 163, 184), Color.WHITE, e -> dispose());
        buttons.add(btnPdf);
        buttons.add(btnClose);
        bottomBar.add(buttons, BorderLayout.EAST);

        panel.add(bottomBar, BorderLayout.SOUTH);
        setContentPane(panel);
    }
}
