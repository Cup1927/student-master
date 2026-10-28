package com.ucup.studentmaster.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Panel dengan latar gradasi warna + gambar pattern di atasnya.
 * Dipakai untuk latar jendela utama dan header.
 */
public class BackgroundPanel extends JPanel {
    private final Color top;
    private final Color bottom;
    private final float patternAlpha;
    private final int radius;

    public BackgroundPanel(Color top, Color bottom, float patternAlpha, int radius) {
        this.top = top;
        this.bottom = bottom;
        this.patternAlpha = patternAlpha;
        this.radius = radius;
        setOpaque(radius == 0);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Theme.smooth(g);
        int w = getWidth(), h = getHeight();
        Shape shape = new RoundRectangle2D.Float(0, 0, w, h, radius, radius);
        g2.setClip(shape);
        g2.setPaint(new GradientPaint(0, 0, top, w, h, bottom));
        g2.fillRect(0, 0, w, h);
        Theme.tilePattern(g2, w, h, patternAlpha);
        g2.dispose();
    }
}
