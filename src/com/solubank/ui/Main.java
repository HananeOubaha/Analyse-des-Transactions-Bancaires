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
    // --- Méthodes utilitaires pour la saisie ---

    private long lireLong(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                long val = scanner.nextLong();
                scanner.nextLine();
                return val;
            } catch (InputMismatchException e) {
                System.err.println(" Erreur de saisie. Veuillez entrer un nombre entier (ID/Montant).");
                scanner.nextLine();
            }
        }
    }

    private double lireDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                double val = scanner.nextDouble();
                scanner.nextLine();
                return val;
            } catch (InputMismatchException e) {
                System.err.println(" Erreur de saisie. Veuillez entrer un nombre décimal (Solde/Taux/Découvert).");
                scanner.nextLine();
            }
        }
    }

    private String lireString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    // --- Menu 1 : Gestion des Clients et Comptes ---

    private void menuGestionClientsComptes() {
        int choix;
        do {
            System.out.println("\n[1] --- GESTION CLIENTS ET COMPTES ---");
            System.out.println("1. Ajouter un nouveau Client");
            System.out.println("2. Créer un Compte pour un Client existant");
            System.out.println("3. Lister tous les Clients et leurs Soldes Totaux");
            System.out.println("4. Consulter les Comptes d'un Client");
            System.out.println("0. Retour au Menu Principal");
            System.out.print("Votre choix : ");

            try {
                choix = scanner.nextInt();
                scanner.nextLine();
                switch (choix) {
                    case 1 -> ajouterClientUI();
                    case 2 -> creerCompteUI();
                    case 3 -> listerClientsEtSoldes();
                    case 4 -> consulterComptesClient();
                    case 0 -> System.out.println("Retour...");
                    default -> System.err.println("Choix invalide.");
                }
            } catch (InputMismatchException e) {
                System.err.println(" Erreur de saisie. Veuillez entrer un nombre.");
                scanner.nextLine();
                choix = -1;
            }
        } while (choix != 0);
    }

    private void ajouterClientUI() {
        System.out.println("\n--- AJOUT D'UN NOUVEAU CLIENT ---");
        String nom = lireString("Nom du Client : ");
        String email = lireString("Email du Client : ");

        // ID à 0 car il sera généré par la base de données
        Client nouveauClient = new Client(0, nom, email);
        Client clientSauvegarde = clientService.ajouterClient(nouveauClient);

        if (clientSauvegarde != null) {
            System.out.printf(" Client %s (ID: %d) ajouté avec succès.\n", clientSauvegarde.nom(), clientSauvegarde.id());
        } else {
            System.err.println(" Échec de l'ajout du client.");
        }
    }

    private void creerCompteUI() {
        long clientId = lireLong("ID du Client pour le nouveau compte : ");
        Optional<Client> clientOpt = clientService.trouverClientParId(clientId);

        if (clientOpt.isEmpty()) {
            System.err.println(" Client avec l'ID " + clientId + " non trouvé.");
            return;
        }

        Client client = clientOpt.get();
        System.out.printf("Création de compte pour : %s\n", client.nom());

        String numero = lireString("Numéro de Compte : ");
        double solde = lireDouble("Solde Initial : ");
        String type = lireString("Type de Compte (courant/epargne) : ").toLowerCase();

        Compte nouveauCompte = null;
        if (type.equals("courant")) {
            double decouvert = lireDouble("Découvert Autorisé : ");
            nouveauCompte = new CompteCourant(0, numero, solde, clientId, decouvert);
        } else if (type.equals("epargne")) {
            double taux = lireDouble("Taux d'Intérêt (%) : ");
            nouveauCompte = new CompteEpargne(0, numero, solde, clientId, taux);
        } else {
            System.err.println(" Type de compte invalide.");
            return;
        }

        Compte compteSauvegarde = compteService.ajouterCompte(nouveauCompte);
        if (compteSauvegarde != null) {
            System.out.printf(" Compte %s créé pour le client %s.\n", compteSauvegarde.getNumero(), client.nom());
        }
    }

    private void listerClientsEtSoldes() {
        System.out.println("\n--- LISTE DES CLIENTS ET SOLDES TOTAUX ---");
        List<Client> clients = clientService.listerTousLesClients();
        if (clients.isEmpty()) {
            System.out.println("Aucun client enregistré.");
            return;
        }

        clients.forEach(c -> {
            double soldeTotal = clientService.calculerSoldeTotalParClient(c.id());
            // Utilisation de FormatUtils
            System.out.printf("[ID: %d] %s (%s) - Solde Total: %s\n",
                    c.id(),
                    c.nom(),
                    c.email(),
                    FormatUtils.formatMontant(soldeTotal));
        });
    }

    private void consulterComptesClient() {
        long clientId = lireLong("ID du Client : ");
        List<Compte> comptes = compteService.compteDAO.findByClientId(clientId);

        if (comptes.isEmpty()) {
            System.out.println("Aucun compte trouvé pour ce client.");
            return;
        }

        System.out.printf("\n--- COMPTES DU CLIENT ID %d ---\n", clientId);
        comptes.forEach(c -> {
            String type = (c instanceof CompteCourant) ? "Courant" : "Épargne";
            // Utilisation de FormatUtils
            System.out.printf("  [ID: %d] N° %s | Type: %s | Solde: %s\n",
                    c.getId(),
                    c.getNumero(),
                    type,
                    FormatUtils.formatMontant(c.getSolde()));
        });
    }
    // --- Menu 2 : Opérations Bancaires ---

    private void menuOperationsBancaires() {
        int choix;
        do {
            System.out.println("\n[2] --- OPÉRATIONS BANCAIRES ---");
            System.out.println("1. Effectuer un Versement");
            System.out.println("2. Effectuer un Retrait");
            System.out.println("3. Effectuer un Virement");
            System.out.println("0. Retour au Menu Principal");
            System.out.print("Votre choix : ");

            try {
                choix = scanner.nextInt();
                scanner.nextLine();
                switch (choix) {
                    case 1 -> effectuerVersementUI();
                    case 2 -> effectuerRetraitUI();
                    case 3 -> effectuerVirementUI();
                    case 0 -> System.out.println("Retour...");
                    default -> System.err.println("Choix invalide.");
                }
            } catch (InputMismatchException e) {
                System.err.println(" Erreur de saisie. Veuillez entrer un nombre.");
                scanner.nextLine();
                choix = -1;
            }
        } while (choix != 0);
    }

    private void effectuerVersementUI() {
        System.out.println("\n--- VERSEMENT ---");
        long idCompte = lireLong("ID du Compte Bénéficiaire : ");
        double montant = lireDouble("Montant du Versement : ");
        String lieu = lireString("Lieu de l'Opération : ");

        if (compteService.effectuerVersement(idCompte, montant, lieu)) {
            System.out.println(" Versement réussi.");
        } else {
            System.err.println(" Versement échoué (vérifiez l'ID du compte).");
        }
    }

    private void effectuerRetraitUI() {
        System.out.println("\n--- RETRAIT ---");
        long idCompte = lireLong("ID du Compte Source : ");
        double montant = lireDouble("Montant du Retrait : ");
        String lieu = lireString("Lieu de l'Opération : ");

        if (compteService.effectuerRetrait(idCompte, montant, lieu)) {
            System.out.println(" Retrait réussi.");
        } else {
            System.err.println(" Retrait échoué (solde insuffisant ou limites dépassées).");
        }
    }

    private void effectuerVirementUI() {
        System.out.println("\n--- VIREMENT ---");
        long idSource = lireLong("ID du Compte Source : ");
        long idDest = lireLong("ID du Compte Destination : ");
        double montant = lireDouble("Montant du Virement : ");

        if (compteService.effectuerVirement(idSource, idDest, montant)) {
            System.out.println(" Virement réussi. Solde des deux comptes mis à jour.");
        } else {
            System.err.println(" Virement échoué (problème au débit ou comptes introuvables).");
        }
    }








}