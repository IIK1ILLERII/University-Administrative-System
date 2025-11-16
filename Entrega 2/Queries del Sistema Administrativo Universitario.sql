"SCRIPT INSERT:"

INSERT INTO students (student_id, first_name, last_name, email, career, semester, gpa) 
VALUES (1, 'Juan', 'Pérez', 'juan.perez@estudiante.edu', 'Ingeniería de Sistemas', 5, 4.2);

INSERT INTO professors (professor_id, first_name, last_name, email, department, specialty) 
VALUES (1, 'Carlos', 'Mendoza', 'carlos.mendoza@universidad.edu', 'Ingeniería', 'Base de Datos');

INSERT INTO subjects (subject_id, subject_name, code, credits, professor_id, department) 
VALUES (1, 'Base de Datos I', 'BD101', 4, 1, 'Ingeniería de Sistemas');

INSERT INTO student_subjects (enrollment_id, student_id, subject_id, professor_id, semester, grade) 
VALUES (1, 1, 1, 1, '2024-01', 85.5);

"UPDATE:"

SCRIPT UPDATE:
UPDATE students SET gpa = 4.5 WHERE student_id = 1;

UPDATE professors SET salary = 60000 WHERE professor_id = 1;

UPDATE student_subjects SET grade = 90.0, status = 'PASSED' WHERE enrollment_id = 1;

UPDATE students SET status = 'GRADUATED' WHERE student_id = 1;

"DELETE:"

SCRIPT DELETE:
DELETE FROM student_subjects WHERE enrollment_id = 1;

DELETE FROM subjects WHERE subject_id = 1;

DELETE FROM students WHERE student_id = 1;

CREATE TABLE students (
    student_id NUMBER PRIMARY KEY,
    first_name VARCHAR2(50) NOT NULL,
    last_name VARCHAR2(50) NOT NULL,
    email VARCHAR2(100) UNIQUE NOT NULL,
    phone VARCHAR2(20),
    career VARCHAR2(100) NOT NULL,
    semester NUMBER NOT NULL CHECK (semester BETWEEN 1 AND 12),
    enrollment_date DATE DEFAULT SYSDATE,
    date_of_birth DATE,
    status VARCHAR2(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'GRADUATED')),
    gpa NUMBER(3,2) DEFAULT 0.0 CHECK (gpa BETWEEN 0.0 AND 5.0)
);

Script Tabla Professors:
CREATE TABLE professors (
    professor_id NUMBER PRIMARY KEY,
    first_name VARCHAR2(50) NOT NULL,
    last_name VARCHAR2(50) NOT NULL,
    email VARCHAR2(100) UNIQUE NOT NULL,
    phone VARCHAR2(20),
    department VARCHAR2(100) NOT NULL,
    specialty VARCHAR2(100),
    salary NUMBER(10,2) DEFAULT 0.0,
    status VARCHAR2(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'ON_LEAVE'))
);

CREATE TABLE subjects (
    subject_id NUMBER PRIMARY KEY,
    subject_name VARCHAR2(100) NOT NULL,
    code VARCHAR2(20) UNIQUE NOT NULL,
    credits NUMBER NOT NULL CHECK (credits BETWEEN 1 AND 10),
    professor_id NUMBER,
    department VARCHAR2(100) NOT NULL,
    difficulty_level VARCHAR2(20) DEFAULT 'BASIC' CHECK (difficulty_level IN ('BASIC', 'INTERMEDIATE', 'ADVANCED')),
    hours_per_week NUMBER DEFAULT 4 CHECK (hours_per_week BETWEEN 1 AND 20),
    FOREIGN KEY (professor_id) REFERENCES professors(professor_id) ON DELETE SET NULL
);

CREATE TABLE student_subjects (
    enrollment_id NUMBER PRIMARY KEY,
    student_id NUMBER NOT NULL,
    subject_id NUMBER NOT NULL,
    professor_id NUMBER NOT NULL,
    enrollment_date DATE DEFAULT SYSDATE,
    grade NUMBER(5,2) CHECK (grade BETWEEN 0 AND 100),
    status VARCHAR2(20) DEFAULT 'ENROLLED' CHECK (status IN ('ENROLLED', 'PASSED', 'FAILED', 'DROPPED')),
    semester VARCHAR2(20) NOT NULL,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE,
    FOREIGN KEY (professor_id) REFERENCES professors(professor_id) ON DELETE CASCADE
);


"Script Consultas Joins:"

SELECT s.first_name, s.last_name, sub.subject_name, e.grade, e.status
FROM students s
INNER JOIN student_subjects e ON s.student_id = e.student_id
INNER JOIN subjects sub ON e.subject_id = sub.subject_id;

SELECT s.first_name, s.last_name, sub.subject_name, 
    p.first_name || ' ' || p.last_name AS professor_name,
    e.grade, e.status, e.semester
FROM student_subjects e
JOIN students s ON e.student_id = s.student_id
JOIN subjects sub ON e.subject_id = sub.subject_id
JOIN professors p ON e.professor_id = p.professor_id;

"Subconsultas:"

Script Subconsultas:
SELECT first_name, last_name, gpa
FROM students
WHERE gpa > (SELECT AVG(gpa) FROM students);

SELECT first_name, last_name, career
FROM students s
WHERE EXISTS (
    SELECT 1 FROM student_subjects e 
    WHERE e.student_id = s.student_id AND e.status = 'PASSED'
);

"Funciones agregadas:"

Script Funciones agregadas:
SELECT sub.subject_name,
        COUNT(e.enrollment_id) AS total_estudiantes,
        AVG(e.grade) AS promedio,
        MAX(e.grade) AS nota_maxima,
        MIN(e.grade) AS nota_minima
FROM subjects sub
JOIN student_subjects e ON sub.subject_id = e.subject_id
WHERE e.grade IS NOT NULL
GROUP BY sub.subject_id, sub.subject_name;

SELECT career, semester, 
        COUNT(*) AS cantidad_estudiantes,
        AVG(gpa) AS promedio_gpa
FROM students
WHERE status = 'ACTIVE'
GROUP BY career, semester
ORDER BY career, semester;

"Script Indices:"

CREATE INDEX idx_students_email ON students(email);
CREATE INDEX idx_students_career ON students(career);
CREATE INDEX idx_students_semester ON students(semester);

CREATE INDEX idx_professors_department ON professors(department);
CREATE INDEX idx_professors_email ON professors(email);

CREATE INDEX idx_subjects_code ON subjects(code);
CREATE INDEX idx_subjects_department ON subjects(department);

CREATE INDEX idx_enrollments_student ON student_subjects(student_id);
CREATE INDEX idx_enrollments_subject ON student_subjects(subject_id);
CREATE INDEX idx_enrollments_semester ON student_subjects(semester);
CREATE INDEX idx_enrollments_status ON student_subjects(status);

"Transacciones:"

BEGIN
    INSERT INTO students VALUES (100, 'Test', 'User', 'test@test.com', 'Ingeniería', 1, 3.5);
    INSERT INTO students VALUES (100, 'Duplicate', 'User', 'test2@test.com', 'Ingeniería', 1, 3.5);
    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('Transacción revertida: ' || SQLERRM);
END;
/

DECLARE
    v_enrollment_id NUMBER;
BEGIN
    SAVEPOINT inicio_inscripcion;
    INSERT INTO student_subjects (enrollment_id, student_id, subject_id, professor_id, semester)
    VALUES (seq_enrollments.NEXTVAL, 1, 1, 1, '2024-01')
RETURNING enrollment_id INTO v_enrollment_id;

    UPDATE students 
    SET semester = semester + 0.1
    WHERE student_id = 1;
    INSERT INTO system_log (log_id, action, user_id, timestamp)
    VALUES (seq_log.NEXTVAL, 'INSCRIPCION', 'ADMIN', SYSDATE);
    COMMIT;

    DBMS_OUTPUT.PUT_LINE('Inscripción exitosa. ID: ' || v_enrollment_id);

EXCEPTION
    WHEN OTHERS THEN
     ROLLBACK TO inicio_inscripcion;
        DBMS_OUTPUT.PUT_LINE('Error en inscripción: ' || SQLERRM);
        RAISE;
END;
/
