package com.tiendazapatos.controller;

import com.tiendazapatos.model.DetallePedido;
import com.tiendazapatos.model.EstadoPedido;
import com.tiendazapatos.model.MovimientoInventario;
import com.tiendazapatos.model.Pedido;
import com.tiendazapatos.model.TipoMovimiento;
import com.tiendazapatos.model.Usuario;
import com.tiendazapatos.repository.MovimientoInventarioRepository;
import com.tiendazapatos.repository.PedidoRepository;
import com.tiendazapatos.repository.TallaStockRepository;
import com.tiendazapatos.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * HU-10: panel de ventas y estados del pedido. Qué rol puede cada acción lo decide SecurityConfig:
 * el Vendedor confirma, entrega y cancela; el Almacenista envía. Las transiciones válidas
 * las controla el propio Pedido (RN-05).
 */
@Controller
@RequestMapping("/gestion/pedidos")
public class GestionPedidoController {

    private final PedidoRepository pedidos;
    private final TallaStockRepository tallas;
    private final MovimientoInventarioRepository movimientos;
    private final UsuarioRepository usuarios;

    public GestionPedidoController(PedidoRepository pedidos, TallaStockRepository tallas,
                                   MovimientoInventarioRepository movimientos, UsuarioRepository usuarios) {
        this.pedidos = pedidos;
        this.tallas = tallas;
        this.movimientos = movimientos;
        this.usuarios = usuarios;
    }

    @GetMapping
    public String lista(@RequestParam(required = false) EstadoPedido estado, Model model) {
        model.addAttribute("pedidos", estado == null
                ? pedidos.findAllByOrderByFechaDesc()
                : pedidos.findByEstadoOrderByFechaDesc(estado));
        model.addAttribute("estados", EstadoPedido.values());
        model.addAttribute("estadoFiltro", estado);
        return "gestion/pedidos";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("pedido", cargar(id));
        model.addAttribute("volver", "/gestion/pedidos");
        return "pedido-detalle";
    }

    @PostMapping("/{id}/confirmar")
    @Transactional
    public String confirmar(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        Pedido p = cargar(id);
        p.cambiarEstado(EstadoPedido.CONFIRMADO);
        p.setVendedor(actual(auth));
        ra.addFlashAttribute("mensaje", "Pedido " + p.getNumero() + " confirmado");
        return "redirect:/gestion/pedidos/" + id;
    }

    @PostMapping("/{id}/enviar")
    @Transactional
    public String enviar(@PathVariable Long id, RedirectAttributes ra) {
        Pedido p = cargar(id);
        p.cambiarEstado(EstadoPedido.ENVIADO);
        ra.addFlashAttribute("mensaje", "Pedido " + p.getNumero() + " enviado");
        return "redirect:/gestion/pedidos/" + id;
    }

    @PostMapping("/{id}/entregar")
    @Transactional
    public String entregar(@PathVariable Long id, RedirectAttributes ra) {
        Pedido p = cargar(id);
        p.cambiarEstado(EstadoPedido.ENTREGADO);
        ra.addFlashAttribute("mensaje", "Pedido " + p.getNumero() + " entregado");
        return "redirect:/gestion/pedidos/" + id;
    }

    /** REQ-ZAP-030: cancela y devuelve el stock de cada ítem. */
    @PostMapping("/{id}/cancelar")
    @Transactional
    public String cancelar(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        Pedido p = cargar(id);
        // Se bloquean las tallas antes de tocarlas, para no perder una venta simultánea.
        tallas.bloquearPorPedido(id);
        p.cancelar();
        Usuario usuario = actual(auth);
        for (DetallePedido d : p.getDetalles()) {
            movimientos.save(new MovimientoInventario(d.getTallaStock(), TipoMovimiento.DEVOLUCION,
                    d.getCantidad(), usuario, p.getNumero()));
        }
        ra.addFlashAttribute("mensaje", "Pedido " + p.getNumero() + " cancelado y stock devuelto");
        return "redirect:/gestion/pedidos/" + id;
    }

    private Pedido cargar(Long id) {
        return pedidos.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private Usuario actual(Authentication auth) {
        return usuarios.findByCorreoIgnoreCase(auth.getName()).orElseThrow();
    }
}
