package com.ucup.studentmaster.ui;

import com.ucup.studentmaster.dao.StudentDAO;
import com.ucup.studentmaster.model.Student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.nio.file.*;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

/**
 * Jendela utama aplikasi Student Master (CRUD data mahasiswa).
 */
public class MainFrame extends JFrame {
    private final StudentDAO dao = new StudentDAO();
    private final List<Student> data = new ArrayList<>();

    // ---- tabel ----
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "NIM", "Nama", "Jurusan", "Angkatan", "Email", "Warna"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    // ---- form ----
    private final JTextField tfNim = new JTextField();
    private final JTextField tfNama = new JTextField();
    private final JComboBox<String> cbJurusan = new JComboBox<>(new String[]{
            "Teknik Informatika", "Sistem Informasi", "Manajemen", "Akuntansi"});
    private final JSpinner spAngkatan = new JSpinner(
            new SpinnerNumberModel(Year.now().getValue(), 2000, 2100, 1));
    private final JTextField tfEmail = new JTextField();
    private final JButton btnWarna = new JButton();
    private final JLabel lblFoto = new JLabel();
    private final JTextField tfCari = new JTextField();
    private final JLabel lblStatus = new JLabel(" ");

    private final ProfileCard card = new ProfileCard();

    private String warnaDipilih;
    private String fotoDipilih;
    private Student selected;      // mahasiswa yang sedang dipilih di tabel
    private boolean filling;       // true saat form sedang diisi otomatis

    public MainFrame() {
        super("Student Master");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1240, 740);
        setMinimumSize(new Dimension(1080, 660));
        setLocationRelativeTo(null);
        Image logo = Theme.resource("/images/logo.png");
        if (logo != null) setIconImage(logo);

        BackgroundPanel root = new BackgroundPanel(Theme.BG_TOP, Theme.BG_BOTTOM, 0.0f, 0);
        root.setLayout(new BorderLayout(16, 16));
        root.setBorder(new EmptyBorder(16, 16, 10, 16));
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildForm(), BorderLayout.WEST);
        root.add(buildCenter(), BorderLayout.CENTER);
        lblStatus.setFont(Theme.BODY);
        lblStatus.setForeground(Theme.MUTED);
        root.add(lblStatus, BorderLayout.SOUTH);
        setContentPane(root);

        resetForm();
        loadData("");

        // capture READ: tampilan data dari database saat aplikasi dibuka
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                ScreenCapture.saveLater(MainFrame.this, "read.png", 1200);
            }
        });
    }

    // =================================================================
    //  BAGIAN TAMPILAN
    // =================================================================

    private JComponent buildHeader() {
        BackgroundPanel header = new BackgroundPanel(Theme.PRIMARY, Theme.SECONDARY, 0.18f, 26);
        header.setLayout(new BorderLayout(16, 0));
        header.setBorder(new EmptyBorder(14, 20, 14, 20));
        header.setPreferredSize(new Dimension(100, 92));

        JLabel logo = new JLabel(Theme.icon("/images/logo.png", 60));
        JLabel title = new JLabel("Student Master");
        title.setFont(Theme.TITLE);
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Sistem pengelolaan data mahasiswa  •  CRUD + Laporan PDF");
        sub.setFont(Theme.SUBTITLE);
        sub.setForeground(new Color(255, 255, 255, 200));
        JPanel texts = new JPanel(new GridLayout(2, 1));
        texts.setOpaque(false);
        texts.add(title);
        texts.add(sub);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        left.setOpaque(false);
        left.add(logo);
        left.add(texts);

        tfCari.putClientProperty("JTextField.placeholderText", "Cari NIM atau nama...");
        tfCari.setPreferredSize(new Dimension(240, 36));
        tfCari.getDocument().addDocumentListener(onChange(() -> loadData(tfCari.getText().trim())));

        JButton btnLaporan = colorButton("Cetak Laporan PDF", Theme.ACCENT);
        btnLaporan.setForeground(Theme.TEXT);
        btnLaporan.setPreferredSize(new Dimension(180, 38));
        btnLaporan.addActionListener(e -> cetakLaporan());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        right.setOpaque(false);
        right.add(tfCari);
        right.add(btnLaporan);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JComponent buildForm() {
        RoundedPanel panel = new RoundedPanel(24, new Color(255, 255, 255, 235));
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(16, 18, 18, 20));
        panel.setPreferredSize(new Dimension(330, 100));

        JLabel judul = new JLabel("Form Mahasiswa");
        judul.setFont(new Font("Segoe UI", Font.BOLD, 18));
        judul.setForeground(Theme.PRIMARY);
        panel.add(judul, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        tfNim.putClientProperty("JTextField.placeholderText", "10 digit, mis. 2311500011");
        tfNama.putClientProperty("JTextField.placeholderText", "Nama lengkap");
        tfEmail.putClientProperty("JTextField.placeholderText", "nama@mail.com");

        addField(fields, c, "NIM", tfNim);
        addField(fields, c, "Nama", tfNama);
        addField(fields, c, "Jurusan", cbJurusan);
        spAngkatan.setEditor(new JSpinner.NumberEditor(spAngkatan, "#")); // tampil 2023, bukan 2,023
        addField(fields, c, "Angkatan", spAngkatan);
        addField(fields, c, "Email", tfEmail);

        btnWarna.addActionListener(e -> pilihWarna());
        addField(fields, c, "Warna Profil", btnWarna);

        JButton btnFoto = new JButton("Pilih Foto...");
        btnFoto.addActionListener(e -> pilihFoto());
        JButton btnHapusFoto = new JButton("Tanpa Foto");
        btnHapusFoto.addActionListener(e -> {
            fotoDipilih = null;
            updateFotoLabel();
            refreshPreview();
        });
        JPanel fotoRow = new JPanel(new GridLayout(1, 2, 6, 0));
        fotoRow.setOpaque(false);
        fotoRow.add(btnFoto);
        fotoRow.add(btnHapusFoto);
        addField(fields, c, "Foto", fotoRow);
        lblFoto.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblFoto.setForeground(Theme.MUTED);
        c.insets = new Insets(2, 0, 0, 0);
        fields.add(lblFoto, c);

        c.weighty = 1;
        fields.add(Box.createVerticalGlue(), c);
        panel.add(fields, BorderLayout.CENTER);

        // tombol CRUD
        JButton btnTambah = colorButton("Tambah", Theme.BTN_ADD);
        JButton btnUbah = colorButton("Ubah", Theme.BTN_EDIT);
        JButton btnHapus = colorButton("Hapus", Theme.BTN_DELETE);
        JButton btnBersih = colorButton("Bersihkan", Theme.BTN_CLEAR);
        btnTambah.addActionListener(e -> tambah());
        btnUbah.addActionListener(e -> ubah());
        btnHapus.addActionListener(e -> hapus());
        btnBersih.addActionListener(e -> {
            table.clearSelection();
            resetForm();
        });
        JPanel buttons = new JPanel(new GridLayout(2, 2, 8, 8));
        buttons.setOpaque(false);
        buttons.add(btnTambah);
        buttons.add(btnUbah);
        buttons.add(btnHapus);
        buttons.add(btnBersih);
        panel.add(buttons, BorderLayout.SOUTH);

        // preview kartu ikut berubah saat mengetik
        DocumentListener preview = onChange(this::refreshPreview);
        tfNim.getDocument().addDocumentListener(preview);
        tfNama.getDocument().addDocumentListener(preview);
        tfEmail.getDocument().addDocumentListener(preview);
        cbJurusan.addActionListener(e -> refreshPreview());
        spAngkatan.addChangeListener(e -> refreshPreview());
        return panel;
    }

    private JComponent buildCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);
        center.add(card, BorderLayout.NORTH);

        table.setRowHeight(32);
        table.setFont(Theme.BODY);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new StudentRowRenderer());

        JTableHeader th = table.getTableHeader();
        th.setFont(Theme.LABEL);
        th.setBackground(Theme.PRIMARY);
        th.setForeground(Color.WHITE);
        th.setPreferredSize(new Dimension(100, 36));
        th.setReorderingAllowed(false);

        int[] widths = {40, 105, 170, 150, 75, 170, 100};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = table.getSelectedRow();
            if (row >= 0) fillForm(data.get(table.convertRowIndexToModel(row)));
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        RoundedPanel tableCard = new RoundedPanel(20, new Color(255, 255, 255, 240));
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(10, 10, 14, 13));
        tableCard.add(scroll);
        center.add(tableCard, BorderLayout.CENTER);
        return center;
    }

    /** Renderer: setiap baris diberi warna lembut sesuai warna khas mahasiswanya. */
    private class StudentRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(t, value, isSelected, false, row, col);
            setBorder(new EmptyBorder(0, 10, 0, 6));
            setIcon(null);
            Student s = data.get(t.convertRowIndexToModel(row));
            Color c = Theme.hex(s.getWarna(), Theme.PRIMARY);
            if (isSelected) {
                setBackground(Theme.blend(c, Color.BLACK, 0.15));
                setForeground(Theme.isDark(c) ? Color.WHITE : Theme.TEXT);
            } else {
                setBackground(Theme.blend(c, Color.WHITE, 0.82));
                setForeground(Theme.TEXT);
            }
            if (col == 6) setIcon(new ColorDot(c));
            return this;
        }
    }

    /** Ikon bulat kecil berisi warna. */
    private static class ColorDot implements Icon {
        private final Color color;

        ColorDot(Color color) {
            this.color = color;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = Theme.smooth(g);
            g2.setColor(color);
            g2.fillOval(x, y, 14, 14);
            g2.setColor(Color.WHITE);
            g2.drawOval(x, y, 14, 14);
            g2.dispose();
        }

        public int getIconWidth() { return 18; }

        public int getIconHeight() { return 14; }
    }

    private void addField(JPanel p, GridBagConstraints c, String label, JComponent field) {
        JLabel l = new JLabel(label);
        l.setFont(Theme.LABEL);
        l.setForeground(Theme.MUTED);
        c.insets = new Insets(6, 0, 2, 0);
        p.add(l, c);
        c.insets = new Insets(0, 0, 0, 0);
        Dimension d = new Dimension(200, 31);
        field.setPreferredSize(d);
        field.setMinimumSize(d);
        p.add(field, c);
    }

    private JButton colorButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(120, 38));
        return b;
    }

    private static DocumentListener onChange(Runnable r) {
        return new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { r.run(); }
            public void removeUpdate(DocumentEvent e) { r.run(); }
            public void changedUpdate(DocumentEvent e) { r.run(); }
        };
    }

    // =================================================================
    //  BAGIAN LOGIKA CRUD
    // =================================================================

    /** READ: ambil data dari database lalu tampilkan di tabel. */
    private void loadData(String keyword) {
        try {
            data.clear();
            data.addAll(dao.search(keyword));
            tableModel.setRowCount(0);
            for (Student s : data) {
                tableModel.addRow(new Object[]{s.getId(), s.getNim(), s.getNama(), s.getJurusan(),
                        s.getAngkatan(), s.getEmail(), s.getWarna()});
            }
            status("Menampilkan " + data.size() + " mahasiswa"
                    + (keyword.isEmpty() ? "" : " untuk pencarian \"" + keyword + "\""));
        } catch (Exception e) {
            error("Gagal memuat data", e);
        }
    }

    /** CREATE */
    private void tambah() {
        Student s = readForm();
        if (s == null) return;
        try {
            dao.insert(s);
            tfCari.setText("");
            loadData("");
            selectById(s.getId());
            status("Data " + s.getNama() + " berhasil ditambahkan.");
            ScreenCapture.saveLater(this, "create.png", 700);
            JOptionPane.showMessageDialog(this, "Data mahasiswa berhasil ditambahkan.",
                    "Tambah Data", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLIntegrityConstraintViolationException e) {
            warn("NIM " + s.getNim() + " sudah terdaftar. Gunakan NIM lain.");
        } catch (Exception e) {
            error("Gagal menambah data", e);
        }
    }

    /** UPDATE */
    private void ubah() {
        if (selected == null) {
            warn("Pilih dulu mahasiswa di tabel yang ingin diubah.");
            return;
        }
        Student s = readForm();
        if (s == null) return;
        s.setId(selected.getId());
        try {
            dao.update(s);
            loadData(tfCari.getText().trim());
            selectById(s.getId());
            status("Data " + s.getNama() + " berhasil diubah.");
            ScreenCapture.saveLater(this, "update.png", 700);
            JOptionPane.showMessageDialog(this, "Data mahasiswa berhasil diubah.",
                    "Ubah Data", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLIntegrityConstraintViolationException e) {
            warn("NIM " + s.getNim() + " sudah dipakai mahasiswa lain.");
        } catch (Exception e) {
            error("Gagal mengubah data", e);
        }
    }

    /** DELETE */
    private void hapus() {
        if (selected == null) {
            warn("Pilih dulu mahasiswa di tabel yang ingin dihapus.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Hapus data " + selected.getNama() + " (" + selected.getNim() + ")?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            dao.delete(selected.getId());
            String nama = selected.getNama();
            loadData(tfCari.getText().trim());
            resetForm();
            status("Data " + nama + " berhasil dihapus.");
            ScreenCapture.saveLater(this, "delete.png", 700);
            JOptionPane.showMessageDialog(this, "Data " + nama + " berhasil dihapus.",
                    "Hapus Data", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            error("Gagal menghapus data", e);
        }
    }

    /** LAPORAN: buka form rentang NIM lalu cetak ke PDF (JasperReports). */
    private void cetakLaporan() {
        try {
            List<String> nims = new ArrayList<>();
            for (Student s : dao.getAll()) nims.add(s.getNim());
            if (nims.isEmpty()) {
                warn("Belum ada data mahasiswa untuk dicetak.");
                return;
            }
            new ReportDialog(this, nims).setVisible(true);
        } catch (Exception e) {
            error("Gagal membuka form laporan", e);
        }
    }

    /** Baca isi form + validasi. Mengembalikan null kalau ada yang salah. */
    private Student readForm() {
        String nim = tfNim.getText().trim();
        String nama = tfNama.getText().trim();
        String email = tfEmail.getText().trim();
        if (!nim.matches("\\d{10}")) {
            warn("NIM harus 10 digit angka.");
            tfNim.requestFocus();
            return null;
        }
        if (nama.isEmpty()) {
            warn("Nama tidak boleh kosong.");
            tfNama.requestFocus();
            return null;
        }
        if (!email.isEmpty() && !email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            warn("Format email tidak valid.");
            tfEmail.requestFocus();
            return null;
        }
        return new Student(nim, nama, (String) cbJurusan.getSelectedItem(),
                (Integer) spAngkatan.getValue(), email.isEmpty() ? null : email,
                warnaDipilih, fotoDipilih);
    }

    private void fillForm(Student s) {
        filling = true;
        selected = s;
        tfNim.setText(s.getNim());
        tfNama.setText(s.getNama());
        cbJurusan.setSelectedItem(s.getJurusan());
        spAngkatan.setValue(s.getAngkatan());
        tfEmail.setText(s.getEmail() == null ? "" : s.getEmail());
        warnaDipilih = s.getWarna();
        fotoDipilih = s.getFoto();
        filling = false;
        updateWarnaButton();
        updateFotoLabel();
        card.setStudent(s);
    }

    private void resetForm() {
        filling = true;
        selected = null;
        tfNim.setText("");
        tfNama.setText("");
        cbJurusan.setSelectedIndex(0);
        spAngkatan.setValue(Year.now().getValue());
        tfEmail.setText("");
        warnaDipilih = Theme.randomColor(); // mahasiswa baru dapat warna acak
        fotoDipilih = null;
        filling = false;
        updateWarnaButton();
        updateFotoLabel();
        card.setStudent(null);
    }

    /** Kartu profil ikut berubah sesuai isi form (preview). */
    private void refreshPreview() {
        if (filling) return;
        if (selected == null && tfNama.getText().isBlank() && tfNim.getText().isBlank()) {
            card.setStudent(null);
            return;
        }
        Object angkatan = spAngkatan.getValue();
        card.setStudent(new Student(tfNim.getText().trim(), tfNama.getText().trim(),
                (String) cbJurusan.getSelectedItem(), (Integer) angkatan,
                tfEmail.getText().trim(), warnaDipilih, fotoDipilih));
    }

    private void pilihWarna() {
        Color c = JColorChooser.showDialog(this, "Pilih warna profil",
                Theme.hex(warnaDipilih, Theme.PRIMARY));
        if (c != null) {
            warnaDipilih = Theme.toHex(c);
            updateWarnaButton();
            refreshPreview();
        }
    }

    /** Foto disalin ke folder "photos" di dalam proyek, path relatifnya disimpan ke DB. */
    private void pilihFoto() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Gambar (jpg, png)", "jpg", "jpeg", "png"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            File src = fc.getSelectedFile();
            Path dir = Paths.get("photos");
            Files.createDirectories(dir);
            String name = System.currentTimeMillis() + "_" + src.getName().replaceAll("\\s+", "_");
            Path dest = dir.resolve(name);
            Files.copy(src.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            fotoDipilih = "photos/" + name;
            updateFotoLabel();
            refreshPreview();
        } catch (Exception e) {
            error("Gagal menyalin foto", e);
        }
    }

    private void updateWarnaButton() {
        Color c = Theme.hex(warnaDipilih, Theme.PRIMARY);
        btnWarna.setText(warnaDipilih + "   (klik untuk ganti)");
        btnWarna.setBackground(c);
        btnWarna.setForeground(Theme.isDark(c) ? Color.WHITE : Theme.TEXT);
    }

    private void updateFotoLabel() {
        lblFoto.setText(fotoDipilih == null ? "Belum ada foto, avatar memakai inisial nama."
                : "Foto: " + fotoDipilih);
    }

    private void selectById(int id) {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getId() == id) {
                int viewRow = table.convertRowIndexToView(i);
                table.setRowSelectionInterval(viewRow, viewRow);
                table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
                return;
            }
        }
    }

    private void status(String msg) {
        lblStatus.setText("  " + msg);
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Periksa Input", JOptionPane.WARNING_MESSAGE);
    }

    private void error(String title, Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, title + ":\n" + e.getMessage(), "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
