package com.tiendazapatos.repository;

import com.tiendazapatos.model.TallaStock;
import com.tiendazapatos.model.Zapato;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Filtros del catálogo público (REQ-ZAP-014 y 015). */
public final class ZapatoSpecs {

    private ZapatoSpecs() {
    }

    /** Zapatos activos que cumplen todos los filtros que no sean nulos. */
    public static Specification<Zapato> catalogo(String q, String marca, String categoria, Integer talla,
                                                 BigDecimal precioMin, BigDecimal precioMax) {
        return (root, query, cb) -> {
            List<Predicate> filtros = new ArrayList<>();
            filtros.add(cb.isTrue(root.get("activo")));
            if (q != null) {
                // Búsqueda libre por nombre, marca, categoría o referencia.
                String patron = "%" + q.toLowerCase() + "%";
                filtros.add(cb.or(
                        cb.like(cb.lower(root.get("nombre")), patron),
                        cb.like(cb.lower(root.get("marca")), patron),
                        cb.like(cb.lower(root.get("categoria")), patron),
                        cb.like(cb.lower(root.get("referencia")), patron)));
            }
            if (marca != null) {
                filtros.add(cb.equal(cb.lower(root.get("marca")), marca.toLowerCase()));
            }
            if (categoria != null) {
                filtros.add(cb.equal(cb.lower(root.get("categoria")), categoria.toLowerCase()));
            }
            if (talla != null) {
                // Solo tallas con stock: la restricción única (zapato, talla) evita filas repetidas.
                Join<Zapato, TallaStock> t = root.join("tallas");
                filtros.add(cb.equal(t.get("talla"), talla));
                filtros.add(cb.greaterThan(t.get("stock"), 0));
            }
            if (precioMin != null) {
                filtros.add(cb.greaterThanOrEqualTo(root.get("precio"), precioMin));
            }
            if (precioMax != null) {
                filtros.add(cb.lessThanOrEqualTo(root.get("precio"), precioMax));
            }
            return cb.and(filtros.toArray(new Predicate[0]));
        };
    }
}
