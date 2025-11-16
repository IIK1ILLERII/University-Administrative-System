package dao;

import model.Enrollment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EnrollmentDAO implements DAO<Enrollment> {
    private final Connection connection;

    public EnrollmentDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<Enrollment> get(int id) {
        String sql = "SELECT e.*, s.first_name || ' ' || s.last_name AS student_name, " +
                "sub.subject_name, p.first_name || ' ' || p.last_name AS professor_name " +
                "FROM student_subjects e " +
                "JOIN students s ON e.student_id = s.student_id " +
                "JOIN subjects sub ON e.subject_id = sub.subject_id " +
                "JOIN professors p ON e.professor_id = p.professor_id " +
                "WHERE e.enrollment_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? Optional.of(mapResultSetToEnrollment(rs)) : Optional.empty();
        } catch (SQLException e) {
            handleException("get enrollment by ID", e);
            return Optional.empty();
        }
    }

    @Override
    public List<Enrollment> getAll() {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT e.*, s.first_name || ' ' || s.last_name AS student_name, " +
                "sub.subject_name, p.first_name || ' ' || p.last_name AS professor_name " +
                "FROM student_subjects e " +
                "JOIN students s ON e.student_id = s.student_id " +
                "JOIN subjects sub ON e.subject_id = sub.subject_id " +
                "JOIN professors p ON e.professor_id = p.professor_id " +
                "ORDER BY e.enrollment_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                enrollments.add(mapResultSetToEnrollment(rs));
            }
        } catch (SQLException e) {
            handleException("get all enrollments", e);
        }
        return enrollments;
    }

    @Override
    public boolean save(Enrollment enrollment) {
        String sql = "INSERT INTO student_subjects (student_id, subject_id, professor_id, semester) " +
                "VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, new String[]{"enrollment_id"})) {
            pstmt.setInt(1, enrollment.getStudentId());
            pstmt.setInt(2, enrollment.getSubjectId());
            pstmt.setInt(3, enrollment.getProfessorId());
            pstmt.setString(4, enrollment.getSemester());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        enrollment.setEnrollmentId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            handleException("save enrollment", e);
            return false;
        }
    }

    @Override
    public boolean update(Enrollment enrollment) {
        String sql = "UPDATE student_subjects SET grade = ?, status = ? WHERE enrollment_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            if (enrollment.getGrade() != null) {
                pstmt.setDouble(1, enrollment.getGrade());
            } else {
                pstmt.setNull(1, java.sql.Types.DOUBLE);
            }
            pstmt.setString(2, enrollment.getStatus());
            pstmt.setInt(3, enrollment.getEnrollmentId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleException("update enrollment", e);
            return false;
        }
    }

    @Override
    public boolean delete(Enrollment enrollment) {
        return deleteById(enrollment.getEnrollmentId());
    }

    @Override
    public boolean deleteById(int id) {
        String sql = "DELETE FROM student_subjects WHERE enrollment_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleException("delete enrollment", e);
            return false;
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM student_subjects";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            handleException("count enrollments", e);
            return 0;
        }
    }

    @Override
    public boolean exists(int id) {
        return get(id).isPresent();
    }

    public List<Enrollment> getByStudent(int studentId) {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT e.*, s.first_name || ' ' || s.last_name AS student_name, " +
                "sub.subject_name, p.first_name || ' ' || p.last_name AS professor_name " +
                "FROM student_subjects e " +
                "JOIN students s ON e.student_id = s.student_id " +
                "JOIN subjects sub ON e.subject_id = sub.subject_id " +
                "JOIN professors p ON e.professor_id = p.professor_id " +
                "WHERE e.student_id = ? ORDER BY e.semester DESC, sub.subject_name";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                enrollments.add(mapResultSetToEnrollment(rs));
            }
        } catch (SQLException e) {
            handleException("get enrollments by student", e);
        }
        return enrollments;
    }

    public List<Enrollment> getBySubject(int subjectId) {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT e.*, s.first_name || ' ' || s.last_name AS student_name, " +
                "sub.subject_name, p.first_name || ' ' || p.last_name AS professor_name " +
                "FROM student_subjects e " +
                "JOIN students s ON e.student_id = s.student_id " +
                "JOIN subjects sub ON e.subject_id = sub.subject_id " +
                "JOIN professors p ON e.professor_id = p.professor_id " +
                "WHERE e.subject_id = ? ORDER BY s.last_name, s.first_name";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, subjectId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                enrollments.add(mapResultSetToEnrollment(rs));
            }
        } catch (SQLException e) {
            handleException("get enrollments by subject", e);
        }
        return enrollments;
    }

    public List<Enrollment> getByProfessor(int professorId) {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT e.*, s.first_name || ' ' || s.last_name AS student_name, " +
                "sub.subject_name, p.first_name || ' ' || p.last_name AS professor_name " +
                "FROM student_subjects e " +
                "JOIN students s ON e.student_id = s.student_id " +
                "JOIN subjects sub ON e.subject_id = sub.subject_id " +
                "JOIN professors p ON e.professor_id = p.professor_id " +
                "WHERE e.professor_id = ? ORDER BY e.semester DESC, sub.subject_name";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, professorId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                enrollments.add(mapResultSetToEnrollment(rs));
            }
        } catch (SQLException e) {
            handleException("get enrollments by professor", e);
        }
        return enrollments;
    }

    public List<Enrollment> getBySemester(String semester) {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT e.*, s.first_name || ' ' || s.last_name AS student_name, " +
                "sub.subject_name, p.first_name || ' ' || p.last_name AS professor_name " +
                "FROM student_subjects e " +
                "JOIN students s ON e.student_id = s.student_id " +
                "JOIN subjects sub ON e.subject_id = sub.subject_id " +
                "JOIN professors p ON e.professor_id = p.professor_id " +
                "WHERE e.semester = ? ORDER BY s.last_name, s.first_name";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, semester);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                enrollments.add(mapResultSetToEnrollment(rs));
            }
        } catch (SQLException e) {
            handleException("get enrollments by semester", e);
        }
        return enrollments;
    }

    public List<Enrollment> getByStatus(String status) {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT e.*, s.first_name || ' ' || s.last_name AS student_name, " +
                "sub.subject_name, p.first_name || ' ' || p.last_name AS professor_name " +
                "FROM student_subjects e " +
                "JOIN students s ON e.student_id = s.student_id " +
                "JOIN subjects sub ON e.subject_id = sub.subject_id " +
                "JOIN professors p ON e.professor_id = p.professor_id " +
                "WHERE e.status = ? ORDER BY e.semester DESC, sub.subject_name";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                enrollments.add(mapResultSetToEnrollment(rs));
            }
        } catch (SQLException e) {
            handleException("get enrollments by status", e);
        }
        return enrollments;
    }

    public boolean updateGrade(int enrollmentId, double grade) {
        String sql = "UPDATE student_subjects SET grade = ?, " +
                "status = CASE WHEN ? >= 70 THEN 'PASSED' ELSE 'FAILED' END " +
                "WHERE enrollment_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, grade);
            pstmt.setDouble(2, grade);
            pstmt.setInt(3, enrollmentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleException("update enrollment grade", e);
            return false;
        }
    }

    public boolean dropEnrollment(int enrollmentId) {
        String sql = "UPDATE student_subjects SET status = 'DROPPED' WHERE enrollment_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, enrollmentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleException("drop enrollment", e);
            return false;
        }
    }

    public double getAverageGradeBySubject(int subjectId) {
        String sql = "SELECT AVG(grade) FROM student_subjects WHERE subject_id = ? AND grade IS NOT NULL";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, subjectId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0.0;
        } catch (SQLException e) {
            handleException("get average grade by subject", e);
            return 0.0;
        }
    }

    public int getEnrollmentCountBySubject(int subjectId) {
        String sql = "SELECT COUNT(*) FROM student_subjects WHERE subject_id = ? AND status = 'ENROLLED'";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, subjectId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            handleException("get enrollment count by subject", e);
            return 0;
        }
    }

    private Enrollment mapResultSetToEnrollment(ResultSet rs) throws SQLException {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentId(rs.getInt("enrollment_id"));
        enrollment.setStudentId(rs.getInt("student_id"));
        enrollment.setSubjectId(rs.getInt("subject_id"));
        enrollment.setProfessorId(rs.getInt("professor_id"));
        enrollment.setEnrollmentDate(rs.getString("enrollment_date"));
        enrollment.setSemester(rs.getString("semester"));

        enrollment.setStudentName(rs.getString("student_name"));
        enrollment.setSubjectName(rs.getString("subject_name"));
        enrollment.setProfessorName(rs.getString("professor_name"));

        if (columnExists(rs, "grade")) {
            double grade = rs.getDouble("grade");
            enrollment.setGrade(rs.wasNull() ? null : grade);
        }
        if (columnExists(rs, "status")) {
            enrollment.setStatus(rs.getString("status"));
        }

        return enrollment;
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