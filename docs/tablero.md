# Tablero — Tienda de Zapatos

| Campo | Valor |
|---|---|
| Producto | Tienda de Zapatos (STEP Calzado) |
| Product Goal | Que ningún par se venda sin existir en el inventario: todo pedido queda registrado, descuenta stock por talla, lo gestiona el rol correcto y se respalda con factura PDF. |
| Periodo | 13 de noviembre – 4 de diciembre de 2026 |
| Equipo | Kevin Andréy Flórez Ramírez y Juan Miguel Corales |
| Product Owner | Docente Julián Barney Jaimes Rincón |
| Última actualización | 25 de noviembre de 2026 |
| Fuente | `spec.md`, `tasks.md` y evidencia del repositorio |

**Criterio de estado**

- **Hecha:** implementada y con prueba automática verde.
- **Documentada:** entregable SDD creado.
- **Pendiente (humano):** requiere revisión de par, evento, decisión del PO o un PostgreSQL real (ADR-006).
- **No iniciada:** no hay evidencia en el repositorio.

> Este tablero se reconstruyó con la evidencia del código y de la suite al 25 de noviembre de 2026. No se inventan velocidades, resultados de reuniones ni aprobaciones. Los eventos y la decisión de release se registran en `docs/actas/` cuando ocurran.

---

## 1. Product Backlog

Prioridad MoSCoW: **M** = Must, **S** = Should, **C** = Could. Los puntos son estimación relativa, no horas.

| ID | Elemento | Rol principal | Prioridad | Puntos | Sprint | Hace | Valida | Estado |
|---|---|---|---:|---:|---:|---|---|---|
| E-01 | Constitución, especificación v1.0 y base del proyecto | Equipo | M | 3 | 1 | Corales y Kevin | Docente (PO) | Documentada + base hecha |
| HU-01 | Iniciar y cerrar sesión | 5 roles | M | 2 | 1 | Kevin | Corales | Hecha |
| HU-02 | Registrarme como cliente | Visitante | M | 2 | 1 | Corales | Kevin | Hecha |
| HU-03 | Gestionar usuarios y roles | Administrador | M | 3 | 1 | Kevin | Corales | Hecha |
| HU-04 | Gestionar el catálogo de zapatos | Almacenista y Administrador | M | 3 | 1 | Corales | Kevin | Hecha |
| HU-05 | Consultar el catálogo con filtros | Visitante y Cliente | M | 3 | 2 | Corales | Kevin | Hecha |
| HU-06 | Gestionar el inventario por talla | Almacenista | M | 3 | 2 | Kevin | Corales | Hecha |
| HU-07 | Armar mi carrito de compras | Cliente | M | 3 | 2 | Corales | Kevin | Hecha |
| HU-08 | Realizar un pedido | Cliente | M | 4 | 2 | Kevin | Corales | Hecha |
| HU-09 | Registrar una venta presencial | Vendedor | S | 3 | 3 | Corales | Kevin | Hecha |
| HU-10 | Gestionar el estado de los pedidos | Vendedor y Almacenista | M | 2 | 3 | Corales | Kevin | Hecha |
| HU-11 | Consultar mi historial de pedidos | Cliente | S | 2 | 3 | Corales | Kevin | Hecha |
| HU-12 | Generar la factura en PDF | Cliente y Vendedor | M | 3 | 3 | Kevin | Corales | Hecha |
| HU-13 | Obtener el reporte de ventas en PDF | Gerente | S | 3 | 3 | Kevin | Corales | Hecha |
| **Total** | **13 HU + 1 habilitador** | | | **39** | **3** | | | **Implementación completa** |

### Distribución por prioridad

| Prioridad | Elementos | Puntos |
|---|---:|---:|
| Must | E-01, HU-01 a HU-08, HU-10, HU-12 | 31 |
| Should | HU-09, HU-11, HU-13 | 8 |
| Could | — | 0 |
| **Total** | **14 elementos** | **39** |

---

## 2. Sprint 1 — Especificación, acceso, usuarios y catálogo

| Campo | Valor |
|---|---|
| Fechas | 17–20 de noviembre de 2026 (4 días hábiles; el lunes 16 es festivo) |
| Meta | Especificación v1.0 aprobada y cada uno de los 5 roles inicia sesión con su menú; el administrador gestiona usuarios y el almacenista carga zapatos. |
| Puntos comprometidos | 13 |
| Estado | Implementación y pruebas completas; revisión de par y acta pendientes |

### Sprint Backlog

| Tarea | Descripción | Puntos/Historia | Estado | Evidencia |
|---|---|---|---:|---|---|
| T-001 | Constitución, especificación, plan y tareas | E-01 / 3 | Documentada | `specs/001-tienda-zapatos/` |
| T-002 | Proyecto Spring Boot, Maven, PostgreSQL (ADR-006) y MVC sin `service` | E-01 | Hecha | `pom.xml`, paquetes |
| T-003 | Script SQL y datos demo | E-01 | Hecha; validada en PostgreSQL 16.15 | `database/tienda_zapatos.sql` |
| T-004 | Spring Security, BCrypt, CSRF y rutas | HU-01 / 2 | Hecha | `SecurityConfig` |
| T-005 | Vista de login y mensaje genérico | HU-01 | Hecha | `login.html`, `TiendaIntegracionTest` |
| T-006 | Registro de cliente | HU-02 / 2 | Hecha | `AuthController`, `RegistroForm` |
| T-007 | Correo duplicado | HU-02 | Hecha | AC-005.1 |
| T-008 | Política de contraseña y BCrypt | HU-02 | Hecha | `PoliticaContrasena`, AC-006.1/2 |
| T-009 | Gestión de usuarios y 5 roles | HU-03 / 3 | Hecha | `UsuarioController` |
| T-010 | Desactivar cuenta sin borrar historial | HU-03 | Hecha | AC-008.1 |
| T-011 | Último administrador activo | HU-03 | Hecha | AC-010.1 |
| T-012 | 403 para roles no autorizados | HU-03 | Hecha | AC-009.1 |
| T-013 | Alta de zapatos con referencia y 12 tallas | HU-04 / 3 | Hecha | `ZapatoController` |
| T-014 | Precio mayor que cero | HU-04 | Hecha | AC-012.1 |
| T-015 | Desactivar zapato conservando historial | HU-04 | Hecha | AC-013.1 |
| T-016 | Pruebas de HU-01 a HU-04 | Todas | Hecha | 18 pruebas del bloque |

### Revisión del sprint

- **Demostración prevista:** login de los 5 roles, alta de usuario VENDEDOR, alta de zapato, desactivación y 403.
- **Resultado real:** pendiente de registrar en `docs/actas/sprint-1-review.md`.
- **Impedimentos:** ninguno técnico. La aprobación de la Puerta 1 depende del PO.

---

## 3. Sprint 2 — Catálogo, inventario, carrito y pedidos

| Campo | Valor |
|---|---|
| Fechas | 23–27 de noviembre de 2026 |
| Meta | Un cliente busca por talla, arma el carrito y confirma un pedido; el stock se descuenta sin negativo y sin doble venta del último par. |
| Puntos comprometidos | 13 |
| Estado | Implementación y pruebas completas; revisión de par y acta pendientes |

### Sprint Backlog

| Tarea | Descripción | Puntos/Historia | Estado | Evidencia |
|---|---|---|---:|---|---|
| T-017 | Catálogo paginado 12 + búsqueda | HU-05 / 3 | Hecha | `CatalogoController`, AC-014.1 |
| T-018 | Filtros combinados | HU-05 | Hecha | `ZapatoSpecs`, AC-015.1 |
| T-019 | Mensaje sin resultados | HU-05 | Hecha | AC-016.1 |
| T-020 | Entradas, ajustes y movimientos | HU-06 / 3 | Hecha | `InventarioController`, AC-017.1/018.1/018.2 |
| T-021 | Talla agotada visible y deshabilitada | HU-06 | Hecha | AC-019.1, `zapato-detalle.html` |
| T-022 | Carrito de sesión y subtotal | HU-07 / 3 | Hecha | `Carrito`, AC-020.1 |
| T-023 | Validación de stock y recálculo | HU-07 | Hecha | AC-021.1/021.2/022.1 |
| T-024 | Checkout con IVA y total | HU-08 / 4 | Hecha | `checkout.html`, AC-025.1 |
| T-025 | Pedido PENDIENTE, número único y descuento | HU-08 | Hecha | `PedidoController`, AC-023.1/023.2 |
| T-026 | Bloqueo pesimista del último par | HU-08 | Hecha | `PedidoConcurrenciaTest`, AC-024.1 |
| T-027 | Pruebas de HU-05 a HU-08 | Todas | Hecha | 24 pruebas del bloque |

### Revisión del sprint

- **Demostración prevista:** filtros combinados, entrada de inventario, carrito, checkout con IVA y dos navegadores compitiendo por el último par.
- **Resultado real:** pendiente de registrar en `docs/actas/sprint-2-review.md`.
- **Impedimentos:** ninguno técnico. La prueba concurrente exige H2 con `LOCK_TIMEOUT` suficiente, ya configurado.

---

## 4. Sprint 3 — Ventas, estados, historial, facturas y reporte

| Campo | Valor |
|---|---|
| Fechas | 30 de noviembre – 4 de diciembre de 2026 |
| Meta | Ciclo completo del pedido, venta presencial, historial, facturas y reporte en PDF; producto con trazabilidad y decisión de release. |
| Puntos comprometidos | 13 |
| Estado | Implementación y pruebas completas; eventos y release pendientes |

### Sprint Backlog

| Tarea | Descripción | Puntos/Historia | Estado | Evidencia |
|---|---|---|---:|---|---|
| T-028 | Venta presencial ENTREGADO y stock | HU-09 / 3 | Hecha | `VentaController`, AC-026.1/026.2 |
| T-029 | Cliente de mostrador | HU-09 | Hecha | AC-027.1 |
| T-030 | Transiciones y permisos | HU-10 / 2 | Hecha | `EstadoPedido`, AC-028.1/029.1 |
| T-031 | Cancelación con devolución de stock | HU-10 | Hecha | AC-030.1/030.2 |
| T-032 | Historial propio ordenado | HU-11 / 2 | Hecha | AC-031.1 |
| T-033 | Detalle propio y 403 ajeno | HU-11 | Hecha | AC-031.2/032.1 |
| T-034 | Factura PDF completa | HU-12 / 3 | Hecha | `GeneradorPdf`, AC-033.1 |
| T-035 | Validación facturable y fallback | HU-12 | Hecha | AC-034.1/035.1 |
| T-036 | Resumen de ventas y top 5 | HU-13 / 3 | Hecha | `ResumenVentas`, AC-036.1 |
| T-037 | Fechas invertidas y rango vacío | HU-13 | Hecha | AC-037.1/038.1 |
| T-038 | Pruebas de HU-09 a HU-13 | Todas | Hecha | 20 pruebas del bloque |
| T-039 | ADR, tablero, matriz y actas | Entregables | Documentada | `docs/` |
| T-040 | Revisión final sin `service`, MongoDB ni claves en logs | NFR | Hecha | Checklist |
| T-041 | Revisión de par y PR | DoD | Pendiente (humano) | — |
| T-042 | Review, Retrospective y release | Scrum | Pendiente (humano) | `docs/actas/` |

### Revisión del sprint

- **Demostración prevista:** venta presencial, cambio de estados, cancelación con stock, historial, factura y reporte PDF.
- **Resultado real:** pendiente de registrar en `docs/actas/sprint-3-review.md`.
- **Decisión de release:** pendiente en `docs/actas/decision-release.md`.

---

## 5. Vista de flujo

```text
Product Backlog         Sprint 1                Sprint 2                Sprint 3
39 pts                  13 pts                  13 pts                  13 pts
┌──────────────┐   ┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│ E-01         │──▶│ Especificar  │──▶│ Catálogo     │──▶│ Ventas       │
│ HU-01..HU-13 │   │ Seguridad    │   │ Inventario   │   │ Estados      │
│              │   │ Usuarios     │   │ Carrito      │   │ Historial    │
│              │   │ Catálogo     │   │ Pedido       │   │ PDF          │
└──────────────┘   └──────────────┘   └──────────────┘   └──────────────┘
                         13               13               13
                    ───────────────────────────────────────────────▶
                    Implementación acumulada: 39/39 puntos
```

## 6. Métricas de calidad al 25 de noviembre de 2026

| Métrica | Valor | Fuente |
|---|---:|---|
| Historias implementadas | 13/13 | Código y pruebas |
| Requisitos funcionales cubiertos | 38/38 | `spec.md` |
| Criterios con prueba | 46/46 | `docs/trazabilidad.md` |
| Pruebas automáticas | 82 | `mvn -B test` |
| Pruebas fallidas | 0 | `mvn -B test` |
| Puntos implementados | 39/39 | Este tablero |
| NFR verificados por prueba o revisión | 6/6 | `plan.md` |
| Cobertura de roles | 5/5 | Pruebas de autorización |
| Deuda crítica conocida | 0 | — |

## 7. Impedimentos y riesgos abiertos

| Impedimento | Tipo | Impacto | Respuesta | Responsable |
|---|---|---|---|---|
| Esquema ejecutado y validado en PostgreSQL 16.15 | Técnico | `validate` podía revelar diferencias | Verificado el 25 nov 2026 con arranque correcto (ADR-006) | Kevin |
| Revisión de par y PR pendientes | Proceso | DoD incompleto | Revisar cada historia antes del release | Kevin y Corales |
| Actas de eventos sin completar | Proceso | Falta evidencia Scrum | Realizar y firmar las actas | PO y equipo |
| NFR-ZAP-001 sin medición formal | Rendimiento | No se conoce el tiempo real con 100 zapatos | Medir en navegador en la Review | Corales |
| Decisión de release pendiente | Producto | No hay autorización formal | Completar `decision-release.md` | PO |

## 8. Nota de transparencia

El repositorio demuestra la implementación y las pruebas. No demuestra por sí solo que una reunión ocurrió, que una persona revisó un PR o que el PO aprobó el release. Por eso el tablero distingue **Hecha** de **Pendiente (humano)** y las actas se entregan como borrador hasta que se completen con los asistentes y las decisiones reales.
