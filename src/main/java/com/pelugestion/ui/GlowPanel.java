package com.pelugestion.ui;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Panel que dibuja un halo (glow) azul hielo alrededor de su contenido.
 * Se usa para la barra de botones anclada abajo.
 */
public class GlowPanel extends JPanel {

    private static final Color GLOW = new Color(0x7E, 0xC8, 0xFF); // azul hielo
    private static final Color FONDO = new Color(0xF5, 0xFB, 0xFF); // interior muy claro
    private static final int SPREAD = 14;  // cuanto se expande el glow
    private static final int ARC = 20;

    public GlowPanel() {
        setOpaque(false);
        setLayout(new MigLayout("fill, insets " + SPREAD, "[grow]", "[grow]"));
        setBorder(BorderFactory.createEmptyBorder());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x = SPREAD, y = SPREAD;
        int w = getWidth() - 2 * SPREAD;
        int h = getHeight() - 2 * SPREAD;
        if (w <= 0 || h <= 0) {
            g2.dispose();
            super.paintComponent(g);
            return;
        }

        // Halo: varios contornos redondeados expandiendose hacia afuera,
        // cada vez mas transparentes.
        for (int i = SPREAD; i >= 1; i--) {
            float ratio = (float) i / SPREAD;
            int alpha = (int) (70 * (1 - ratio) + 8); // mas opaco cerca del borde
            g2.setColor(new Color(GLOW.getRed(), GLOW.getGreen(), GLOW.getBlue(), alpha));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(x - i, y - i, w + 2 * i, h + 2 * i, ARC + 2 * i, ARC + 2 * i);
        }

        // Interior claro para que la barra parezca una tarjeta flotante
        g2.setColor(FONDO);
        g2.fillRoundRect(x, y, w, h, ARC, ARC);
        g2.setColor(GLOW);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x, y, w, h, ARC, ARC);

        g2.dispose();
        super.paintComponent(g);
    }
}
