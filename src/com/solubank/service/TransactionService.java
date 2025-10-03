
package service;

import dao.TransactionDAO;
import dao.TransactionDAOImpl;
import entity.Transaction;
import entity.TypeTransaction;
import dao.CompteDAOImpl;
import entity.Compte;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TransactionService {

    private final TransactionDAO transactionDAO;
    private static final double SEUIL_MONTANT_SUSPECT = 10000.0;
    private static final String PAYS_HABITUEL = "MAROC";

    public TransactionService() {
        this.transactionDAO = new TransactionDAOImpl();
    }

    // --- Opérations de consultation et filtrage

    public List<Transaction> listerTransactionsParCompte(long idCompte) {
        return transactionDAO.findByCompteId(idCompte).stream()
                .sorted(Comparator.comparing(Transaction::date).reversed())
                .toList();
    }

    public List<Transaction> filtrerParMontantMin(double montantMin) {
        return transactionDAO.findAll().stream()
                .filter(t -> t.montant() >= montantMin)
                .toList();
    }

    public Map<TypeTransaction, List<Transaction>> regrouperParType() {
        return transactionDAO.findAll().stream()
                .collect(Collectors.groupingBy(Transaction::type));
    }

    public double calculerMoyenneTransactionsParCompte(long idCompte) {
        List<Transaction> transactions = transactionDAO.findByCompteId(idCompte);

        // Programmation Fonctionnelle : Stream, mapToDouble, average
        return transactions.stream()
                .mapToDouble(Transaction::montant)
                .average()
                // Utilise orElse(0.0) pour gérer le cas où la liste est vide (OptionalDouble)
                .orElse(0.0);
    }

    // --- Détection des Anomalies

    public List<Transaction> detecterTransactionsSuspectes() {
        // Simule la détection des anomalies en filtrant l'ensemble des transactions
        return transactionDAO.findAll().stream()
                .filter(t -> t.montant() > SEUIL_MONTANT_SUSPECT ||
                        (t.lieu() != null && !t.lieu().toUpperCase().contains(PAYS_HABITUEL)))
                .toList();
    }

    public List<Transaction> detecterFrequenceExcessive() {
        // 1. Regrouper toutes les transactions par ID de Compte
        Map<Long, List<Transaction>> transactionsParCompte = transactionDAO.findAll().stream()
                .collect(Collectors.groupingBy(Transaction::idCompte));

        // 2. Traiter chaque groupe (compte) et filtrer les transactions suspectes
        // Utilise Stream pour parcourir la Map des comptes
        return transactionsParCompte.values().stream()
                // FlatMap pour aplatir les listes de transactions de chaque compte
                .flatMap(transactions -> {
                    // Filtrer les transactions du jour et trier par date/heure (simulé par date seule ici)
                    List<Transaction> transactionsAujourdhui = transactions.stream()
                            .filter(t -> t.date().isEqual(LocalDate.now()))
                            .sorted(Comparator.comparing(Transaction::date)) // Tri par date pour simuler l'ordre temporel
                            .toList();

                    // Logique simplifiée pour détecter l'excès (plus de 3 transactions en 1 minute simulée)
                    if (transactionsAujourdhui.size() > 3) {
                        System.out.println("Alerte: " + transactionsAujourdhui.get(0).idCompte() + " a " + transactionsAujourdhui.size() + " transactions aujourd'hui.");
                        return transactionsAujourdhui.stream(); // Retourne toutes les transactions du compte comme suspectes
                    }
                    return java.util.stream.Stream.empty();
                })
                .toList();
    }
    // Filtre les transactions selon le lieu (recherche partielle).
    public List<Transaction> filtrerParLieu(String lieu) {
        return transactionDAO.findAll().stream()
                .filter(t -> t.lieu() != null && t.lieu().toLowerCase().contains(lieu.toLowerCase()))
                .toList();
    }
// Filtre les transactions selon une date spécifique.
public List<Transaction> filtrerParDate(LocalDate date) {
    return transactionDAO.findAll().stream()
            .filter(t -> t.date() != null && t.date().isEqual(date))
            .toList();
}

// Calcule le total du volume des transactions pour un client donné.
public double calculerTotalTransactionsParClient(long idClient) {
    List<Long> compteIds = new CompteDAOImpl().findByClientId(idClient).stream()
            .map(Compte::getId)
            .toList();
    return transactionDAO.findAll().stream()
            .filter(t -> compteIds.contains(t.idCompte()))
            .mapToDouble(Transaction::montant)
            .sum();
}
}