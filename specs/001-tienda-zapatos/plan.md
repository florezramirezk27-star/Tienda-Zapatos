# Plan Técnico — Tienda de Zapatos v1.0

| Campo | Valor |
|---|---|
| Especificación | `specs/001-tienda-zapatos/spec.md` |
| Constitución | `specs/001-tienda-zapatos/constitution.md` |
| Versión | 1.0 |
| Fecha | 25 de noviembre de 2026 |
| Estado | Implementado — Puerta 2 |
| Arquitectura | MVC en capas de presentación, control y persistencia, sin capa `service` |
| Base de datos | PostgreSQL (producción, ADR-006) / H2 en modo PostgreSQL (pruebas) |

---

## 1. Resumen ejecutivo

Tienda de Zapatos es una aplicación web monolítica construida con Spring Boot 3.5.7 y Java 21. Las vistas se generan en el servidor con Thymeleaf y Bootstrap 5. La persistencia usa Spring Data JPA sobre PostgreSQL (desviación ADR-006 respecto al MySQL 8 del DOCX). La seguridad se apoya en Spring Security con cinco roles, BCrypt y CSRF. La generación de PDF se aísla en el paquete `pdf` con OpenPDF. Las pruebas usan JUnit 5, MockMvc, Mockito y H2 en memoria.

El sistema cubre las 13 historias, 38 requisitos funcionales y 46 criterios de la especificación v1.0. No existe capa `service`, no se usa MongoDB y el esquema se versiona en `database/tienda_zapatos.sql`.

## 2. Contexto técnico

| Elemento | Decisión |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.5.7 (Maven) |
| Vista | Thymeleaf + Bootstrap 5 + CSS propio |
| Control | `@Controller` de Spring MVC |
| Modelo | Entidades JPA + objetos de sesión (`Carrito`) + récords de solo lectura (`ResumenVentas`) |
| Persistencia | Spring Data JPA / Hibernate 6 |
| Base oficial | PostgreSQL (`org.postgresql:postgresql`, ADR-006; el DOCX pedía MySQL 8) |
| Base de pruebas | H2 2.3 en memoria, `MODE=PostgreSQL`, `LOCK_TIMEOUT=10000` |
| Seguridad | Spring Security 6, BCrypt, CSRF, autorización por ruta |
| PDF | OpenPDF 2.0.3, confinado en `com.tiendazapatos.pdf` |
| Pruebas | JUnit 5, AssertJ, MockMvc, Mockito, `@MockitoBean` |
| Validación | Jakarta Bean Validation en formularios |
| Construcción | `mvn -B test`, `mvn spring-boot:run` |
| Control de versiones | Git / GitHub con revisión de la otra persona |

## 3. Decisiones de arquitectura

| ADR | Decisión | Consecuencia |
|---|---|---|
| [ADR-001](../../docs/adr/ADR-001.md) | MVC con Thymeleaf | Pantallas generadas en servidor, sin SPA ni API REST separada. |
| [ADR-002](../../docs/adr/ADR-002.md) | Sin capa `service` | Reglas en entidades, coordinación en controladores y consultas en repositorios. |
| [ADR-003](../../docs/adr/ADR-003.md) | MySQL 8 con JPA | **Reemplazado por ADR-006**; se conserva como registro histórico. |
| [ADR-004](../../docs/adr/ADR-004.md) | Seguridad por rol | Cada ruta declara sus roles; lo no declarado exige sesión. |
| [ADR-005](../../docs/adr/ADR-005.md) | PDF con una API aislada | Si OpenPDF cambia, solo cambia el paquete `pdf`. |
| [ADR-006](../../docs/adr/ADR-006.md) | PostgreSQL en lugar de MySQL 8 | Desviación autorizada; esquema SQL versionado; `ddl-auto=validate` contra PostgreSQL. |

## 4. Estructura de paquetes

```text
tienda-zapatos/
├── database/
│   └── tienda_zapatos.sql              Esquema oficial PostgreSQL (ADR-006)
├── docs/
│   ├── adr/                            ADR-001 a ADR-006
│   ├── actas/                          Review, Retrospective y decisión de release
│   ├── tablero.md                      Product Backlog y Sprint Backlogs
│   └── trazabilidad.md                 Criterio → requisito → tarea → código → prueba
├── specs/001-tienda-zapatos/
│   ├── constitution.md
│   ├── spec.md
│   ├── plan.md
│   └── tasks.md
├── src/main/java/com/tiendazapatos/
│   ├── TiendaZapatosApplication.java
│   ├── config/          SecurityConfig, DatosIniciales, VistaAdvice
│   ├── controller/      Auth, Catalogo, Usuario, Zapato, Inventario,
│   │                    Carrito, Pedido, Venta, GestionPedido,
│   │                    Factura, Reporte, ManejadorErrores
│   ├── exception/       ReglaNegocioException
│   ├── form/            RegistroForm, UsuarioForm, ZapatoForm
│   ├── model/           Usuario, Rol, Zapato, TallaStock, Pedido,
│   │                    DetallePedido, MovimientoInventario, Carrito,
│   │                    EstadoPedido, Canal, MetodoPago, TipoMovimiento,
│   │                    ResumenVentas
│   ├── pdf/             GeneradorPdf, PdfGeneracionException
│   ├── repository/      una interfaz JPA por entidad + ZapatoSpecs
│   └── util/            Dinero, PoliticaContrasena
├── src/main/resources/
│   ├── application.properties           PostgreSQL y validación del esquema
│   ├── static/                          css, js, imágenes, Bootstrap local
│   └── templates/                       vistas Thymeleaf por rol
└── src/test/
    ├── java/com/tiendazapatos/          82 pruebas derivadas de los criterios
    └── resources/application.properties  H2 en modo PostgreSQL
```

No existe `src/main/java/**/service` ni `src/main/resources/**/service`. La compilación y la revisión de estructura lo confirman.

## 5. Flujo de una petición

```text
Navegador
  │ HTTPS/HTTP + cookie de sesión + token CSRF
  ▼
SecurityFilterChain (ADR-004)
  │ autenticación, rol y CSRF
  ▼
@Controller
  │ valida formulario, coordina repositorios y entidades, marca @Transactional
  ▼
Entidad / repositorio
  │ reglas: stock, estados, totales, facturación
  ▼
PostgreSQL (producción) · H2 modo PostgreSQL (pruebas)
  │
  ▼
Thymeleaf o GeneradorPdf → respuesta HTML/PDF
```

## 6. Modelo de datos

### Entidades y tablas

| Entidad | Tabla | Campos principales | Relaciones |
|---|---|---|---|
| `Usuario` | `usuarios` | id, nombre, correo único, password BCrypt, teléfono, rol, activo | 1 rol; pedidos como cliente o vendedor; movimientos |
| `Zapato` | `zapatos` | id, referencia única, nombre, marca, categoría, color, precio, descripción, imagen, activo | 1..12 `TallaStock`; detalles históricos |
| `TallaStock` | `tallas_stock` | id, talla 34–45, stock ≥ 0 | N:1 `Zapato`; 1:N detalles y movimientos; único (zapato, talla) |
| `Pedido` | `pedidos` | id, número único, canal, estado, fecha, dirección, método de pago, subtotal, IVA, total | N:1 cliente y vendedor (opcionales); 1:N detalles |
| `DetallePedido` | `detalles_pedido` | id, nombre_zapato, talla, cantidad, precio_unitario | N:1 pedido, zapato y talla; copia histórica |
| `MovimientoInventario` | `movimientos_inventario` | id, tipo, cantidad con signo, fecha | N:1 talla y usuario |

### Invariantes

1. `stock >= 0` siempre; `TallaStock` rechaza cualquier operación que lo deje negativo.
2. Existe exactamente una fila por cada combinación zapato-talla; un zapato nuevo crea las 12 tallas (34–45) con stock 0.
3. `Pedido.total = subtotal + iva`, con `iva = subtotal × 0,19` redondeado a dos decimales. No hay costo de envío.
4. `Pedido.numero` es único y se genera con prefijo `PED-`.
5. Una venta presencial nace `ENTREGADO` y con canal `PRESENCIAL`.
6. `DetallePedido` conserva nombre y precio aunque el zapato se edite o desactive.
7. La cancelación devuelve el stock exactamente una vez y solo desde `PENDIENTE` o `CONFIRMADO`.
8. La factura solo se genera para `CONFIRMADO`, `ENVIADO` o `ENTREGADO`.

### Diagrama simplificado

```text
Usuario 1 ─── N Pedido 1 ─── N DetallePedido N ─── 1 Zapato 1 ─── N TallaStock
   │                │                                      │
   │                └── N MovimientoInventario N ──────────┘
   └── N MovimientoInventario
```

## 7. Seguridad y rutas

### Reglas de autorización

| Ruta | Método | Roles |
|---|---|---|
| `/`, `/catalogo`, `/zapatos/**` | GET | Público |
| `/login`, `/registro` | GET/POST | Público |
| `/admin/usuarios/**` | GET/POST | ADMINISTRADOR |
| `/almacen/zapatos/**` | GET/POST | ADMINISTRADOR, ALMACENISTA |
| `/almacen/inventario/**` | GET/POST | ALMACENISTA |
| `/carrito/**`, `/pedidos/**`, `/mis-pedidos/**` | GET/POST | CLIENTE |
| `/gestion/pedidos/**` | GET/POST | VENDEDOR, ALMACENISTA |
| `/ventas/**` | GET/POST | VENDEDOR |
| `/facturas/{id}` | GET | CLIENTE dueño, VENDEDOR |
| `/reportes/ventas/**` | GET | GERENTE |
| Cualquier otra ruta | — | Sesión iniciada |

La autorización fina (dueño del pedido, última cuenta de administrador, estado facturable) se valida además en el controlador o en la entidad. La seguridad de ruta no sustituye la regla de negocio.

### Controles

- CSRF activo en todos los formularios; las pruebas usan `csrf()`.
- Sesión inválida al cerrar sesión; las rutas protegidas redirigen al login.
- Mensaje de login genérico, sin revelar si el correo existe.
- BCrypt con coste por defecto; ninguna clave en logs.
- Datos personales limitados a nombre, correo y teléfono; no se publican.

## 8. Concurrencia del último par

El riesgo principal es que dos clientes compren el mismo último par. La solución combina:

1. `PedidoController.confirmar` es `@Transactional`.
2. Antes de descontar, se bloquea la fila de `TallaStock` con `TallaStockRepository.bloquear(id)` (`PESSIMISTIC_WRITE`).
3. Se vuelve a leer el stock dentro de la transacción; si no alcanza, se lanza `ReglaNegocioException` y la transacción revierte.
4. El descuento ocurre en la entidad y el pedido se guarda en la misma transacción.
5. `PedidoConcurrenciaTest` dispara dos peticiones HTTP reales con un `CountDownLatch`; solo una crea pedido y la otra recibe «Stock insuficiente».

En H2 el bloqueo se prueba con `LOCK_TIMEOUT=10000`; en PostgreSQL el bloqueo es de fila.

## 9. Generación de PDF

```text
FacturaController / ReporteController
          │
          ▼
   GeneradorPdf (paquete pdf)
          │  OpenPDF 2.0.3
          ▼
   byte[] → ResponseEntity<byte[]> (application/pdf)
```

- `GeneradorPdf.factura(Pedido)` arma número, fecha, cliente, ítems, subtotal, IVA y total.
- `GeneradorPdf.reporte(ResumenVentas)` arma rango, total de pedidos, unidades, ingresos y top 5.
- Si OpenPDF falla, se lanza `PdfGeneracionException`; el controlador muestra el mensaje y no modifica el pedido.
- Las pruebas extraen el texto con `PdfTextExtractor` para verificar contenido, no solo el encabezado `%PDF`.

## 10. Estrategia de pruebas

### Niveles

| Nivel | Clases | Qué verifica |
|---|---|---|
| Unidad de dominio | `CarritoTest`, `PedidoTest`, `TallaStockTest`, `PoliticaContrasenaTest`, `ResumenVentasTest` | Reglas puras de stock, totales, estados, contraseña y reporte. |
| Integración MVC | `TiendaIntegracionTest` (42 pruebas) | Rutas, roles, formularios, persistencia, PDF y mensajes exactos. |
| Vistas | `TiendaVistasTest` (8 pruebas) | HTML renderizado, tallas agotadas, navegación, carrito/checkout y recursos. |
| Concurrencia | `PedidoConcurrenciaTest` | Dos peticiones reales por el último par. |
| Fallback PDF | `FacturaPdfFallbackTest` | `@MockitoBean GeneradorPdf` que falla; mensaje y pedido intacto. |

### Cobertura de criterios

Los 46 criterios tienen al menos una prueba con prefijo `acNNN_M`. La correspondencia exacta está en `docs/trazabilidad.md`.

### Datos de prueba

- `TestDatos` crea zapatos con las 12 tallas y stock controlado.
- Cada prueba de integración es `@Transactional` y revierte al terminar, salvo concurrencia, que limpia en `@BeforeEach`/`@AfterEach`.
- `tienda.datos-demo=false` en pruebas para no mezclar los datos ficticios de demostración.

### Comando

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
mvn -B test
```

Resultado de referencia: **82 pruebas, 0 fallos, 0 errores, 0 omitidas**.

## 11. Configuración

### Producción (PostgreSQL, ADR-006)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/tienda_zapatos?stringtype=unspecified
spring.datasource.username=${DB_USER:postgres}
spring.datasource.password=${DB_PASSWORD:}
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

Pasos:

1. Crear la base (`createdb -U postgres tienda_zapatos`) y ejecutar `database/tienda_zapatos.sql`.
2. Definir `DB_USER` y `DB_PASSWORD`.
3. `mvn spring-boot:run`.
4. Abrir `http://localhost:8080`.

### Pruebas (H2)

```properties
spring.datasource.url=jdbc:h2:mem:tienda_zapatos_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;DATABASE_TO_LOWER=TRUE;LOCK_TIMEOUT=10000
spring.jpa.hibernate.ddl-auto=create-drop
tienda.datos-demo=false
```

## 12. Puertas y criterios de calidad

| Puerta | Criterio | Evidencia |
|---|---|---|
| Puerta 0 | Constitución aprobada | `constitution.md` |
| Puerta 1 | Especificación v1.0 con 38 REQ, 46 AC, 6 NFR y 10 RN | `spec.md` |
| Puerta 2 | Plan y ADR-001 a ADR-006 | `plan.md`, `docs/adr/` |
| Puerta 3 | Tareas trazables | `tasks.md` |
| Revisión de código | Sin `service`, sin MongoDB, sin claves en logs, pruebas verdes | Checklist y `mvn test` |
| Release | PO aprueba | `docs/actas/decision-release.md` |

## 13. Riesgos técnicos

| Riesgo | Impacto | Mitigación | Estado |
|---|---|---|---|
| Dos clientes por el último par | Alto | Bloqueo pesimista + transacción + prueba concurrente | Mitigado |
| Sin capa `service`, controladores extensos | Medio | Reglas en entidades y consultas en repositorios | Mitigado |
| API de PDF caída o incompatible | Medio | Paquete `pdf` aislado + excepción + prueba de fallback | Mitigado |
| Esquema SQL desalineado con entidades | Alto | `ddl-auto=validate` y revisión del script | **Verificado** el 25 nov 2026: arranque correcto contra PostgreSQL 16.15 |
| Poco tiempo para 39 puntos | Alto | Prioridad MoSCoW y entregas semanales | Controlado |
| Pérdida de datos personales | Alto | Ley 1581, datos mínimos, sin logs sensibles | Mitigado |
| Pruebas dependientes del servidor | Medio | H2 en modo PostgreSQL | Mitigado |

## 14. Entrega por sprint

| Sprint | Fechas | Incremento | Puntos |
|---|---|---|---|
| 1 | 17–20 nov 2026 | Especificación, base, seguridad, usuarios y catálogo | 13 |
| 2 | 23–27 nov 2026 | Catálogo con filtros, inventario, carrito y pedido | 13 |
| 3 | 30 nov–4 dic 2026 | Ventas, estados, historial, facturas y reporte | 13 |

## 15. Trabajo operativo pendiente (no automatizable)

1. **Hecho (25 nov 2026):** script ejecutado y arranque con `ddl-auto=validate` correcto contra PostgreSQL 16.15 (ADR-006).
2. Completar las actas de Review y Retrospective con los asistentes reales.
3. Registrar la aprobación de la Puerta 1 y la decisión de release del Product Owner.
4. Hacer la revisión de par y el pull request de cada historia.
5. Medir NFR-ZAP-001 con 100 zapatos en un navegador real.

Estos puntos no se pueden declarar cumplidos desde el código; quedan registrados como pendientes en `docs/tablero.md` y en las actas.
