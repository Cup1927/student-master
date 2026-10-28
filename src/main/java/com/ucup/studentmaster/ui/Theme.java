package com.ucup.studentmaster.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Tempat semua warna, font, dan helper gambar supaya tampilan konsisten.
 */
public final class Theme {
    public static final Color PRIMARY   = new Color(0x1E3A5F); // navy
    public static final Color SECONDARY = new Color(0x0D6E7A); // teal
    public static final Color ACCENT    = new Color(0xF2A541); // oranye
    public static final Color BG_TOP    = new Color(0xFDF6EC); // krem
    public static final Color BG_BOTTOM = new Color(0xE3EEF2); // biru muda
    public static final Color TEXT      = new Color(0x1B2733);
    public static final Color MUTED     = new Color(0x6B7785);

    public static final Color BTN_ADD    = new Color(0x2E9E6B);
    public static final Color BTN_EDIT   = new Color(0x2F6FD0);
    public static final Color BTN_DELETE = new Color(0xD64545);
    public static final Color BTN_CLEAR  = new Color(0x7A8794);

    public static final Font TITLE    = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font LABEL    = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font BODY     = new Font("Segoe UI", Font.PLAIN, 13);

    /** Palet warna acak untuk mahasiswa baru (supaya tiap mahasiswa beda). */
    public static final String[] PALETTE = {
            "#E57373", "#F06292", "#BA68C8", "#9575CD", "#7986CB", "#64B5F6",
            "#4FC3F7", "#4DD0E1", "#4DB6AC", "#81C784", "#AED581", "#FFD54F",
            "#FFB74D", "#FF8A65", "#A1887F", "#90A4AE"
    };

    private static final Map<String, BufferedImage> CACHE = new HashMap<>();

    private Theme() {
    }

    /** Ubah "#RRGGBB" jadi Color; kalau gagal pakai warna cadangan. */
    public static Color hex(String hex, Color fallback) {
        try {
            return Color.decode(hex);
        } catch (Exception e) {
            return fallback;
        }
    }

    public static String toHex(Color c) {
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    /** Campur dua warna. t=0 -> a, t=1 -> b. */
    public static Color blend(Color a, Color b, double t) {
        return new Color(
                (int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    /** True kalau warnanya gelap -> teks di atasnya sebaiknya putih. */
    public static boolean isDark(Color c) {
        double lum = (0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue()) / 255;
        return lum < 0.62;
    }

    public static String randomColor() {
        return PALETTE[(int) (Math.random() * PALETTE.length)];
    }

    /** Ambil inisial nama, mis. "Andi Pratama" -> "AP". */
    public static String initials(String nama) {
        if (nama == null || nama.isBlank()) return "?";
        String[] parts = nama.trim().split("\\s+");
        String a = parts[0].substring(0, 1);
        String b = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
        return (a + b).toUpperCase();
    }

    /** Gambar dari folder resources (mis. "/images/logo.png"). */
    public static BufferedImage resource(String path) {
        return CACHE.computeIfAbsent("res:" + path, k -> {
            try (InputStream in = Theme.class.getResourceAsStream(path)) {
                return in == null ? null : ImageIO.read(in);
            } catch (Exception e) {
                return null;
            }
        });
    }

    /** Gambar dari file di disk (dipakai untuk foto mahasiswa). */
    public static BufferedImage file(String path) {
        if (path == null || path.isBlank()) return null;
        File f = new File(path);
        if (!f.exists()) return null;
        return CACHE.computeIfAbsent("file:" + f.getAbsolutePath() + f.lastModified(), k -> {
            try {
                return ImageIO.read(f);
            } catch (Exception e) {
                return null;
            }
        });
    }

    public static ImageIcon icon(String path, int size) {
        BufferedImage img = resource(path);
        if (img == null) return null;
        return new ImageIcon(img.getScaledInstance(size, size, Image.SCALE_SMOOTH));
    }

    /** Tempel gambar pattern berulang-ulang (tile) di area tertentu. */
    public static void tilePattern(Graphics2D g, int w, int h, float alpha) {
        BufferedImage pat = resource("/images/pattern.png");
        if (pat == null) return;
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        for (int y = 0; y < h; y += pat.getHeight()) {
            for (int x = 0; x < w; x += pat.getWidth()) {
                g.drawImage(pat, x, y, null);
            }
        }
        g.setComposite(old);
    }

    public static Graphics2D smooth(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        return g2;
    }
}
