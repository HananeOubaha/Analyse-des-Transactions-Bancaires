// Dans le package com.solubank.dao
package dao;

import entity.Transaction;
import java.util.List;
import java.util.Optional;

public interface TransactionDAO {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(long id);
    List<Transaction> findByCompteId(long idCompte);
    List<Transaction> findAll();
    void delete(long id);
}