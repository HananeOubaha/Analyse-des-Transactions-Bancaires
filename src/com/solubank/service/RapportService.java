// Dans le package com.solubank.service
package service;

import dao.ClientDAO;
import dao.ClientDAOImpl;
import dao.CompteDAO;
import dao.CompteDAOImpl;
import dao.TransactionDAO;
import dao.TransactionDAOImpl;
import entity.Client;
import entity.Compte;
import entity.Transaction;
import entity.TypeTransaction;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class RapportService {

    private final ClientDAO clientDAO;
    private final CompteDAO compteDAO;
    private final TransactionDAO transactionDAO;

    // Définition des seuils
    public static final int PERIODE_INACTIVITE_JOURS = 90; // 3 mois d'inactivité
    private static final double SEUIL_MONTANT_SUSPECT = 10000.0; // Même seuil que TransactionService

    public RapportService() {
        this.clientDAO = new ClientDAOImpl();
        this.compteDAO = new CompteDAOImpl();
        this.transactionDAO = new TransactionDAOImpl();
    }

    // --- Génération de Rapports Statistique (Stream API Avancé) ---

    /**
     * Génère le top 5 des clients ayant le solde total le plus élevé.
     * Utilise Stream API pour regrouper, sommer, trier et limiter.
     */
    public List<Map.Entry<Client, Double>> genererTop5ClientsParSolde() {
        // 1. Récupérer tous les comptes et les regrouper par ID Client
        Map<Long, List<Compte>> comptesParClient = compteDAO.findAll().stream()
                .collect(Collectors.groupingBy(Compte::getIdClient));

        // 2. Calculer le solde total pour chaque Client et le stocker dans un Map<Client, Double>
        Map<Client, Double> soldeTotalParClient = comptesParClient.entrySet().stream()
                // Map Entry<Long, List<Compte>> -> Map Entry<Client, Double>
                .collect(Collectors.toMap(
                        // Clé : Récupère l'objet Client correspondant à l'ID
                        entry -> clientDAO.findById(entry.getKey()).orElse(null),
                        // Valeur : Somme les soldes de tous les comptes de la liste
                        entry -> entry.getValue().stream()
                                .mapToDouble(Compte::getSolde)
                                .sum()
                ));

        // 3. Filtrer les clients null (si non trouvés), trier par solde décroissant et limiter à 5
        return soldeTotalParClient.entrySet().stream()
                .filter(entry -> entry.getKey() != null)
                // Tri par valeur (solde total) en ordre décroissant
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .toList();
    }

    /**
     * Produit un rapport mensuel : Nombre de transactions par type et volume total.
     * Utilise groupingBy et mapping (opérations complexes de Collectors).
     */
    public Map<TypeTransaction, Map<String, Object>> genererRapportMensuel(int mois, int annee) {
        LocalDate debutMois = LocalDate.of(annee, mois, 1);
        LocalDate finMois = debutMois.plusMonths(1).minusDays(1);

        List<Transaction> transactionsDuMois = transactionDAO.findAll().stream()
                // Filtrer par date (Lambda expression)
                .filter(t -> t.date() != null && !t.date().isBefore(debutMois) && !t.date().isAfter(finMois))
                .toList();

        // Utiliser groupingBy et Collectors.collectingAndThen pour générer des statistiques par Type
        return transactionsDuMois.stream()
                .collect(Collectors.groupingBy(
                        Transaction::type,
                        // Mapping de chaque groupe de transactions vers un Map<String, Object> de statistiques
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> Map.of(
                                        "nombre", list.size(),
                                        "volumeTotal", list.stream().mapToDouble(Transaction::montant).sum()
                                )
                        )
                ));
    }

    // --- Détection des Anomalies (Spécifique au Rapport) ---

    /**
     * Identifie les comptes inactifs depuis une certaine période (90 jours simulés).
     * Un compte est inactif si sa dernière transaction est plus ancienne que le seuil.
     */
    public List<Compte> identifierComptesInactifs() {
        List<Compte> tousComptes = compteDAO.findAll();
        LocalDate seuilInactivite = LocalDate.now().minusDays(PERIODE_INACTIVITE_JOURS);

        // Utilise Stream pour trouver la dernière transaction et filtrer
        return tousComptes.stream()
                .filter(compte -> {
                    List<Transaction> transactions = transactionDAO.findByCompteId(compte.getId());

                    // Fonctionnelle : Trouver la date maximale de transaction (Optional)
                    Optional<LocalDate> dateDerniereTransaction = transactions.stream()
                            .map(Transaction::date)
                            .max(Comparator.naturalOrder());

                    // Si aucune transaction, le compte est inactif par défaut.
                    // Si présente, vérifier si elle est avant le seuil.
                    return dateDerniereTransaction
                            .map(date -> date.isBefore(seuilInactivite))
                            .orElse(true); // Aucune transaction trouvée -> inactif
                })
                .toList();
    }
}