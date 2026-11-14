package com.tiendazapatos.controller;

import com.tiendazapatos.exception.ReglaNegocioException;
import com.tiendazapatos.model.Canal;
import com.tiendazapatos.model.EstadoPedido;
import com.tiendazapatos.model.MetodoPago;
import com.tiendazapatos.model.MovimientoInventario;
import com.tiendazapatos.model.Pedido;
import com.tiendazapatos.model.Rol;
import com.tiendazapatos.model.TallaStock;
import com.tiendazapatos.model.TipoMovimiento;
import com.tiendazapatos.model.Usuario;
import com.tiendazapatos.repository.MovimientoInventarioRepository;
import com.tiendazapatos.repository.PedidoRepository;
import com.tiendazapatos.repository.TallaStockRepository;
import com.tiendazapatos.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** HU-09: el vendedor registra las ventas de la tienda física. */
@Controller
@RequestMapping("/ventas")
public class VentaController {

    private final PedidoRepository pedidos;
    private final TallaStockRepository tallas;
    private final MovimientoInventarioRepository movimientos;
    private final UsuarioRepository usuarios;

    public VentaController(PedidoRepository pedidos, TallaStockRepository tallas,
                           MovimientoInventarioRepository movimientos, UsuarioRepository usuarios) {
        this.pedidos = pedidos;
        this.tallas = tallas;
        this.movimientos = movimientos;
        this.usuarios = usuarios;
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("tallas", tallas.disponiblesParaVenta());
        model.addAttribute("metodos", List.of(MetodoPago.EFECTIVO, MetodoPago.TARJETA, MetodoPago.TRANSFERENCIA));
        return "ventas/nueva";
    }

    /**
     * REQ-ZAP-026 y 027: crea un pedido PRESENCIAL ya ENTREGADO y descuenta el stock. Sin correo de cliente
     * la venta queda como «Cliente de mostrador». Una talla agotada rechaza toda la venta (AC-026.2).
     */
    @PostMapping
    @Transactional
    public String registrar(@RequestParam(required = false) List<Long> tallaId,
                            @RequestParam(required = false) List<Integer> cantidad,
                            @RequestParam(defaultValue = "") String clienteCorreo,
                            @RequestParam MetodoPago metodoPago,
                            Authentication auth, RedirectAttributes ra) {
        Map<Long, Integer> lineas = new TreeMap<>();
        if (tallaId != null && cantidad != null) {
            for (int i = 0; i < Math.min(tallaId.size(), cantidad.size()); i++) {
                if (tallaId.get(i) != null && cantidad.get(i) != null) {
                    lineas.merge(tallaId.get(i), cantidad.get(i), Integer::sum);
                }
            }
        }
        if (lineas.isEmpty()) {
            throw new ReglaNegocioException("Agrega al menos un producto a la venta");
        }
        if (metodoPago == MetodoPago.CONTRA_ENTREGA) {
            throw new ReglaNegocioException("Método de pago no válido para una venta presencial");
        }

        Usuario vendedor = usuarios.findByCorreoIgnoreCase(auth.getName()).orElseThrow();
        Usuario cliente = null;
        if (!clienteCorreo.isBlank()) {
            cliente = usuarios.findByCorreoIgnoreCase(clienteCorreo.trim())
                    .filter(u -> u.getRol() == Rol.CLIENTE)
                    .orElseThrow(() -> new ReglaNegocioException("No existe un cliente con ese correo"));
        }

        Pedido pedido = new Pedido();
        pedido.setCanal(Canal.PRESENCIAL);
        pedido.setEstado(EstadoPedido.ENTREGADO);
        pedido.setDireccion("Tienda física");
        pedido.setMetodoPago(metodoPago);
        pedido.setVendedor(vendedor);
        pedido.setCliente(cliente);

        for (Map.Entry<Long, Integer> linea : lineas.entrySet()) {
            TallaStock talla = tallas.bloquear(linea.getKey())
                    .orElseThrow(() -> new ReglaNegocioException("Una de las tallas elegidas no existe"));
            if (!talla.getZapato().isActivo()) {
                throw new ReglaNegocioException("Un zapato de la venta ya no está disponible");
            }
            pedido.agregarDetalle(talla, linea.getValue());
            movimientos.save(new MovimientoInventario(talla, TipoMovimiento.VENTA, -linea.getValue(),
                    vendedor, pedido.getNumero()));
        }
        pedidos.save(pedido);
        ra.addFlashAttribute("mensaje", "Venta " + pedido.getNumero() + " registrada");
        return "redirect:/gestion/pedidos/" + pedido.getId();
    }
}
