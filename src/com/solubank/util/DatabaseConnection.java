package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

public class DatabaseConnection {
    // Rendus statiques pour être facilement accessibles dans la méthode getConnection()
    private static Properties props;
    private static String URL;
    private static String USER;
    private static String PASSWORD;

    static {
        try {
            // Charger les propriétés une seule fois
            props = new Properties();
            // L'emplacement du fichier peut nécessiter un chemin absolu ou un chemin relatif au projet
            // Assurez-vous que db.properties est dans le dossier racine du projet.
            props.load(new FileInputStream("db.properties"));
            URL = props.getProperty("db.url");
            USER = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");

            // Test de la connexion initiale pour charger le driver
            DriverManager.getConnection(URL, USER, PASSWORD).close();

        } catch (IOException e) {
            System.err.println(" Erreur de lecture du fichier db.properties: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println(" Erreur de connexion initiale (Vérifiez les identifiants) : " + e.getMessage());
        }
    }

    // Le constructeur doit rester privé pour des raisons de conception
    private DatabaseConnection() {
        throw new UnsupportedOperationException("Impossible d'instancier une classe utilitaire!");
    }

    /**
     * Ouvre et retourne une NOUVELLE connexion à la base de données.
     * C'est cette connexion qui sera fermée par le bloc try-with-resources des DAO.
     */
    public static Connection getConnection() throws SQLException {
        // La connexion est créée UNIQUEMENT ici
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Teste l'établissement d'une connexion.
     */
    public static boolean testConnection() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Si la connexion réussit et se ferme correctement, on retourne true
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    // La méthode closeConnection() devient inutile car les DAO gèrent la fermeture
    // via try-with-resources, mais nous laissons un stub pour éviter les erreurs de compilation.
    public static void closeConnection() {
        // Laisser vide ou retirer si vous n'avez plus besoin d'un nettoyage global.
        // Les DAO gèrent la fermeture de leur propre connexion.
    }
}