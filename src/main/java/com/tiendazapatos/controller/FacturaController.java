package com.tiendazapatos.controller;

import com.tiendazapatos.exception.ReglaNegocioException;
import com.tiendazapatos.model.Pedido;
import com.tiendazapatos.pdf.GeneradorPdf;
import com.tiendazapatos.pdf.PdfGeneracionException;
import com.tiendazapatos.repository.PedidoRepository;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

/** HU-12: factura de un pedido en PDF para el cliente dueño o el vendedor. */
@Controller
@RequestMapping("/facturas")
public class FacturaController {

    private final PedidoRepository pedidos;
    private final GeneradorPdf generador;

    public FacturaController(PedidoRepository pedidos, GeneradorPdf generador) {
        this.pedidos = pedidos;
        this.generador = generador;
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> factura(@PathVariable Long id, Authentication auth) {
        Pedido pedido = pedidos.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // RN-09: un cliente solo descarga las facturas de sus propios pedidos.
        boolean esCliente = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));
        if (esCliente && (pedido.getCliente() == null
                || !pedido.getCliente().getCorreo().equalsIgnoreCase(auth.getName()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        // REQ-ZAP-034 y RN-07
        if (!pedido.getEstado().isFacturable()) {
            throw new ReglaNegocioException("La factura solo está disponible para pedidos confirmados");
        }
        byte[] pdf;
        try {
            pdf = generador.factura(pedido);
        } catch (PdfGeneracionException e) {
            // REQ-ZAP-035: el pedido no se modifica; este controlador solo lee.
            throw new ReglaNegocioException("No fue posible generar la factura, intenta de nuevo");
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("factura-" + pedido.getNumero() + ".pdf").build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
