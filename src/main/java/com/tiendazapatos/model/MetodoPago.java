package com.tiendazapatos.model;

/** No hay pasarela de pagos real: el método solo se registra en el pedido. */
public enum MetodoPago {
    CONTRA_ENTREGA("Pago contra entrega"),
    TRANSFERENCIA("Transferencia"),
    EFECTIVO("Efectivo"),
    TARJETA("Tarjeta en el punto de venta");

    private final String etiqueta;

    MetodoPago(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
