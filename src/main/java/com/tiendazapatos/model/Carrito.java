package com.tiendazapatos.model;

import com.tiendazapatos.exception.ReglaNegocioException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** El carrito no es una entidad: vive en la sesión HTTP del cliente hasta que confirma el pedido. */
@Component
@SessionScope
public class Carrito implements Serializable {

    public static class Item implements Serializable {
        private final Long tallaId;
        private final Long zapatoId;
        private final String nombre;
        private final String marca;
        private final String categoria;
        private final String color;
        private final String imagen;
        private final int talla;
        private final BigDecimal precio;
        private int cantidad;

        Item(TallaStock t, int cantidad) {
            this.tallaId = t.getId();
            this.zapatoId = t.getZapato().getId();
            this.nombre = t.getZapato().getNombre();
            this.marca = t.getZapato().getMarca();
            this.categoria = t.getZapato().getCategoria();
            this.color = t.getZapato().getColor();
            this.imagen = t.getZapato().getImagen();
            this.talla = t.getTalla();
            this.precio = t.getZapato().getPrecio();
            this.cantidad = cantidad;
        }

        public BigDecimal getSubtotal() {
            return precio.multiply(BigDecimal.valueOf(cantidad));
        }

        public Long getTallaId() {
            return tallaId;
        }

        public Long getZapatoId() {
            return zapatoId;
        }

        public String getNombre() {
            return nombre;
        }

        public String getMarca() {
            return marca;
        }

        public String getCategoria() {
            return categoria;
        }

        public String getColor() {
            return color;
        }

        public String getImagen() {
            return imagen;
        }

        public int getTalla() {
            return talla;
        }

        public BigDecimal getPrecio() {
            return precio;
        }

        public int getCantidad() {
            return cantidad;
        }
    }

    private final Map<Long, Item> items = new LinkedHashMap<>();

    /** REQ-ZAP-020 y 021: agrega al carrito sin superar el stock de la talla. */
    public void agregar(TallaStock t, int cantidad) {
        int existente = items.containsKey(t.getId()) ? items.get(t.getId()).cantidad : 0;
        validarStock(t, existente + cantidad);
        if (existente == 0) {
            items.put(t.getId(), new Item(t, cantidad));
        } else {
            items.get(t.getId()).cantidad = existente + cantidad;
        }
    }

    /** REQ-ZAP-022: cambia la cantidad de un ítem (0 o menos lo elimina). */
    public void actualizar(TallaStock t, int cantidad) {
        if (cantidad <= 0) {
            items.remove(t.getId());
            return;
        }
        Item item = items.get(t.getId());
        if (item == null) {
            return;
        }
        validarStock(t, cantidad);
        item.cantidad = cantidad;
    }

    public void quitar(Long tallaId) {
        items.remove(tallaId);
    }

    public void vaciar() {
        items.clear();
    }

    private void validarStock(TallaStock t, int solicitada) {
        if (solicitada > t.getStock()) {
            throw new ReglaNegocioException("Cantidad superior al stock. Disponible: " + t.getStock());
        }
    }

    public List<Item> getItems() {
        return new ArrayList<>(items.values());
    }

    public boolean isVacio() {
        return items.isEmpty();
    }

    public int getUnidades() {
        return items.values().stream().mapToInt(Item::getCantidad).sum();
    }

    public BigDecimal getSubtotal() {
        return items.values().stream().map(Item::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getIva() {
        return Pedido.ivaDe(getSubtotal());
    }

    /** REQ-ZAP-025: total = subtotal + IVA; no se agrega ningún costo de envío. */
    public BigDecimal getTotal() {
        return getSubtotal().add(getIva());
    }
}
