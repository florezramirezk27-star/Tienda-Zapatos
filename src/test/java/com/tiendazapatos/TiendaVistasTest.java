package com.tiendazapatos;

import com.tiendazapatos.model.Rol;
import com.tiendazapatos.model.TallaStock;
import com.tiendazapatos.model.Usuario;
import com.tiendazapatos.model.Zapato;
import com.tiendazapatos.repository.UsuarioRepository;
import com.tiendazapatos.repository.ZapatoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de presentación de la tienda rediseñada: búsqueda del catálogo, tallas reales en el
 * detalle del producto y renderizado del carrito/checkout con la talla elegida.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TiendaVistasTest {

    private static final String CLAVE = "Clave1234";

    @Autowired MockMvc mvc;
    @Autowired ZapatoRepository zapatos;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder encoder;

    private Usuario cliente() {
        return usuarios.save(new Usuario("Cliente Vistas", "cliente@vistas.test",
                encoder.encode(CLAVE), "3001234567", Rol.CLIENTE));
    }

    private Zapato zapatoConStock(String nombre, String marca, int talla, int stock) {
        Zapato z = TestDatos.zapato(nombre, "329000");
        z.setMarca(marca);
        TestDatos.talla(z, talla, stock);
        return zapatos.save(z);
    }

    private static int contar(String texto, String patron) {
        Matcher m = Pattern.compile(patron).matcher(texto);
        int total = 0;
        while (m.find()) {
            total++;
        }
        return total;
    }

    @Test
    void losRecursosEstaticosLocalesSonPublicos() throws Exception {
        String[] recursos = {
                "/vendor/bootstrap.min.css",
                "/vendor/bootstrap.bundle.min.js",
                "/fonts/fonts.css",
                "/fonts/fraunces-latin-500-italic.woff2",
                "/css/app.css",
                "/js/app.js",
                "/img/hero.jpg",
                "/img/categorias/casual.jpg",
                "/img/favicon.svg"
        };
        for (String recurso : recursos) {
            mvc.perform(get(recurso))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void elSpriteDeIconosSeIncluyeUnaVezEnLaPagina() throws Exception {
        MvcResult resultado = mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("class=\"icon-sprite\"")))
                .andExpect(content().string(containsString("<symbol id=\"house\"")))
                .andExpect(content().string(containsString("<symbol id=\"cart\"")))
                .andReturn();

        String html = resultado.getResponse().getContentAsString();
        assertThat(contar(html, "class=\"icon-sprite\"")).isEqualTo(1);
        assertThat(contar(html, "<symbol ")).isGreaterThan(25);
    }

    @Test
    void laBusquedaDelCatalogoFiltraPorNombreMarcaYReferencia() throws Exception {
        Zapato air = zapatoConStock("Air Runner", "Nike", 40, 3);
        zapatoConStock("Suede Urbano", "Puma", 41, 2);

        mvc.perform(get("/catalogo").param("q", "  runner  "))
                .andExpect(status().isOk())
                .andExpect(model().attribute("pagina", hasProperty("totalElements", is(1L))))
                .andExpect(content().string(containsString("Air Runner")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Suede Urbano"))));

        mvc.perform(get("/catalogo").param("q", "NIKE"))
                .andExpect(model().attribute("pagina", hasProperty("totalElements", is(1L))))
                .andExpect(content().string(containsString("Air Runner")));

        mvc.perform(get("/catalogo").param("q", air.getReferencia().toLowerCase()))
                .andExpect(model().attribute("pagina", hasProperty("totalElements", is(1L))))
                .andExpect(content().string(containsString("Air Runner")));

        mvc.perform(get("/catalogo").param("q", "sin-coincidencias"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("pagina", hasProperty("totalElements", is(0L))))
                .andExpect(content().string(containsString("No encontramos zapatos con esos filtros")));
    }

    @Test
    void ac019_1_laTallaAgotadaSeMuestraComoAgotadaYElBotonEstaDeshabilitado() throws Exception {
        Zapato z = TestDatos.zapato("Air Runner", "329000");
        z.setMarca("Nike");
        for (int talla = TallaStock.TALLA_MIN; talla <= TallaStock.TALLA_MAX; talla++) {
            TestDatos.talla(z, talla, talla == 41 ? 0 : 2);
        }
        zapatos.save(z);

        Long talla40Id = TestDatos.talla(z, 40).getId();

        MvcResult resultado = mvc.perform(get("/zapatos/{id}", z.getId())
                        .with(user("cliente@vistas.test").roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Air Runner")))
                .andExpect(content().string(containsString("action=\"/carrito/agregar\"")))
                .andExpect(content().string(containsString("name=\"tallaId\"")))
                .andExpect(content().string(containsString(
                        "data-talla-id=\"" + talla40Id + "\" data-talla=\"40\" data-stock=\"2\"")))
                .andExpect(content().string(containsString("aria-label=\"Talla 41, agotada\"")))
                .andExpect(content().string(containsString(">Agotada</span>")))
                .andExpect(content().string(containsString("Agregar al carrito")))
                .andReturn();

        String html = resultado.getResponse().getContentAsString();
        assertThat(contar(html, "class=\"talla-chip[ \"]")).isEqualTo(12);
        assertThat(contar(html, "class=\"talla-chip\\s+agotada\"")).isEqualTo(1);
        assertThat(html).contains("Selecciona una talla para ver la disponibilidad");
        assertThat(html).containsPattern("class=\"talla-chip agotada\"[^>]*disabled");
    }

    @Test
    void elVisitanteNoVeElCarritoEnLaNavegacionPeroElClienteSi() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        containsString("aria-label=\"Carrito de compras\""))));

        mvc.perform(get("/").with(user("cliente@vistas.test").roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("aria-label=\"Carrito de compras\"")));
    }

    @Test
    void elDetalleAvisaCuandoTodasLasTallasEstanAgotadas() throws Exception {
        Zapato z = TestDatos.zapato("Agotado Total", "329000");
        zapatos.save(z);

        mvc.perform(get("/zapatos/{id}", z.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Todas las tallas están agotadas por el momento.")))
                .andExpect(content().string(containsString(">Agotado</span>")));

        MvcResult resultado = mvc.perform(get("/zapatos/{id}", z.getId())).andReturn();
        String html = resultado.getResponse().getContentAsString();
        assertThat(contar(html, "class=\"talla-chip\\s+agotada\"")).isEqualTo(12);
    }

    @Test
    void elDetalleInvitaAIniciarSesionAlVisitante() throws Exception {
        Zapato z = zapatoConStock("Air Runner", "Nike", 40, 3);

        mvc.perform(get("/zapatos/{id}", z.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Inicia sesión con tu cuenta de cliente")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Agregar al carrito"))));
    }

    @Test
    void elCarritoYElCheckoutConservanLaTallaElegida() throws Exception {
        cliente();
        Zapato z = zapatoConStock("Air Runner", "Nike", 40, 5);
        TallaStock talla = TestDatos.talla(z, 40);
        MockHttpSession sesion = new MockHttpSession();

        mvc.perform(post("/carrito/agregar").session(sesion).with(csrf())
                        .with(user("cliente@vistas.test").roles("CLIENTE"))
                        .param("tallaId", String.valueOf(talla.getId()))
                        .param("cantidad", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/carrito"));

        mvc.perform(get("/carrito").session(sesion)
                        .with(user("cliente@vistas.test").roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Air Runner")))
                .andExpect(content().string(containsString("Talla <strong>40</strong>")))
                .andExpect(content().string(containsString("$783.020")))
                .andExpect(content().string(containsString("Continuar con el pedido")));

        mvc.perform(get("/pedidos/confirmar").session(sesion)
                        .with(user("cliente@vistas.test").roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Air Runner")))
                .andExpect(content().string(containsString("name=\"metodoPago\"")))
                .andExpect(content().string(containsString("value=\"CONTRA_ENTREGA\"")))
                .andExpect(content().string(containsString("value=\"TRANSFERENCIA\"")))
                .andExpect(content().string(containsString("$783.020")))
                .andExpect(content().string(containsString("Confirmar pedido")));
    }
}
