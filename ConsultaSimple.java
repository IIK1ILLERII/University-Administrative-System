import model.DatabaseConnection;
import java.sql.*;

public class ConsultaSimple {

    public static void main(String[] args) {
        System.out.println("MOSTRANDO ESTUDIANTES DESDE LA BASE DE DATOS");
        System.out.println("===============================================");

        String sql = "SELECT student_id, first_name, last_name, career, semester FROM students";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("ID | NOMBRE | APELLIDO | CARRERA | SEMESTRE");
            System.out.println("--------------------------------------------");

            while (rs.next()) {
                int id = rs.getInt("student_id");
                String nombre = rs.getString("first_name");
                String apellido = rs.getString("last_name");
                String carrera = rs.getString("career");
                int semestre = rs.getInt("semester");

                System.out.println(id + " | " + nombre + " | " + apellido + " | " + carrera + " | " + semestre);
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

}
