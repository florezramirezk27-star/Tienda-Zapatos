package com.tiendazapatos;

import com.tiendazapatos.exception.ReglaNegocioException;
import com.tiendazapatos.model.TallaStock;
import com.tiendazapatos.model.Zapato;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** HU-06: inventario por talla (RN-01: el stock nunca es negativo). */
class TallaStockTest {

    private final Zapato zapato = TestDatos.zapato("Air Runner", "120000");

    @Test
    void zapatoNuevoTieneLasTallas34a45ConStockCero() {   // RN-02
        assertThat(zapato.getTallas()).hasSize(12);
        assertThat(zapato.getTallas().get(0).getTalla()).isEqualTo(34);
        assertThat(zapato.getTallas().get(11).getTalla()).isEqualTo(45);
        assertThat(zapato.getStockTotal()).isZero();
    }

    @Test
    void ac017_1_entradaDeMercanciaSumaAlStock() {
        TallaStock t = TestDatos.talla(zapato, 40, 4);
        t.agregar(10);
        assertThat(t.getStock()).isEqualTo(14);
    }

    @Test
    void entradaConCantidadCeroSeRechaza() {
        TallaStock t = TestDatos.talla(zapato, 40, 4);
        assertThatThrownBy(() -> t.agregar(0)).isInstanceOf(ReglaNegocioException.class);
        assertThat(t.getStock()).isEqualTo(4);
    }

    @Test
    void ac018_1_ajusteQueDejaStockNegativoSeRechaza() {
        TallaStock t = TestDatos.talla(zapato, 40, 4);
        assertThatThrownBy(() -> t.ajustar(-10)).isInstanceOf(ReglaNegocioException.class);
        assertThat(t.getStock()).isEqualTo(4);
    }

    @Test
    void ac018_2_ajusteHastaCeroEsValido() {   // borde
        TallaStock t = TestDatos.talla(zapato, 40, 4);
        t.ajustar(-4);
        assertThat(t.getStock()).isZero();
        assertThat(t.isAgotada()).isTrue();
    }

    @Test
    void ac019_1_tallaConStockCeroEstaAgotada() {
        assertThat(TestDatos.talla(zapato, 41, 0).isAgotada()).isTrue();
        assertThat(TestDatos.talla(zapato, 41, 1).isAgotada()).isFalse();
    }

    @Test
    void descontarMasDeLoDisponibleSeRechaza() {
        TallaStock t = TestDatos.talla(zapato, 40, 1);
        assertThatThrownBy(() -> t.descontar(2)).isInstanceOf(ReglaNegocioException.class);
        assertThat(t.getStock()).isEqualTo(1);
    }
}
