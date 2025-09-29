package entity;

import java.time.LocalDate;

public record Transaction(long id, LocalDate date, double montant, TypeTransaction type, String lieu, long idCompte) {
}
