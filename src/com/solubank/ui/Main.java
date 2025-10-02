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

    }