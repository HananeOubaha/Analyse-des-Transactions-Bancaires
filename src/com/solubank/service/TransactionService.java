// Dans le package com.solubank.service
package service;

import dao.TransactionDAO;
import dao.TransactionDAOImpl;
import entity.Transaction;
import entity.TypeTransaction;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TransactionService {

    private final TransactionDAO transactionDAO;
    private static final double SEUIL_MONTANT_SUSPECT = 10000.0; // Seuil pour la détection d'anomalie
    private static final String PAYS_HABITUEL = "MAROC"; // Simuler le pays habituel

    public TransactionService() {
        this.transactionDAO = new TransactionDAOImpl();
    }

    // --- Opérations de consultation et filtrage (Utilisation intensive de Stream API) ---

    /**
     * Liste les transactions d'un compte, triées par date (du plus récent au plus ancien).
     * Utilise le tri du DAO puis assure l'ordre.
     */
    public List<Transaction> listerTransactionsParCompte(long idCompte) {
        return transactionDAO.findByCompteId(idCompte).stream()
                .sorted(Comparator.comparing(Transaction::date).reversed())
                .toList();
    }

    /**
     * Filtre les transactions selon le montant minimum.
     * Utilise la fonction filter().
     */
    public List<Transaction> filtrerParMontantMin(double montantMin) {
        return transactionDAO.findAll().stream()
                .filter(t -> t.montant() >= montantMin)
                .toList();
    }

    /**
     * Regroupe les transactions par Type (VERSEMENT, RETRAIT, VIREMENT).
     * Utilise Collectors.groupingBy().
     */
    public Map<TypeTransaction, List<Transaction>> regrouperParType() {
        return transactionDAO.findAll().stream()
                .collect(Collectors.groupingBy(Transaction::type));
    }

    /**
     * Calcule la moyenne des transactions pour un client donné.
     * Utilise MapToDouble et Average.
     */
    public double calculerMoyenneTransactionsParCompte(long idCompte) {
        List<Transaction> transactions = transactionDAO.findByCompteId(idCompte);

        // Programmation Fonctionnelle : Stream, mapToDouble, average
        return transactions.stream()
                .mapToDouble(Transaction::montant)
                .average()
                // Utilise orElse(0.0) pour gérer le cas où la liste est vide (OptionalDouble)
                .orElse(0.0);
    }

    // --- Détection des Anomalies (Logique Métier Avancée) ---

    /**
     * Détecte les transactions suspectes :
     * 1. Montant élevé (supérieur au seuil défini).
     * 2. Lieu inhabituel (lieu différent du pays habituel simulé).
     */
    public List<Transaction> detecterTransactionsSuspectes() {
        // Simule la détection des anomalies en filtrant l'ensemble des transactions
        return transactionDAO.findAll().stream()
                .filter(t -> t.montant() > SEUIL_MONTANT_SUSPECT ||
                        (t.lieu() != null && !t.lieu().toUpperCase().contains(PAYS_HABITUEL)))
                .toList();
    }

    /**
     * Détecte la "fréquence excessive" : plus de 3 opérations en moins de 1 minute (simulation).
     * Pour une implémentation sans base de données, nous simulons une vérification par lot de compte.
     * REMARQUE : Cette vérification nécessite des timestamps (heure/minute) et est simplifiée ici
     * en considérant les transactions du jour pour un même compte.
     */
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
}