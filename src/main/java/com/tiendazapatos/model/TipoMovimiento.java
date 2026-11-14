package com.tiendazapatos.model;

public enum TipoMovimiento {
    ENTRADA("Entrada"),
    AJUSTE("Ajuste"),
    VENTA("Venta"),
    DEVOLUCION("Devolución");

    private final String etiqueta;

    TipoMovimiento(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
