package dao;

import model.Professor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProfessorDAO implements DAO<Professor> {
    private final Connection connection;

    public ProfessorDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<Professor> get(int id) {
        String sql = "SELECT * FROM professors WHERE professor_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? Optional.of(mapResultSetToProfessor(rs)) : Optional.empty();
        } catch (SQLException e) {
            handleException("get professor by ID", e);
            return Optional.empty();
        }
    }

    @Override
    public List<Professor> getAll() {
        List<Professor> professors = new ArrayList<>();
        String sql = "SELECT * FROM professors ORDER BY professor_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                professors.add(mapResultSetToProfessor(rs));
            }
        } catch (SQLException e) {
            handleException("get all professors", e);
        }
        return professors;
    }

    @Override
    public boolean save(Professor professor) {
        String sql = "INSERT INTO professors (first_name, last_name, email, phone, department, specialty, salary) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, new String[]{"professor_id"})) {
            setProfessorParameters(pstmt, professor);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        professor.setProfessorId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            handleException("save professor", e);
            return false;
        }
    }

    @Override
    public boolean update(Professor professor) {
        String sql = "UPDATE professors SET first_name = ?, last_name = ?, email = ?, phone = ?, " +
                "department = ?, specialty = ?, salary = ?, status = ? " +
                "WHERE professor_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, professor.getFirstName());
            pstmt.setString(2, professor.getLastName());
            pstmt.setString(3, professor.getEmail());
            pstmt.setString(4, professor.getPhone());
            pstmt.setString(5, professor.getDepartment());
            pstmt.setString(6, professor.getSpecialty());
            pstmt.setDouble(7, professor.getSalary());
            pstmt.setString(8, professor.getStatus());
            pstmt.setInt(9, professor.getProfessorId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleException("update professor", e);
            return false;
        }
    }

    @Override
    public boolean delete(Professor professor) {
        return deleteById(professor.getProfessorId());
    }

    @Override
    public boolean deleteById(int id) {
        String sql = "DELETE FROM professors WHERE professor_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleException("delete professor", e);
            return false;
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM professors";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            handleException("count professors", e);
            return 0;
        }
    }

    @Override
    public boolean exists(int id) {
        return get(id).isPresent();
    }

    public Optional<Professor> getByEmail(String email) {
        String sql = "SELECT * FROM professors WHERE email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? Optional.of(mapResultSetToProfessor(rs)) : Optional.empty();
        } catch (SQLException e) {
            handleException("get professor by email", e);
            return Optional.empty();
        }
    }

    public List<Professor> getByDepartment(String department) {
        List<Professor> professors = new ArrayList<>();
        String sql = "SELECT * FROM professors WHERE UPPER(department) LIKE ? ORDER BY last_name, first_name";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + department.toUpperCase() + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                professors.add(mapResultSetToProfessor(rs));
            }
        } catch (SQLException e) {
            handleException("get professors by department", e);
        }
        return professors;
    }

    public List<Professor> getByStatus(String status) {
        List<Professor> professors = new ArrayList<>();
        String sql = "SELECT * FROM professors WHERE status = ? ORDER BY last_name, first_name";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                professors.add(mapResultSetToProfessor(rs));
            }
        } catch (SQLException e) {
            handleException("get professors by status", e);
        }
        return professors;
    }

    public List<Professor> getActiveProfessors() {
        return getByStatus("ACTIVE");
    }

    private Professor mapResultSetToProfessor(ResultSet rs) throws SQLException {
        Professor professor = new Professor();
        professor.setProfessorId(rs.getInt("professor_id"));
        professor.setFirstName(rs.getString("first_name"));
        professor.setLastName(rs.getString("last_name"));
        professor.setEmail(rs.getString("email"));
        professor.setPhone(rs.getString("phone"));
        professor.setDepartment(rs.getString("department"));
        professor.setSpecialty(rs.getString("specialty"));

        // Campos opcionales
        if (columnExists(rs, "salary")) {
            professor.setSalary(rs.getDouble("salary"));
        }
        if (columnExists(rs, "status")) {
            professor.setStatus(rs.getString("status"));
        }

        return professor;
    }

    private void setProfessorParameters(PreparedStatement pstmt, Professor professor) throws SQLException {
        pstmt.setString(1, professor.getFirstName());
        pstmt.setString(2, professor.getLastName());
        pstmt.setString(3, professor.getEmail());
        pstmt.setString(4, professor.getPhone());
        pstmt.setString(5, professor.getDepartment());
        pstmt.setString(6, professor.getSpecialty());
        pstmt.setDouble(7, professor.getSalary());
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