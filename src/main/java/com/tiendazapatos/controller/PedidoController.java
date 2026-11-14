package com.tiendazapatos.controller;

import com.tiendazapatos.exception.ReglaNegocioException;
import com.tiendazapatos.model.Carrito;
import com.tiendazapatos.model.MetodoPago;
import com.tiendazapatos.model.MovimientoInventario;
import com.tiendazapatos.model.Pedido;
import com.tiendazapatos.model.TallaStock;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** HU-08 (realizar un pedido) y HU-11 (historial de pedidos del cliente). */
@Controller
public class PedidoController {

    private final Carrito carrito;
    private final PedidoRepository pedidos;
    private final TallaStockRepository tallas;
    private final MovimientoInventarioRepository movimientos;
    private final UsuarioRepository usuarios;

    public PedidoController(Carrito carrito, PedidoRepository pedidos, TallaStockRepository tallas,
                            MovimientoInventarioRepository movimientos, UsuarioRepository usuarios) {
        this.carrito = carrito;
        this.pedidos = pedidos;
        this.tallas = tallas;
        this.movimientos = movimientos;
        this.usuarios = usuarios;
    }

    @GetMapping("/pedidos/confirmar")
    public String resumen(Model model) {
        model.addAttribute("carrito", carrito);
        model.addAttribute("metodos", List.of(MetodoPago.CONTRA_ENTREGA, MetodoPago.TRANSFERENCIA));
        return "checkout";
    }

    /**
     * REQ-ZAP-023 y 024. Todo ocurre en una sola transacción: si una talla no tiene stock se revierte
     * el pedido completo. Las filas de stock se bloquean en orden de id para que dos clientes que
     * compran el último par no lo vendan dos veces (AC-024.1) ni se bloqueen entre sí.
     */
    @PostMapping("/pedidos/confirmar")
    @Transactional
    public String confirmar(@RequestParam(defaultValue = "") String direccion,
                            @RequestParam MetodoPago metodoPago,
                            Authentication auth, RedirectAttributes ra) {
        if (carrito.isVacio()) {
            throw new ReglaNegocioException("Tu carrito está vacío");
        }
        if (direccion.isBlank()) {
            throw new ReglaNegocioException("Escribe la dirección de entrega");
        }
        if (metodoPago != MetodoPago.CONTRA_ENTREGA && metodoPago != MetodoPago.TRANSFERENCIA) {
            throw new ReglaNegocioException("Método de pago no válido para compras web");
        }
        Usuario cliente = usuarios.findByCorreoIgnoreCase(auth.getName()).orElseThrow();

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setDireccion(direccion.trim());
        pedido.setMetodoPago(metodoPago);

        Map<Long, Integer> lineas = new TreeMap<>();
        for (Carrito.Item item : carrito.getItems()) {
            lineas.merge(item.getTallaId(), item.getCantidad(), Integer::sum);
        }
        for (Map.Entry<Long, Integer> linea : lineas.entrySet()) {
            TallaStock talla = tallas.bloquear(linea.getKey())
                    .orElseThrow(() -> new ReglaNegocioException("Un producto del carrito ya no existe"));
            if (!talla.getZapato().isActivo()) {
                throw new ReglaNegocioException("Un zapato del carrito ya no está disponible");
            }
            pedido.agregarDetalle(talla, linea.getValue());
            movimientos.save(new MovimientoInventario(talla, TipoMovimiento.VENTA, -linea.getValue(),
                    cliente, pedido.getNumero()));
        }
        pedidos.save(pedido);
        carrito.vaciar();
        ra.addFlashAttribute("mensaje", "Pedido " + pedido.getNumero() + " creado");
        return "redirect:/mis-pedidos/" + pedido.getId();
    }

    /** REQ-ZAP-031: solo los pedidos del cliente, del más reciente al más antiguo. */
    @GetMapping("/mis-pedidos")
    public String misPedidos(Authentication auth, Model model) {
        Usuario cliente = usuarios.findByCorreoIgnoreCase(auth.getName()).orElseThrow();
        model.addAttribute("pedidos", pedidos.findByClienteOrderByFechaDesc(cliente));
        return "mis-pedidos";
    }

    /** REQ-ZAP-032 y AC-031.2: el pedido de otro cliente responde 403. */
    @GetMapping("/mis-pedidos/{id}")
    public String detalle(@PathVariable Long id, Authentication auth, Model model) {
        Pedido pedido = pedidos.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (pedido.getCliente() == null || !pedido.getCliente().getCorreo().equalsIgnoreCase(auth.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        model.addAttribute("pedido", pedido);
        model.addAttribute("volver", "/mis-pedidos");
        return "pedido-detalle";
    }
}
