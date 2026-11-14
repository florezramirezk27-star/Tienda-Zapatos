package com.tiendazapatos.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Entity
@Table(name = "zapatos")
public class Zapato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String referencia;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 60)
    private String marca;

    @Column(nullable = false, length = 60)
    private String categoria;

    @Column(nullable = false, length = 40)
    private String color;

    /** RN-03: en pesos colombianos y sin IVA. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(length = 500)
    private String descripcion;

    /** URL de la imagen; si está vacía se usa una imagen por defecto. */
    @Column(length = 300)
    private String imagen;

    /** RN-08: un zapato desactivado no aparece en el catálogo pero conserva su historial. */
    @Column(nullable = false)
    private boolean activo = true;

    @OneToMany(mappedBy = "zapato", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("talla ASC")
    private List<TallaStock> tallas = new ArrayList<>();

    /** RN-02: tallas de la 34 a la 45, todas con stock 0 al crear el zapato. */
    public void crearTallasVacias() {
        for (int t = TallaStock.TALLA_MIN; t <= TallaStock.TALLA_MAX; t++) {
            tallas.add(new TallaStock(this, t, 0));
        }
    }

    public int getStockTotal() {
        return tallas.stream().mapToInt(TallaStock::getStock).sum();
    }

    public static String nuevaReferencia() {
        String alfabeto = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        ThreadLocalRandom azar = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder("ZAP-");
        for (int i = 0; i < 6; i++) {
            sb.append(alfabeto.charAt(azar.nextInt(alfabeto.length())));
        }
        return sb.toString();
    }

    public Zapato() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List<TallaStock> getTallas() {
        return tallas;
    }

    public void setTallas(List<TallaStock> tallas) {
        this.tallas = tallas;
    }
}
