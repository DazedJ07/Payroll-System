package com.payroll.ui;

import com.payroll.util.UIHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * PayStubDialog - Displays the formatted pay slip with built-in Print / Save to PDF capabilities.
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

        JButton btnPrint = UIHelper.createButton("Print / Save as PDF", UIHelper.PRIMARY, Color.WHITE, e -> {
            try {
                MessageFormat header = new MessageFormat("Pay Stub - " + employeeName + " (" + employeeId + ")");
                MessageFormat footer = new MessageFormat("Page {0}");
                if (txtReceipt.print(header, footer, true, null, null, true)) {
                    UIHelper.showInfo(this, "Print job sent successfully!", "Print Success");
                }
            } catch (PrinterException pe) {
                UIHelper.showError(this, "Printing failed: " + pe.getMessage(), "Print Error");
            }
        });

        JButton btnClose = UIHelper.createButton("Close", new Color(148, 163, 184), Color.WHITE, e -> dispose());
        buttons.add(btnPrint);
        buttons.add(btnClose);
        bottomBar.add(buttons, BorderLayout.EAST);

        panel.add(bottomBar, BorderLayout.SOUTH);
        setContentPane(panel);
    }
}
