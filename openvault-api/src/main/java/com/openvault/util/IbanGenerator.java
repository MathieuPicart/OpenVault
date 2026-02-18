package com.openvault.util;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class IbanGenerator {

    private static final String COUNTRY_CODE = "FR";
    private static final String BANK_CODE = "10278"; // Code fictif
    private static final Random random = new Random();

    /**
     * Génère un IBAN français valide
     * Format: FR76 XXXX XXXX XXXX XXXX XXXX XXX (27 caractères)
     */
    public String generateIban() {
        // Générer le BBAN (Basic Bank Account Number)
        String branchCode = generateNumericString(5);
        String accountNumber = generateNumericString(11);
        String nationalCheckDigits = generateNumericString(2);
        
        String bban = BANK_CODE + branchCode + accountNumber + nationalCheckDigits;
        
        // Calculer la clé de contrôle
        String checkDigits = calculateCheckDigits(COUNTRY_CODE, bban);
        
        // Formater l'IBAN
        String iban = COUNTRY_CODE + checkDigits + bban;
        
        return formatIban(iban);
    }

    /**
     * Calcule les 2 chiffres de contrôle de l'IBAN
     */
    private String calculateCheckDigits(String countryCode, String bban) {
        // Déplacer les 4 premiers caractères à la fin
        String rearranged = bban + countryCode + "00";
        
        // Remplacer les lettres par des chiffres (A=10, B=11, etc.)
        StringBuilder numericString = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            if (Character.isLetter(c)) {
                numericString.append(Character.getNumericValue(c));
            } else {
                numericString.append(c);
            }
        }
        
        // Calculer mod 97
        int checksum = 98 - mod97(numericString.toString());
        
        return String.format("%02d", checksum);
    }

    /**
     * Calcule le modulo 97 pour de grands nombres
     */
    private int mod97(String numericString) {
        String remainder = "0";
        for (int i = 0; i < numericString.length(); i += 7) {
            int end = Math.min(i + 7, numericString.length());
            String part = remainder + numericString.substring(i, end);
            remainder = String.valueOf(Long.parseLong(part) % 97);
        }
        return Integer.parseInt(remainder);
    }

    /**
     * Génère une chaîne numérique aléatoire
     */
    private String generateNumericString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * Formate l'IBAN avec des espaces tous les 4 caractères
     */
    private String formatIban(String iban) {
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < iban.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(" ");
            }
            formatted.append(iban.charAt(i));
        }
        return formatted.toString();
    }

    /**
     * Valide un IBAN
     */
    public boolean isValidIban(String iban) {
        if (iban == null || iban.isEmpty()) {
            return false;
        }
        
        // Supprimer les espaces
        String cleanIban = iban.replaceAll("\\s", "");
        
        // Vérifier la longueur (27 pour la France)
        if (cleanIban.length() != 27) {
            return false;
        }
        
        // Vérifier le format
        if (!cleanIban.matches("^[A-Z]{2}\\d{25}$")) {
            return false;
        }
        
        // Vérifier la clé de contrôle
        String rearranged = cleanIban.substring(4) + cleanIban.substring(0, 4);
        StringBuilder numericString = new StringBuilder();
        
        for (char c : rearranged.toCharArray()) {
            if (Character.isLetter(c)) {
                numericString.append(Character.getNumericValue(c));
            } else {
                numericString.append(c);
            }
        }
        
        return mod97(numericString.toString()) == 1;
    }
}