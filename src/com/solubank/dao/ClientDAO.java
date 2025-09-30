package dao;

import entity.Client;
import java.util.List;
import java.util.Optional;

public interface ClientDAO {
    Client save(Client client);
    Optional<Client> findById(long id);
    List<Client> findAll();
    void update(Client client);
    void delete(long id);
}