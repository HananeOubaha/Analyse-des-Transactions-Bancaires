// Dans le package com.solubank.dao
package dao;

import entity.Compte;
import java.util.List;
import java.util.Optional;

public interface CompteDAO {
    Compte save(Compte compte);
    Optional<Compte> findById(long id);
    Optional<Compte> findByNumero(String numero);
    List<Compte> findAll();
    List<Compte> findByClientId(long clientId);
    void update(Compte compte);
    void delete(long id);
}