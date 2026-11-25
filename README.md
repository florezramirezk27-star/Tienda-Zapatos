# Tienda de Zapatos

Tienda web de zapatos (Scrum + SDD) — Spring Boot 3.5 · Java 21 · MVC con Thymeleaf · JPA/Hibernate · PostgreSQL · PDF con OpenPDF.
Autores: Kevin Andréy Flórez Ramírez y Juan Miguel Corales — UTS.

> **Desviación documentada:** el DOCX original exigía MySQL 8 (NFR-ZAP-004). El usuario decidió el 25 nov 2026 usar PostgreSQL; el cambio está registrado en [ADR-006](docs/adr/ADR-006.md). NFR-ZAP-004 se reporta como **desviación autorizada**, no como cumplimiento del texto original.

## Requisitos
- JDK 21 y Maven 3.9+
- Desarrollo: PostgreSQL local en `localhost:5432`
- Producción: base Neon (PostgreSQL en la nube) usada por Render

## Puesta en marcha (local / STS)
1. Crear la base y las tablas:
   - `createdb -U postgres tienda_zapatos`
   - `psql -U postgres -d tienda_zapatos -f database/tienda_zapatos.sql`
   El esquema se valida al arrancar con `spring.jpa.hibernate.ddl-auto=validate`.
2. Credenciales por variables de entorno (por defecto `postgres` sin clave):
   - PowerShell: `$env:DB_USER="postgres"; $env:DB_PASSWORD="tu_clave"`
   - Bash: `export DB_USER=postgres DB_PASSWORD=tu_clave`
   - En STS: Run Configurations → Environment → `DB_USER` y `DB_PASSWORD`
3. Arrancar: `mvn spring-boot:run` y abrir http://localhost:8080

Verificado el 25 nov 2026: esquema aplicado y arranque con `ddl-auto=validate` correctos contra PostgreSQL 16.15.

Pruebas: `mvn test` (usan H2 en memoria en modo PostgreSQL, no necesitan servidor ni tocan la base real).

## Despliegue en Render con Neon

La misma aplicación usa **Neon en Render** y **PostgreSQL local en STS**; no son dos bases de la tienda compitiendo, sino dos entornos.

1. Ejecutar `database/tienda_zapatos_neon.sql` en el SQL Editor de Neon. Ese script crea el esquema en `public` y es el que valida `ddl-auto=validate` en producción.
2. En Render → Environment, configurar:

   | Variable | Valor |
   |---|---|
   | `DB_URL` | Cadena JDBC de Neon, por ejemplo `jdbc:postgresql://<host>/<db>?sslmode=require&stringtype=unspecified` |
   | `DB_USER` | Usuario de Neon |
   | `DB_PASSWORD` | Contraseña de Neon |
   | `TIENDA_DATOS_DEMO` | `false` (para no cargar usuarios de prueba en producción) |

3. Build: `mvn -B clean package`; Start: `java -jar target/tienda-zapatos-1.0.0.jar`.

Si `DB_URL` no está definida, la aplicación usa `jdbc:postgresql://localhost:5432/tienda_zapatos`. Así el mismo JAR sirve para local y para Render sin cambiar código ni guardar claves en el repositorio.

Si usas Eclipse/STS ya no necesitas Lombok: los getters y setters están escritos en el código.

## Usuarios de demostración
Con `TIENDA_DATOS_DEMO=true` (valor por defecto en local) se crean al primer arranque, con clave **Zapatos2026**:

| Rol | Correo |
|---|---|
| Administrador | admin@tienda.test |
| Gerente | gerente@tienda.test |
| Vendedor | vendedor@tienda.test |
| Almacenista | almacenista@tienda.test |
| Cliente | cliente@tienda.test |

Además se cargan 14 zapatos. En producción use `TIENDA_DATOS_DEMO=false` y cambie las claves antes de usarlo en serio.

## Arquitectura (MVC sin carpeta `service`, ADR-002)
`controller` → `model` (entidades con las reglas de negocio) → `repository` (Spring Data JPA).
`pdf` aísla la API de PDF (facturas y reporte de ventas). `config` tiene la seguridad y los datos iniciales.
Las transacciones se declaran en los controladores; el stock se protege con bloqueo pesimista.

## Reglas clave
IVA 19 %, tallas 34–45, total del pedido = subtotal + IVA (sin costo de envío),
estados PENDIENTE→CONFIRMADO→ENVIADO→ENTREGADO (CANCELADO devuelve stock),
factura solo para pedidos confirmados o posteriores, contraseña de 8+ caracteres con letra y número.

## Documentación SDD
- Constitución, especificación, plan y tareas: `specs/001-tienda-zapatos/`
- Decisiones de arquitectura: `docs/adr/ADR-001.md` a `docs/adr/ADR-006.md`
- Trazabilidad criterio → código → prueba: `docs/trazabilidad.md`
- Tablero de los tres sprints: `docs/tablero.md`
- Actas de Review/Retrospective y decisión de release: `docs/actas/`
