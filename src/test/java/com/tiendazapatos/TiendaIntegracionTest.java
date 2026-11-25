package com.tiendazapatos;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import com.tiendazapatos.model.EstadoPedido;
import com.tiendazapatos.model.MetodoPago;
import com.tiendazapatos.model.MovimientoInventario;
import com.tiendazapatos.model.Pedido;
import com.tiendazapatos.model.Rol;
import com.tiendazapatos.model.TallaStock;
import com.tiendazapatos.model.TipoMovimiento;
import com.tiendazapatos.model.Usuario;
import com.tiendazapatos.model.Zapato;
import com.tiendazapatos.repository.MovimientoInventarioRepository;
import com.tiendazapatos.repository.PedidoRepository;
import com.tiendazapatos.repository.UsuarioRepository;
import com.tiendazapatos.repository.ZapatoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
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
    @Autowired MovimientoInventarioRepository movimientos;
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

    /** Extrae el texto de un PDF para comprobar su contenido (facturas y reportes). */
    private static String textoPdf(byte[] pdf) throws Exception {
        try (PdfReader reader = new PdfReader(pdf)) {
            PdfTextExtractor extractor = new PdfTextExtractor(reader);
            StringBuilder texto = new StringBuilder();
            for (int pagina = 1; pagina <= reader.getNumberOfPages(); pagina++) {
                texto.append(extractor.getTextFromPage(pagina));
            }
            return texto.toString();
        }
    }

    private MockHttpSession sesionConCarrito(String correo, Long tallaId, int cantidad) throws Exception {
        MockHttpSession sesion = new MockHttpSession();
        mvc.perform(post("/carrito/agregar").session(sesion).with(csrf())
                        .with(user(correo).roles("CLIENTE"))
                        .param("tallaId", String.valueOf(tallaId)).param("cantidad", String.valueOf(cantidad)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/carrito"));
        return sesion;
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
    void ac002_1_elLoginMuestraElMensajeGenericoExacto() throws Exception {
        mvc.perform(get("/login").param("error", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Correo o contraseña incorrectos")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Inténtalo de nuevo"))));
    }

    @Test
    void ac003_1_cerrarSesionInvalidaLaSesionYVuelveAlCatalogo() throws Exception {
        usuario("cliente@t.test", Rol.CLIENTE);
        MvcResult login = mvc.perform(formLogin("/login").userParameter("correo").passwordParam("password")
                        .user("cliente@t.test").password(CLAVE))
                .andExpect(authenticated())
                .andReturn();
        MockHttpSession sesion = (MockHttpSession) login.getRequest().getSession(false);

        mvc.perform(post("/logout").session(sesion).with(csrf()))
                .andExpect(redirectedUrl("/"));
        assertThat(sesion.isInvalid()).isTrue();

        // Al volver con el botón «atrás» a una ruta protegida se exige iniciar sesión.
        mvc.perform(get("/carrito"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
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
                .andExpect(model().attributeHasFieldErrors("form", "correo"))
                .andExpect(content().string(containsString("El correo ya está registrado")));
        assertThat(usuarios.count()).isEqualTo(1);
    }

    @Test
    void ac006_1_contrasenaCortaSeRechazaConElMensajeExacto() throws Exception {
        mvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Ana").param("correo", "ana@correo.test")
                        .param("telefono", "3001112233").param("password", "abc123"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "password"))
                .andExpect(content().string(containsString(
                        "La contraseña debe tener mínimo 8 caracteres, con letras y números")));
        assertThat(usuarios.count()).isZero();
    }

    @Test
    void ac006_2_contrasenaEnElLimiteSeAceptaYCifra() throws Exception {
        mvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Ana").param("correo", "limite@correo.test")
                        .param("telefono", "3001112233").param("password", "abcdefg1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        Usuario u = usuarios.findByCorreoIgnoreCase("limite@correo.test").orElseThrow();
        assertThat(u.getPassword()).startsWith("$2").isNotEqualTo("abcdefg1");
        assertThat(encoder.matches("abcdefg1", u.getPassword())).isTrue();
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

        mvc.perform(formLogin("/login").userParameter("correo").passwordParam("password")
                        .user("nv@t.test").password("abcdefg1"))
                .andExpect(authenticated().withRoles("VENDEDOR"))
                .andExpect(redirectedUrl("/gestion/pedidos"));
        mvc.perform(get("/gestion/pedidos").with(user("nv@t.test").roles("VENDEDOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nueva venta")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Reporte de ventas"))));
    }

    @Test
    void ac008_1_desactivarUnaCuentaImpideElLoginYConservaSusMovimientos() throws Exception {
        usuario("admin@t.test", Rol.ADMINISTRADOR);
        Usuario alma = usuario("alma@t.test", Rol.ALMACENISTA);
        Zapato z = zapatoConStock("Zapato", 40, 4);
        TallaStock t = TestDatos.talla(z, 40);
        movimientos.save(new MovimientoInventario(t, TipoMovimiento.ENTRADA, 2, alma, null));

        mvc.perform(post("/admin/usuarios/{id}/desactivar", alma.getId()).with(csrf())
                        .with(user("admin@t.test").roles("ADMINISTRADOR")))
                .andExpect(status().is3xxRedirection());
        assertThat(usuarios.findById(alma.getId()).orElseThrow().isActivo()).isFalse();

        mvc.perform(formLogin("/login").userParameter("correo").passwordParam("password")
                        .user("alma@t.test").password(CLAVE))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));

        // El historial no se borra: el movimiento sigue visible para el administrador.
        mvc.perform(get("/almacen/inventario").with(user("admin@t.test").roles("ADMINISTRADOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Usuario ALMACENISTA")));
        assertThat(movimientos.count()).isEqualTo(1);
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
    void ac012_1_precioCeroSeRechazaConMensajeYPrecioUnoSeAcepta() throws Exception {
        MvcResult rechazo = mvc.perform(post("/almacen/zapatos").with(csrf())
                        .with(user("alma@t.test").roles("ALMACENISTA"))
                        .param("nombre", "Zapato").param("marca", "Nike").param("categoria", "Casual")
                        .param("color", "Negro").param("precio", "0"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "precio"))
                .andExpect(content().string(containsString("El precio debe ser mayor que cero")))
                .andReturn();
        BindingResult resultado = (BindingResult) rechazo.getModelAndView().getModel()
                .get(BindingResult.MODEL_KEY_PREFIX + "form");
        assertThat(resultado.getFieldError("precio").getDefaultMessage())
                .isEqualTo("El precio debe ser mayor que cero");
        assertThat(zapatos.count()).isZero();

        // Borde: con precio 1 el guardado se acepta.
        mvc.perform(post("/almacen/zapatos").with(csrf()).with(user("alma@t.test").roles("ALMACENISTA"))
                        .param("nombre", "Zapato").param("marca", "Nike").param("categoria", "Casual")
                        .param("color", "Negro").param("precio", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/almacen/zapatos"));
        assertThat(zapatos.count()).isEqualTo(1);
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
    void ac013_1_unZapatoDesactivadoSaleDelCatalogoPeroConservaSusVentas() throws Exception {
        Usuario cliente = usuario("cliente@t.test", Rol.CLIENTE);
        Zapato z = zapatoConStock("Oculto", 40, 5);
        Pedido venta = pedidoDe(cliente, z, 40, 2);
        String numero = venta.getNumero();
        String detalle = venta.getDetalles().get(0).getNombreZapato();
        java.math.BigDecimal total = venta.getTotal();

        mvc.perform(post("/almacen/zapatos/{id}/desactivar", z.getId()).with(csrf())
                        .with(user("alma@t.test").roles("ALMACENISTA")))
                .andExpect(redirectedUrl("/almacen/zapatos"));
        mvc.perform(get("/catalogo"))
                .andExpect(model().attribute("pagina", hasProperty("totalElements", is(0L))));
        mvc.perform(get("/zapatos/{id}", z.getId())).andExpect(status().isNotFound());

        Pedido historico = pedidos.findById(venta.getId()).orElseThrow();
        assertThat(historico.getNumero()).isEqualTo(numero);
        assertThat(historico.getDetalles()).hasSize(1);
        assertThat(historico.getDetalles().get(0).getNombreZapato()).isEqualTo(detalle);
        assertThat(historico.getTotal()).isEqualByComparingTo(total);
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

        assertThat(movimientos.count()).isEqualTo(1);
        MovimientoInventario movimiento = movimientos.findAll().get(0);
        assertThat(movimiento.getTipo()).isEqualTo(TipoMovimiento.ENTRADA);
        assertThat(movimiento.getCantidad()).isEqualTo(10);
        assertThat(movimiento.getUsuario().getCorreo()).isEqualTo("alma@t.test");
        assertThat(movimiento.getFecha()).isNotNull();
        assertThat(movimiento.getTallaStock().getId()).isEqualTo(t.getId());
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
        assertThat(movimientos.count()).isZero();
    }

    @Test
    void ac018_2_unAjusteHastaCeroSeAcepta() throws Exception {
        usuario("alma@t.test", Rol.ALMACENISTA);
        Zapato z = zapatoConStock("Zapato", 40, 4);
        TallaStock t = TestDatos.talla(z, 40);
        mvc.perform(post("/almacen/inventario/movimiento").with(csrf()).with(user("alma@t.test").roles("ALMACENISTA"))
                        .param("tallaId", String.valueOf(t.getId())).param("tipo", "AJUSTE").param("cantidad", "-4"))
                .andExpect(redirectedUrl("/almacen/inventario"));
        assertThat(t.getStock()).isZero();
        assertThat(t.isAgotada()).isTrue();
        assertThat(movimientos.count()).isEqualTo(1);
        assertThat(movimientos.findAll().get(0).getCantidad()).isEqualTo(-4);
    }

    @Test
    void soloElAlmacenistaRegistraMovimientosDeInventario() throws Exception {
        Zapato z = zapatoConStock("Zapato", 40, 4);
        mvc.perform(post("/almacen/inventario/movimiento").with(csrf()).with(user("admin@t.test").roles("ADMINISTRADOR"))
                        .param("tallaId", String.valueOf(TestDatos.talla(z, 40).getId()))
                        .param("tipo", "ENTRADA").param("cantidad", "1"))
                .andExpect(status().isForbidden());
    }

    // ---------- HU-08: realizar un pedido ----------

    @Test
    void ac023_1_pedidoExitosoDescuentaStockYVaciaElCarrito() throws Exception {
        usuario("cliente@t.test", Rol.CLIENTE);
        Zapato z = TestDatos.zapato("Court", "100000");
        TallaStock t = TestDatos.talla(z, 39, 5);
        zapatos.save(z);
        MockHttpSession sesion = sesionConCarrito("cliente@t.test", t.getId(), 2);

        mvc.perform(post("/pedidos/confirmar").session(sesion).with(csrf())
                        .with(user("cliente@t.test").roles("CLIENTE"))
                        .param("direccion", "Calle 1 # 2-3").param("metodoPago", "CONTRA_ENTREGA"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/mis-pedidos/*"));

        Pedido p = pedidos.findAll().get(0);
        assertThat(p.getEstado()).isEqualTo(EstadoPedido.PENDIENTE);
        assertThat(p.getNumero()).startsWith("PED-");
        assertThat(p.getUnidades()).isEqualTo(2);
        assertThat(t.getStock()).isEqualTo(3);
        assertThat(p.getTotal()).isEqualByComparingTo("238000");

        mvc.perform(get("/carrito").session(sesion).with(user("cliente@t.test").roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tu carrito está vacío")));
    }

    @Test
    void ac023_2_carritoVacioRechazaLaConfirmacion() throws Exception {
        usuario("cliente@t.test", Rol.CLIENTE);
        MockHttpSession sesion = new MockHttpSession();
        mvc.perform(post("/pedidos/confirmar").session(sesion).with(csrf())
                        .with(user("cliente@t.test").roles("CLIENTE"))
                        .param("direccion", "Calle 1 # 2-3").param("metodoPago", "CONTRA_ENTREGA"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", "Tu carrito está vacío"));
        assertThat(pedidos.count()).isZero();
    }

    @Test
    void ac025_1_elCheckoutMuestraIvaYTotalYElPedidoGuardaElMismoTotal() throws Exception {
        usuario("cliente@t.test", Rol.CLIENTE);
        Zapato z = TestDatos.zapato("Court", "100000");
        TallaStock t = TestDatos.talla(z, 39, 5);
        zapatos.save(z);
        MockHttpSession sesion = sesionConCarrito("cliente@t.test", t.getId(), 2);

        mvc.perform(get("/pedidos/confirmar").session(sesion)
                        .with(user("cliente@t.test").roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("IVA (19%)")))
                .andExpect(content().string(containsString("$38.000")))
                .andExpect(content().string(containsString("$238.000")));

        mvc.perform(post("/pedidos/confirmar").session(sesion).with(csrf())
                        .with(user("cliente@t.test").roles("CLIENTE"))
                        .param("direccion", "Calle 1 # 2-3").param("metodoPago", "CONTRA_ENTREGA"))
                .andExpect(status().is3xxRedirection());

        Pedido p = pedidos.findAll().get(0);
        assertThat(p.getSubtotal()).isEqualByComparingTo("200000");
        assertThat(p.getIva()).isEqualByComparingTo("38000");
        assertThat(p.getTotal()).isEqualByComparingTo("238000");
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
                .andExpect(flash().attribute("error",
                        org.hamcrest.Matchers.containsString("Stock insuficiente")));
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

    @Test
    void ac031_1_soloMisPedidosYDelMasRecienteAlMasAntiguo() throws Exception {
        Usuario uno = usuario("uno@t.test", Rol.CLIENTE);
        Usuario dos = usuario("dos@t.test", Rol.CLIENTE);
        Zapato z = zapatoConStock("Zapato", 40, 20);

        Pedido viejo = pedidoDe(uno, z, 40, 1);
        viejo.setFecha(LocalDateTime.now().minusDays(2));
        pedidos.save(viejo);
        Pedido nuevo = pedidoDe(uno, z, 40, 1);
        nuevo.setFecha(LocalDateTime.now());
        pedidos.save(nuevo);
        Pedido ajeno = pedidoDe(dos, z, 40, 1);

        MvcResult resultado = mvc.perform(get("/mis-pedidos")
                        .with(user("uno@t.test").roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(viejo.getNumero())))
                .andExpect(content().string(containsString(nuevo.getNumero())))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString(ajeno.getNumero()))))
                .andReturn();

        String html = resultado.getResponse().getContentAsString();
        assertThat(html.indexOf(nuevo.getNumero())).isLessThan(html.indexOf(viejo.getNumero()));
    }

    @Test
    void ac032_1_elDetalleMuestraItemsTallasCantidadesYtotales() throws Exception {
        Usuario cliente = usuario("cliente@t.test", Rol.CLIENTE);
        Zapato a = TestDatos.zapato("Zapato A", "100000");
        TallaStock ta = TestDatos.talla(a, 40, 5);
        zapatos.save(a);
        Zapato b = TestDatos.zapato("Zapato B", "50000");
        TallaStock tb = TestDatos.talla(b, 41, 5);
        zapatos.save(b);

        Pedido p = new Pedido();
        p.setCliente(cliente);
        p.setDireccion("Calle 1 # 2-3");
        p.setMetodoPago(MetodoPago.CONTRA_ENTREGA);
        p.agregarDetalle(ta, 1);
        p.agregarDetalle(tb, 2);
        pedidos.save(p);

        mvc.perform(get("/mis-pedidos/{id}", p.getId()).with(user("cliente@t.test").roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Zapato A")))
                .andExpect(content().string(containsString("Zapato B")))
                .andExpect(content().string(containsString("Talla <strong>40</strong>")))
                .andExpect(content().string(containsString("Talla <strong>41</strong>")))
                .andExpect(content().string(containsString("Cantidad: <span>1</span>")))
                .andExpect(content().string(containsString("Cantidad: <span>2</span>")))
                .andExpect(content().string(containsString("$200.000")))
                .andExpect(content().string(containsString("$38.000")))
                .andExpect(content().string(containsString("$238.000")))
                .andExpect(content().string(containsString("PENDIENTE")));
    }

    // ---------- HU-12: factura en PDF ----------

    @Test
    void ac033_1_unPedidoConfirmadoDescargaUnPdfConNumeroItemsIvaYTotal() throws Exception {
        Usuario cliente = usuario("cliente@t.test", Rol.CLIENTE);
        Zapato z = TestDatos.zapato("Zapato Factura", "100000");
        TestDatos.talla(z, 40, 5);
        zapatos.save(z);
        Pedido p = pedidoDe(cliente, z, 40, 2);
        p.cambiarEstado(EstadoPedido.CONFIRMADO);

        MvcResult r = mvc.perform(get("/facturas/{id}", p.getId()).with(user("cliente@t.test").roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
                .andReturn();
        byte[] pdf = r.getResponse().getContentAsByteArray();
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");

        String texto = textoPdf(pdf);
        assertThat(texto).contains(p.getNumero());
        assertThat(texto).contains("Zapato Factura");
        assertThat(texto).contains("Talla");
        assertThat(texto).contains("38.000");
        assertThat(texto).contains("238.000");
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
    void ac036_1_elReporteSumaLosPedidosValidosYExcluyeLosCancelados() throws Exception {
        Usuario cliente = usuario("cliente@t.test", Rol.CLIENTE);
        Zapato z = TestDatos.zapato("Zapato Reporte", "100000");
        TestDatos.talla(z, 40, 20);
        zapatos.save(z);

        pedidoDe(cliente, z, 40, 2).setEstado(EstadoPedido.CONFIRMADO);   // 238.000
        pedidoDe(cliente, z, 40, 1).setEstado(EstadoPedido.ENVIADO);       // 119.000
        pedidoDe(cliente, z, 40, 3).setEstado(EstadoPedido.ENTREGADO);     // 357.000

        Pedido cancelado = new Pedido();
        cancelado.setCliente(cliente);
        cancelado.setDireccion("Calle 1 # 2-3");
        cancelado.setMetodoPago(MetodoPago.CONTRA_ENTREGA);
        cancelado.setEstado(EstadoPedido.CANCELADO);
        cancelado.setSubtotal(new java.math.BigDecimal("420168.07"));
        cancelado.setIva(new java.math.BigDecimal("79831.93"));
        cancelado.setTotal(new java.math.BigDecimal("500000"));
        pedidos.save(cancelado);

        String hoy = LocalDate.now().toString();
        MvcResult r = mvc.perform(get("/reportes/ventas/pdf")
                        .with(user("gerente@t.test").roles("GERENTE"))
                        .param("desde", hoy).param("hasta", hoy))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
                .andReturn();

        String texto = textoPdf(r.getResponse().getContentAsByteArray());
        assertThat(texto).contains("Total de pedidos");
        assertThat(texto).contains("714.000");
        assertThat(texto).contains("Los 5 zapatos más vendidos");
        assertThat(texto).doesNotContain("500.000");
    }

    @Test
    void ac037_1_fechasInvertidasSeRechazan() throws Exception {
        mvc.perform(get("/reportes/ventas/pdf").with(user("gerente@t.test").roles("GERENTE"))
                        .param("desde", "2026-12-10").param("hasta", "2026-12-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", "La fecha inicial no puede ser posterior a la final"));
    }

    @Test
    void ac038_1_unRangoSinVentasGeneraElPdfConElMensaje() throws Exception {
        MvcResult r = mvc.perform(get("/reportes/ventas/pdf").with(user("gerente@t.test").roles("GERENTE"))
                        .param("desde", "2000-01-01").param("hasta", "2000-01-31"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
                .andReturn();
        byte[] pdf = r.getResponse().getContentAsByteArray();
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
        assertThat(textoPdf(pdf)).contains("Sin ventas en el rango seleccionado");
    }

    @Test
    void soloElGerenteVeElReporte() throws Exception {
        mvc.perform(get("/reportes/ventas").with(user("alma@t.test").roles("ALMACENISTA")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/reportes/ventas").with(user("gerente@t.test").roles("GERENTE")))
                .andExpect(status().isOk());
    }
}
