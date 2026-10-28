package com.ucup.studentmaster.ui;

import com.ucup.studentmaster.model.Student;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * Kartu profil mahasiswa.
 * Warna latar diambil dari kolom "warna" dan foto dari kolom "foto",
 * jadi tampilan kartu BERBEDA untuk setiap mahasiswa.
 */
public class ProfileCard extends JComponent {
    private Student student;

    public ProfileCard() {
        setPreferredSize(new Dimension(600, 190));
    }

    public void setStudent(Student student) {
        this.student = student;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Theme.smooth(g);
        int w = getWidth(), h = getHeight();
        Color base = student == null ? Theme.PRIMARY : Theme.hex(student.getWarna(), Theme.PRIMARY);
        Color dark = Theme.blend(base, Color.BLACK, 0.35);

        // 1. latar gradasi sesuai warna mahasiswa
        Shape card = new RoundRectangle2D.Float(0, 0, w, h, 28, 28);
        g2.setClip(card);
        g2.setPaint(new GradientPaint(0, 0, base, w, h, dark));
        g2.fillRect(0, 0, w, h);

        // 2. gambar pattern + lingkaran dekorasi
        Theme.tilePattern(g2, w, h, 0.20f);
        g2.setColor(new Color(255, 255, 255, 35));
        g2.fillOval(w - 230, -90, 320, 320);
        g2.fillOval(w - 120, h - 70, 160, 160);

        // 3. avatar: foto kalau ada, kalau tidak inisial nama
        int size = h - 56;
        int ax = 30, ay = 28;
        Shape circle = new Ellipse2D.Float(ax, ay, size, size);
        BufferedImage foto = student == null ? null : Theme.file(student.getFoto());
        if (foto != null) {
            Shape oldClip = g2.getClip();
            g2.clip(circle);
            double s = Math.max((double) size / foto.getWidth(), (double) size / foto.getHeight());
            int iw = (int) (foto.getWidth() * s), ih = (int) (foto.getHeight() * s);
            g2.drawImage(foto, ax + (size - iw) / 2, ay + (size - ih) / 2, iw, ih, null);
            g2.setClip(oldClip);
        } else {
            g2.setColor(Theme.blend(base, Color.WHITE, 0.55));
            g2.fill(circle);
            String ini = student == null ? "SM" : Theme.initials(student.getNama());
            g2.setFont(new Font("Segoe UI", Font.BOLD, size / 3));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(dark);
            g2.drawString(ini, ax + (size - fm.stringWidth(ini)) / 2,
                    ay + (size - fm.getHeight()) / 2 + fm.getAscent());
        }
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(4f));
        g2.draw(circle);

        // 4. teks profil (warna teks menyesuaikan gelap/terangnya latar)
        Color fg = Theme.isDark(base) ? Color.WHITE : Theme.TEXT;
        Color fg2 = Theme.isDark(base) ? new Color(255, 255, 255, 210) : new Color(27, 39, 51, 190);
        int tx = ax + size + 30;
        if (student == null) {
            g2.setColor(fg);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
            g2.drawString("Pilih mahasiswa", tx, 80);
            g2.setColor(fg2);
            g2.setFont(Theme.BODY);
            g2.drawString("Klik salah satu baris di tabel untuk melihat profil.", tx, 108);
        } else {
            g2.setColor(fg2);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.drawString("PROFIL MAHASISWA", tx, 50);
            g2.setColor(fg);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 28));
            g2.drawString(nz(student.getNama()), tx, 86);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            g2.setColor(fg2);
            g2.drawString("NIM  " + nz(student.getNim()), tx, 116);
            g2.drawString(nz(student.getJurusan()) + "  •  Angkatan " + student.getAngkatan(), tx, 140);
            g2.drawString(nz(student.getEmail()), tx, 164);
        }
        g2.dispose();
    }

    private static String nz(String s) {
        return s == null || s.isBlank() ? "-" : s;
    }
}
