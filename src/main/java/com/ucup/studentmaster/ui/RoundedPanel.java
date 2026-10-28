package com.ucup.studentmaster.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Kartu putih semi-transparan dengan sudut membulat.
 */
public class RoundedPanel extends JPanel {
    private final int radius;
    private final Color fill;

    public RoundedPanel(int radius, Color fill) {
        this.radius = radius;
        this.fill = fill;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Theme.smooth(g);
        g2.setColor(new Color(0, 0, 0, 25)); // bayangan tipis
        g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, radius, radius);
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 5, radius, radius);
        g2.dispose();
        super.paintComponent(g);
    }
}
