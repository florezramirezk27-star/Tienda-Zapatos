package com.tiendazapatos;

import com.tiendazapatos.exception.ReglaNegocioException;
import com.tiendazapatos.model.EstadoPedido;
import com.tiendazapatos.model.MetodoPago;
import com.tiendazapatos.model.Pedido;
import com.tiendazapatos.model.TallaStock;
import com.tiendazapatos.model.Zapato;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** HU-08 y HU-10: totales, IVA, descuento de stock, estados y cancelación. */
class PedidoTest {

    private Pedido pedidoNuevo() {
        Pedido p = new Pedido();
        p.setMetodoPago(MetodoPago.CONTRA_ENTREGA);
        return p;
    }

    @Test
    void ac025_1_ivaDel19PorCientoSobreElSubtotal() {
        Zapato z = TestDatos.zapato("Court", "100000");
        TallaStock t = TestDatos.talla(z, 39, 5);
        Pedido p = pedidoNuevo();
        p.agregarDetalle(t, 2);

        assertThat(p.getSubtotal()).isEqualByComparingTo("200000");
        assertThat(p.getIva()).isEqualByComparingTo("38000");
        assertThat(p.getTotal()).isEqualByComparingTo("238000");
    }

    @Test
    void ac023_1_crearPedidoDescuentaElStockYEmpiezaPendiente() {
        Zapato z = TestDatos.zapato("Court", "120000");
        TallaStock t = TestDatos.talla(z, 39, 5);
        Pedido p = pedidoNuevo();
        p.agregarDetalle(t, 2);

        assertThat(t.getStock()).isEqualTo(3);
        assertThat(p.getEstado()).isEqualTo(EstadoPedido.PENDIENTE);
        assertThat(p.getNumero()).startsWith("PED-");
        assertThat(p.getUnidades()).isEqualTo(2);
    }

    @Test
    void ac024_1_sinStockSuficienteElPedidoSeRechazaYElStockNoCambia() {
        Zapato z = TestDatos.zapato("Court", "120000");
        TallaStock t = TestDatos.talla(z, 39, 1);
        Pedido p = pedidoNuevo();
        p.agregarDetalle(t, 1);   // el primero se lleva el último par

        assertThatThrownBy(() -> p.agregarDetalle(t, 1)).isInstanceOf(ReglaNegocioException.class);
        assertThat(t.getStock()).isZero();
    }

    @Test
    void ac028_1_pendientePasaAConfirmado() {
        Pedido p = pedidoNuevo();
        p.cambiarEstado(EstadoPedido.CONFIRMADO);
        assertThat(p.getEstado()).isEqualTo(EstadoPedido.CONFIRMADO);
    }

    @Test
    void ac029_1_transicionInvalidaSeRechazaYConservaElEstado() {
        Pedido p = pedidoNuevo();
        p.setEstado(EstadoPedido.ENTREGADO);
        assertThatThrownBy(() -> p.cambiarEstado(EstadoPedido.PENDIENTE)).isInstanceOf(ReglaNegocioException.class);
        assertThat(p.getEstado()).isEqualTo(EstadoPedido.ENTREGADO);
    }

    @Test
    void noSePuedeSaltarDePendienteAEntregado() {
        Pedido p = pedidoNuevo();
        assertThatThrownBy(() -> p.cambiarEstado(EstadoPedido.ENTREGADO)).isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void ac030_1_cancelarDevuelveElStock() {
        Zapato z = TestDatos.zapato("Air", "120000");
        TallaStock t = TestDatos.talla(z, 40, 5);
        Pedido p = pedidoNuevo();
        p.agregarDetalle(t, 2);
        p.cambiarEstado(EstadoPedido.CONFIRMADO);
        assertThat(t.getStock()).isEqualTo(3);

        p.cancelar();

        assertThat(p.getEstado()).isEqualTo(EstadoPedido.CANCELADO);
        assertThat(t.getStock()).isEqualTo(5);
    }

    @Test
    void ac030_2_pedidoEnviadoNoSePuedeCancelar() {   // borde
        Zapato z = TestDatos.zapato("Air", "120000");
        TallaStock t = TestDatos.talla(z, 40, 5);
        Pedido p = pedidoNuevo();
        p.agregarDetalle(t, 2);
        p.setEstado(EstadoPedido.ENVIADO);

        assertThatThrownBy(p::cancelar).isInstanceOf(ReglaNegocioException.class);
        assertThat(p.getEstado()).isEqualTo(EstadoPedido.ENVIADO);
        assertThat(t.getStock()).isEqualTo(3);
    }

    @Test
    void soloLosPedidosConfirmadosEnviadosOEntregadosSonFacturables() {   // RN-07
        assertThat(EstadoPedido.PENDIENTE.isFacturable()).isFalse();
        assertThat(EstadoPedido.CANCELADO.isFacturable()).isFalse();
        assertThat(EstadoPedido.CONFIRMADO.isFacturable()).isTrue();
        assertThat(EstadoPedido.ENVIADO.isFacturable()).isTrue();
        assertThat(EstadoPedido.ENTREGADO.isFacturable()).isTrue();
    }

    @Test
    void ventaDeMostradorNoTieneClienteYSeLlamaClienteDeMostrador() {   // AC-027.1
        assertThat(pedidoNuevo().getNombreCliente()).isEqualTo("Cliente de mostrador");
    }
}
