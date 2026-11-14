package com.tiendazapatos;

import com.tiendazapatos.model.EstadoPedido;
import com.tiendazapatos.model.MetodoPago;
import com.tiendazapatos.model.Pedido;
import com.tiendazapatos.model.Rol;
import com.tiendazapatos.model.TallaStock;
import com.tiendazapatos.model.Usuario;
import com.tiendazapatos.model.Zapato;
import com.tiendazapatos.repository.PedidoRepository;
import com.tiendazapatos.repository.UsuarioRepository;
import com.tiendazapatos.repository.ZapatoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de los criterios Given/When/Then que pasan por los controladores, la seguridad por rol
 * y la base de datos (MySQL, base tienda_zapatos_test). Cada prueba se revierte al terminar.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TiendaIntegracionTest {

    private static final String CLAVE = "Clave1234";

    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired ZapatoRepository zapatos;
    @Autowired PedidoRepository pedidos;
    @Autowired PasswordEncoder encoder;

    private Usuario usuario(String correo, Rol rol) {
        return usuarios.save(new Usuario("Usuario " + rol, correo, encoder.encode(CLAVE), "3001234567", rol));
    }

    private Zapato zapatoConStock(String nombre, int talla, int stock) {
        Zapato z = TestDatos.zapato(nombre, "120000");
        TestDatos.talla(z, talla, stock);
        return zapatos.save(z);
    }

    private Pedido pedidoDe(Usuario cliente, Zapato z, int talla, int cantidad) {
        Pedido p = new Pedido();
        p.setCliente(cliente);
        p.setDireccion("Calle 1 # 2-3");
        p.setMetodoPago(MetodoPago.CONTRA_ENTREGA);
        p.agregarDetalle(TestDatos.talla(z, talla), cantidad);
        return pedidos.save(p);
    }

    // ---------- HU-01: inicio y cierre de sesión ----------

    @Test
    void ac001_1_inicioDeSesionExitosoLlevaAlPanelDelRol() throws Exception {
        usuario("vendedor@t.test", Rol.VENDEDOR);
        mvc.perform(formLogin("/login").userParameter("correo").passwordParam("password")
                        .user("vendedor@t.test").password(CLAVE))
                .andExpect(authenticated().withRoles("VENDEDOR"))
                .andExpect(redirectedUrl("/gestion/pedidos"));
    }

    @Test
    void ac002_1_contrasenaIncorrectaSeRechazaConMensajeGenerico() throws Exception {
        usuario("vendedor@t.test", Rol.VENDEDOR);
        mvc.perform(formLogin("/login").userParameter("correo").passwordParam("password")
                        .user("vendedor@t.test").password("incorrecta1"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void ac002_2_cuentaDesactivadaNoPuedeIniciarSesion() throws Exception {
        Usuario u = usuario("inactivo@t.test", Rol.CLIENTE);
        u.setActivo(false);
        usuarios.save(u);
        mvc.perform(formLogin("/login").userParameter("correo").passwordParam("password")
                        .user("inactivo@t.test").password(CLAVE))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void loginMuestraElMensajeGenericoEnLaPagina() throws Exception {
        mvc.perform(get("/login").param("error", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Correo o contraseña incorrectos")));
    }

    // ---------- HU-02: registro ----------

    @Test
    void ac004_1_registroCreaUnClienteConLaContrasenaCifrada() throws Exception {
        mvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Ana Pérez").param("correo", "Ana@Correo.test")
                        .param("telefono", "3001112233").param("password", "abcdefg1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        Usuario u = usuarios.findByCorreoIgnoreCase("ana@correo.test").orElseThrow();
        assertThat(u.getRol()).isEqualTo(Rol.CLIENTE);
        assertThat(u.isActivo()).isTrue();
        assertThat(u.getPassword()).startsWith("$2").isNotEqualTo("abcdefg1");   // BCrypt
    }

    @Test
    void ac005_1_correoDuplicadoSeRechaza() throws Exception {
        usuario("ana@correo.test", Rol.CLIENTE);
        mvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Otra Ana").param("correo", "ana@correo.test")
                        .param("telefono", "3001112233").param("password", "abcdefg1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "correo"));
        assertThat(usuarios.count()).isEqualTo(1);
    }

    @Test
    void ac006_1_contrasenaCortaSeRechaza() throws Exception {
        mvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Ana").param("correo", "ana@correo.test")
                        .param("telefono", "3001112233").param("password", "abc123"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "password"));
        assertThat(usuarios.count()).isZero();
    }

    // ---------- HU-03: usuarios y roles ----------

    @Test
    void ac007_1_elAdministradorCreaUnUsuarioConUnRol() throws Exception {
        usuario("admin@t.test", Rol.ADMINISTRADOR);
        mvc.perform(post("/admin/usuarios").with(csrf()).with(user("admin@t.test").roles("ADMINISTRADOR"))
                        .param("nombre", "Nuevo Vendedor").param("correo", "nv@t.test")
                        .param("telefono", "3001112233").param("password", "abcdefg1").param("rol", "VENDEDOR"))
                .andExpect(redirectedUrl("/admin/usuarios"));
        assertThat(usuarios.findByCorreoIgnoreCase("nv@t.test").orElseThrow().getRol()).isEqualTo(Rol.VENDEDOR);
    }

    @Test
    void ac008_1_desactivarUnaCuentaConservaElUsuario() throws Exception {
        usuario("admin@t.test", Rol.ADMINISTRADOR);
        Usuario alma = usuario("alma@t.test", Rol.ALMACENISTA);
        mvc.perform(post("/admin/usuarios/{id}/desactivar", alma.getId()).with(csrf())
                        .with(user("admin@t.test").roles("ADMINISTRADOR")))
                .andExpect(status().is3xxRedirection());
        assertThat(usuarios.findById(alma.getId()).orElseThrow().isActivo()).isFalse();
    }

    @Test
    void ac009_1_unRolNoAutorizadoRecibe403() throws Exception {
        mvc.perform(get("/admin/usuarios").with(user("alma@t.test").roles("ALMACENISTA")))
                .andExpect(status().isForbidden());
    }

    @Test
    void ac010_1_noSeDesactivaAlUnicoAdministrador() throws Exception {
        Usuario admin = usuario("admin@t.test", Rol.ADMINISTRADOR);
        mvc.perform(post("/admin/usuarios/{id}/desactivar", admin.getId()).with(csrf())
                        .with(user("admin@t.test").roles("ADMINISTRADOR")))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", "Debe existir al menos un administrador activo"));
        assertThat(usuarios.findById(admin.getId()).orElseThrow().isActivo()).isTrue();
    }

    @Test
    void sinSesionLasRutasProtegidasLlevanAlLogin() throws Exception {
        mvc.perform(get("/carrito"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    // ---------- HU-04: catálogo de zapatos (almacenista) ----------

    @Test
    void ac012_1_precioCeroSeRechaza() throws Exception {
        mvc.perform(post("/almacen/zapatos").with(csrf()).with(user("alma@t.test").roles("ALMACENISTA"))
                        .param("nombre", "Zapato").param("marca", "Nike").param("categoria", "Casual")
                        .param("color", "Negro").param("precio", "0"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "precio"));
        assertThat(zapatos.count()).isZero();
    }

    @Test
    void ac011_1_crearUnZapatoGeneraReferenciaUnicaYLasTallas() throws Exception {
        mvc.perform(post("/almacen/zapatos").with(csrf()).with(user("alma@t.test").roles("ALMACENISTA"))
                        .param("nombre", "Zapato").param("marca", "Nike").param("categoria", "Casual")
                        .param("color", "Negro").param("precio", "120000"))
                .andExpect(redirectedUrl("/almacen/zapatos"));
        Zapato z = zapatos.findAll().get(0);
        assertThat(z.getReferencia()).startsWith("ZAP-");
        assertThat(z.getTallas()).hasSize(12);
    }

    @Test
    void ac013_1_unZapatoDesactivadoSaleDelCatalogoPublico() throws Exception {
        Zapato z = zapatoConStock("Oculto", 40, 3);
        mvc.perform(post("/almacen/zapatos/{id}/desactivar", z.getId()).with(csrf())
                        .with(user("alma@t.test").roles("ALMACENISTA")))
                .andExpect(redirectedUrl("/almacen/zapatos"));
        mvc.perform(get("/catalogo"))
                .andExpect(model().attribute("pagina", hasProperty("totalElements", is(0L))));
        mvc.perform(get("/zapatos/{id}", z.getId())).andExpect(status().isNotFound());
    }

    // ---------- HU-05: catálogo con filtros ----------

    @Test
    void ac014_1_elCatalogoPaginaDe12En12() throws Exception {
        for (int i = 1; i <= 13; i++) {
            zapatoConStock("Zapato " + String.format("%02d", i), 40, 2);
        }
        mvc.perform(get("/catalogo"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("pagina", hasProperty("numberOfElements", is(12))))
                .andExpect(model().attribute("pagina", hasProperty("totalElements", is(13L))));
        mvc.perform(get("/catalogo").param("pagina", "1"))
                .andExpect(model().attribute("pagina", hasProperty("numberOfElements", is(1))));
    }

    @Test
    void ac015_1_losFiltrosSeCombinan() throws Exception {
        Zapato conTalla40 = TestDatos.zapato("Nike 40", "120000");
        conTalla40.setMarca("Nike");
        TestDatos.talla(conTalla40, 40, 3);
        zapatos.save(conTalla40);

        Zapato sinTalla40 = TestDatos.zapato("Nike 41", "120000");
        sinTalla40.setMarca("Nike");
        TestDatos.talla(sinTalla40, 41, 3);
        zapatos.save(sinTalla40);

        Zapato otraMarca = TestDatos.zapato("Adidas 40", "120000");
        otraMarca.setMarca("Adidas");
        TestDatos.talla(otraMarca, 40, 3);
        zapatos.save(otraMarca);

        mvc.perform(get("/catalogo").param("marca", "Nike").param("talla", "40"))
                .andExpect(model().attribute("pagina", hasProperty("totalElements", is(1L))));
    }

    @Test
    void ac016_1_sinResultadosMuestraElMensaje() throws Exception {
        zapatoConStock("Zapato", 40, 3);
        mvc.perform(get("/catalogo").param("marca", "Puma").param("precioMax", "50000"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No encontramos zapatos con esos filtros")));
    }

    // ---------- HU-06: inventario ----------

    @Test
    void ac017_1_laEntradaDeMercanciaSumaAlStockYGuardaElMovimiento() throws Exception {
        usuario("alma@t.test", Rol.ALMACENISTA);
        Zapato z = zapatoConStock("Zapato", 40, 4);
        TallaStock t = TestDatos.talla(z, 40);
        mvc.perform(post("/almacen/inventario/movimiento").with(csrf()).with(user("alma@t.test").roles("ALMACENISTA"))
                        .param("tallaId", String.valueOf(t.getId())).param("tipo", "ENTRADA").param("cantidad", "10"))
                .andExpect(redirectedUrl("/almacen/inventario"));
        assertThat(t.getStock()).isEqualTo(14);
    }

    @Test
    void ac018_1_unAjusteQueDejaStockNegativoSeRechaza() throws Exception {
        usuario("alma@t.test", Rol.ALMACENISTA);
        Zapato z = zapatoConStock("Zapato", 40, 4);
        TallaStock t = TestDatos.talla(z, 40);
        mvc.perform(post("/almacen/inventario/movimiento").with(csrf()).with(user("alma@t.test").roles("ALMACENISTA"))
                        .param("tallaId", String.valueOf(t.getId())).param("tipo", "AJUSTE").param("cantidad", "-10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));
        assertThat(t.getStock()).isEqualTo(4);
    }

    @Test
    void soloElAlmacenistaRegistraMovimientosDeInventario() throws Exception {
        Zapato z = zapatoConStock("Zapato", 40, 4);
        mvc.perform(post("/almacen/inventario/movimiento").with(csrf()).with(user("admin@t.test").roles("ADMINISTRADOR"))
                        .param("tallaId", String.valueOf(TestDatos.talla(z, 40).getId()))
                        .param("tipo", "ENTRADA").param("cantidad", "1"))
                .andExpect(status().isForbidden());
    }

    // ---------- HU-09: venta presencial ----------

    @Test
    void ac026_1_ventaPresencialDescuentaStockYQuedaEntregada() throws Exception {
        usuario("vendedor@t.test", Rol.VENDEDOR);
        Zapato z = zapatoConStock("Zapato", 41, 5);
        TallaStock t = TestDatos.talla(z, 41);
        mvc.perform(post("/ventas").with(csrf()).with(user("vendedor@t.test").roles("VENDEDOR"))
                        .param("tallaId", String.valueOf(t.getId())).param("cantidad", "1")
                        .param("metodoPago", "EFECTIVO"))
                .andExpect(status().is3xxRedirection());

        assertThat(t.getStock()).isEqualTo(4);
        Pedido p = pedidos.findAll().get(0);
        assertThat(p.getCanal().name()).isEqualTo("PRESENCIAL");
        assertThat(p.getEstado()).isEqualTo(EstadoPedido.ENTREGADO);
        assertThat(p.getNombreCliente()).isEqualTo("Cliente de mostrador");   // AC-027.1
    }

    @Test
    void ac026_2_tallaAgotadaRechazaLaVenta() throws Exception {
        usuario("vendedor@t.test", Rol.VENDEDOR);
        Zapato z = zapatoConStock("Zapato", 41, 0);
        mvc.perform(post("/ventas").with(csrf()).with(user("vendedor@t.test").roles("VENDEDOR"))
                        .param("tallaId", String.valueOf(TestDatos.talla(z, 41).getId())).param("cantidad", "1")
                        .param("metodoPago", "EFECTIVO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));
        assertThat(pedidos.count()).isZero();
    }

    // ---------- HU-10: estados del pedido ----------

    @Test
    void ac028_1_elVendedorConfirmaUnPedidoPendiente() throws Exception {
        usuario("vendedor@t.test", Rol.VENDEDOR);
        Usuario cliente = usuario("cliente@t.test", Rol.CLIENTE);
        Pedido p = pedidoDe(cliente, zapatoConStock("Zapato", 40, 5), 40, 1);
        mvc.perform(post("/gestion/pedidos/{id}/confirmar", p.getId()).with(csrf())
                        .with(user("vendedor@t.test").roles("VENDEDOR")))
                .andExpect(redirectedUrl("/gestion/pedidos/" + p.getId()));
        assertThat(p.getEstado()).isEqualTo(EstadoPedido.CONFIRMADO);
    }

    @Test
    void elAlmacenistaNoPuedeConfirmarPeroSiEnviar() throws Exception {
        usuario("alma@t.test", Rol.ALMACENISTA);
        Usuario cliente = usuario("cliente@t.test", Rol.CLIENTE);
        Pedido p = pedidoDe(cliente, zapatoConStock("Zapato", 40, 5), 40, 1);
        mvc.perform(post("/gestion/pedidos/{id}/confirmar", p.getId()).with(csrf())
                        .with(user("alma@t.test").roles("ALMACENISTA")))
                .andExpect(status().isForbidden());

        p.cambiarEstado(EstadoPedido.CONFIRMADO);
        mvc.perform(post("/gestion/pedidos/{id}/enviar", p.getId()).with(csrf())
                        .with(user("alma@t.test").roles("ALMACENISTA")))
                .andExpect(status().is3xxRedirection());
        assertThat(p.getEstado()).isEqualTo(EstadoPedido.ENVIADO);
    }

    @Test
    void ac030_1_cancelarUnPedidoConfirmadoDevuelveElStock() throws Exception {
        usuario("vendedor@t.test", Rol.VENDEDOR);
        Usuario cliente = usuario("cliente@t.test", Rol.CLIENTE);
        Zapato z = zapatoConStock("Zapato", 40, 5);
        Pedido p = pedidoDe(cliente, z, 40, 2);
        p.cambiarEstado(EstadoPedido.CONFIRMADO);
        assertThat(TestDatos.talla(z, 40).getStock()).isEqualTo(3);

        mvc.perform(post("/gestion/pedidos/{id}/cancelar", p.getId()).with(csrf())
                        .with(user("vendedor@t.test").roles("VENDEDOR")))
                .andExpect(status().is3xxRedirection());

        assertThat(p.getEstado()).isEqualTo(EstadoPedido.CANCELADO);
        assertThat(TestDatos.talla(z, 40).getStock()).isEqualTo(5);
    }

    // ---------- HU-11: historial del cliente ----------

    @Test
    void ac031_2_unClienteNoPuedeAbrirElPedidoDeOtro() throws Exception {
        Usuario dueno = usuario("dueno@t.test", Rol.CLIENTE);
        usuario("otro@t.test", Rol.CLIENTE);
        Pedido p = pedidoDe(dueno, zapatoConStock("Zapato", 40, 5), 40, 1);

        mvc.perform(get("/mis-pedidos/{id}", p.getId()).with(user("otro@t.test").roles("CLIENTE")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/mis-pedidos/{id}", p.getId()).with(user("dueno@t.test").roles("CLIENTE")))
                .andExpect(status().isOk());
    }

    // ---------- HU-12: factura en PDF ----------

    @Test
    void ac033_1_unPedidoConfirmadoDescargaUnPdf() throws Exception {
        Usuario cliente = usuario("cliente@t.test", Rol.CLIENTE);
        Pedido p = pedidoDe(cliente, zapatoConStock("Zapato", 40, 5), 40, 2);
        p.cambiarEstado(EstadoPedido.CONFIRMADO);

        MvcResult r = mvc.perform(get("/facturas/{id}", p.getId()).with(user("cliente@t.test").roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
                .andReturn();
        assertThat(new String(r.getResponse().getContentAsByteArray(), 0, 4)).isEqualTo("%PDF");
    }

    @Test
    void ac034_1_unPedidoPendienteNoTieneFactura() throws Exception {
        Usuario cliente = usuario("cliente@t.test", Rol.CLIENTE);
        Pedido p = pedidoDe(cliente, zapatoConStock("Zapato", 40, 5), 40, 1);
        mvc.perform(get("/facturas/{id}", p.getId()).with(user("cliente@t.test").roles("CLIENTE")))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", "La factura solo está disponible para pedidos confirmados"));
    }

    @Test
    void unClienteNoDescargaLaFacturaDeOtro() throws Exception {
        Usuario dueno = usuario("dueno@t.test", Rol.CLIENTE);
        usuario("otro@t.test", Rol.CLIENTE);
        Pedido p = pedidoDe(dueno, zapatoConStock("Zapato", 40, 5), 40, 1);
        p.cambiarEstado(EstadoPedido.CONFIRMADO);
        mvc.perform(get("/facturas/{id}", p.getId()).with(user("otro@t.test").roles("CLIENTE")))
                .andExpect(status().isForbidden());
    }

    // ---------- HU-13: reporte de ventas ----------

    @Test
    void ac037_1_fechasInvertidasSeRechazan() throws Exception {
        mvc.perform(get("/reportes/ventas/pdf").with(user("gerente@t.test").roles("GERENTE"))
                        .param("desde", "2026-12-10").param("hasta", "2026-12-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", "La fecha inicial no puede ser posterior a la final"));
    }

    @Test
    void ac038_1_unRangoSinVentasGeneraElPdf() throws Exception {
        MvcResult r = mvc.perform(get("/reportes/ventas/pdf").with(user("gerente@t.test").roles("GERENTE"))
                        .param("desde", "2000-01-01").param("hasta", "2000-01-31"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
                .andReturn();
        assertThat(new String(r.getResponse().getContentAsByteArray(), 0, 4)).isEqualTo("%PDF");
    }

    @Test
    void soloElGerenteVeElReporte() throws Exception {
        mvc.perform(get("/reportes/ventas").with(user("alma@t.test").roles("ALMACENISTA")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/reportes/ventas").with(user("gerente@t.test").roles("GERENTE")))
                .andExpect(status().isOk());
    }
}
