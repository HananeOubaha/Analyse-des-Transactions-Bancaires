package util;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class FormatUtils {

    // Définition d'un formateur de montant pour le formatage monétaire
    // Utilisation de la Locale France pour le séparateur de milliers et décimal
    private static final DecimalFormat MONETARY_FORMAT = new DecimalFormat("#,##0.00 dh",
            new java.text.DecimalFormatSymbols(Locale.FRANCE));

    // Définition d'un formateur de date standard (ex: 29/09/2025)
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private FormatUtils() {
        // Empêche l'instanciation de cette classe utilitaire
        throw new UnsupportedOperationException("Cette classe ne doit pas être instanciée.");
    }


    public static String formatMontant(double montant) {
        return MONETARY_FORMAT.format(montant);
    }

    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "N/A";
        }
        return date.format(DATE_FORMATTER);
    }

    public static boolean isValidEmail(String email) {
        // Expression régulière simple pour vérifier la présence de @ et d'au moins un point après @
        return email != null && email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
    }
}