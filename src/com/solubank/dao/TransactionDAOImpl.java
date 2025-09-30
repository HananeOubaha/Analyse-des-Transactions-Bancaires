// Dans le package com.solubank.dao
package dao;

import entity.Transaction;
import entity.TypeTransaction; // Assurez-vous d'importer l'enum
import util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionDAOImpl implements TransactionDAO {

    // Requêtes SQL
    private static final String INSERT_TRANSACTION =
            "INSERT INTO Transaction (dateTrans, montant, type, lieu, idCompte) " +
                    "VALUES (?, ?, ?::TransactionType, ?, ?) RETURNING id";
    private static final String SELECT_TRANSACTION_BY_ID =
            "SELECT * FROM Transaction WHERE id = ?";
    private static final String SELECT_TRANSACTION_BY_COMPTE =
            "SELECT * FROM Transaction WHERE idCompte = ? ORDER BY dateTrans DESC"; // Tri par défaut
    private static final String SELECT_ALL_TRANSACTIONS =
            "SELECT * FROM Transaction ORDER BY dateTrans DESC";
    private static final String DELETE_TRANSACTION =
            "DELETE FROM Transaction WHERE id = ?";

    // --- Méthode d'aide pour le mapping ---
    private Transaction mapRowToTransaction(ResultSet rs) throws SQLException {
        // Conversion de Date SQL en LocalDate Java 8+
        Date sqlDate = rs.getDate("dateTrans");
        LocalDate date = (sqlDate != null) ? sqlDate.toLocalDate() : null;

        return new Transaction(
                rs.getLong("id"),
                date,
                rs.getDouble("montant"),
                // Convertit la chaîne de caractères (ex: 'VERSEMENT') en enum TypeTransaction
                TypeTransaction.valueOf(rs.getString("type")),
                rs.getString("lieu"),
                rs.getLong("idCompte")
        );
    }

    @Override
    public Transaction save(Transaction transaction) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_TRANSACTION)) {

            // Conversion de LocalDate en Date SQL
            ps.setDate(1, Date.valueOf(transaction.date()));
            ps.setDouble(2, transaction.montant());
            // Utilisation de .name() pour obtenir le nom de la constante ENUM (ex: "VERSEMENT")
            ps.setString(3, transaction.type().name());
            ps.setString(4, transaction.lieu());
            ps.setLong(5, transaction.idCompte());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                long generatedId = rs.getLong(1);
                // Retourne un nouvel objet Transaction avec l'ID généré
                return new Transaction(generatedId, transaction.date(), transaction.montant(),
                        transaction.type(), transaction.lieu(), transaction.idCompte());
            }

        } catch (SQLException e) {
            System.err.println("Erreur DAO lors de l'ajout de la transaction: " + e.getMessage());
        }
        return transaction;
    }

    // Utilisation d'Optional
    @Override
    public Optional<Transaction> findById(long id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_TRANSACTION_BY_ID)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRowToTransaction(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur DAO lors de la recherche de la transaction (ID " + id + "): " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Transaction> findByCompteId(long idCompte) {
        return findListByLongParameter(SELECT_TRANSACTION_BY_COMPTE, idCompte);
    }

    @Override
    public List<Transaction> findAll() {
        return findListByLongParameter(SELECT_ALL_TRANSACTIONS, null);
    }

    // Méthode générique interne pour réutiliser le code de recherche de listes
    private List<Transaction> findListByLongParameter(String sql, Long parameter) {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (parameter != null) {
                ps.setLong(1, parameter);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapRowToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur DAO lors de la recherche de liste de transactions: " + e.getMessage());
        }
        return transactions;
    }

    // Note: Pas de méthode update(Transaction) car les transactions sont généralement immuables
    // et ne sont jamais modifiées une fois enregistrées.

    @Override
    public void delete(long id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_TRANSACTION)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur DAO lors de la suppression de la transaction: " + e.getMessage());
        }
    }
}