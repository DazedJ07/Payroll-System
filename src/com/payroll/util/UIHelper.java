package com.payroll.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 UIHelper 
 */
public class UIHelper {
    
    public static final Color BG_LIGHT = new Color(245, 247, 250);       // Soft light gray #F5F7FA
    public static final Color CARD_BG = Color.WHITE;                      // Clean white card #FFFFFF
    public static final Color HEADER_DARK = new Color(30, 41, 59);       // Slate Navy #1E293B
    public static final Color PRIMARY = new Color(37, 99, 235);          // Royal Blue #2563EB
    public static final Color SUCCESS = new Color(22, 163, 74);          // Forest Green #16A34A
    public static final Color DANGER = new Color(220, 38, 38);           // Crimson Red #DC2626
    public static final Color WARNING = new Color(217, 119, 6);          // Amber #D97706
    public static final Color PURPLE = new Color(124, 58, 237);          // Violet #7C3AED
    public static final Color TEXT_DARK = new Color(30, 41, 59);
    public static final Color TEXT_MUTED = new Color(100, 116, 139);
    public static final Color BORDER = new Color(226, 232, 240);

    // TYPOGRAPHY
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 19);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_PLAIN = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);

    // REUSABLE UI BUILDERS
    public static JButton createButton(String text, Color bg, Color fg, ActionListener al) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        if (al != null) btn.addActionListener(al);
        return btn;
    }

    public static JTextField createField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setFont(FONT_PLAIN);
        tf.setPreferredSize(new Dimension(200, 32));
        return tf;
    }

    public static JPanel createPasswordPanel(int cols, final JPasswordField[] outField) {
        JPanel panel = new JPanel(new BorderLayout(4, 0));
        panel.setOpaque(false);
        final JPasswordField pf = new JPasswordField(cols);
        pf.setFont(FONT_PLAIN);
        pf.setPreferredSize(new Dimension(200, 32));
        final char defaultEcho = pf.getEchoChar();

        final JButton toggle = new JButton("\u25c9");
        toggle.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 14));
        toggle.setFocusable(false);
        toggle.setPreferredSize(new Dimension(36, 32));
        toggle.setToolTipText("Show/Hide password");
        toggle.addActionListener(e -> {
            boolean visible = pf.getEchoChar() != 0;
            pf.setEchoChar(visible ? (char) 0 : defaultEcho);
        });

        panel.add(pf, BorderLayout.CENTER);
        panel.add(toggle, BorderLayout.EAST);
        if (outField != null && outField.length > 0) outField[0] = pf;
        return panel;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setFont(FONT_SMALL);
        table.getTableHeader().setFont(FONT_BOLD);
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(TEXT_DARK);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(BORDER);
        table.setShowGrid(true);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    public static void addGridRow(JPanel p, GridBagConstraints gbc, int row, String label, Component comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_BOLD);
        lbl.setForeground(TEXT_DARK);
        p.add(lbl, gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 0.7;
        p.add(comp, gbc);
    }

    public static Insets insets(int t, int l, int b, int r) {
        return new Insets(t, l, b, r);
    }

    public static String currency(double val) {
        return String.format("$%.2f", val);
    }

    public static String hours(double val) {
        return String.format("%.2f", val);
    }

    public static void showInfo(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showWarning(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }

    public static void showError(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(Component parent, String message, String title) {
        return JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static boolean isValidCsv(String s) {
        return s != null && !s.trim().isEmpty() && !s.contains(",") && !s.contains("\n") && !s.contains("\r");
    }
}
