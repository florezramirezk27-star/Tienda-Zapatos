package com.tiendazapatos.controller;

import com.tiendazapatos.model.Zapato;
import com.tiendazapatos.repository.ZapatoRepository;
import com.tiendazapatos.repository.ZapatoSpecs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

/** HU-05: catálogo público con filtros y paginación de 12 en 12. */
@Controller
public class CatalogoController {

    static final int TAMANO_PAGINA = 12;

    private final ZapatoRepository zapatos;

    public CatalogoController(ZapatoRepository zapatos) {
        this.zapatos = zapatos;
    }

    @GetMapping({"/", "/catalogo"})
    public String catalogo(@RequestParam(required = false) String marca,
                           @RequestParam(required = false) String categoria,
                           @RequestParam(required = false) Integer talla,
                           @RequestParam(required = false) BigDecimal precioMin,
                           @RequestParam(required = false) BigDecimal precioMax,
                           @RequestParam(defaultValue = "0") int pagina,
                           Model model) {
        String m = vacioANulo(marca);
        String c = vacioANulo(categoria);
        Page<Zapato> resultado = zapatos.findAll(
                ZapatoSpecs.catalogo(m, c, talla, precioMin, precioMax),
                PageRequest.of(Math.max(pagina, 0), TAMANO_PAGINA, Sort.by("nombre")));

        model.addAttribute("pagina", resultado);
        model.addAttribute("marcas", zapatos.marcasActivas());
        model.addAttribute("categorias", zapatos.categoriasActivas());
        model.addAttribute("marca", m);
        model.addAttribute("categoria", c);
        model.addAttribute("talla", talla);
        model.addAttribute("precioMin", precioMin);
        model.addAttribute("precioMax", precioMax);
        return "catalogo";
    }

    @GetMapping("/zapatos/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        Zapato zapato = zapatos.findById(id)
                .filter(Zapato::isActivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("zapato", zapato);
        return "zapato-detalle";
    }

    private static String vacioANulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
