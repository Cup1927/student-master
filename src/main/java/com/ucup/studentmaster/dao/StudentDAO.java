package com.ucup.studentmaster.dao;

import com.ucup.studentmaster.db.DBConnection;
import com.ucup.studentmaster.model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object: semua query CRUD ke tabel students ada di sini.
 */
public class StudentDAO {

    // READ - ambil semua data
    public List<Student> getAll() throws SQLException {
        return search("");
    }

    // READ - cari berdasarkan NIM atau nama
    public List<Student> search(String keyword) throws SQLException {
        String sql = "SELECT * FROM students WHERE nim LIKE ? OR nama LIKE ? ORDER BY nim";
        List<Student> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    // Hitung jumlah mahasiswa dalam rentang NIM (dipakai form laporan)
    public int countByRange(String nimAwal, String nimAkhir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM students WHERE nim BETWEEN ? AND ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nimAwal);
            ps.setString(2, nimAkhir);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // CREATE
    public boolean insert(Student s) throws SQLException {
        String sql = "INSERT INTO students (nim, nama, jurusan, angkatan, email, warna, foto) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fill(ps, s);
            int rows = ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    s.setId(keys.getInt(1)); // simpan id baru ke objek
                }
            }
            return rows > 0;
        }
    }

    // UPDATE
    public boolean update(Student s) throws SQLException {
        String sql = "UPDATE students SET nim=?, nama=?, jurusan=?, angkatan=?, email=?, warna=?, foto=? "
                   + "WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            fill(ps, s);
            ps.setInt(8, s.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // DELETE
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM students WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Helper: isi parameter ? nomor 1-7 (dipakai insert & update)
    private void fill(PreparedStatement ps, Student s) throws SQLException {
        ps.setString(1, s.getNim());
        ps.setString(2, s.getNama());
        ps.setString(3, s.getJurusan());
        ps.setInt(4, s.getAngkatan());
        ps.setString(5, s.getEmail());
        ps.setString(6, s.getWarna());
        ps.setString(7, s.getFoto());
    }

    // Helper: ubah 1 baris hasil query jadi objek Student
    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student(
                rs.getString("nim"),
                rs.getString("nama"),
                rs.getString("jurusan"),
                rs.getInt("angkatan"),
                rs.getString("email"),
                rs.getString("warna"),
                rs.getString("foto")
        );
        s.setId(rs.getInt("id"));
        return s;
    }
}
