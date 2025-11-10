CREATE TABLE students (
    student_id NUMBER PRIMARY KEY,
    first_name VARCHAR2(100) NOT NULL,
    last_name VARCHAR2(100) NOT NULL,
    email VARCHAR2(150) UNIQUE NOT NULL,
    phone VARCHAR2(15),
    career VARCHAR2(100) NOT NULL,
    semester NUMBER NOT NULL,
    enrollment_date DATE DEFAULT SYSDATE
);

CREATE TABLE professors (
    professor_id NUMBER PRIMARY KEY,
    first_name VARCHAR2(100) NOT NULL,
    last_name VARCHAR2(100) NOT NULL,
    email VARCHAR2(150) UNIQUE NOT NULL,
    phone VARCHAR2(15),
    department VARCHAR2(100) NOT NULL,
    specialty VARCHAR2(100) NOT NULL
);

CREATE TABLE professors (
    professor_id NUMBER PRIMARY KEY,
    first_name VARCHAR2(100) NOT NULL,
    last_name VARCHAR2(100) NOT NULL,
    email VARCHAR2(150) UNIQUE NOT NULL,
    phone VARCHAR2(15),
    department VARCHAR2(100) NOT NULL,
    specialty VARCHAR2(100) NOT NULL
);
