package dao;

import model.Student;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentDAO implements DAO<Student> {
    private final Connection connection;

    public StudentDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<Student> get(int id) {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? Optional.of(mapResultSetToStudent(rs)) : Optional.empty();
        } catch (SQLException e) {
            handleException("get student by ID", e);
            return Optional.empty();
        }
    }

    @Override
    public List<Student> getAll() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY student_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
        } catch (SQLException e) {
            handleException("get all students", e);
        }
        return students;
    }

    @Override
    public boolean save(Student student) {
        String sql = "INSERT INTO students (first_name, last_name, email, phone, career, semester, date_of_birth, gpa) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, new String[]{"student_id"})) {
            setStudentParameters(pstmt, student);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        student.setStudentId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            handleException("save student", e);
            return false;
        }
    }

    @Override
    public boolean update(Student student) {
        String sql = "UPDATE students SET first_name = ?, last_name = ?, email = ?, phone = ?, " +
                "career = ?, semester = ?, date_of_birth = ?, gpa = ?, status = ? " +
                "WHERE student_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setStudentParameters(pstmt, student);
            pstmt.setString(9, student.getStatus());
            pstmt.setInt(10, student.getStudentId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleException("update student", e);
            return false;
        }
    }

    @Override
    public boolean delete(Student student) {
        return deleteById(student.getStudentId());
    }

    @Override
    public boolean deleteById(int id) {
        String sql = "DELETE FROM students WHERE student_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleException("delete student", e);
            return false;
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM students";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            handleException("count students", e);
            return 0;
        }
    }

    @Override
    public boolean exists(int id) {
        return get(id).isPresent();
    }

    public Optional<Student> getByEmail(String email) {
        String sql = "SELECT * FROM students WHERE email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? Optional.of(mapResultSetToStudent(rs)) : Optional.empty();
        } catch (SQLException e) {
            handleException("get student by email", e);
            return Optional.empty();
        }
    }

    public List<Student> getByName(String name) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students WHERE UPPER(first_name) LIKE ? OR UPPER(last_name) LIKE ? ORDER BY last_name, first_name";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + name.toUpperCase() + "%");
            pstmt.setString(2, "%" + name.toUpperCase() + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
        } catch (SQLException e) {
            handleException("get students by name", e);
        }
        return students;
    }

    public List<Student> getByCareer(String career) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students WHERE UPPER(career) LIKE ? ORDER BY last_name, first_name";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + career.toUpperCase() + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
        } catch (SQLException e) {
            handleException("get students by career", e);
        }
        return students;
    }

    public List<Student> getBySemester(int semester) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students WHERE semester = ? ORDER BY last_name, first_name";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, semester);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
        } catch (SQLException e) {
            handleException("get students by semester", e);
        }
        return students;
    }

    public List<Student> getByStatus(String status) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students WHERE status = ? ORDER BY last_name, first_name";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
        } catch (SQLException e) {
            handleException("get students by status", e);
        }
        return students;
    }

    public boolean updateGPA(int studentId, double gpa) {
        String sql = "UPDATE students SET gpa = ? WHERE student_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, gpa);
            pstmt.setInt(2, studentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleException("update student GPA", e);
            return false;
        }
    }

    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setStudentId(rs.getInt("student_id"));
        student.setFirstName(rs.getString("first_name"));
        student.setLastName(rs.getString("last_name"));
        student.setEmail(rs.getString("email"));
        student.setPhone(rs.getString("phone"));
        student.setCareer(rs.getString("career"));
        student.setSemester(rs.getInt("semester"));
        student.setEnrollmentDate(rs.getString("enrollment_date"));

        if (columnExists(rs, "date_of_birth")) {
            java.sql.Date dob = rs.getDate("date_of_birth");
            student.setDateOfBirth(dob != null ? dob.toString() : null);
        }
        if (columnExists(rs, "gpa")) {
            student.setGpa(rs.getDouble("gpa"));
        }
        if (columnExists(rs, "status")) {
            student.setStatus(rs.getString("status"));
        }

        return student;
    }

    private void setStudentParameters(PreparedStatement pstmt, Student student) throws SQLException {
        pstmt.setString(1, student.getFirstName());
        pstmt.setString(2, student.getLastName());
        pstmt.setString(3, student.getEmail());
        pstmt.setString(4, student.getPhone());
        pstmt.setString(5, student.getCareer());
        pstmt.setInt(6, student.getSemester());

        if (student.getDateOfBirth() != null && !student.getDateOfBirth().isEmpty()) {
            pstmt.setDate(7, java.sql.Date.valueOf(student.getDateOfBirth()));
        } else {
            pstmt.setNull(7, java.sql.Types.DATE);
        }

        pstmt.setDouble(8, student.getGpa());
    }

    private boolean columnExists(ResultSet rs, String columnName) {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}