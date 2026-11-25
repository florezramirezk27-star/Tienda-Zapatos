# Tareas — Tienda de Zapatos v1.0

| Campo | Valor |
|---|---|
| Especificación | `specs/001-tienda-zapatos/spec.md` |
| Plan | `specs/001-tienda-zapatos/plan.md` |
| Versión | 1.0 |
| Fecha | 25 de noviembre de 2026 |
| Estado | Implementación completa; eventos y revisión de par pendientes |

**Estados usados**

- **Hecha:** código implementado y prueba automática verde en la suite local.
- **Documentada:** entregable SDD creado.
- **Pendiente (humano):** requiere una persona, una reunión o un entorno externo.

Los puntos son estimación relativa de la historia, no horas. Las tareas se agrupan por sprint y cada una declara qué requisitos y criterios cubre.

---

## Sprint 1 — Especificación, acceso, usuarios y catálogo

**Meta:** especificación v1.0 aprobada, base del proyecto y los 5 roles con acceso según su menú; usuarios y catálogo operativos.

| ID | Tarea | Cubre | Responsable | Estado |
|---|---|---|---|---|
| T-001 | Redactar constitución, especificación v1.0, plan técnico y lista de tareas. | E-01 | Corales y Kevin | Documentada |
| T-002 | Crear el proyecto Spring Boot 3.5.7 con Java 21, Maven, Thymeleaf, JPA y PostgreSQL (ADR-006; el DOCX pedía MySQL); estructura MVC sin `service`. | E-01, NFR-006 | Kevin | Hecha |
| T-003 | Crear `database/tienda_zapatos.sql` y los datos ficticios de demostración. | E-01, NFR-004 | Kevin | Hecha y validada en PostgreSQL 16.15 |
| T-004 | Configurar Spring Security: login por correo, logout, BCrypt, CSRF y rutas por rol. | REQ-001, REQ-002, REQ-003, NFR-002 | Kevin | Hecha |
| T-005 | Construir la vista de login y el mensaje genérico exacto. | REQ-002, AC-002.1 | Corales | Hecha |
| T-006 | Construir el formulario de registro y crear el usuario CLIENTE activo. | REQ-004, AC-004.1 | Corales | Hecha |
| T-007 | Validar correo duplicado y mostrar «El correo ya está registrado». | REQ-005, AC-005.1 | Corales | Hecha |
| T-008 | Implementar la política de contraseña y el cifrado BCrypt. | REQ-006, AC-006.1, AC-006.2 | Kevin | Hecha |
| T-009 | Implementar la gestión de usuarios con exactamente uno de los 5 roles. | REQ-007, AC-007.1 | Kevin | Hecha |
| T-010 | Desactivar cuentas sin borrar su historial e impedir su login. | REQ-008, AC-008.1 | Kevin | Hecha |
| T-011 | Rechazar la desactivación del último administrador activo. | REQ-010, AC-010.1 | Kevin | Hecha |
| T-012 | Rechazar con HTTP 403 el acceso de roles no autorizados a administración. | REQ-009, AC-009.1 | Kevin | Hecha |
| T-013 | Implementar el alta de zapatos con referencia única y las 12 tallas (34–45). | REQ-011, AC-011.1 | Corales | Hecha |
| T-014 | Validar precio mayor que cero, con borde en 0 y aceptación en 1. | REQ-012, AC-012.1 | Corales | Hecha |
| T-015 | Desactivar zapatos sin alterar pedidos históricos. | REQ-013, AC-013.1 | Corales | Hecha |
| T-016 | Escribir las pruebas de HU-01 a HU-04 y del cierre de sesión. | AC-001.1 a AC-013.1 | Kevin y Corales | Hecha |

**Evidencia del sprint:** `TiendaIntegracionTest` (login, registro, usuarios, zapatos), `PoliticaContrasenaTest`, `SecurityConfig`, `database/tienda_zapatos.sql`.

---

## Sprint 2 — Catálogo, inventario, carrito y pedidos

**Meta:** un cliente encuentra zapatos por talla, arma su carrito y confirma un pedido; el stock se descuenta sin quedar negativo y sin doble venta del último par.

| ID | Tarea | Cubre | Responsable | Estado |
|---|---|---|---|---|
| T-017 | Catálogo público de zapatos activos, paginado de 12 en 12, con búsqueda por nombre, marca o referencia. | REQ-014, AC-014.1 | Corales | Hecha |
| T-018 | Filtros combinados por marca, categoría, talla y rango de precio con `ZapatoSpecs`. | REQ-015, AC-015.1 | Corales | Hecha |
| T-019 | Mensaje «No encontramos zapatos con esos filtros» cuando no hay resultados. | REQ-016, AC-016.1 | Corales | Hecha |
| T-020 | Inventario: entrada de mercancía, ajuste y bitácora con usuario, fecha y cantidad. | REQ-017, REQ-018, AC-017.1, AC-018.1, AC-018.2 | Kevin | Hecha |
| T-021 | Mostrar «Agotada» y deshabilitar la talla sin stock en el detalle. | REQ-019, AC-019.1 | Corales | Hecha |
| T-022 | Carrito en sesión con ítem, talla, cantidad y subtotal. | REQ-020, AC-020.1 | Corales | Hecha |
| T-023 | Rechazar cantidades sobre el stock, permitir el borde igual al stock y recalcular al actualizar/eliminar. | REQ-021, REQ-022, AC-021.1, AC-021.2, AC-022.1 | Corales | Hecha |
| T-024 | Checkout con dirección, método de pago y resumen de subtotal, IVA del 19 % y total. | REQ-023, REQ-025, AC-025.1 | Kevin | Hecha |
| T-025 | Crear el pedido PENDIENTE con número único, descontar stock y vaciar el carrito. | REQ-023, AC-023.1, AC-023.2 | Kevin | Hecha |
| T-026 | Bloquear la talla con `PESSIMISTIC_WRITE` y rechazar el pedido completo si no hay stock. | REQ-024, AC-024.1 | Kevin | Hecha |
| T-027 | Escribir las pruebas de HU-05 a HU-08, incluida la concurrencia real. | AC-014.1 a AC-025.1 | Kevin y Corales | Hecha |

**Evidencia del sprint:** `CarritoTest`, `TallaStockTest`, `PedidoTest`, `TiendaIntegracionTest`, `PedidoConcurrenciaTest`, `TiendaVistasTest`.

---

## Sprint 3 — Ventas, estados, historial, facturas y reporte

**Meta:** ciclo completo del pedido, venta presencial, historial del cliente, facturas y reporte de ventas en PDF, con trazabilidad completa.

| ID | Tarea | Cubre | Responsable | Estado |
|---|---|---|---|---|
| T-028 | Registrar venta presencial con canal PRESENCIAL, estado ENTREGADO y descuento de stock. | REQ-026, AC-026.1, AC-026.2 | Corales | Hecha |
| T-029 | Permitir la venta sin cuenta como «Cliente de mostrador». | REQ-027, AC-027.1 | Corales | Hecha |
| T-030 | Implementar las transiciones PENDIENTE → CONFIRMADO → ENVIADO → ENTREGADO y los permisos de Vendedor/Almacenista. | REQ-028, REQ-029, AC-028.1, AC-029.1 | Corales | Hecha |
| T-031 | Cancelar solo PENDIENTE o CONFIRMADO y devolver el stock de cada ítem. | REQ-030, AC-030.1, AC-030.2 | Corales | Hecha |
| T-032 | Mostrar solo los pedidos del cliente, del más reciente al más antiguo. | REQ-031, AC-031.1 | Corales | Hecha |
| T-033 | Mostrar el detalle completo del pedido propio y responder 403 ante uno ajeno. | REQ-032, AC-031.2, AC-032.1 | Corales | Hecha |
| T-034 | Generar la factura PDF con número, fecha, cliente, ítems, IVA y total. | REQ-033, AC-033.1 | Kevin | Hecha |
| T-035 | Rechazar facturas de pedidos no facturables y manejar la falla de la API sin cambiar el pedido. | REQ-034, REQ-035, AC-034.1, AC-035.1 | Kevin | Hecha |
| T-036 | Calcular el resumen de ventas (pedidos, unidades, ingresos, top 5) y generar el PDF. | REQ-036, AC-036.1 | Kevin | Hecha |
| T-037 | Rechazar fechas invertidas y generar el PDF con «Sin ventas en el rango seleccionado». | REQ-037, REQ-038, AC-037.1, AC-038.1 | Kevin | Hecha |
| T-038 | Escribir las pruebas de HU-09 a HU-13, incluidas las de PDF y roles. | AC-026.1 a AC-038.1 | Kevin y Corales | Hecha |
| T-039 | Crear los ADR, el tablero, la matriz de 46 criterios y las actas borrador. | Entregables SDD | Corales y Kevin | Documentada |
| T-040 | Revisión final: sin `service`, sin MongoDB, sin claves en logs y suite completa verde. | NFR-004, NFR-005, NFR-006 | Kevin | Hecha |
| T-041 | Revisión de par y pull request de cada historia. | DoD | Kevin y Corales | Pendiente (humano) |
| T-042 | Realizar Sprint Review y Retrospective de los tres sprints y registrar la decisión de release. | Scrum | PO y equipo | Pendiente (humano) |

**Evidencia del sprint:** `VentaController`, `GestionPedidoController`, `FacturaController`, `ReporteController`, `GeneradorPdf`, `FacturaPdfFallbackTest`, `ResumenVentasTest`.

---

## Definition of Ready aplicada

Una historia entró al sprint solo si cumplía:

- [x] Rol, necesidad y beneficio claros.
- [x] Al menos dos criterios, uno de borde o error.
- [x] Tamaño adecuado y estimación en puntos.
- [x] Sin dependencias externas al equipo.
- [x] Requisito EARS y criterios Given/When/Then en `spec.md`.

## Definition of Done aplicada

| Criterio | Estado | Evidencia |
|---|---|---|
| Requisito y criterios en la especificación aprobada | Hecho | `spec.md` |
| Una prueba automática por criterio | Hecho | 82 pruebas, 46/46 criterios |
| Código revisado por la otra persona y PR | Pendiente | T-041 |
| Sin carpeta/paquete `service` y sin MongoDB | Hecho | Revisión de estructura y `pom.xml` |
| Ruta con rol correcto y 403 para los demás | Hecho | Pruebas de roles y `SecurityConfig` |
| Base de datos con BCrypt y sin claves en logs (PostgreSQL por ADR-006) | Hecho | `application.properties`, `DatosIniciales` |
| Demostración en Sprint Review | Pendiente | Actas por completar |
| Fila en la matriz de trazabilidad | Hecho | `docs/trazabilidad.md` |

## Resumen

| Sprint | Puntos | Historias | Tareas | Estado de implementación |
|---|---:|---|---:|---|
| 1 | 13 | E-01, HU-01 a HU-04 | T-001 a T-016 | Código y pruebas completos |
| 2 | 13 | HU-05 a HU-08 | T-017 a T-027 | Código y pruebas completos |
| 3 | 13 | HU-09 a HU-13 | T-028 a T-042 | Código y pruebas completos; eventos pendientes |
| **Total** | **39** | **13 HU + E-01** | **42 tareas** | **82 pruebas verdes** |

## Pendientes que no puede cerrar el código

1. **Hecho (25 nov 2026):** esquema ejecutado y arranque con `ddl-auto=validate` correcto en PostgreSQL 16.15 (T-003, ADR-006).
2. Revisión de par y PR por historia (T-041).
3. Sprint Review, Retrospective y decisión de release (T-042).
4. Medición de NFR-ZAP-001 con 100 zapatos en navegador real.
