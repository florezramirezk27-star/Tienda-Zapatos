package com.tiendazapatos;

import com.tiendazapatos.model.Rol;
import com.tiendazapatos.model.TallaStock;
import com.tiendazapatos.model.Usuario;
import com.tiendazapatos.model.Zapato;
import com.tiendazapatos.repository.MovimientoInventarioRepository;
import com.tiendazapatos.repository.PedidoRepository;
import com.tiendazapatos.repository.TallaStockRepository;
import com.tiendazapatos.repository.UsuarioRepository;
import com.tiendazapatos.repository.ZapatoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AC-024.1 con dos peticiones HTTP reales en paralelo: el bloqueo pesimista de la talla debe
 * impedir que los dos clientes compren el mismo último par.
 *
 * <p>Esta clase no es @Transactional porque el escenario necesita transacciones independientes
 * que compitan por la misma fila.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class PedidoConcurrenciaTest {

    private static final String CLAVE = "Clave1234";

    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired ZapatoRepository zapatos;
    @Autowired TallaStockRepository tallas;
    @Autowired PedidoRepository pedidos;
    @Autowired MovimientoInventarioRepository movimientos;
    @Autowired PasswordEncoder encoder;

    @BeforeEach
    void limpiarBase() {
        movimientos.deleteAll();
        pedidos.deleteAll();
        zapatos.deleteAll();
        usuarios.deleteAll();
    }

    @AfterEach
    void limpiarDespues() {
        limpiarBase();
    }

    private Usuario cliente(String correo, String nombre) {
        return usuarios.save(new Usuario(nombre, correo, encoder.encode(CLAVE), "3001234567", Rol.CLIENTE));
    }

    private MockHttpSession agregarAlCarrito(String correo, Long tallaId) throws Exception {
        MockHttpSession sesion = new MockHttpSession();
        mvc.perform(post("/carrito/agregar").session(sesion).with(csrf())
                        .with(user(correo).roles("CLIENTE"))
                        .param("tallaId", String.valueOf(tallaId)).param("cantidad", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/carrito"));
        return sesion;
    }

    private MvcResult confirmar(MockHttpSession sesion, String correo) throws Exception {
        return mvc.perform(post("/pedidos/confirmar").session(sesion).with(csrf())
                        .with(user(correo).roles("CLIENTE"))
                        .param("direccion", "Calle 1 # 2-3")
                        .param("metodoPago", "CONTRA_ENTREGA"))
                .andReturn();
    }

    @Test
    void ac024_1_dosClientesPorElUltimoParSoloCreanUnPedido() throws Exception {
        Usuario uno = cliente("uno@concurrencia.test", "Cliente Uno");
        Usuario dos = cliente("dos@concurrencia.test", "Cliente Dos");
        Zapato z = TestDatos.zapato("Último Par", "100000");
        TallaStock talla = TestDatos.talla(z, 40, 1);
        zapatos.save(z);

        MockHttpSession sesionUno = agregarAlCarrito(uno.getCorreo(), talla.getId());
        MockHttpSession sesionDos = agregarAlCarrito(dos.getCorreo(), talla.getId());

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch listos = new CountDownLatch(2);
        CountDownLatch disparo = new CountDownLatch(1);
        try {
            Callable<MvcResult> peticionUno = () -> {
                listos.countDown();
                listos.await(5, TimeUnit.SECONDS);
                disparo.await(5, TimeUnit.SECONDS);
                return confirmar(sesionUno, uno.getCorreo());
            };
            Callable<MvcResult> peticionDos = () -> {
                listos.countDown();
                listos.await(5, TimeUnit.SECONDS);
                disparo.await(5, TimeUnit.SECONDS);
                return confirmar(sesionDos, dos.getCorreo());
            };

            Future<MvcResult> futuroUno = pool.submit(peticionUno);
            Future<MvcResult> futuroDos = pool.submit(peticionDos);
            assertThat(listos.await(5, TimeUnit.SECONDS)).isTrue();
            disparo.countDown();

            List<MvcResult> resultados = List.of(
                    futuroUno.get(20, TimeUnit.SECONDS),
                    futuroDos.get(20, TimeUnit.SECONDS));

            long creados = resultados.stream()
                    .filter(r -> r.getResponse().getRedirectedUrl() != null
                            && r.getResponse().getRedirectedUrl().startsWith("/mis-pedidos/"))
                    .count();
            assertThat(creados).isEqualTo(1);

            MvcResult rechazado = resultados.stream()
                    .filter(r -> r.getResponse().getRedirectedUrl() == null
                            || !r.getResponse().getRedirectedUrl().startsWith("/mis-pedidos/"))
                    .findFirst().orElseThrow();
            assertThat(String.valueOf(rechazado.getFlashMap().get("error")))
                    .contains("Stock insuficiente");

            assertThat(pedidos.count()).isEqualTo(1);
            assertThat(tallas.findById(talla.getId()).orElseThrow().getStock()).isZero();
        } finally {
            pool.shutdownNow();
        }
    }
}
