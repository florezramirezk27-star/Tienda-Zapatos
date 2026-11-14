package com.tiendazapatos.model;

/** Los 5 roles del sistema. Cada usuario tiene exactamente uno (RN-04). */
public enum Rol {
    ADMINISTRADOR("Administrador"),
    GERENTE("Gerente"),
    VENDEDOR("Vendedor"),
    ALMACENISTA("Almacenista"),
    CLIENTE("Cliente");

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
