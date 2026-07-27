package com.pelugestion.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Fabrica de componentes con estilo unificado para toda la app.
 * Centraliza colores y botones para que la interfaz sea coherente.
 */
public final class UiFactory {

    private UiFactory() {}

    // --- Paleta ---
    public static final Color PRIMARY      = new Color(0x5D, 0xAD, 0xEE);
    public static final Color PRIMARY_DARK = new Color(0x4A, 0x90, 0xD9);
    public static final Color DANGER       = new Color(0xE7, 0x4C, 0x3C);
    public static final Color DANGER_DARK  = new Color(0xC0, 0x39, 0x2B);
    public static final Color HEADER_BG     = new Color(0x2C, 0x3E, 0x50);
    public static final Color HEADER_BG_END = new Color(0x34, 0x49, 0x5E);
    public static final Color TEXT_MUTED   = new Color(0x95, 0xA5, 0xA6);

    /** Boton de accion principal: azul clarito con texto blanco. */
    public static JButton primaryButton(String text) {
        return coloredButton(text, PRIMARY, PRIMARY_DARK);
    }

    /** Boton de borrado: ROJO con texto blanco, para que se vea que es destructivo. */
    public static JButton dangerButton(String text) {
        return coloredButton(text, DANGER, DANGER_DARK);
    }

    private static JButton coloredButton(String text, Color bg, Color hover) {
        JButton btn = new JButton(text);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 15f));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        // Colores propios de FlatLaf para fondo, hover y pulsado
        btn.putClientProperty("JButton.background", bg);
        btn.putClientProperty("JButton.hoverBackground", hover);
        btn.putClientProperty("JButton.pressedBackground", hover.darker());
        return btn;
    }

    /** Boton de la barra superior (texto blanco sobre el azul marino). */
    public static JButton headerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(btn.getFont().deriveFont(Font.PLAIN, 15f));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        return btn;
    }
}
