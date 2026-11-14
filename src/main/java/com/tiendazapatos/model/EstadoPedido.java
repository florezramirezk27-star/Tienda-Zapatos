package com.tiendazapatos.model;

import java.util.EnumSet;
import java.util.Set;

/** RN-05: PENDIENTE → CONFIRMADO → ENVIADO → ENTREGADO, y CANCELADO desde PENDIENTE o CONFIRMADO. */
public enum EstadoPedido {
    PENDIENTE, CONFIRMADO, ENVIADO, ENTREGADO, CANCELADO;

    public Set<EstadoPedido> siguientes() {
        return switch (this) {
            case PENDIENTE -> EnumSet.of(CONFIRMADO, CANCELADO);
            case CONFIRMADO -> EnumSet.of(ENVIADO, CANCELADO);
            case ENVIADO -> EnumSet.of(ENTREGADO);
            case ENTREGADO, CANCELADO -> EnumSet.noneOf(EstadoPedido.class);
        };
    }

    public boolean permite(EstadoPedido nuevo) {
        return siguientes().contains(nuevo);
    }

    /** RN-07: la factura solo existe para pedidos CONFIRMADO, ENVIADO o ENTREGADO. */
    public boolean isFacturable() {
        return this == CONFIRMADO || this == ENVIADO || this == ENTREGADO;
    }
}
