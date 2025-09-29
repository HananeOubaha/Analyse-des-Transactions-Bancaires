package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

public class DatabaseConnection {
    private static Connection connection;

    private DatabaseConnection() {
        throw new UnsupportedOperationException("impossible d'instancier une classe utilitaiere!");
    }

    static {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("db.properties"));
            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");
            connection = DriverManager.getConnection(url, user, password);
        } catch (IOException e) {
            System.err.println("la connexion a la base de donnée est echouée " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("la connexion a la base de donnée est echouée" + e.getMessage());
        }
    }
    public static boolean testConnection() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            System.err.println("Test de connexion échoué: " + e.getMessage());
            return false;
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Connexion à la base de données fermée");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
        }
    }
}