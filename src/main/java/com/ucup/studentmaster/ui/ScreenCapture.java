package com.ucup.studentmaster.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Mengambil screenshot jendela aplikasi lalu menyimpannya ke folder "captures"
 * (dipakai sebagai bukti setiap aksi CRUD dan laporan).
 */
public final class ScreenCapture {
    public static final File DIR = new File("captures");

    private ScreenCapture() {
    }

    /** Ambil screenshot area jendela sekarang juga. */
    public static void save(Window w, String fileName) {
        try {
            DIR.mkdirs();
            Rectangle r = w.getBounds();
            BufferedImage img = new Robot().createScreenCapture(r);
            File out = new File(DIR, fileName);
            ImageIO.write(img, "png", out);
            System.out.println("Capture disimpan: " + out.getPath());
        } catch (Exception e) {
            System.err.println("Gagal menyimpan capture " + fileName + ": " + e.getMessage());
        }
    }

    /**
     * Ambil screenshot sedikit lebih lambat, misalnya setelah dialog
     * "berhasil" muncul, supaya dialog ikut terekam.
     */
    public static void saveLater(Window w, String fileName, int delayMs) {
        Timer t = new Timer(delayMs, e -> save(w, fileName));
        t.setRepeats(false);
        t.start();
    }
}
