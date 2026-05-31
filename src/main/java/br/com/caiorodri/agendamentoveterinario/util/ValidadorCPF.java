package br.com.caiorodri.agendamentoveterinario.util;

public class ValidadorCPF {

    public static boolean isValido(String cpf) {

        if (cpf == null) {
            return false;
        }

        cpf = cpf.replaceAll("\\D", "");

        if (cpf.length() != 11) {
            return false;
        }

        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        try {
            int soma = 0;
            int peso = 10;
            for (int i = 0; i < 9; i++) {
                int numero = cpf.charAt(i) - '0';
                soma += numero * peso;
                peso--;
            }

            int resto = soma % 11;
            int digito1 = (resto < 2) ? 0 : (11 - resto);

            soma = 0;
            peso = 11;
            for (int i = 0; i < 10; i++) {
                int numero = cpf.charAt(i) - '0';
                soma += numero * peso;
                peso--;
            }

            resto = soma % 11;
            int digito2 = (resto < 2) ? 0 : (11 - resto);

            int digitoInformado1 = cpf.charAt(9) - '0';
            int digitoInformado2 = cpf.charAt(10) - '0';

            return (digito1 == digitoInformado1) && (digito2 == digitoInformado2);

        } catch (Exception e) {
            return false;
        }
    }

}