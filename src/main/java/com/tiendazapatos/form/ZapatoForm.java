package com.tiendazapatos.form;

import com.tiendazapatos.model.Zapato;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** HU-04: datos de un zapato. */
public class ZapatoForm {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120, message = "El nombre admite máximo 120 caracteres")
    private String nombre;

    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 60, message = "La marca admite máximo 60 caracteres")
    private String marca;

    @NotBlank(message = "La categoría es obligatoria")
    @Size(max = 60, message = "La categoría admite máximo 60 caracteres")
    private String categoria;

    @NotBlank(message = "El color es obligatorio")
    @Size(max = 40, message = "El color admite máximo 40 caracteres")
    private String color;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0", inclusive = false, message = "El precio debe ser mayor que cero")
    private BigDecimal precio;

    @Size(max = 500, message = "La descripción admite máximo 500 caracteres")
    private String descripcion;

    @Size(max = 300, message = "La URL de la imagen admite máximo 300 caracteres")
    private String imagen;

    public static ZapatoForm desde(Zapato z) {
        ZapatoForm f = new ZapatoForm();
        f.nombre = z.getNombre();
        f.marca = z.getMarca();
        f.categoria = z.getCategoria();
        f.color = z.getColor();
        f.precio = z.getPrecio();
        f.descripcion = z.getDescripcion();
        f.imagen = z.getImagen();
        return f;
    }

    public void copiarA(Zapato z) {
        z.setNombre(nombre.trim());
        z.setMarca(marca.trim());
        z.setCategoria(categoria.trim());
        z.setColor(color.trim());
        z.setPrecio(precio);
        z.setDescripcion(descripcion == null || descripcion.isBlank() ? null : descripcion.trim());
        z.setImagen(imagen == null || imagen.isBlank() ? null : imagen.trim());
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
}
