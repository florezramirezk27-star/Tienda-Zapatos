package com.tiendazapatos.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.tiendazapatos.model.DetallePedido;
import com.tiendazapatos.model.Pedido;
import com.tiendazapatos.model.ResumenVentas;
import com.tiendazapatos.model.Usuario;
import com.tiendazapatos.util.Dinero;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * ADR-005: único lugar que conoce la API de PDF (OpenPDF). Si la API cambia, solo cambia este paquete.
 */
@Component
public class GeneradorPdf {

    private static final Font TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
    private static final Font SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
    private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font NEGRITA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** REQ-ZAP-033: número, fecha, datos del cliente, ítems, IVA y total. */
    public byte[] factura(Pedido p) {
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 50, 50);
            PdfWriter.getInstance(doc, salida);
            doc.open();

            doc.add(new Paragraph("Tienda de Zapatos", TITULO));
            Paragraph numero = new Paragraph("Factura " + p.getNumero(), SUBTITULO);
            numero.setSpacingAfter(10);
            doc.add(numero);

            doc.add(new Paragraph("Fecha: " + FECHA_HORA.format(p.getFecha()), NORMAL));
            doc.add(new Paragraph("Estado del pedido: " + p.getEstado(), NORMAL));
            doc.add(new Paragraph("Canal: " + p.getCanal().getEtiqueta(), NORMAL));
            doc.add(new Paragraph("Método de pago: " + p.getMetodoPago().getEtiqueta(), NORMAL));
            doc.add(new Paragraph("Cliente: " + p.getNombreCliente(), NORMAL));
            Usuario cliente = p.getCliente();
            if (cliente != null) {
                doc.add(new Paragraph("Correo: " + cliente.getCorreo(), NORMAL));
                if (cliente.getTelefono() != null) {
                    doc.add(new Paragraph("Teléfono: " + cliente.getTelefono(), NORMAL));
                }
            }
            if (p.getDireccion() != null && !p.getDireccion().isBlank()) {
                doc.add(new Paragraph("Dirección de entrega: " + p.getDireccion(), NORMAL));
            }

            PdfPTable tabla = new PdfPTable(new float[]{4f, 1.2f, 1.2f, 2f, 2f});
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(14);
            encabezado(tabla, "Zapato", Element.ALIGN_LEFT);
            encabezado(tabla, "Talla", Element.ALIGN_CENTER);
            encabezado(tabla, "Cant.", Element.ALIGN_CENTER);
            encabezado(tabla, "Precio", Element.ALIGN_RIGHT);
            encabezado(tabla, "Subtotal", Element.ALIGN_RIGHT);
            for (DetallePedido d : p.getDetalles()) {
                celda(tabla, d.getNombreZapato(), Element.ALIGN_LEFT, NORMAL);
                celda(tabla, String.valueOf(d.getTalla()), Element.ALIGN_CENTER, NORMAL);
                celda(tabla, String.valueOf(d.getCantidad()), Element.ALIGN_CENTER, NORMAL);
                celda(tabla, Dinero.cop(d.getPrecioUnitario()), Element.ALIGN_RIGHT, NORMAL);
                celda(tabla, Dinero.cop(d.getSubtotal()), Element.ALIGN_RIGHT, NORMAL);
            }
            doc.add(tabla);

            PdfPTable totales = new PdfPTable(new float[]{7f, 3f});
            totales.setWidthPercentage(100);
            totales.setSpacingBefore(10);
            fila(totales, "Subtotal", Dinero.cop(p.getSubtotal()), NORMAL);
            fila(totales, "IVA (19 %)", Dinero.cop(p.getIva()), NORMAL);
            fila(totales, "Total", Dinero.cop(p.getTotal()), NEGRITA);
            doc.add(totales);

            doc.close();
            return salida.toByteArray();
        } catch (Exception e) {
            throw new PdfGeneracionException("No fue posible generar la factura", e);
        }
    }

    /** REQ-ZAP-036 y 038: totales y los 5 zapatos más vendidos, o el mensaje de rango sin ventas. */
    public byte[] reporteVentas(ResumenVentas r) {
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 50, 50);
            PdfWriter.getInstance(doc, salida);
            doc.open();

            doc.add(new Paragraph("Tienda de Zapatos", TITULO));
            Paragraph sub = new Paragraph("Reporte de ventas del " + FECHA.format(r.desde())
                    + " al " + FECHA.format(r.hasta()), SUBTITULO);
            sub.setSpacingAfter(12);
            doc.add(sub);

            if (r.sinVentas()) {
                doc.add(new Paragraph("Sin ventas en el rango seleccionado", NORMAL));
            } else {
                PdfPTable resumen = new PdfPTable(new float[]{6f, 4f});
                resumen.setWidthPercentage(60);
                resumen.setHorizontalAlignment(Element.ALIGN_LEFT);
                fila(resumen, "Total de pedidos", String.valueOf(r.pedidos()), NORMAL);
                fila(resumen, "Unidades vendidas", String.valueOf(r.unidades()), NORMAL);
                fila(resumen, "Ingresos (con IVA)", Dinero.cop(r.ingresos()), NEGRITA);
                doc.add(resumen);

                Paragraph top = new Paragraph("Los 5 zapatos más vendidos", SUBTITULO);
                top.setSpacingBefore(16);
                top.setSpacingAfter(6);
                doc.add(top);

                PdfPTable tabla = new PdfPTable(new float[]{1f, 6f, 2f});
                tabla.setWidthPercentage(100);
                encabezado(tabla, "#", Element.ALIGN_CENTER);
                encabezado(tabla, "Zapato", Element.ALIGN_LEFT);
                encabezado(tabla, "Unidades", Element.ALIGN_RIGHT);
                int puesto = 1;
                for (ResumenVentas.ZapatoVendido z : r.masVendidos()) {
                    celda(tabla, String.valueOf(puesto++), Element.ALIGN_CENTER, NORMAL);
                    celda(tabla, z.nombre(), Element.ALIGN_LEFT, NORMAL);
                    celda(tabla, String.valueOf(z.unidades()), Element.ALIGN_RIGHT, NORMAL);
                }
                doc.add(tabla);
            }

            doc.close();
            return salida.toByteArray();
        } catch (Exception e) {
            throw new PdfGeneracionException("No fue posible generar el reporte", e);
        }
    }

    private void encabezado(PdfPTable tabla, String texto, int alineacion) {
        PdfPCell c = new PdfPCell(new Phrase(texto, NEGRITA));
        c.setHorizontalAlignment(alineacion);
        c.setBackgroundColor(new java.awt.Color(235, 235, 235));
        c.setPadding(5);
        tabla.addCell(c);
    }

    private void celda(PdfPTable tabla, String texto, int alineacion, Font fuente) {
        PdfPCell c = new PdfPCell(new Phrase(texto, fuente));
        c.setHorizontalAlignment(alineacion);
        c.setPadding(5);
        tabla.addCell(c);
    }

    private void fila(PdfPTable tabla, String etiqueta, String valor, Font fuente) {
        PdfPCell a = new PdfPCell(new Phrase(etiqueta, fuente));
        a.setBorder(Rectangle.NO_BORDER);
        a.setHorizontalAlignment(Element.ALIGN_RIGHT);
        PdfPCell b = new PdfPCell(new Phrase(valor, fuente));
        b.setBorder(Rectangle.NO_BORDER);
        b.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabla.addCell(a);
        tabla.addCell(b);
    }
}
