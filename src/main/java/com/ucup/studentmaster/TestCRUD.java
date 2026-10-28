package com.ucup.studentmaster;

import com.ucup.studentmaster.dao.StudentDAO;
import com.ucup.studentmaster.model.Student;

/**
 * Uji cepat CRUD lewat console sebelum membuat GUI.
 */
public class TestCRUD {
    public static void main(String[] args) {
        StudentDAO dao = new StudentDAO();
        try {
            System.out.println("=== READ: semua data ===");
            dao.getAll().forEach(System.out::println);

            System.out.println("\n=== CREATE ===");
            Student s = new Student("2311599999", "Mahasiswa Tes", "Teknik Informatika",
                    2023, "tes@mail.com", "#000000", null);
            System.out.println("Insert berhasil? " + dao.insert(s) + " | id baru = " + s.getId());

            System.out.println("\n=== UPDATE ===");
            s.setNama("Mahasiswa Tes (Diubah)");
            System.out.println("Update berhasil? " + dao.update(s));
            dao.search("2311599999").forEach(System.out::println);

            System.out.println("\n=== DELETE ===");
            System.out.println("Delete berhasil? " + dao.delete(s.getId()));
            System.out.println("Sisa data dengan NIM itu: " + dao.search("2311599999").size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
