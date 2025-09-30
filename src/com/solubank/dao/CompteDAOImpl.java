// Dans le package com.solubank.dao
package dao;

import entity.*;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CompteDAOImpl implements CompteDAO {

    // Requêtes SQL
    private static final String INSERT_COMPTE =
            "INSERT INTO Compte (numero, solde, idClient, typeCompte, decouvertAutorise, tauxInteret) " +
                    "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
    private static final String SELECT_COMPTE_BY_ID =
            "SELECT * FROM Compte WHERE id = ?";
    private static final String SELECT_COMPTE_BY_NUMERO =
            "SELECT * FROM Compte WHERE numero = ?";
    private static final String SELECT_ALL_COMPTES =
            "SELECT * FROM Compte";
    private static final String SELECT_COMPTE_BY_CLIENT =
            "SELECT * FROM Compte WHERE idClient = ?";
    private static final String UPDATE_COMPTE =
            "UPDATE Compte SET solde = ?, decouvertAutorise = ?, tauxInteret = ? WHERE id = ?";
    private static final String DELETE_COMPTE =
            "DELETE FROM Compte WHERE id = ?";

    // --- Méthode d'aide pour le mapping (Cruciale pour la hiérarchie sealed) ---
    private Compte mapRowToCompte(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String numero = rs.getString("numero");
        double solde = rs.getDouble("solde");
        long idClient = rs.getLong("idClient");
        String typeCompte = rs.getString("typeCompte");

        if ("courant".equalsIgnoreCase(typeCompte)) {
            double decouvertAutorise = rs.getDouble("decouvertAutorise");
            return new CompteCourant(id, numero, solde, idClient, decouvertAutorise);
        } else if ("epargne".equalsIgnoreCase(typeCompte)) {
            double tauxInteret = rs.getDouble("tauxInteret");
            return new CompteEpargne(id, numero, solde, idClient, tauxInteret);
        }
        throw new SQLException("Type de compte inconnu: " + typeCompte);
    }

    @Override
    public Compte save(Compte compte) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_COMPTE)) {

            String typeCompte = "";
            Double decouvert = null;
            Double taux = null;

            if (compte instanceof CompteCourant cc) { // Utilisation de Pattern Matching for instanceof (Java 17)
                typeCompte = "courant";
                decouvert = cc.getDecouvertAutorise();
            } else if (compte instanceof CompteEpargne ce) {
                typeCompte = "epargne";
                taux = ce.getTauxInteret();
            }

            ps.setString(1, compte.getNumero());
            ps.setDouble(2, compte.getSolde());
            ps.setLong(3, compte.getIdClient());
            ps.setString(4, typeCompte);

            // Gestion des valeurs nulles pour les colonnes spécifiques
            if (decouvert != null) ps.setDouble(5, decouvert); else ps.setNull(5, Types.DECIMAL);
            if (taux != null) ps.setDouble(6, taux); else ps.setNull(6, Types.DECIMAL);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                long generatedId = rs.getLong(1);
                // On met à jour l'ID du compte pour le retourner (même si Compte n'est pas un record, c'est utile)
                // Pour les classes non-record, on devrait modifier le constructeur ou utiliser une Factory,
                // mais pour simplifier ici on retourne l'objet avec l'ID, en supposant que l'objet a été créé avec id=0.
                if (compte instanceof CompteCourant cc) return new CompteCourant(generatedId, cc.getNumero(), cc.getSolde(), cc.getIdClient(), cc.getDecouvertAutorise());
                if (compte instanceof CompteEpargne ce) return new CompteEpargne(generatedId, ce.getNumero(), ce.getSolde(), ce.getIdClient(), ce.getTauxInteret());
            }

        } catch (SQLException e) {
            System.err.println("Erreur DAO lors de l'ajout du compte: " + e.getMessage());
        }
        return compte;
    }

    @Override
    public Optional<Compte> findById(long id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_COMPTE_BY_ID)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRowToCompte(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur DAO lors de la recherche du compte (ID " + id + "): " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Compte> findByNumero(String numero) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_COMPTE_BY_NUMERO)) {

            ps.setString(1, numero);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRowToCompte(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur DAO lors de la recherche du compte (Numéro " + numero + "): " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Compte> findAll() {
        return findListByQuery(SELECT_ALL_COMPTES, null);
    }

    @Override
    public List<Compte> findByClientId(long clientId) {
        return findListByQuery(SELECT_COMPTE_BY_CLIENT, clientId);
    }

    // Méthode générique interne pour réutiliser le code de recherche de listes
    private List<Compte> findListByQuery(String sql, Long parameter) {
        List<Compte> comptes = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (parameter != null) {
                ps.setLong(1, parameter);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    comptes.add(mapRowToCompte(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur DAO lors de la recherche de liste de comptes: " + e.getMessage());
        }
        return comptes;
    }

    @Override
    public void update(Compte compte) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_COMPTE)) {

            Double decouvert = null;
            Double taux = null;

            if (compte instanceof CompteCourant cc) {
                decouvert = cc.getDecouvertAutorise();
            } else if (compte instanceof CompteEpargne ce) {
                taux = ce.getTauxInteret();
            }

            ps.setDouble(1, compte.getSolde());
            if (decouvert != null) ps.setDouble(2, decouvert); else ps.setNull(2, Types.DECIMAL);
            if (taux != null) ps.setDouble(3, taux); else ps.setNull(3, Types.DECIMAL);
            ps.setLong(4, compte.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur DAO lors de la mise à jour du compte: " + e.getMessage());
        }
    }

    @Override
    public void delete(long id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_COMPTE)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur DAO lors de la suppression du compte: " + e.getMessage());
        }
    }
}