package com.pelugestion.ui;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Dialogo de login / creacion de contrasena.
 * Solo RECOGE la contrasena; el descifrado de la base de datos lo hace App.
 */
public class LoginDialog extends JDialog {

    private static final Color PRIMARY = new Color(0x9B, 0x59, 0xB6);
    private static final Color PRIMARY_DARK = new Color(0x7D, 0x3C, 0x98);

    private JPasswordField passwordField;
    private JPasswordField confirmField;
    private JLabel errorLabel;

    private final boolean isFirstRun;
    private boolean ok = false;
    private char[] password;

    public LoginDialog(boolean isFirstRun, String errorMessage) {
        super((Frame) null, "PeluGestion", true);
        this.isFirstRun = isFirstRun;
        initComponents();
        if (errorMessage != null) {
            errorLabel.setText(errorMessage);
        }
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new MigLayout(
                "fill, wrap, insets 0", "[grow, 360::]", ""));

        JPanel header = new JPanel(new MigLayout(
                "fill, wrap, insets 30 40 20 40", "[center]")) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, PRIMARY, getWidth(), getHeight(), PRIMARY_DARK);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setOpaque(false);

        JLabel title = new JLabel("PeluGestion");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 29f));
        title.setForeground(Color.WHITE);
        header.add(title, "wrap");

        JLabel subtitle = new JLabel(isFirstRun
                ? "Crea tu contrasena de acceso"
                : "Introduce tu contrasena");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 15f));
        subtitle.setForeground(new Color(255, 255, 255, 200));
        header.add(subtitle);

        mainPanel.add(header, "growx");

        JPanel form = new JPanel(new MigLayout(
                "wrap, insets 30 40 30 40, gapy 8", "[grow, 280::]"));

        JLabel passLabel = new JLabel("Contrasena");
        passLabel.setFont(passLabel.getFont().deriveFont(Font.PLAIN, 14f));
        form.add(passLabel);

        passwordField = new JPasswordField(20);
        passwordField.putClientProperty("JTextField.placeholderText", "Introduce tu contrasena");
        form.add(passwordField, "growx, h 36!");

        if (isFirstRun) {
            JLabel confirmLabel = new JLabel("Confirmar contrasena");
            confirmLabel.setFont(confirmLabel.getFont().deriveFont(Font.PLAIN, 14f));
            form.add(confirmLabel, "gaptop 5");

            confirmField = new JPasswordField(20);
            confirmField.putClientProperty("JTextField.placeholderText", "Repite la contrasena");
            form.add(confirmField, "growx, h 36!");
        }

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(new Color(0xE7, 0x4C, 0x3C));
        errorLabel.setFont(errorLabel.getFont().deriveFont(Font.PLAIN, 13f));
        form.add(errorLabel, "gaptop 5");

        if (!isFirstRun) {
            JLabel aviso = new JLabel("<html><i>La contrasena cifra tus datos: se pide en cada apertura.</i></html>");
            aviso.setForeground(new Color(0x95, 0xA5, 0xA6));
            aviso.setFont(aviso.getFont().deriveFont(Font.PLAIN, 12f));
            form.add(aviso, "gaptop 2");
        }

        JButton actionButton = new JButton(isFirstRun ? "Crear contrasena" : "Entrar");
        actionButton.setFont(actionButton.getFont().deriveFont(Font.BOLD, 15f));
        actionButton.setBackground(PRIMARY);
        actionButton.setForeground(Color.WHITE);
        actionButton.setFocusPainted(false);
        actionButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        actionButton.putClientProperty("JButton.buttonType", "roundRect");
        form.add(actionButton, "growx, h 40!, gaptop 10");

        mainPanel.add(form, "growx");
        setContentPane(mainPanel);

        actionButton.addActionListener(e -> handleAction());
        passwordField.addActionListener(e -> handleAction());
        if (confirmField != null) {
            confirmField.addActionListener(e -> handleAction());
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                passwordField.requestFocusInWindow();
            }
        });
    }

    private void handleAction() {
        char[] pw = passwordField.getPassword();

        if (pw.length == 0) {
            showError("La contrasena no puede estar vacia");
            return;
        }

        if (isFirstRun) {
            char[] confirm = confirmField.getPassword();
            if (!java.util.Arrays.equals(pw, confirm)) {
                showError("Las contrasenas no coinciden");
                return;
            }
            if (pw.length < 4) {
                showError("La contrasena debe tener al menos 4 caracteres");
                return;
            }
        }

        this.password = pw;
        this.ok = true;
        dispose();
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }

    public boolean isOk() {
        return ok;
    }

    public char[] getPassword() {
        return password;
    }
}
