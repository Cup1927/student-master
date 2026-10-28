package com.ucup.studentmaster.model;

/**
 * Model (POJO) yang mewakili satu baris di tabel students.
 */
public class Student {
    private int id;
    private String nim;
    private String nama;
    private String jurusan;
    private int angkatan;
    private String email;
    private String warna; // warna khas mahasiswa, format hex mis. #E57373
    private String foto;  // path file foto (boleh null)

    public Student() {
    }

    public Student(String nim, String nama, String jurusan, int angkatan,
                   String email, String warna, String foto) {
        this.nim = nim;
        this.nama = nama;
        this.jurusan = jurusan;
        this.angkatan = angkatan;
        this.email = email;
        this.warna = warna;
        this.foto = foto;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNim() { return nim; }
    public void setNim(String nim) { this.nim = nim; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getJurusan() { return jurusan; }
    public void setJurusan(String jurusan) { this.jurusan = jurusan; }

    public int getAngkatan() { return angkatan; }
    public void setAngkatan(int angkatan) { this.angkatan = angkatan; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getWarna() { return warna; }
    public void setWarna(String warna) { this.warna = warna; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    @Override
    public String toString() {
        return id + " | " + nim + " | " + nama + " | " + jurusan + " | " + angkatan + " | " + warna;
    }
}
