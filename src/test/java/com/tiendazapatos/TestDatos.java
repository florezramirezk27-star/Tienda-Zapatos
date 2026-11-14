package com.tiendazapatos;

import com.tiendazapatos.model.TallaStock;
import com.tiendazapatos.model.Zapato;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

/** Fábrica de datos para las pruebas. */
public final class TestDatos {

    private static final AtomicLong SECUENCIA = new AtomicLong(1000);

    private TestDatos() {
    }

    /** Zapato con las 12 tallas (34 a 45) en stock 0 y sin ids: sirve para guardarlo con JPA. */
    public static Zapato zapato(String nombre, String precio) {
        Zapato z = new Zapato();
        z.setReferencia(Zapato.nuevaReferencia());
        z.setNombre(nombre);
        z.setMarca("Marca");
        z.setCategoria("Casual");
        z.setColor("Negro");
        z.setPrecio(new BigDecimal(precio));
        z.crearTallasVacias();
        return z;
    }

    /** Igual que zapato(), pero con ids asignados a mano para pruebas que no usan la base de datos. */
    public static Zapato zapatoConIds(String nombre, String precio) {
        Zapato z = zapato(nombre, precio);
        z.setId(SECUENCIA.incrementAndGet());
        z.getTallas().forEach(t -> t.setId(SECUENCIA.incrementAndGet()));
        return z;
    }

    public static TallaStock talla(Zapato z, int talla) {
        return z.getTallas().get(talla - TallaStock.TALLA_MIN);
    }

    public static TallaStock talla(Zapato z, int talla, int stock) {
        TallaStock t = talla(z, talla);
        t.setStock(stock);
        return t;
    }
}
