package ui;

import service.*;
import entity.*;
import util.DatabaseConnection;
import util.FormatUtils; // Import de la classe utilitaire de formatage

import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;


public class Main {

    private final Scanner scanner = new Scanner(System.in);
    private final ClientService clientService = new ClientService();
    private final CompteService compteService = new CompteService();
    private final TransactionService transactionService = new TransactionService();
    private final RapportService rapportService = new RapportService();

    public static void main(String[] args) {
        // Test de la connexion au démarrage
        if (DatabaseConnection.testConnection()) {
            System.out.println(" Connexion à la Banque Al Baraka établie avec succès. Bienvenue!");
            new Main().afficherMenuPrincipal();
        } else {
            System.err.println(" Échec de la connexion à la base de données. Veuillez vérifier db.properties et le pilote JDBC.");
        }
    }
    // --- Menu Principal ---

    private void afficherMenuPrincipal() {
        int choix = -1;
        do {
            System.out.println("\n=============================================");
            System.out.println("  SYSTÈME D'ANALYSE DES TRANSACTIONS BANCAIRES");
            System.out.println("=============================================");
            System.out.println("1. Gestion des Clients et Comptes");
            System.out.println("2. Opérations Bancaires (Versements, Retraits, Virements)");
            System.out.println("3. Consultation des Transactions");
            System.out.println("4. Analyse et Rapports (Détection d'Anomalies)");
            System.out.println("0. Quitter l'application");
            System.out.print("Votre choix : ");

            try {
                choix = scanner.nextInt();
                scanner.nextLine(); // Consommer le retour à la ligne
                traiterChoixPrincipal(choix);
            } catch (InputMismatchException e) {
                System.err.println(" Erreur de saisie. Veuillez entrer un nombre.");
                scanner.nextLine(); // Nettoyer l'entrée invalide
                choix = -1;
            }
        } while (choix != 0);

        DatabaseConnection.closeConnection();
        System.out.println(" Merci d'avoir utilisé SoluBank Systems. Au revoir.");
    }

    private void traiterChoixPrincipal(int choix) {
        switch (choix) {
            case 1 -> menuGestionClientsComptes();
            case 2 -> menuOperationsBancaires();
            case 3 -> menuConsultationTransactions();
            case 4 -> menuAnalyseRapports();
            case 0 -> {}
            default -> System.err.println("Choix invalide.");
        }
    }



}