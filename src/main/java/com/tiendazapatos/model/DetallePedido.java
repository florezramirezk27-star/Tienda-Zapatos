package com.tiendazapatos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "detalles_pedido")
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zapato_id")
    private Zapato zapato;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "talla_stock_id")
    private TallaStock tallaStock;

    /** Copia del nombre y del precio al momento de la compra: el pedido no cambia si el zapato cambia. */
    @Column(nullable = false, length = 120)
    private String nombreZapato;

    @Column(nullable = false)
    private int talla;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    public DetallePedido(Pedido pedido, TallaStock tallaStock, int cantidad) {
        this.pedido = pedido;
        this.tallaStock = tallaStock;
        this.zapato = tallaStock.getZapato();
        this.nombreZapato = zapato.getNombre();
        this.talla = tallaStock.getTalla();
        this.cantidad = cantidad;
        this.precioUnitario = zapato.getPrecio();
    }

    public BigDecimal getSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public DetallePedido() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public void setZapato(Zapato zapato) {
        this.zapato = zapato;
    }

    public TallaStock getTallaStock() {
        return tallaStock;
    }

    public void setTallaStock(TallaStock tallaStock) {
        this.tallaStock = tallaStock;
    }

    public String getNombreZapato() {
        return nombreZapato;
    }

    public void setNombreZapato(String nombreZapato) {
        this.nombreZapato = nombreZapato;
    }

    public void setTalla(int talla) {
        this.talla = talla;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public Zapato getZapato() {
        return zapato;
    }

    public int getTalla() {
        return talla;
    }
}
