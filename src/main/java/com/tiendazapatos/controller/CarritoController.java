package com.tiendazapatos.controller;

import com.tiendazapatos.exception.ReglaNegocioException;
import com.tiendazapatos.model.Carrito;
import com.tiendazapatos.model.TallaStock;
import com.tiendazapatos.repository.TallaStockRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** HU-07: el carrito del cliente, guardado en su sesión. */
@Controller
@RequestMapping("/carrito")
public class CarritoController {

    private final Carrito carrito;
    private final TallaStockRepository tallas;

    public CarritoController(Carrito carrito, TallaStockRepository tallas) {
        this.carrito = carrito;
        this.tallas = tallas;
    }

    @GetMapping
    public String ver(Model model) {
        model.addAttribute("carrito", carrito);
        return "carrito";
    }

    /** REQ-ZAP-020 y 021. */
    @PostMapping("/agregar")
    public String agregar(@RequestParam Long tallaId, @RequestParam(defaultValue = "1") int cantidad,
                          RedirectAttributes ra) {
        if (cantidad < 1) {
            throw new ReglaNegocioException("La cantidad debe ser mayor que cero");
        }
        TallaStock talla = buscar(tallaId);
        if (!talla.getZapato().isActivo()) {
            throw new ReglaNegocioException("Este zapato ya no está disponible");
        }
        carrito.agregar(talla, cantidad);
        ra.addFlashAttribute("mensaje", "Agregado al carrito");
        return "redirect:/carrito";
    }

    /** REQ-ZAP-022: cambiar la cantidad recalcula el total. */
    @PostMapping("/actualizar")
    public String actualizar(@RequestParam Long tallaId, @RequestParam int cantidad) {
        carrito.actualizar(buscar(tallaId), cantidad);
        return "redirect:/carrito";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Long tallaId) {
        carrito.quitar(tallaId);
        return "redirect:/carrito";
    }

    private TallaStock buscar(Long id) {
        return tallas.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
