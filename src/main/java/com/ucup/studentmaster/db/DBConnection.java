package com.ucup.studentmaster.db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Membuka koneksi ke MySQL.
 * Username & password dibaca dari file src/main/resources/db.properties
 * (file itu TIDAK ikut di-push ke GitHub, lihat .gitignore).
 */
public class DBConnection {
    private static final Properties CONFIG = new Properties();

    static {
        try (InputStream in = DBConnection.class.getResourceAsStream("/db.properties")) {
            if (in == null) {
                throw new IllegalStateException(
                        "File db.properties tidak ditemukan. Salin db.properties.example menjadi db.properties.");
            }
            CONFIG.load(in);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                CONFIG.getProperty("db.url"),
                CONFIG.getProperty("db.user"),
                CONFIG.getProperty("db.password", ""));
    }
}
