package util;

import util.DatabaseConnection;

public class TestConnexion {

    public static void main(String[] args) {
        if (DatabaseConnection.testConnection()) {
            System.out.println("Connexion à la base de données établie avec succès ! ");
        } else {
            System.out.println("Échec de la connexion à la base de données. ");
        }
        // N'oubliez pas de fermer la connexion si nécessaire
        DatabaseConnection.closeConnection();
    }
}