// Dans le package com.solubank.entity
package entity;

public sealed abstract class Compte permits CompteCourant, CompteEpargne {
    private final long id;
    private final String numero;
    private double solde;
    private final long idClient;

    public Compte(long id, String numero, double solde, long idClient) {
        this.id = id;
        this.numero = numero;
        this.solde = solde;
        this.idClient = idClient;
    }

    public long getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public double getSolde() {
        return solde;
    }

    public long getIdClient() {
        return idClient;
    }

    public void setSolde(double solde) {
        this.solde = solde;
    }

    public abstract void debiter(double montant);

    public abstract void crediter(double montant);

    @Override
    public String toString() {
        return "Compte{" +
                "id=" + id +
                ", numero='" + numero + '\'' +
                ", solde=" + solde +
                '}';
    }
}