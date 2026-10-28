package com.ucup.studentmaster.ui;

import com.ucup.studentmaster.dao.StudentDAO;
import com.ucup.studentmaster.report.StudentReport;
import net.sf.jasperreports.engine.JasperPrint;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Form untuk meminta rentang NIM yang akan dicetak ke laporan PDF.
 */
public class ReportDialog extends JDialog {
    private final JComboBox<String> cbAwal;
    private final JComboBox<String> cbAkhir;
    private final JButton btnCetak;
    private final JLabel lblInfo = new JLabel(" ");
    private final StudentDAO dao = new StudentDAO();

    public ReportDialog(Frame owner, List<String> daftarNim) {
        super(owner, "Cetak Laporan Mahasiswa", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        String[] nims = daftarNim.toArray(new String[0]);
        cbAwal = new JComboBox<>(nims);
        cbAkhir = new JComboBox<>(nims);
        cbAwal.setEditable(true);   // boleh pilih dari daftar atau ketik manual
        cbAkhir.setEditable(true);
        if (nims.length > 0) {
            cbAwal.setSelectedIndex(0);
            cbAkhir.setSelectedIndex(nims.length - 1);
        }

        // ---- header berwarna + logo ----
        BackgroundPanel header = new BackgroundPanel(Theme.PRIMARY, Theme.SECONDARY, 0.18f, 0);
        header.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 12));
        header.add(new JLabel(Theme.icon("/images/logo.png", 44)));
        JPanel ht = new JPanel(new GridLayout(2, 1));
        ht.setOpaque(false);
        JLabel t1 = new JLabel("Laporan Data Mahasiswa");
        t1.setFont(new Font("Segoe UI", Font.BOLD, 18));
        t1.setForeground(Color.WHITE);
        JLabel t2 = new JLabel("Pilih rentang NIM yang ingin dicetak ke PDF");
        t2.setFont(Theme.SUBTITLE);
        t2.setForeground(new Color(255, 255, 255, 210));
        ht.add(t1);
        ht.add(t2);
        header.add(ht);

        // ---- isi form ----
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.BG_TOP);
        form.setBorder(new EmptyBorder(18, 22, 8, 22));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 6, 6, 6);
        c.gridx = 0;
        c.gridy = 0;
        form.add(label("NIM awal"), c);
        c.gridx = 1;
        c.weightx = 1;
        form.add(cbAwal, c);
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        form.add(label("NIM akhir"), c);
        c.gridx = 1;
        c.weightx = 1;
        form.add(cbAkhir, c);
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblInfo.setForeground(Theme.MUTED);
        form.add(lblInfo, c);

        // ---- tombol ----
        btnCetak = new JButton("Cetak PDF");
        btnCetak.setBackground(Theme.ACCENT);
        btnCetak.setForeground(Theme.TEXT);
        btnCetak.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCetak.setPreferredSize(new Dimension(130, 36));
        btnCetak.addActionListener(e -> cetak());
        JButton btnBatal = new JButton("Batal");
        btnBatal.setPreferredSize(new Dimension(100, 36));
        btnBatal.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttons.setBackground(Theme.BG_TOP);
        buttons.add(btnBatal);
        buttons.add(btnCetak);

        JPanel root = new JPanel(new BorderLayout());
        root.add(header, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);
        getRootPane().setDefaultButton(btnCetak);

        cbAwal.addActionListener(e -> updateInfo());
        cbAkhir.addActionListener(e -> updateInfo());
        updateInfo();

        setSize(480, 320);
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.LABEL);
        l.setForeground(Theme.TEXT);
        return l;
    }

    private String value(JComboBox<String> cb) {
        Object v = cb.getEditor().getItem();
        return v == null ? "" : v.toString().trim();
    }

    /** Tampilkan berapa mahasiswa yang masuk rentang. */
    private void updateInfo() {
        String awal = value(cbAwal), akhir = value(cbAkhir);
        if (!awal.matches("\\d{10}") || !akhir.matches("\\d{10}")) {
            lblInfo.setText("NIM harus 10 digit angka.");
            return;
        }
        try {
            int n = dao.countByRange(awal, akhir);
            lblInfo.setText(n + " mahasiswa akan dicetak (NIM " + awal + " s/d " + akhir + ").");
        } catch (Exception ex) {
            lblInfo.setText("Gagal menghitung data: " + ex.getMessage());
        }
    }

    private void cetak() {
        String awal = value(cbAwal), akhir = value(cbAkhir);
        if (!awal.matches("\\d{10}") || !akhir.matches("\\d{10}")) {
            warn("NIM awal dan NIM akhir harus 10 digit angka.");
            return;
        }
        if (awal.compareTo(akhir) > 0) {
            warn("NIM awal tidak boleh lebih besar dari NIM akhir.");
            return;
        }
        try {
            if (dao.countByRange(awal, akhir) == 0) {
                warn("Tidak ada mahasiswa dengan NIM antara " + awal + " dan " + akhir + ".");
                return;
            }
        } catch (Exception ex) {
            error(ex);
            return;
        }

        ScreenCapture.save(this, "report_form.png"); // bukti form rentang NIM

        btnCetak.setEnabled(false);
        btnCetak.setText("Memproses...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // proses laporan di background supaya tampilan tidak macet
        new SwingWorker<File, Void>() {
            @Override
            protected File doInBackground() throws Exception {
                JasperPrint print = StudentReport.fill(awal, akhir);
                File pdf = new File("reports", "Laporan_Mahasiswa_" + awal + "_" + akhir + ".pdf");
                StudentReport.exportPdf(print, pdf);
                StudentReport.savePageImage(print, new File(ScreenCapture.DIR, "report_pdf.png"));
                return pdf;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                btnCetak.setEnabled(true);
                btnCetak.setText("Cetak PDF");
                try {
                    File pdf = get();
                    if (Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().open(pdf);
                        } catch (Exception ignored) {
                            // tidak ada aplikasi PDF, cukup tampilkan lokasi file
                        }
                    }
                    JOptionPane.showMessageDialog(ReportDialog.this,
                            "Laporan PDF berhasil dibuat:\n" + pdf.getAbsolutePath(),
                            "Laporan", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } catch (Exception ex) {
                    error(ex);
                }
            }
        }.execute();
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Periksa Input", JOptionPane.WARNING_MESSAGE);
    }

    private void error(Exception e) {
        Throwable root = e;
        while (root.getCause() != null) root = root.getCause();
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Gagal membuat laporan:\n" + root.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
