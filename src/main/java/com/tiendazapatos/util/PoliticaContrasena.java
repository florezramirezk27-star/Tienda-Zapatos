package com.tiendazapatos.util;

/** REQ-ZAP-006: mínimo 8 caracteres con al menos una letra y un número. */
public final class PoliticaContrasena {

    public static final String REGEX = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$";
    public static final String MENSAJE = "La contraseña debe tener mínimo 8 caracteres, con letras y números";

    private PoliticaContrasena() {
    }

    public static boolean cumple(String contrasena) {
        return contrasena != null && contrasena.matches(REGEX);
    }
}
