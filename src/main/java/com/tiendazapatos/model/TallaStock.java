package com.tiendazapatos.model;

import com.tiendazapatos.exception.ReglaNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Stock de un zapato en una talla. RN-01: nunca puede ser negativo. */
@Entity
@Table(name = "tallas_stock", uniqueConstraints = @UniqueConstraint(columnNames = {"zapato_id", "talla"}))
public class TallaStock {

    public static final int TALLA_MIN = 34;
    public static final int TALLA_MAX = 45;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zapato_id")
    private Zapato zapato;

    @Column(nullable = false)
    private int talla;

    @Column(nullable = false)
    private int stock;

    public TallaStock(Zapato zapato, int talla, int stock) {
        this.zapato = zapato;
        this.talla = talla;
        this.stock = stock;
    }

    /** REQ-ZAP-017: entrada de mercancía. */
    public void agregar(int cantidad) {
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor que cero");
        }
        stock += cantidad;
    }

    /** REQ-ZAP-023: descuenta por una venta. */
    public void descontar(int cantidad) {
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor que cero");
        }
        if (cantidad > stock) {
            throw new ReglaNegocioException("Stock insuficiente (talla " + talla + ", disponible: " + stock + ")");
        }
        stock -= cantidad;
    }

    /** REQ-ZAP-018: ajuste positivo o negativo que nunca deja el stock por debajo de 0. */
    public void ajustar(int delta) {
        if (delta == 0) {
            throw new ReglaNegocioException("El ajuste no puede ser cero");
        }
        if (stock + delta < 0) {
            throw new ReglaNegocioException("El ajuste dejaría el stock en negativo. Stock actual: " + stock);
        }
        stock += delta;
    }

    /** REQ-ZAP-019: con stock 0 la talla se muestra como «Agotada». */
    public boolean isAgotada() {
        return stock == 0;
    }

    public TallaStock() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Zapato getZapato() {
        return zapato;
    }

    public void setZapato(Zapato zapato) {
        this.zapato = zapato;
    }

    public int getTalla() {
        return talla;
    }

    public void setTalla(int talla) {
        this.talla = talla;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}
