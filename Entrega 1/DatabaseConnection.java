package model;

import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:oracle:thin:@localhost:1521/XE";
    private static final String USER = "NUEVO_ADMIN";
    private static final String PASSWORD = "AdminPass123";
    private static Connection connection;

    public static Connection getConnection() {
        try {

            if (connection == null || connection.isClosed()) {
                System.out.println("Creando nueva conexión...");
                Class.forName("oracle.jdbc.driver.OracleDriver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);

                connection.setAutoCommit(true);
                System.out.println("Conexión creada exitosamente");
            } else {
                System.out.println("Reutilizando conexión existente");
            }

            if (!connection.isValid(2)) {
                System.out.println("Conexión no válida, recreando...");
                closeConnection();
                Class.forName("oracle.jdbc.driver.OracleDriver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Error en la conexión: " + e.getMessage());
            connection = null;
            JOptionPane.showMessageDialog(null,
                    "Error de conexión a Oracle:\n" + e.getMessage(),
                    "Error de Conexión",
                    JOptionPane.ERROR_MESSAGE);
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("Conexión cerrada");
                }
            } catch (SQLException e) {
                System.out.println("Error al cerrar conexión: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }

    public static boolean testConnection() {
        try (Connection testConn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            return testConn.isValid(2);
        } catch (SQLException e) {
            System.out.println("Test de conexión falló: " + e.getMessage());
            return false;
        }
    }
}
