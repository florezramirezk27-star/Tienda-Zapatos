package com.tiendazapatos.controller;

import com.tiendazapatos.exception.ReglaNegocioException;
import com.tiendazapatos.model.EstadoPedido;
import com.tiendazapatos.model.Pedido;
import com.tiendazapatos.model.ResumenVentas;
import com.tiendazapatos.pdf.GeneradorPdf;
import com.tiendazapatos.pdf.PdfGeneracionException;
import com.tiendazapatos.repository.PedidoRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;

/** HU-13: reporte de ventas en PDF por rango de fechas, solo para el gerente. */
@Controller
@RequestMapping("/reportes")
public class ReporteController {

    private final PedidoRepository pedidos;
    private final GeneradorPdf generador;

    public ReporteController(PedidoRepository pedidos, GeneradorPdf generador) {
        this.pedidos = pedidos;
        this.generador = generador;
    }

    @GetMapping("/ventas")
    public String formulario(Model model) {
        LocalDate hoy = LocalDate.now();
        model.addAttribute("desde", hoy.withDayOfMonth(1));
        model.addAttribute("hasta", hoy);
        return "reportes/ventas";
    }

    /** REQ-ZAP-036, 037 y 038. RN-10: cuenta CONFIRMADO, ENVIADO y ENTREGADO; excluye PENDIENTE y CANCELADO. */
    @GetMapping("/ventas/pdf")
    public ResponseEntity<byte[]> reporte(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new ReglaNegocioException("Selecciona la fecha inicial y la final");
        }
        if (desde.isAfter(hasta)) {
            throw new ReglaNegocioException("La fecha inicial no puede ser posterior a la final");
        }
        List<Pedido> ventas = pedidos.ventasEntre(
                EnumSet.of(EstadoPedido.CONFIRMADO, EstadoPedido.ENVIADO, EstadoPedido.ENTREGADO),
                desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
        ResumenVentas resumen = ResumenVentas.de(desde, hasta, ventas);
        byte[] pdf;
        try {
            pdf = generador.reporteVentas(resumen);
        } catch (PdfGeneracionException e) {
            throw new ReglaNegocioException("No fue posible generar el reporte, intenta de nuevo");
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("reporte-ventas-" + desde + "_" + hasta + ".pdf").build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
