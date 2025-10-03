package ui;

import service.*;
import entity.*;
import util.DatabaseConnection;
import util.FormatUtils;
import java.time.LocalDate;

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
            System.out.println("5. Supprimer un Client et ses Comptes");
            System.out.println("6. Rechercher un Client par Nom");
            System.out.println("7. Compte avec Solde Maximum/Minimum");
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
            System.out.printf("  [ID: %d] N° %s | Type: %s | Solde: %s\n",
                    c.getId(),
                    c.getNumero(),
                    type,
                    FormatUtils.formatMontant(c.getSolde()));

            // Intégration de l'alerte ici
            verifierEtAfficherAlerteSoldeBas(c);
        });;
    }

    private void supprimerClientUI() {
        System.out.println("\n--- SUPPRESSION D'UN CLIENT ---");
        long clientId = lireLong("ID du Client à supprimer : ");

        Optional<Client> clientOpt = clientService.trouverClientParId(clientId);

        if (clientOpt.isEmpty()) {
            System.err.println(" Client avec l'ID " + clientId + " non trouvé.");
            return;
        }

        System.out.printf("ATTENTION : Voulez-vous vraiment supprimer le client %s et TOUS ses comptes ? (oui/non) : ", clientOpt.get().nom());
        String confirmation = scanner.nextLine().toLowerCase();

        if (confirmation.equals("oui")) {
            clientService.supprimerClient(clientId);
            System.out.printf(" Client (ID: %d) et toutes ses données associées ont été supprimés.\n", clientId);
        } else {
            System.out.println("Opération annulée.");
        }
    }
    private void menuModificationDonnees() {
        System.out.println("\n--- MODIFICATION DE DONNÉES ---");
        System.out.println("1. Modifier les informations d'un Client (Nom/Email)");
        System.out.println("2. Modifier les paramètres d'un Compte (Découvert/Taux)");
        System.out.print("Votre choix : ");

        try {
            int choix = scanner.nextInt();
            scanner.nextLine();

            if (choix == 1) {
                modifierClientUI();
            } else if (choix == 2) {
                modifierCompteUI();
            } else {
                System.err.println(" Choix invalide.");
            }
        } catch (InputMismatchException e) {
            System.err.println(" Erreur de saisie.");
            scanner.nextLine();
        }
    }

    private void modifierClientUI() {
        long clientId = lireLong("ID du Client à modifier : ");
        Optional<Client> clientOpt = clientService.trouverClientParId(clientId);

        if (clientOpt.isEmpty()) {
            System.err.println(" Client non trouvé.");
            return;
        }

        Client clientActuel = clientOpt.get();
        System.out.printf("Modification du client : %s\n", clientActuel.nom());

        String nom = lireString("Nouveau Nom (Laissez vide pour garder : " + clientActuel.nom() + ") : ");
        String email = lireString("Nouvel Email (Laissez vide pour garder : " + clientActuel.email() + ") : ");

        // Utilisation de l'ancien attribut si le nouveau est vide
        String nouveauNom = nom.isEmpty() ? clientActuel.nom() : nom;
        String nouvelEmail = email.isEmpty() ? clientActuel.email() : email;

        Client clientModifie = new Client(clientId, nouveauNom, nouvelEmail);
        clientService.modifierClient(clientModifie);

        System.out.printf(" Client (ID: %d) mis à jour.\n", clientId);
    }

    private void modifierCompteUI() {
        long compteId = lireLong("ID du Compte à modifier : ");
        Optional<Compte> compteOpt = compteService.trouverCompteParId(compteId);

        if (compteOpt.isEmpty()) {
            System.err.println(" Compte non trouvé.");
            return;
        }

        Compte compteActuel = compteOpt.get();

        if (compteActuel instanceof CompteCourant cc) {
            System.out.printf("Modification du Compte Courant N°%s\n", cc.getNumero());
            double decouvert = lireDouble("Nouveau Découvert Autorisé (Actuel: " + cc.getDecouvertAutorise() + ") : ");

            // Création d'un nouvel objet CompteCourant avec l'ID correct pour la mise à jour
            CompteCourant compteModifie = new CompteCourant(cc.getId(), cc.getNumero(), cc.getSolde(), cc.getIdClient(), decouvert);
            compteService.compteDAO.update(compteModifie);
            System.out.println(" Découvert autorisé mis à jour.");

        } else if (compteActuel instanceof CompteEpargne ce) {
            System.out.printf("Modification du Compte Épargne N°%s\n", ce.getNumero());
            double taux = lireDouble("Nouveau Taux d'Intérêt (Actuel: " + ce.getTauxInteret() + ") : ");

            // Création d'un nouvel objet CompteEpargne avec l'ID correct
            CompteEpargne compteModifie = new CompteEpargne(ce.getId(), ce.getNumero(), ce.getSolde(), ce.getIdClient(), taux);
            compteService.compteDAO.update(compteModifie);
            System.out.println(" Taux d'intérêt mis à jour.");

        } else {
            System.err.println("Type de compte non pris en charge pour la modification.");
        }
    }

    private void rechercherClientParNomUI() {
        System.out.println("\n--- RECHERCHE DE CLIENT PAR NOM ---");
        String nom = lireString("Nom (partiel) du Client à rechercher : ");

        List<Client> clientsTrouves = clientService.trouverClientsParNom(nom);

        if (clientsTrouves.isEmpty()) {
            System.out.println("Aucun client trouvé contenant '" + nom + "'.");
            return;
        }

        System.out.printf("\n--- %d CLIENT(S) TROUVÉ(S) --- \n", clientsTrouves.size());
        clientsTrouves.forEach(c -> System.out.printf("[ID: %d] %s (%s)\n", c.id(), c.nom(), c.email()));
    }

    private void afficherComptesMaxMinSolde() {
        System.out.println("\n--- COMPTES AVEC SOLDE MAXIMAL ET MINIMAL ---");

        Optional<Compte> maxCompte = compteService.trouverCompteAvecSoldeMaximum();
        Optional<Compte> minCompte = compteService.trouverCompteAvecSoldeMinimum();

        if (maxCompte.isPresent()) {
            Compte c = maxCompte.get();
            System.out.printf(" Solde MAXIMUM : Compte N° %s (Client ID: %d) - Solde: %s\n",
                    c.getNumero(),
                    c.getIdClient(),
                    FormatUtils.formatMontant(c.getSolde()));
        } else {
            System.out.println("Impossible de trouver un compte pour le solde maximum.");
        }

        if (minCompte.isPresent()) {
            Compte c = minCompte.get();
            System.out.printf("Solde MINIMUM : Compte N° %s (Client ID: %d) - Solde: %s\n",
                    c.getNumero(),
                    c.getIdClient(),
                    FormatUtils.formatMontant(c.getSolde()));
        } else {
            System.out.println("Impossible de trouver un compte pour le solde minimum.");
        }
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

    // --- Menu 3 : Consultation des Transactions ---

    private void menuConsultationTransactions() {
        int choix;
        do {
            System.out.println("\n[3] --- CONSULTATION TRANSACTIONS ---");
            System.out.println("1. Lister les Transactions par Compte");
            System.out.println("2. Filtrer les Transactions par Montant Minimum");
            System.out.println("3. Regrouper les Transactions par Type");
            System.out.println("4. Filtrer les Transactions par Date");
            System.out.println("5. Filtrer les Transactions par Lieu");
            System.out.println("6. Calculer le Volume Total des Transactions par Client");
            System.out.println("0. Retour au Menu Principal");
            System.out.print("Votre choix : ");

            try {
                choix = scanner.nextInt();
                scanner.nextLine();
                switch (choix) {
                    case 1 -> listerTransactionsParCompteUI();
                    case 2 -> filtrerParMontantMinUI();
                    case 3 -> regrouperParTypeUI();
                    case 4 -> filtrerParDateUI();
                    case 5 -> filtrerParLieuUI();
                    case 6 -> calculerTotalTransactionsClientUI();
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

    private void listerTransactionsParCompteUI() {
        long idCompte = lireLong("ID du Compte : ");
        List<Transaction> transactions = transactionService.listerTransactionsParCompte(idCompte);

        if (transactions.isEmpty()) {
            System.out.println("Aucune transaction trouvée pour ce compte.");
            return;
        }

        System.out.printf("\n--- HISTORIQUE DES TRANSACTIONS DU COMPTE ID %d ---\n", idCompte);
        transactions.forEach(t -> System.out.printf("[%s] %s: %s à %s\n",
                FormatUtils.formatDate(t.date()),
                t.type(),
                FormatUtils.formatMontant(t.montant()),
                t.lieu()));
    }

    private void filtrerParMontantMinUI() {
        double montantMin = lireDouble("Montant Minimum pour le filtre : ");
        List<Transaction> transactions = transactionService.filtrerParMontantMin(montantMin);

        if (transactions.isEmpty()) {
            System.out.println("Aucune transaction trouvée avec un montant supérieur ou égal à " + FormatUtils.formatMontant(montantMin) + ".");
            return;
        }

        System.out.printf("\n--- TRANSACTIONS SUPÉRIEURES OU ÉGALES À %s ---\n", FormatUtils.formatMontant(montantMin));
        transactions.forEach(t -> System.out.printf("[ID Compte: %d] %s: %s à %s\n",
                t.idCompte(),
                t.type(),
                FormatUtils.formatMontant(t.montant()),
                t.lieu()));
    }

    private void regrouperParTypeUI() {
        System.out.println("\n--- REGROUPEMENT DES TRANSACTIONS PAR TYPE ---");
        Map<TypeTransaction, List<Transaction>> regroupement = transactionService.regrouperParType();

        regroupement.forEach((type, list) -> {
            double total = list.stream().mapToDouble(Transaction::montant).sum();
            // Utilisation de FormatUtils
            System.out.printf("Type: %s | Nombre: %d | Total Volume: %s\n",
                    type,
                    list.size(),
                    FormatUtils.formatMontant(total));
        });
    }
    private void filtrerParDateUI() {
        System.out.println("\n--- FILTRE PAR DATE (JJ/MM/AAAA) ---");
        String dateStr = lireString("Entrez la date (Ex: 01/10/2025) : ");

        try {
            // Utiliser le formateur de date pour convertir l'entrée utilisateur
            LocalDate dateFiltre = LocalDate.parse(dateStr, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            List<Transaction> transactions = transactionService.filtrerParDate(dateFiltre);

            if (transactions.isEmpty()) {
                System.out.printf("Aucune transaction trouvée pour la date %s.\n", FormatUtils.formatDate(dateFiltre));
                return;
            }

            System.out.printf("\n--- TRANSACTIONS DU %s ---\n", FormatUtils.formatDate(dateFiltre));
            transactions.forEach(t -> System.out.printf("[ID Compte: %d] %s: %s à %s\n",
                    t.idCompte(),
                    t.type(),
                    FormatUtils.formatMontant(t.montant()),
                    t.lieu()));
        } catch (java.time.format.DateTimeParseException e) {
            System.err.println(" Format de date invalide. Utilisez JJ/MM/AAAA.");
        }
    }

    private void filtrerParLieuUI() {
        System.out.println("\n--- FILTRE PAR LIEU ---");
        String lieu = lireString("Entrez le lieu (partiel) à rechercher : ");

        List<Transaction> transactions = transactionService.filtrerParLieu(lieu);

        if (transactions.isEmpty()) {
            System.out.printf("Aucune transaction trouvée pour le lieu contenant '%s'.\n", lieu);
            return;
        }

        System.out.printf("\n--- TRANSACTIONS EFFECTUÉES À/DANS %s ---\n", lieu.toUpperCase());
        transactions.forEach(t -> System.out.printf("[%s] %s: %s à %s (Compte %d)\n",
                FormatUtils.formatDate(t.date()),
                t.type(),
                FormatUtils.formatMontant(t.montant()),
                t.lieu(),
                t.idCompte()));
    }

    private void calculerTotalTransactionsClientUI() {
        System.out.println("\n--- VOLUME TOTAL DES TRANSACTIONS PAR CLIENT ---");
        long clientId = lireLong("ID du Client : ");

        if (clientService.trouverClientParId(clientId).isEmpty()) {
            System.err.println(" Client avec l'ID " + clientId + " non trouvé.");
            return;
        }

        double volumeTotal = transactionService.calculerTotalTransactionsParClient(clientId);

        System.out.printf(" Le volume total des transactions pour le client ID %d est de : %s\n",
                clientId,
                FormatUtils.formatMontant(volumeTotal));
    }

    // --- Menu 4 : Analyse et Rapports ---

    private void menuAnalyseRapports() {
        int choix = -1;
        do {
            System.out.println("\n[4] --- ANALYSE ET RAPPORTS ---");
            System.out.println("1. Top 5 des Clients par Solde Total");
            System.out.println("2. Rapport Mensuel (Volume et Nombre par Type)");
            System.out.println("3. Détection des Comptes Inactifs (> " + RapportService.PERIODE_INACTIVITE_JOURS + " jours)");
            System.out.println("4. Détection des Transactions Suspectes (Montant/Lieu)");
            System.out.println("5. Détection de Fréquence Excessive");
            System.out.println("0. Retour au Menu Principal");
            System.out.print("Votre choix : ");

            try {
                choix = scanner.nextInt();
                scanner.nextLine();
                traiterChoixRapports(choix);
            } catch (InputMismatchException e) {
                System.err.println(" Erreur de saisie. Veuillez entrer un nombre.");
                scanner.nextLine();
                choix = -1;
            }
        } while (choix != 0);
    }

    private void traiterChoixRapports(int choix) {
        switch (choix) {
            case 1 -> afficherTop5Clients();
            case 2 -> afficherRapportMensuel();
            case 3 -> afficherComptesInactifs();
            case 4 -> afficherTransactionsSuspectes();
            case 5 -> afficherFrequenceExcessive();
            case 0 -> System.out.println("Retour au menu principal...");
            default -> System.err.println("Choix invalide.");
        }
    }

    private void afficherTop5Clients() {
        System.out.println("\n--- TOP 5 DES CLIENTS PAR SOLDE TOTAL ---");
        List<Map.Entry<Client, Double>> topClients = rapportService.genererTop5ClientsParSolde();

        if (topClients.isEmpty()) {
            System.out.println("Aucun client trouvé pour le classement.");
            return;
        }

        for (int i = 0; i < topClients.size(); i++) {
            Map.Entry<Client, Double> entry = topClients.get(i);
            // Utilisation de FormatUtils
            System.out.printf("%d. %s (%s) - Solde Total: %s\n",
                    (i + 1),
                    entry.getKey().nom(),
                    entry.getKey().email(),
                    FormatUtils.formatMontant(entry.getValue()));
        }
    }

    private void afficherRapportMensuel() {
        int mois = (int) lireLong("Entrez le mois (1-12) : ");
        int annee = (int) lireLong("Entrez l'année : ");

        System.out.printf("\n--- RAPPORT MENSUEL (%d/%d) ---\n", mois, annee);

        Map<TypeTransaction, Map<String, Object>> rapport = rapportService.genererRapportMensuel(mois, annee);

        if (rapport.isEmpty()) {
            System.out.println("Aucune transaction enregistrée pour cette période.");
            return;
        }

        rapport.forEach((type, stats) -> {
            double volumeTotal = (double) stats.get("volumeTotal");
            // Utilisation de FormatUtils
            System.out.printf("Type : %s | Nombre de Transactions : %d | Volume Total : %s\n",
                    type,
                    (long) stats.get("nombre"),
                    FormatUtils.formatMontant(volumeTotal));
        });
    }

    private void afficherComptesInactifs() {
        System.out.println("\n--- COMPTES INACTIFS (> " + RapportService.PERIODE_INACTIVITE_JOURS + " jours) ---");
        List<Compte> inactifs = rapportService.identifierComptesInactifs();

        if (inactifs.isEmpty()) {
            System.out.println(" Aucune anomalie détectée : Tous les comptes sont actifs.");
            return;
        }

        inactifs.forEach(c -> System.out.printf(" ALERTE : Compte N° %s (ID Client: %d) est inactif.\n", c.getNumero(), c.getIdClient()));
    }

    private void afficherTransactionsSuspectes() {
        System.out.println("\n--- TRANSACTIONS SUSPECTES (Montant > 10000 ou Lieu Inhabituel) ---");
        List<Transaction> suspectes = transactionService.detecterTransactionsSuspectes();

        if (suspectes.isEmpty()) {
            System.out.println(" Aucune anomalie de montant ou de lieu détectée.");
            return;
        }

        suspectes.forEach(t -> System.out.printf(" SUSPECT : Compte %d | Montant: %s | Type: %s | Lieu: %s\n",
                t.idCompte(),
                FormatUtils.formatMontant(t.montant()), // Utilisation de FormatUtils
                t.type(),
                t.lieu()));
    }

    private void afficherFrequenceExcessive() {
        System.out.println("\n--- TRANSACTIONS À FRÉQUENCE EXCESSIVE (Simulées) ---");
        List<Transaction> suspectes = transactionService.detecterFrequenceExcessive();

        if (suspectes.isEmpty()) {
            System.out.println(" Aucune anomalie de fréquence excessive détectée.");
            return;
        }

        suspectes.forEach(t -> System.out.printf(" FRÉQUENCE ALERTE : Compte %d | Montant: %s | Date: %s\n",
                t.idCompte(),
                FormatUtils.formatMontant(t.montant()),
                FormatUtils.formatDate(t.date())));
    }

    private static final double SEUIL_ALERTE_SOLDE_BAS = 100.0;

    private void verifierEtAfficherAlerteSoldeBas(Compte compte) {
        if (compte.getSolde() < SEUIL_ALERTE_SOLDE_BAS) {
            System.out.printf(" ALERTE SOLDE BAS : Le compte N° %s a un solde de %s, inférieur au seuil de %s.\n",
                    compte.getNumero(),
                    FormatUtils.formatMontant(compte.getSolde()),
                    FormatUtils.formatMontant(SEUIL_ALERTE_SOLDE_BAS));
        }
    }
}