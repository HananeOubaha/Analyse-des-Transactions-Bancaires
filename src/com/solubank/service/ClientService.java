package service;

import dao.ClientDAO;
import dao.ClientDAOImpl;
import dao.CompteDAO;
import dao.CompteDAOImpl;
import entity.Client;
import entity.Compte;

import java.util.List;
import java.util.Optional;

public class ClientService {

    private final ClientDAO clientDAO;
    private final CompteDAO compteDAO;

    // Le constructeur
    public ClientService() {
        this.clientDAO = new ClientDAOImpl();
        this.compteDAO = new CompteDAOImpl();
    }

    // --- Opérations CRUD de base ---

    public Client ajouterClient(Client client) {
        // Logique de validation métier avant l'enregistrement
        if (client.nom() == null || client.nom().trim().isEmpty() || !client.email().contains("@")) {
            System.err.println("Validation échouée: Nom ou Email invalide.");
            return null;
        }
        return clientDAO.save(client);
    }

    public Optional<Client> trouverClientParId(long id) {
        return clientDAO.findById(id); // Utilisation directe de Optional<Client>
    }

    //  Rechercher un client par nom
    public List<Client> trouverClientsParNom(String nom) {
        // Utilisation de Stream API et lambda pour filtrer une liste (simulée ici car le DAO ne fait pas encore de recherche par nom)
        // Pour une implémentation réelle, cette recherche devrait se faire au niveau du DAO (SQL LIKE)
        List<Client> allClients = clientDAO.findAll();

        //  Filtrer la liste de tous les clients
        return allClients.stream()
                .filter(c -> c.nom().toLowerCase().contains(nom.toLowerCase()))
                .toList(); // .toList() est une fonctionnalité de Java 17
    }

    public List<Client> listerTousLesClients() {
        return clientDAO.findAll();
    }

    public void modifierClient(Client client) {
        if (client.id() > 0) {
            clientDAO.update(client);
        } else {
            System.err.println("Impossible de modifier un client sans ID.");
        }
    }

    public void supprimerClient(long id) {
        // Le CompteDAO gère la suppression en cascade via SQL, donc on supprime directement le client.
        clientDAO.delete(id);
    }

    // --- Méthodes de Rapport (Utilisation de la Programmation Fonctionnelle) ---

    public double calculerSoldeTotalParClient(long clientId) {
        List<Compte> comptes = compteDAO.findByClientId(clientId);

        //  Stream, MapToDouble et Sum
        return comptes.stream()
                .mapToDouble(Compte::getSolde) // Mapping fonctionnel vers le solde
                .sum();                       // Somme de tous les soldes
    }

    public long compterNombreDeComptesParClient(long clientId) {
        List<Compte> comptes = compteDAO.findByClientId(clientId);

        // Programmation Fonctionnelle : Retourne la taille de la liste
        return comptes.stream().count();
    }
}