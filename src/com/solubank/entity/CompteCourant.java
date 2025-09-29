package entity;

public final class CompteCourant extends Compte {
    private double decouvertAutorise;

    public CompteCourant(long id, String numero, double solde, long idClient, double decouvertAutorise) {
        super(id, numero, solde, idClient);
        this.decouvertAutorise = decouvertAutorise;
    }

    public double getDecouvertAutorise() {
        return decouvertAutorise;
    }

    public void setDecouvertAutorise(double decouvertAutorise) {
        this.decouvertAutorise = decouvertAutorise;
    }

    @Override
    public void debiter(double montant) {
        if (getSolde() - montant >= -decouvertAutorise) {
            setSolde(getSolde() - montant);
        } else {
            System.err.println("Opération de débit impossible : découvert autorisé dépassé.");
        }
    }

    @Override
    public void crediter(double montant) {
        setSolde(getSolde() + montant);
    }
}