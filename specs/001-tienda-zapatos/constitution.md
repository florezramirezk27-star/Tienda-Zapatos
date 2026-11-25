# Constitución del Proyecto — Tienda de Zapatos

| Campo | Valor |
|---|---|
| Proyecto | Tienda de Zapatos (STEP Calzado) |
| Especificación | `specs/001-tienda-zapatos/` |
| Versión | 1.0 |
| Fecha | 25 de noviembre de 2026 |
| Estado | Vigente para el equipo — firma de Puerta 0 pendiente |
| Autores | Kevin Andréy Flórez Ramírez y Juan Miguel Corales |
| Product Owner | Docente Julián Barney Jaimes Rincón |
| Curso | Programación Java — UTS |

Esta constitución es el acuerdo mínimo del equipo. Toda historia, requisito, tarea, prueba y decisión de arquitectura debe respetarla. Si una práctica entra en conflicto con un artículo, prevalece la constitución hasta que se apruebe una enmienda.

---

## Artículo 1. La especificación es la fuente de verdad

1. Solo se programa lo que está escrito y aprobado en `spec.md`.
2. Todo requisito funcional se redacta con uno de los cinco patrones EARS: ubicuo, evento, estado, opcional o no deseado.
3. Todo requisito tiene al menos un criterio de aceptación en formato Given/When/Then, con caso normal, borde o error.
4. Un cambio de comportamiento se describe primero en la especificación; después se implementa y se prueba.
5. No se permiten marcadores `[NECESITA ACLARACIÓN]` en una historia que entre a un sprint. Las dudas se resuelven con el Product Owner antes del Sprint Planning.
6. La especificación v1.0 contiene exactamente 13 historias, 38 requisitos funcionales, 46 criterios de aceptación, 6 requisitos no funcionales, 10 reglas de negocio y 5 roles de usuario.

## Artículo 2. Arquitectura MVC sin capa `service`

1. La aplicación usa Spring Boot 3.5, Java 21, Spring MVC y Thymeleaf. Las pantallas se generan en el servidor.
2. No existe carpeta ni paquete llamado `service`. La lógica de negocio vive en métodos de las entidades (`Pedido`, `TallaStock`, `Carrito`, `EstadoPedido`, `ResumenVentas`), los controladores coordinan y los repositorios resuelven consultas.
3. Los controladores que modifican varias tablas se marcan con `@Transactional` para que la operación sea atómica.
4. Las reglas que deben cumplirse siempre se validan en el modelo o en el repositorio, no solo en la vista.
5. No se usan microservicios ni una interfaz separada (Spa, Android o iOS).

## Artículo 3. Persistencia oficial en PostgreSQL con JPA (desviación ADR-006)

1. La base oficial de la configuración vigente es PostgreSQL en `localhost:5432`. El acceso a datos se hace con Spring Data JPA e Hibernate.
2. El esquema se crea y versiona con `database/tienda_zapatos.sql`; la aplicación arranca con `spring.jpa.hibernate.ddl-auto=validate`.
3. No se usa MongoDB ni ninguna base de documentos.
4. Las entidades conservan copias históricas de los datos que una venta no debe perder: nombre del zapato y precio unitario en `DetallePedido`.
5. Los datos de demostración son ficticios y se cargan solo con `tienda.datos-demo=true`.
6. Las pruebas automáticas usan H2 en memoria en modo PostgreSQL y nunca apuntan a la base real.
7. **Desviación:** el DOCX original y NFR-ZAP-004 exigen MySQL 8. El 25 de noviembre de 2026 el usuario decidió PostgreSQL; ver [ADR-006](../../docs/adr/ADR-006.md). Este artículo ya no cumple literalmente el documento fuente.

## Artículo 4. Seguridad y protección de datos

1. Cada ruta declara el rol que puede usarla. Lo no declarado exige sesión iniciada. Un rol sin permiso recibe HTTP 403.
2. Los cinco roles son Administrador, Gerente, Vendedor, Almacenista y Cliente. Cada usuario tiene exactamente uno.
3. Las contraseñas se guardan con BCrypt y cumplen mínimo 8 caracteres, al menos una letra y un número.
4. La protección CSRF permanece activa en todos los formularios.
5. Ninguna contraseña, clave temporal o hash se escribe en logs, mensajes de error o páginas. El mensaje de login es siempre «Correo o contraseña incorrectos».
6. Los datos personales se tratan según la Ley 1581 de 2012: se recogen solo los necesarios, se usan para la compra y no se publican.
7. El cliente solo ve y descarga sus propios pedidos; el acceso a un pedido ajeno responde 403.

## Artículo 5. Cada criterio tiene una prueba automática

1. Cada uno de los 46 criterios de aceptación tiene al menos una prueba automatizada identificable por el prefijo `acNNN_M`.
2. Las pruebas de reglas puras usan JUnit 5; las de rutas, roles, vistas y PDF usan `@SpringBootTest` y MockMvc.
3. Una prueba debe fallar si se rompe la regla que verifica. No se aceptan pruebas que solo comprueben que la aplicación arranca.
4. Los casos de borde (precio 0, stock igual a la cantidad, ajuste a cero, último administrador, fechas invertidas, rango sin ventas, PDF caído) son obligatorios.
5. La matriz `docs/trazabilidad.md` conecta criterio → requisito → tarea → código → prueba.
6. La suite completa debe pasar antes de declarar terminada una historia.

## Artículo 6. PDF aislado

1. Todo el uso de la API de PDF se concentra en el paquete `com.tiendazapatos.pdf`.
2. Los controladores no conocen clases de OpenPDF; solo llaman a `GeneradorPdf`.
3. Si la API falla, se lanza `PdfGeneracionException`, se muestra un mensaje al usuario y el pedido no cambia.
4. La factura solo se genera para pedidos `CONFIRMADO`, `ENVIADO` o `ENTREGADO`.
5. El reporte de ventas excluye pedidos `PENDIENTE` y `CANCELADO`.

## Artículo 7. Scrum y trazabilidad

1. El trabajo se organiza en tres sprints de una semana: 17–20 de noviembre, 23–27 de noviembre y 30 de noviembre–4 de diciembre de 2026.
2. Cada sprint tiene Sprint Planning, Daily Scrum, Sprint Review y Sprint Retrospective. Las actas se registran en `docs/actas/`.
3. No se inventan resultados de reuniones. Un acta sin realizar se marca como borrador y se completa después del evento.
4. La Definition of Ready exige rol, necesidad, beneficio, al menos dos criterios (uno de borde o error), tamaño adecuado y ausencia de dependencias externas.
5. La Definition of Done exige especificación aprobada, prueba por criterio, revisión de la otra persona, rutas protegidas, base de datos con BCrypt, demostración en Review y fila en la matriz. El DOCX decía «MySQL con BCrypt»; al usar PostgreSQL (ADR-006) se reporta la desviación.
6. El tablero `docs/tablero.md` muestra Product Backlog y Sprint Backlogs. Los puntos son estimación relativa, no horas.

## Artículo 8. Calidad del código

1. Nombres de clases, métodos y variables en español, coherentes con el dominio; anotaciones y APIs estándar en su idioma original.
2. Inyección por constructor; sin Lombok; sin campos públicos mutables.
3. Se prefieren objetos de valor y métodos de entidad a cálculos duplicados en las vistas.
4. Los errores de negocio se expresan con `ReglaNegocioException` y se convierten en un mensaje para el usuario.
5. El dinero se maneja con `BigDecimal` y se formatea en pesos colombianos con `Dinero`.
6. No se dejan credenciales, rutas absolutas ni datos personales reales en el repositorio.

## Artículo 9. Gobierno de la constitución

1. Una enmienda requiere: justificación, impacto en requisitos y pruebas, aprobación del Product Owner y registro en un ADR.
2. La versión se incrementa con cada enmienda aprobada.
3. La revisión de cumplimiento se hace al final de cada sprint y antes de la decisión de release.
4. La decisión de release es del Product Owner y se registra en `docs/actas/decision-release.md`.

## Lista de cumplimiento

- [x] 13 historias con rol, necesidad y beneficio.
- [x] 38 requisitos funcionales en EARS.
- [x] 46 criterios Given/When/Then.
- [x] 6 requisitos no funcionales.
- [x] 10 reglas de negocio.
- [x] 5 roles con matriz de permisos.
- [x] Sin carpeta ni paquete `service`.
- [x] Sin MongoDB; JPA sobre PostgreSQL (desviación ADR-006 del MySQL 8 del DOCX).
- [x] PDF aislado en el paquete `pdf`.
- [x] Contraseñas con BCrypt y sin registro en logs.
- [x] Una prueba automática por criterio (`82` pruebas en total).
- [x] Matriz de trazabilidad completa.
- [x] Product Backlog y Sprint Backlogs.
- [ ] Actas de Review y Retrospective completadas por los asistentes.
- [ ] Decisión de release del Product Owner.

Las dos casillas abiertas dependen de eventos y personas, no del código; por eso se entregan como borrador y no se declaran cumplidas.
