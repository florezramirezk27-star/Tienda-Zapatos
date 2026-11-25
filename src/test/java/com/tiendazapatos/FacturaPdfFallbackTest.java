package com.tiendazapatos;

import com.tiendazapatos.model.EstadoPedido;
import com.tiendazapatos.model.MetodoPago;
import com.tiendazapatos.model.Pedido;
import com.tiendazapatos.model.Rol;
import com.tiendazapatos.model.TallaStock;
import com.tiendazapatos.model.Usuario;
import com.tiendazapatos.model.Zapato;
import com.tiendazapatos.pdf.GeneradorPdf;
import com.tiendazapatos.pdf.PdfGeneracionException;
import com.tiendazapatos.repository.PedidoRepository;
import com.tiendazapatos.repository.UsuarioRepository;
import com.tiendazapatos.repository.ZapatoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** AC-035.1: si la API de PDF falla, se informa al usuario y el pedido no cambia. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FacturaPdfFallbackTest {

    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired ZapatoRepository zapatos;
    @Autowired PedidoRepository pedidos;
    @Autowired PasswordEncoder encoder;
    @MockitoBean GeneradorPdf generador;

    @Test
    void ac035_1_siLaApiDePdfFallaSeMuestraElMensajeYElPedidoNoCambia() throws Exception {
        Usuario cliente = usuarios.save(new Usuario("Cliente PDF", "cliente@pdf.test",
                encoder.encode("Clave1234"), "3001234567", Rol.CLIENTE));
        Zapato z = TestDatos.zapato("Zapato PDF", "100000");
        TallaStock t = TestDatos.talla(z, 40, 5);
        zapatos.save(z);

        Pedido p = new Pedido();
        p.setCliente(cliente);
        p.setDireccion("Calle 1 # 2-3");
        p.setMetodoPago(MetodoPago.CONTRA_ENTREGA);
        p.agregarDetalle(t, 2);
        p.cambiarEstado(EstadoPedido.CONFIRMADO);
        pedidos.save(p);

        given(generador.factura(any(Pedido.class)))
                .willThrow(new PdfGeneracionException("API caída", new RuntimeException("sin respuesta")));

        mvc.perform(get("/facturas/{id}", p.getId())
                        .with(user("cliente@pdf.test").roles("CLIENTE")))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error",
                        "No fue posible generar la factura, intenta de nuevo"));

        Pedido despues = pedidos.findById(p.getId()).orElseThrow();
        assertThat(despues.getEstado()).isEqualTo(EstadoPedido.CONFIRMADO);
        assertThat(despues.getTotal()).isEqualByComparingTo("238000");
    }
}
