package com.tiendazapatos;

import com.tiendazapatos.exception.ReglaNegocioException;
import com.tiendazapatos.model.Carrito;
import com.tiendazapatos.model.TallaStock;
import com.tiendazapatos.model.Zapato;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** HU-07: carrito en la sesión del cliente. */
class CarritoTest {

    private final Carrito carrito = new Carrito();

    @Test
    void ac020_1_agregarAlCarritoMuestraCantidadYSubtotal() {
        Zapato z = TestDatos.zapatoConIds("Court", "120000");
        TallaStock t = TestDatos.talla(z, 39, 10);
        carrito.agregar(t, 2);

        assertThat(carrito.getItems()).hasSize(1);
        assertThat(carrito.getItems().get(0).getCantidad()).isEqualTo(2);
        assertThat(carrito.getSubtotal()).isEqualByComparingTo("240000");
    }

    @Test
    void ac021_1_cantidadMayorAlStockSeRechaza() {
        Zapato z = TestDatos.zapatoConIds("Court", "120000");
        TallaStock t = TestDatos.talla(z, 39, 3);

        assertThatThrownBy(() -> carrito.agregar(t, 4))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("3");
        assertThat(carrito.isVacio()).isTrue();
    }

    @Test
    void ac021_2_cantidadIgualAlStockSeAcepta() {   // borde
        Zapato z = TestDatos.zapatoConIds("Court", "120000");
        TallaStock t = TestDatos.talla(z, 39, 3);
        carrito.agregar(t, 3);
        assertThat(carrito.getUnidades()).isEqualTo(3);
    }

    @Test
    void agregarDosVecesLaMismaTallaSumaPeroNoSuperaElStock() {
        Zapato z = TestDatos.zapatoConIds("Court", "120000");
        TallaStock t = TestDatos.talla(z, 39, 3);
        carrito.agregar(t, 2);

        assertThatThrownBy(() -> carrito.agregar(t, 2)).isInstanceOf(ReglaNegocioException.class);
        assertThat(carrito.getUnidades()).isEqualTo(2);
    }

    @Test
    void ac022_1_eliminarUnItemRecalculaElTotal() {
        Zapato a = TestDatos.zapatoConIds("Air", "100000");
        Zapato b = TestDatos.zapatoConIds("Court", "50000");
        TallaStock ta = TestDatos.talla(a, 40, 5);
        TallaStock tb = TestDatos.talla(b, 40, 5);
        carrito.agregar(ta, 1);
        carrito.agregar(tb, 2);
        assertThat(carrito.getSubtotal()).isEqualByComparingTo("200000");

        carrito.quitar(tb.getId());

        assertThat(carrito.getItems()).hasSize(1);
        assertThat(carrito.getSubtotal()).isEqualByComparingTo("100000");
        assertThat(carrito.getIva()).isEqualByComparingTo("19000");
        assertThat(carrito.getTotal()).isEqualByComparingTo("119000");
    }

    @Test
    void cambiarLaCantidadRecalculaYCeroElimina() {
        Zapato z = TestDatos.zapatoConIds("Court", "100000");
        TallaStock t = TestDatos.talla(z, 39, 10);
        carrito.agregar(t, 1);

        carrito.actualizar(t, 4);
        assertThat(carrito.getSubtotal()).isEqualByComparingTo("400000");

        carrito.actualizar(t, 0);
        assertThat(carrito.isVacio()).isTrue();
    }
}
