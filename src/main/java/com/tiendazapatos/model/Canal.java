package com.tiendazapatos.model;

public enum Canal {
    WEB("Web"),
    PRESENCIAL("Presencial");

    private final String etiqueta;

    Canal(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
