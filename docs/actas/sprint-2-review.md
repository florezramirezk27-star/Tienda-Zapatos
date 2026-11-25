# Acta — Sprint Review 2

> **BORRADOR.** Plantilla programada. No afirma que la reunión haya ocurrido ni inventa resultados. Se completa después del evento.

| Campo | Valor |
|---|---|
| Evento | Sprint Review 2 |
| Fecha prevista | 27 de noviembre de 2026 |
| Hora | Por acordar con el docente |
| Duración | 45 min |
| Lugar / enlace | Por definir |
| Sprint | 2 (23–27 de noviembre de 2026) |
| Meta del sprint | Un cliente busca zapatos por talla, arma su carrito y confirma un pedido; el stock se descuenta sin negativo y sin doble venta del último par. |
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

1. Objetivo de la Review y estado del sprint (5 min).
2. Demostración del incremento (25 min).
3. Revisión del Backlog y feedback del PO (10 min).
4. Acuerdos y próximos pasos (5 min).

## Incremento a demostrar

| Historia | Guion de demostración | Criterios que se muestran | Estado técnico |
|---|---|---|---|
| HU-05 | Catálogo de 13 zapatos, página 1 y 2, filtro Nike + talla 40, filtro sin resultados. | AC-014.1, AC-015.1, AC-016.1 | Implementada y probada |
| HU-06 | Entrada de 10 pares, ajuste -10 rechazado, ajuste -4 aceptado, talla agotada. | AC-017.1, AC-018.1, AC-018.2, AC-019.1 | Implementada y probada |
| HU-07 | Agregar 2 pares, intentar 4 con stock 3, agregar 3, eliminar un ítem. | AC-020.1, AC-021.1, AC-021.2, AC-022.1 | Implementada y probada |
| HU-08 | Checkout con IVA, pedido PENDIENTE, carrito vacío y dos navegadores por el último par. | AC-023.1, AC-023.2, AC-024.1, AC-025.1 | Implementada y probada |

## Evidencia técnica disponible

- Suite: **82 pruebas, 0 fallos, 0 errores** al 25 de noviembre de 2026.
- Criterios del Sprint 2 con prueba: 16/16.
- Prueba de concurrencia real: `PedidoConcurrenciaTest`.
- Bloqueo pesimista: `TallaStockRepository.bloquear` con `PESSIMISTIC_WRITE`.
- Totales: `Pedido.total = subtotal + IVA`; sin costo de envío.

## Resultados de la demostración

| Punto | Resultado observado | Aceptado por el PO |
|---|---|---|
| Catálogo y filtros | | ☐ |
| Inventario y movimientos | | ☐ |
| Carrito y validación de stock | | ☐ |
| Checkout con IVA | | ☐ |
| Concurrencia del último par | | ☐ |

## Feedback y decisiones del Product Owner

> Escribir textualmente lo que diga el PO. No completar con suposiciones.

- 
 
## Ajustes al Product Backlog

| Elemento | Cambio propuesto | Prioridad nueva | Decisión |
|---|---|---|---|
| | | | |

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
