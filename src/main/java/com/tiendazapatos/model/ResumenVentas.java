package com.tiendazapatos.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** REQ-ZAP-036: total de pedidos, unidades vendidas, ingresos y los 5 zapatos más vendidos. */
public record ResumenVentas(LocalDate desde, LocalDate hasta, int pedidos, long unidades,
                            BigDecimal ingresos, List<ZapatoVendido> masVendidos) {

    public record ZapatoVendido(String nombre, long unidades) {
    }

    public static ResumenVentas de(LocalDate desde, LocalDate hasta, List<Pedido> ventas) {
        Map<String, Long> porZapato = new HashMap<>();
        long unidades = 0;
        BigDecimal ingresos = BigDecimal.ZERO;
        for (Pedido pedido : ventas) {
            ingresos = ingresos.add(pedido.getTotal());
            for (DetallePedido d : pedido.getDetalles()) {
                unidades += d.getCantidad();
                porZapato.merge(d.getNombreZapato(), (long) d.getCantidad(), Long::sum);
            }
        }
        List<ZapatoVendido> top = porZapato.entrySet().stream()
                .map(e -> new ZapatoVendido(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingLong(ZapatoVendido::unidades).reversed()
                        .thenComparing(ZapatoVendido::nombre))
                .limit(5)
                .toList();
        return new ResumenVentas(desde, hasta, ventas.size(), unidades, ingresos, top);
    }

    public boolean sinVentas() {
        return pedidos == 0;
    }
}
