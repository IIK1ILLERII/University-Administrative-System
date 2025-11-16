package dao;

import model.Subject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SubjectDAO implements DAO<Subject> {
    private final Connection connection;

    public SubjectDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<Subject> get(int id) {
        String sql = "SELECT s.*, p.first_name || ' ' || p.last_name AS professor_name " +
                "FROM subjects s LEFT JOIN professors p ON s.professor_id = p.professor_id " +
                "WHERE s.subject_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? Optional.of(mapResultSetToSubject(rs)) : Optional.empty();
        } catch (SQLException e) {
            handleException("get subject by ID", e);
            return Optional.empty();
        }
    }

    @Override
    public List<Subject> getAll() {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT s.*, p.first_name || ' ' || p.last_name AS professor_name " +
                "FROM subjects s LEFT JOIN professors p ON s.professor_id = p.professor_id " +
                "ORDER BY s.subject_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                subjects.add(mapResultSetToSubject(rs));
            }
        } catch (SQLException e) {
            handleException("get all subjects", e);
        }
        return subjects;
    }

    @Override
    public boolean save(Subject subject) {
        String sql = "INSERT INTO subjects (subject_name, code, credits, professor_id, department, difficulty_level, hours_per_week) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, new String[]{"subject_id"})) {
            setSubjectParameters(pstmt, subject);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        subject.setSubjectId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            handleException("save subject", e);
            return false;
        }
    }

    @Override
    public boolean update(Subject subject) {
        String sql = "UPDATE subjects SET subject_name = ?, code = ?, credits = ?, professor_id = ?, " +
                "department = ?, difficulty_level = ?, hours_per_week = ? " +
                "WHERE subject_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, subject.getSubjectName());
            pstmt.setString(2, subject.getCode());
            pstmt.setInt(3, subject.getCredits());

            if (subject.getProfessorId() > 0) {
                pstmt.setInt(4, subject.getProfessorId());
            } else {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            }

            pstmt.setString(5, subject.getDepartment());
            pstmt.setString(6, subject.getDifficultyLevel());
            pstmt.setInt(7, subject.getHoursPerWeek());
            pstmt.setInt(8, subject.getSubjectId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleException("update subject", e);
            return false;
        }
    }

    @Override
    public boolean delete(Subject subject) {
        return deleteById(subject.getSubjectId());
    }

    @Override
    public boolean deleteById(int id) {
        String sql = "DELETE FROM subjects WHERE subject_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleException("delete subject", e);
            return false;
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM subjects";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            handleException("count subjects", e);
            return 0;
        }
    }

    @Override
    public boolean exists(int id) {
        return get(id).isPresent();
    }

    public Optional<Subject> getByCode(String code) {
        String sql = "SELECT s.*, p.first_name || ' ' || p.last_name AS professor_name " +
                "FROM subjects s LEFT JOIN professors p ON s.professor_id = p.professor_id " +
                "WHERE s.code = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? Optional.of(mapResultSetToSubject(rs)) : Optional.empty();
        } catch (SQLException e) {
            handleException("get subject by code", e);
            return Optional.empty();
        }
    }

    public List<Subject> getByProfessor(int professorId) {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT s.*, p.first_name || ' ' || p.last_name AS professor_name " +
                "FROM subjects s LEFT JOIN professors p ON s.professor_id = p.professor_id " +
                "WHERE s.professor_id = ? ORDER BY s.subject_name";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, professorId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                subjects.add(mapResultSetToSubject(rs));
            }
        } catch (SQLException e) {
            handleException("get subjects by professor", e);
        }
        return subjects;
    }

    public List<Subject> getByDepartment(String department) {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT s.*, p.first_name || ' ' || p.last_name AS professor_name " +
                "FROM subjects s LEFT JOIN professors p ON s.professor_id = p.professor_id " +
                "WHERE UPPER(s.department) LIKE ? ORDER BY s.subject_name";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + department.toUpperCase() + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                subjects.add(mapResultSetToSubject(rs));
            }
        } catch (SQLException e) {
            handleException("get subjects by department", e);
        }
        return subjects;
    }

    public List<Subject> getByDifficultyLevel(String difficultyLevel) {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT s.*, p.first_name || ' ' || p.last_name AS professor_name " +
                "FROM subjects s LEFT JOIN professors p ON s.professor_id = p.professor_id " +
                "WHERE s.difficulty_level = ? ORDER BY s.subject_name";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, difficultyLevel);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                subjects.add(mapResultSetToSubject(rs));
            }
        } catch (SQLException e) {
            handleException("get subjects by difficulty level", e);
        }
        return subjects;
    }

    private Subject mapResultSetToSubject(ResultSet rs) throws SQLException {
        Subject subject = new Subject();
        subject.setSubjectId(rs.getInt("subject_id"));
        subject.setSubjectName(rs.getString("subject_name"));
        subject.setCode(rs.getString("code"));
        subject.setCredits(rs.getInt("credits"));
        subject.setProfessorId(rs.getInt("professor_id"));
        subject.setProfessorName(rs.getString("professor_name"));
        subject.setDepartment(rs.getString("department"));

        if (columnExists(rs, "difficulty_level")) {
            subject.setDifficultyLevel(rs.getString("difficulty_level"));
        }
        if (columnExists(rs, "hours_per_week")) {
            subject.setHoursPerWeek(rs.getInt("hours_per_week"));
        }

        return subject;
    }

    private void setSubjectParameters(PreparedStatement pstmt, Subject subject) throws SQLException {
        pstmt.setString(1, subject.getSubjectName());
        pstmt.setString(2, subject.getCode());
        pstmt.setInt(3, subject.getCredits());

        if (subject.getProfessorId() > 0) {
            pstmt.setInt(4, subject.getProfessorId());
        } else {
            pstmt.setNull(4, java.sql.Types.INTEGER);
        }

        pstmt.setString(5, subject.getDepartment());
        pstmt.setString(6, subject.getDifficultyLevel());
        pstmt.setInt(7, subject.getHoursPerWeek());
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