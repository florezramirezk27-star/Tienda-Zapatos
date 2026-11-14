package com.tiendazapatos.controller;

import com.tiendazapatos.form.ZapatoForm;
import com.tiendazapatos.model.Zapato;
import com.tiendazapatos.repository.ZapatoRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** HU-04: el almacenista (o el administrador) registra, edita y desactiva zapatos. */
@Controller
@RequestMapping("/almacen/zapatos")
public class ZapatoController {

    private final ZapatoRepository zapatos;

    public ZapatoController(ZapatoRepository zapatos) {
        this.zapatos = zapatos;
    }

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("zapatos", zapatos.findAllByOrderByNombreAsc());
        return "almacen/zapatos";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("form", new ZapatoForm());
        model.addAttribute("zapatoId", null);
        return "almacen/zapato-form";
    }

    /** REQ-ZAP-011 y 012: referencia única y precio mayor que cero. */
    @PostMapping
    @Transactional
    public String crear(@Valid @ModelAttribute("form") ZapatoForm form, BindingResult resultado,
                        Model model, RedirectAttributes ra) {
        if (resultado.hasErrors()) {
            model.addAttribute("zapatoId", null);
            return "almacen/zapato-form";
        }
        Zapato z = new Zapato();
        form.copiarA(z);
        String referencia;
        do {
            referencia = Zapato.nuevaReferencia();
        } while (zapatos.existsByReferencia(referencia));
        z.setReferencia(referencia);
        z.crearTallasVacias();   // RN-02: tallas 34 a 45 con stock 0; el stock lo carga el inventario
        zapatos.save(z);
        ra.addFlashAttribute("mensaje", "Zapato creado con la referencia " + referencia
                + ". Registra su stock en Inventario.");
        return "redirect:/almacen/zapatos";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("form", ZapatoForm.desde(buscar(id)));
        model.addAttribute("zapatoId", id);
        return "almacen/zapato-form";
    }

    @PostMapping("/{id}")
    @Transactional
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("form") ZapatoForm form,
                             BindingResult resultado, Model model, RedirectAttributes ra) {
        Zapato z = buscar(id);
        if (resultado.hasErrors()) {
            model.addAttribute("zapatoId", id);
            return "almacen/zapato-form";
        }
        form.copiarA(z);
        ra.addFlashAttribute("mensaje", "Zapato " + z.getReferencia() + " actualizado");
        return "redirect:/almacen/zapatos";
    }

    /** REQ-ZAP-013: se oculta del catálogo público y las ventas anteriores no cambian. */
    @PostMapping("/{id}/desactivar")
    @Transactional
    public String desactivar(@PathVariable Long id, RedirectAttributes ra) {
        Zapato z = buscar(id);
        z.setActivo(false);
        ra.addFlashAttribute("mensaje", "Zapato " + z.getReferencia() + " desactivado");
        return "redirect:/almacen/zapatos";
    }

    @PostMapping("/{id}/activar")
    @Transactional
    public String activar(@PathVariable Long id, RedirectAttributes ra) {
        Zapato z = buscar(id);
        z.setActivo(true);
        ra.addFlashAttribute("mensaje", "Zapato " + z.getReferencia() + " activado");
        return "redirect:/almacen/zapatos";
    }

    private Zapato buscar(Long id) {
        return zapatos.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
