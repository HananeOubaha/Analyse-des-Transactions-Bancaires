// Dans le package com.solubank.dao
package dao;

import entity.Client;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientDAOImpl implements ClientDAO {

    // Requêtes SQL
    private static final String INSERT_CLIENT = "INSERT INTO Client (nom, email) VALUES (?, ?) RETURNING id";
    private static final String SELECT_CLIENT_BY_ID = "SELECT id, nom, email FROM Client WHERE id = ?";
    private static final String SELECT_ALL_CLIENTS = "SELECT id, nom, email FROM Client";
    private static final String UPDATE_CLIENT = "UPDATE Client SET nom = ?, email = ? WHERE id = ?";
    private static final String DELETE_CLIENT = "DELETE FROM Client WHERE id = ?";

    // --- Méthode d'aide pour le mapping ---
    private Client mapRowToClient(ResultSet rs) throws SQLException {
        return new Client(
                rs.getLong("id"),
                rs.getString("nom"),
                rs.getString("email")
        );
    }

    @Override
    public Client save(Client client) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_CLIENT)) {

            ps.setString(1, client.nom());
            ps.setString(2, client.email());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                long generatedId = rs.getLong(1);
                // Retourne un nouvel objet Client avec l'ID généré
                return new Client(generatedId, client.nom(), client.email());
            }

        } catch (SQLException e) {
            System.err.println("Erreur DAO lors de l'ajout du client: " + e.getMessage());
        }
        return client; // Retourne l'objet original si l'insertion échoue
    }

    // Utilisation d'Optional pour gérer l'absence de résultat (programmation fonctionnelle)
    @Override
    public Optional<Client> findById(long id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_CLIENT_BY_ID)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRowToClient(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur DAO lors de la recherche du client (ID " + id + "): " + e.getMessage());
        }
        return Optional.empty(); // Retourne un Optional vide si non trouvé
    }

    @Override
    public List<Client> findAll() {
        List<Client> clients = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(SELECT_ALL_CLIENTS)) {

            while (rs.next()) {
                clients.add(mapRowToClient(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur DAO lors du listage de tous les clients: " + e.getMessage());
        }
        return clients;
    }

    @Override
    public void update(Client client) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_CLIENT)) {

            ps.setString(1, client.nom());
            ps.setString(2, client.email());
            ps.setLong(3, client.id());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                System.out.println("Avertissement: Aucun client mis à jour pour l'ID " + client.id());
            }

        } catch (SQLException e) {
            System.err.println("Erreur DAO lors de la mise à jour du client: " + e.getMessage());
        }
    }

    @Override
    public void delete(long id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_CLIENT)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur DAO lors de la suppression du client (ID " + id + "): " + e.getMessage());
        }
    }
}