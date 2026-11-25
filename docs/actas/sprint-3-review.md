# Acta — Sprint Review 3

> **BORRADOR.** Plantilla programada para el cierre. No afirma que la reunión haya ocurrido ni anticipa la decisión del Product Owner.

| Campo | Valor |
|---|---|
| Evento | Sprint Review 3 |
| Fecha prevista | 4 de diciembre de 2026 |
| Hora | Por acordar con el docente |
| Duración | 45 min |
| Lugar / enlace | Por definir |
| Sprint | 3 (30 de noviembre – 4 de diciembre de 2026) |
| Meta del sprint | Ciclo completo del pedido, venta presencial, historial, facturas y reporte PDF; producto con trazabilidad y decisión de release. |
| Puntos comprometidos | 13 |
| Estado del acta | Borrador — programada |

## Asistentes

| Nombre | Rol Scrum | Asistió | Firma |
|---|---|---|---|
| Julián Barney Jaimes Rincón | Product Owner (docente) | ☐ | |
| Kevin Andréy Flórez Ramírez | Developer | ☐ | |
| Juan Miguel Corales | Scrum Master / Developer | ☐ | |
| | Invitado | ☐ | |

## Orden del día

1. Objetivo de la Review y estado final (5 min).
2. Demostración del incremento completo (25 min).
3. Revisión de la trazabilidad y de los NFR (10 min).
4. Decisión de release y cierre (5 min).

## Incremento a demostrar

| Historia | Guion de demostración | Criterios que se muestran | Estado técnico |
|---|---|---|---|
| HU-09 | Venta presencial de 1 par, stock 5→4, talla agotada y cliente de mostrador. | AC-026.1, AC-026.2, AC-027.1 | Implementada y probada |
| HU-10 | Confirmar, enviar, entregar, transición inválida, cancelar y devolver stock. | AC-028.1, AC-029.1, AC-030.1, AC-030.2 | Implementada y probada |
| HU-11 | Dos clientes, historial propio ordenado, detalle y 403 ajeno. | AC-031.1, AC-031.2, AC-032.1 | Implementada y probada |
| HU-12 | Factura PDF, pedido pendiente rechazado y fallback de la API. | AC-033.1, AC-034.1, AC-035.1 | Implementada y probada |
| HU-13 | Reporte con 3 ventas y 1 cancelada, fechas invertidas y rango vacío. | AC-036.1, AC-037.1, AC-038.1 | Implementada y probada |

## Evidencia técnica disponible

- Suite: **82 pruebas, 0 fallos, 0 errores** al 25 de noviembre de 2026.
- Criterios del Sprint 3 con prueba: 15/15.
- Criterios totales con prueba: 46/46.
- Facturas y reportes verificados extrayendo texto del PDF.
- Fallback de PDF probado con `@MockitoBean`.
- Matriz completa en `docs/trazabilidad.md`.
- Estructura sin `service`, sin MongoDB y sin claves en logs.

## Resultados de la demostración

| Punto | Resultado observado | Aceptado por el PO |
|---|---|---|
| Venta presencial | | ☐ |
| Estados y cancelación | | ☐ |
| Historial y detalle | | ☐ |
| Factura PDF | | ☐ |
| Reporte PDF | | ☐ |
| Trazabilidad 46/46 | | ☐ |

## Feedback y decisiones del Product Owner

> Escribir textualmente lo que diga el PO. No completar con suposiciones.

- 
 
## Ajustes finales

| Elemento | Cambio propuesto | Decisión |
|---|---|---|
| | | |

## Acuerdos y responsables

| Acuerdo | Responsable | Fecha límite |
|---|---|---|
| | | |

## Firma

| Nombre | Rol | Fecha | Firma |
|---|---|---|---|
| | Product Owner | | |
| | Scrum Master | | |
| | Developer | | |
