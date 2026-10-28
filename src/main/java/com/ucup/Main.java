package com.ucup;

import com.formdev.flatlaf.FlatLightLaf;
import com.ucup.studentmaster.ui.MainFrame;

import javax.swing.*;

/**
 * Titik awal program: pasang tema FlatLaf lalu buka jendela utama.
 */
public class Main {
    public static void main(String[] args) {
        // matikan library native FlatLaf -> menghilangkan WARNING "restricted method" di JDK 24+
        System.setProperty("flatlaf.useNativeLibrary", "false");
        FlatLightLaf.setup();
        UIManager.put("Button.arc", 14);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 10);

        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
