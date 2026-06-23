package br.com.agendafacilpro.util;

public final class PhoneNormalizer {

    private PhoneNormalizer() {
    }

    /**
     * Normaliza telefones brasileiros para o formato usado na identificação do cliente.
     * Remove máscara e o prefixo 55 quando o número contém DDI + DDD + celular.
     */
    public static String normalize(String phone) {
        String normalized = digitsWithoutBrazilCountryCode(phone);
        if (!isValidNormalized(normalized)) {
            throw new IllegalArgumentException("Telefone inválido.");
        }
        return normalized;
    }

    /**
     * Mantém somente dígitos e remove o DDI brasileiro quando ele aparece no padrão esperado.
     */
    public static String digitsWithoutBrazilCountryCode(String phone) {
        String digits = phone == null ? "" : phone.replaceAll("\\D", "");
        return digits.startsWith("55") && digits.length() == 13 ? digits.substring(2) : digits;
    }

    public static boolean isValidNormalized(String normalizedPhone) {
        return normalizedPhone != null && normalizedPhone.length() >= 10 && normalizedPhone.length() <= 11;
    }
}
