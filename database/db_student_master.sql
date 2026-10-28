CREATE DATABASE IF NOT EXISTS db_student_master;
USE db_student_master;

CREATE TABLE IF NOT EXISTS students (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    nim       CHAR(10)     NOT NULL UNIQUE,
    nama      VARCHAR(100) NOT NULL,
    jurusan   VARCHAR(50)  NOT NULL,
    angkatan  INT          NOT NULL,
    email     VARCHAR(100),
    warna     CHAR(7)      NOT NULL DEFAULT '#4A90E2',
    foto      VARCHAR(255) NULL
);

INSERT INTO students (nim, nama, jurusan, angkatan, email, warna) VALUES
('2311500001', 'Andi Pratama',     'Teknik Informatika', 2023, 'andi@mail.com',   '#E57373'),
('2311500002', 'Bunga Lestari',    'Sistem Informasi',   2023, 'bunga@mail.com',  '#F06292'),
('2311500003', 'Cahyo Nugroho',    'Teknik Informatika', 2023, 'cahyo@mail.com',  '#BA68C8'),
('2311500004', 'Dewi Anggraini',   'Manajemen',          2023, 'dewi@mail.com',   '#7986CB'),
('2311500005', 'Eko Saputra',      'Akuntansi',          2023, 'eko@mail.com',    '#4FC3F7'),
('2311500006', 'Fitri Handayani',  'Sistem Informasi',   2023, 'fitri@mail.com',  '#4DB6AC'),
('2311500007', 'Gilang Ramadhan',  'Teknik Informatika', 2023, 'gilang@mail.com', '#81C784'),
('2311500008', 'Hana Permata',     'Manajemen',          2023, 'hana@mail.com',   '#FFD54F'),
('2311500009', 'Indra Wijaya',     'Akuntansi',          2023, 'indra@mail.com',  '#FF8A65'),
('2311500010', 'Joko Santoso',     'Teknik Informatika', 2023, 'joko@mail.com',   '#A1887F');
