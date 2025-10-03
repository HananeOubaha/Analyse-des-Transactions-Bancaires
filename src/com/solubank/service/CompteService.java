// Dans le package com.solubank.service
package service;

import dao.CompteDAO;
import dao.CompteDAOImpl;
import dao.TransactionDAO;
import dao.TransactionDAOImpl;
import entity.Compte;
import entity.Transaction;
import entity.TypeTransaction;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class CompteService {

    public final CompteDAO compteDAO;
    private final TransactionDAO transactionDAO;

    public CompteService() {
        this.compteDAO = new CompteDAOImpl();
        this.transactionDAO = new TransactionDAOImpl();
    }

    // --- Opérations CRUD de base ---

    public Compte ajouterCompte(Compte compte) {
        //   S'assurer qu'un numéro de compte est unique avant de l'enregistrer
        if (compteDAO.findByNumero(compte.getNumero()).isPresent()) {
            System.err.println("Erreur: Le numéro de compte " + compte.getNumero() + " existe déjà.");
            return null;
        }
        return compteDAO.save(compte);
    }

    public Optional<Compte> trouverCompteParId(long id) {
        return compteDAO.findById(id);
    }

    public List<Compte> listerTousLesComptes() {
        return compteDAO.findAll();
    }

    //  Transactions
    public boolean effectuerRetrait(long idCompte, double montant, String lieu) {
        Optional<Compte> compteOpt = compteDAO.findById(idCompte);

        if (compteOpt.isEmpty()) {
            System.err.println("Erreur: Compte non trouvé.");
            return false;
        }

        Compte compte = compteOpt.get();
        double soldeInitial = compte.getSolde();

        // la méthode debiter() de CompteCourant/Epargne vérifie les plafonds
        compte.debiter(montant);

        if (compte.getSolde() < soldeInitial) {
            // 2. Persistance : Si le débit est autorisé (le solde a changé)
            compteDAO.update(compte);

            // 3. Enregistrement de la Transaction
            Transaction transaction = new Transaction(0, LocalDate.now(), montant, TypeTransaction.RETRAIT, lieu, idCompte);
            transactionDAO.save(transaction);

            System.out.println("Retrait de " + montant + " effectué avec succès sur le compte " + compte.getNumero());
            return true;
        } else {
            // La méthode debiter a échoué (solde insuffisant ou découvert dépassé)
            System.err.println("Opération échouée : Solde insuffisant ou limites dépassées.");
            return false;
        }
    }

    public boolean effectuerVersement(long idCompte, double montant, String lieu) {
        return trouverCompteParId(idCompte).map(compte -> {
            compte.crediter(montant);
            compteDAO.update(compte);

            Transaction transaction = new Transaction(0, LocalDate.now(), montant, TypeTransaction.VERSEMENT, lieu, idCompte);
            transactionDAO.save(transaction);

            System.out.println("Versement de " + montant + " effectué avec succès sur le compte " + compte.getNumero());
            return true;
        }).orElseGet(() -> {
            System.err.println("Erreur: Compte non trouvé pour le versement.");
            return false;
        });
    }

    public boolean effectuerVirement(long idCompteSource, long idCompteDest, double montant) {
        // Logique transactionnelle (simplifiée ici, sans bloc transactionnel JDBC)
        boolean debitReussi = this.effectuerRetrait(idCompteSource, montant, "Virement sortant vers Compte ID " + idCompteDest);

        if (debitReussi) {
            // Si le retrait est réussi, on procède au crédit
            return this.effectuerVersement(idCompteDest, montant, "Virement entrant de Compte ID " + idCompteSource);
        }

        System.err.println("Virement échoué au débit. Annulation du processus.");
        return false;
    }
    // Trouve le compte ayant le solde le plus élevé.
    public Optional<Compte> trouverCompteAvecSoldeMaximum() {
        return compteDAO.findAll().stream()
                // Utilise max() avec un Comparator pour trouver l'élément ayant la plus grande valeur
                .max(Comparator.comparingDouble(Compte::getSolde));
    }
    // Trouve le compte ayant le solde le moins élevé.
    public Optional<Compte> trouverCompteAvecSoldeMinimum() {
        return compteDAO.findAll().stream()
                // Utilise min() avec un Comparator pour trouver l'élément ayant la plus petite valeur
                .min(Comparator.comparingDouble(Compte::getSolde));
    }


    // --- Méthodes de Rapport

    public List<Transaction> trouverTransactionsTrieesParMontant(long idCompte) {
        List<Transaction> transactions = transactionDAO.findByCompteId(idCompte);

        return transactions.stream()
                // Tri : utilise la référence de méthode Transaction::montant
                .sorted(Comparator.comparingDouble(Transaction::montant).reversed())
                .toList();
    }
}