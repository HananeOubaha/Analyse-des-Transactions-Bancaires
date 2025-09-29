package entity;

public final class CompteEpargne extends Compte {
    private double tauxInteret;

    public CompteEpargne(long id, String numero, double solde, long idClient, double tauxInteret) {
        super(id, numero, solde, idClient);
        this.tauxInteret = tauxInteret;
    }

    public double getTauxInteret() {
        return tauxInteret;
    }

    public void setTauxInteret(double tauxInteret) {
        this.tauxInteret = tauxInteret;
    }

    @Override
    public void debiter(double montant) {
        if (getSolde() - montant >= 0) {
            setSolde(getSolde() - montant);
        } else {
            System.err.println("Opération de débit impossible : solde insuffisant.");
        }
    }

    @Override
    public void crediter(double montant) {
        setSolde(getSolde() + montant);
    }
}