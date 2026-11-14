package com.tiendazapatos.controller;

import com.tiendazapatos.exception.ReglaNegocioException;
import com.tiendazapatos.model.MovimientoInventario;
import com.tiendazapatos.model.TallaStock;
import com.tiendazapatos.model.TipoMovimiento;
import com.tiendazapatos.model.Usuario;
import com.tiendazapatos.repository.MovimientoInventarioRepository;
import com.tiendazapatos.repository.TallaStockRepository;
import com.tiendazapatos.repository.UsuarioRepository;
import com.tiendazapatos.repository.ZapatoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** HU-06: entradas y ajustes de mercancía por talla. */
@Controller
@RequestMapping("/almacen/inventario")
public class InventarioController {

    private final ZapatoRepository zapatos;
    private final TallaStockRepository tallas;
    private final MovimientoInventarioRepository movimientos;
    private final UsuarioRepository usuarios;

    public InventarioController(ZapatoRepository zapatos, TallaStockRepository tallas,
                                MovimientoInventarioRepository movimientos, UsuarioRepository usuarios) {
        this.zapatos = zapatos;
        this.tallas = tallas;
        this.movimientos = movimientos;
        this.usuarios = usuarios;
    }

    @GetMapping
    public String inventario(Model model) {
        model.addAttribute("zapatos", zapatos.findAllByOrderByNombreAsc());
        model.addAttribute("movimientos", movimientos.findTop20ByOrderByFechaDesc());
        return "almacen/inventario";
    }

    /**
     * REQ-ZAP-017 y 018. La fila se bloquea para que dos movimientos simultáneos no pisen el stock.
     * En un ajuste la cantidad puede ser negativa.
     */
    @PostMapping("/movimiento")
    @Transactional
    public String registrar(@RequestParam Long tallaId, @RequestParam TipoMovimiento tipo,
                            @RequestParam int cantidad, Authentication auth, RedirectAttributes ra) {
        if (tipo != TipoMovimiento.ENTRADA && tipo != TipoMovimiento.AJUSTE) {
            throw new ReglaNegocioException("Tipo de movimiento no válido");
        }
        TallaStock talla = tallas.bloquear(tallaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (tipo == TipoMovimiento.ENTRADA) {
            talla.agregar(cantidad);
        } else {
            talla.ajustar(cantidad);
        }
        Usuario usuario = usuarios.findByCorreoIgnoreCase(auth.getName()).orElseThrow();
        movimientos.save(new MovimientoInventario(talla, tipo, cantidad, usuario, null));
        ra.addFlashAttribute("mensaje", "Stock de la talla " + talla.getTalla() + " queda en " + talla.getStock());
        return "redirect:/almacen/inventario";
    }
}
