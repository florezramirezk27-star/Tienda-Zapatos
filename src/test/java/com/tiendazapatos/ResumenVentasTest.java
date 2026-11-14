package com.tiendazapatos;

import com.tiendazapatos.model.MetodoPago;
import com.tiendazapatos.model.Pedido;
import com.tiendazapatos.model.ResumenVentas;
import com.tiendazapatos.model.TallaStock;
import com.tiendazapatos.model.Zapato;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** HU-13: contenido del reporte de ventas. */
class ResumenVentasTest {

    private Pedido pedido(TallaStock t, int cantidad) {
        Pedido p = new Pedido();
        p.setMetodoPago(MetodoPago.EFECTIVO);
        p.agregarDetalle(t, cantidad);
        return p;
    }

    @Test
    void ac036_1_totalesYLosMasVendidos() {
        Zapato air = TestDatos.zapato("Air", "100000");
        Zapato court = TestDatos.zapato("Court", "50000");
        TallaStock a = TestDatos.talla(air, 40, 10);
        TallaStock c = TestDatos.talla(court, 40, 10);

        Pedido p1 = pedido(a, 2);
        Pedido p2 = pedido(a, 1);
        p2.agregarDetalle(c, 1);
        Pedido p3 = pedido(c, 3);

        ResumenVentas r = ResumenVentas.de(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), List.of(p1, p2, p3));

        assertThat(r.pedidos()).isEqualTo(3);
        assertThat(r.unidades()).isEqualTo(7);
        BigDecimal esperado = p1.getTotal().add(p2.getTotal()).add(p3.getTotal());
        assertThat(r.ingresos()).isEqualByComparingTo(esperado);
        assertThat(r.masVendidos()).extracting(ResumenVentas.ZapatoVendido::nombre).containsExactly("Court", "Air");
        assertThat(r.masVendidos()).extracting(ResumenVentas.ZapatoVendido::unidades).containsExactly(4L, 3L);
    }

    @Test
    void soloSeListanLosCincoMasVendidos() {
        Pedido p = new Pedido();
        p.setMetodoPago(MetodoPago.EFECTIVO);
        for (int i = 1; i <= 7; i++) {
            Zapato z = TestDatos.zapato("Modelo " + i, "10000");
            p.agregarDetalle(TestDatos.talla(z, 40, 20), i);
        }
        ResumenVentas r = ResumenVentas.de(LocalDate.now(), LocalDate.now(), List.of(p));
        assertThat(r.masVendidos()).hasSize(5);
        assertThat(r.masVendidos().get(0).nombre()).isEqualTo("Modelo 7");
    }

    @Test
    void ac038_1_sinVentasEnElRango() {
        ResumenVentas r = ResumenVentas.de(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), List.of());
        assertThat(r.sinVentas()).isTrue();
        assertThat(r.ingresos()).isEqualByComparingTo("0");
        assertThat(r.masVendidos()).isEmpty();
    }
}
