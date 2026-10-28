package com.ucup.studentmaster.report;

import com.ucup.studentmaster.db.DBConnection;
import net.sf.jasperreports.engine.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Membuat laporan data mahasiswa dengan JasperReports.
 * Alur: compile .jrxml -> isi data dari MySQL -> export ke PDF.
 */
public class StudentReport {
    private static final String JRXML = "/reports/student_report.jrxml";
    private static JasperReport compiled; // disimpan supaya compile cukup sekali

    /** Compile file desain laporan (.jrxml) menjadi JasperReport. */
    public static synchronized JasperReport compile() throws Exception {
        if (compiled == null) {
            try (InputStream in = StudentReport.class.getResourceAsStream(JRXML)) {
                if (in == null) throw new IllegalStateException("File " + JRXML + " tidak ditemukan.");
                compiled = JasperCompileManager.compileReport(in);
            }
        }
        return compiled;
    }

    /** Parameter laporan: rentang NIM (dipakai di WHERE) dan logo header. */
    public static Map<String, Object> parameters(String nimAwal, String nimAkhir) throws Exception {
        Map<String, Object> p = new HashMap<>();
        p.put("NIM_AWAL", nimAwal);
        p.put("NIM_AKHIR", nimAkhir);
        try (InputStream in = StudentReport.class.getResourceAsStream("/images/logo.png")) {
            p.put("LOGO", in == null ? null : ImageIO.read(in));
        }
        return p;
    }

    /** Isi laporan dengan data dari database sesuai rentang NIM. */
    public static JasperPrint fill(String nimAwal, String nimAkhir) throws Exception {
        try (Connection c = DBConnection.getConnection()) {
            return JasperFillManager.fillReport(compile(), parameters(nimAwal, nimAkhir), c);
        }
    }

    /** Simpan laporan sebagai file PDF. */
    public static File exportPdf(JasperPrint print, File out) throws Exception {
        File dir = out.getAbsoluteFile().getParentFile();
        if (dir != null) dir.mkdirs();
        JasperExportManager.exportReportToPdfFile(print, out.getPath());
        return out;
    }

    /** Simpan halaman pertama laporan sebagai gambar PNG (untuk folder captures). */
    public static void savePageImage(JasperPrint print, File out) throws Exception {
        Image img = JasperPrintManager.printPageToImage(print, 0, 1.6f);
        BufferedImage bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
        g.drawImage(img, 0, 0, null);
        g.dispose();
        File dir = out.getAbsoluteFile().getParentFile();
        if (dir != null) dir.mkdirs();
        ImageIO.write(bi, "png", out);
    }
}
