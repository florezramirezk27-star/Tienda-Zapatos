<div align="center">

# STEP · Tienda de Zapatos

**Tienda web de calzado con stock real por talla, pedidos web y presenciales, facturación y reportes en PDF.**

[![Java 21](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.7-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3-005F0F?logo=thymeleaf&logoColor=white)](https://www.thymeleaf.org/)
[![Pruebas](https://img.shields.io/badge/pruebas-82%20en%20verde-2EA44F)](#pruebas)
[![Estado](https://img.shields.io/badge/estado-v1.0%20%C2%B7%20release%20pendiente-F2C744)](#estado-del-proyecto)

[Repositorio](https://github.com/florezramirezk27-star/Tienda-Zapatos) · [Especificación](specs/001-tienda-zapatos/spec.md) · [Trazabilidad](docs/trazabilidad.md)

<img src="src/main/resources/static/img/hero.jpg" alt="STEP Calzado" width="820">

</div>

---

## Descripción

**Tienda de Zapatos (STEP Calzado)** es una aplicación web MVC desarrollada con **Spring Boot 3.5.7 y Java 21** para una tienda pequeña de calzado. Centraliza el catálogo, el inventario por talla, el carrito, los pedidos web y presenciales, los estados del pedido, las facturas en PDF y el reporte de ventas.

El proyecto se construyó como trabajo académico de la **UTS** aplicando **Scrum + SDD (Spec-Driven Development)**: 13 historias de usuario, 38 requisitos funcionales, 46 criterios de aceptación, 6 requisitos no funcionales y 82 pruebas automatizadas distribuidas en tres sprints.

> **Idea central:** que ningún par de zapatos se venda sin existir en el inventario. Todo pedido queda registrado, descuenta stock por talla, lo gestiona el rol correspondiente y se respalda con una factura en PDF.

## Funcionalidades

### Por rol

| Rol | Qué puede hacer |
|---|---|
| **Visitante** | Explorar el catálogo, buscar y filtrar por marca, categoría, talla o precio; registrarse como cliente. |
| **Cliente** | Iniciar sesión, armar su carrito, confirmar pedidos, consultar su historial y descargar sus facturas. |
| **Administrador** | Gestionar usuarios y roles, catálogo, inventario y consultar pedidos. |
| **Gerente** | Consultar pedidos e inventario y generar el reporte de ventas en PDF. |
| **Vendedor** | Registrar ventas presenciales, confirmar, entregar o cancelar pedidos y descargar facturas. |
| **Almacenista** | Gestionar el catálogo y el inventario, y despachar pedidos confirmados. |

### Lo más destacado

- **Inventario real por talla:** cada combinación zapato-talla (34–45) tiene su propio stock y nunca puede quedar negativo.
- **Sin sobreventa:** el stock se descuenta dentro de una transacción con bloqueo pesimista; dos clientes no pueden comprar el último par.
- **Pedidos trazables:** flujo `PENDIENTE → CONFIRMADO → ENVIADO → ENTREGADO`; al cancelar se devuelve el stock.
- **Facturación y reportes PDF:** facturas por pedido y reporte de ventas por rango de fechas con los más vendidos.
- **Seguridad por rol:** Spring Security con cinco roles, BCrypt, CSRF y respuestas 403 para accesos no autorizados.
- **Experiencia responsive:** Thymeleaf + Bootstrap 5 + CSS propio, usable desde computador y celular sin instalar nada.
- **Diseño cuidado:** identidad visual STEP con tipografías Fraunces + DM Sans, catálogo con imágenes y selector de talla.

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.5.7 (Maven) |
| Vistas | Thymeleaf, Bootstrap 5, CSS propio, JavaScript |
| Persistencia | Spring Data JPA / Hibernate 6 |
| Base de datos | PostgreSQL 16 en local; Neon (PostgreSQL en la nube) en Render; H2 en modo PostgreSQL para pruebas |
| Seguridad | Spring Security, BCrypt y protección CSRF |
| PDF | OpenPDF 2.0.3 (paquete aislado `pdf`, ADR-005) |
| Pruebas | JUnit 5, Spring Boot Test, MockMvc, Mockito y H2 |
| Build | Maven 3.9+ |
| Contenedor | Docker multi-stage (Maven + JRE 21) |

## Puesta en marcha

### Requisitos

- JDK 21 y Maven 3.9+
- PostgreSQL local en `localhost:5432` para desarrollo
- Opcional: Docker, si prefieres ejecutar la aplicación en contenedor

### 1. Clonar el repositorio

```bash
git clone https://github.com/florezramirezk27-star/Tienda-Zapatos.git
cd Tienda-Zapatos
```

### 2. Crear la base de datos y las tablas

```bash
createdb -U postgres tienda_zapatos
psql -U postgres -d tienda_zapatos -f database/tienda_zapatos.sql
```

El esquema se valida al arrancar con `spring.jpa.hibernate.ddl-auto=validate`, así que la aplicación no modifica las tablas por su cuenta.

### 3. Configurar las credenciales

Las claves se leen de variables de entorno y **nunca se guardan en el repositorio** (NFR-ZAP-005).

| Variable | Obligatoria | Valor por defecto | Descripción |
|---|---|---|---|
| `DB_URL` | No | `jdbc:postgresql://localhost:5432/tienda_zapatos?stringtype=unspecified` | Cadena JDBC de PostgreSQL |
| `DB_USER` | No | `postgres` | Usuario de la base de datos |
| `DB_PASSWORD` | No | *(vacío)* | Contraseña de la base de datos |
| `TIENDA_DATOS_DEMO` | No | `true` | Carga usuarios y zapatos de demostración al primer arranque |

**PowerShell:**

```powershell
$env:DB_USER = "postgres"
$env:DB_PASSWORD = "tu_clave"
```

**Bash:**

```bash
export DB_USER=postgres
export DB_PASSWORD=tu_clave
```

En STS/Eclipse: `Run Configurations → Environment → DB_USER y DB_PASSWORD`.

### 4. Arrancar la aplicación

```bash
mvn spring-boot:run
```

Abre [http://localhost:8080](http://localhost:8080) en el navegador.

> Verificado el 25 de noviembre de 2026: esquema aplicado y arranque con `ddl-auto=validate` correctos contra PostgreSQL 16.15.

### Alternativa con Docker

```bash
docker build -t tienda-zapatos .
docker run --rm -p 8080:8080 \
  -e DB_URL="jdbc:postgresql://host:5432/tienda_zapatos?sslmode=require&stringtype=unspecified" \
  -e DB_USER="usuario" \
  -e DB_PASSWORD="clave" \
  -e TIENDA_DATOS_DEMO=false \
  tienda-zapatos
```

## Usuarios de demostración

Con `TIENDA_DATOS_DEMO=true` (valor por defecto en local) se crean al primer arranque. La contraseña para todos es **`Zapatos2026`**.

| Rol | Correo |
|---|---|
| Administrador | `admin@tienda.test` |
| Gerente | `gerente@tienda.test` |
| Vendedor | `vendedor@tienda.test` |
| Almacenista | `almacenista@tienda.test` |
| Cliente | `cliente@tienda.test` |

Además se cargan 14 zapatos de ejemplo. En producción usa `TIENDA_DATOS_DEMO=false` y cambia las claves antes de usar el sistema en serio.

## Pruebas

```bash
mvn test
```

La suite tiene **82 pruebas** y se ejecuta contra **H2 en memoria en modo PostgreSQL**, por lo que no necesita un servidor PostgreSQL ni toca la base real.

- **46/46 criterios de aceptación** tienen al menos una prueba automática.
- Se verifican reglas de negocio, seguridad por rol, CSRF, concurrencia del stock y generación de PDF.
- La matriz completa criterio → código → prueba está en [`docs/trazabilidad.md`](docs/trazabilidad.md).

Para empaquetar el JAR:

```bash
mvn -B clean package
java -jar target/tienda-zapatos-1.0.0.jar
```

## Despliegue en Render con Neon

La misma aplicación usa **Neon en Render** y **PostgreSQL local en STS**; no son dos bases compitiendo, sino dos entornos con el mismo esquema.

1. Ejecuta [`database/tienda_zapatos_neon.sql`](database/tienda_zapatos_neon.sql) en el **SQL Editor** de Neon. Ese script crea el esquema en `public` y es el que valida `ddl-auto=validate` en producción.
2. En Render → **Environment**, configura:

   | Variable | Valor |
   |---|---|
   | `DB_URL` | Cadena JDBC de Neon, por ejemplo `jdbc:postgresql://<host>/<db>?sslmode=require&stringtype=unspecified` |
   | `DB_USER` | Usuario de Neon |
   | `DB_PASSWORD` | Contraseña de Neon |
   | `TIENDA_DATOS_DEMO` | `false` (para no cargar usuarios de prueba en producción) |

3. **Build:** `mvn -B clean package`
4. **Start:** `java -jar target/tienda-zapatos-1.0.0.jar`

Si `DB_URL` no está definida, la aplicación usa `jdbc:postgresql://localhost:5432/tienda_zapatos`. Así el mismo JAR sirve para local y para Render sin cambiar código ni guardar claves en el repositorio.

## Arquitectura

```text
src/main/java/com/tiendazapatos/
├── config/       Seguridad, datos iniciales y variables para las vistas
├── controller/   Rutas MVC y límites transaccionales
├── form/         Formularios y validación de entrada
├── model/        Entidades JPA y reglas de negocio
├── repository/   Spring Data JPA y especificaciones de filtros
├── pdf/          Facturas y reportes con OpenPDF
└── util/         Dinero y política de contraseñas

src/main/resources/
├── templates/    Vistas Thymeleaf (catálogo, carrito, admin, almacén, reportes…)
└── static/       CSS, JavaScript, fuentes, imágenes y Bootstrap
```

El flujo es **`controller → model → repository`**. Siguiendo el ADR-002 **no existe una carpeta `service`**: las reglas de negocio viven en las entidades, los controladores declaran las transacciones y los repositorios encapsulan el acceso a datos. El stock se protege con bloqueo pesimista y la API de PDF se aísla en el paquete `pdf` (ADR-005).

## Reglas de negocio

| Regla | Detalle |
|---|---|
| Stock por talla | Cada combinación zapato-talla tiene su propio stock y nunca puede ser negativo. Tallas válidas: 34–45. |
| IVA y total | IVA del 19 % sobre el subtotal. Total = subtotal + IVA, sin costo de envío. Precios en COP. |
| Estados del pedido | `PENDIENTE → CONFIRMADO → ENVIADO → ENTREGADO`; `CANCELADO` devuelve el stock. |
| Cancelación | Solo se cancelan pedidos `PENDIENTE` o `CONFIRMADO`. |
| Facturación | La factura solo existe para pedidos `CONFIRMADO`, `ENVIADO` o `ENTREGADO`. |
| Venta presencial | Nace en estado `ENTREGADO` y puede registrarse como «Cliente de mostrador». |
| Catálogo | Un zapato desactivado se oculta del catálogo, pero conserva su historial de ventas. |
| Privacidad | Un cliente solo ve y descarga lo que pertenece a sus propios pedidos. |
| Usuarios | Un rol por usuario y siempre debe existir al menos un administrador activo. |
| Reportes | Solo cuentan pedidos `CONFIRMADO`, `ENVIADO` y `ENTREGADO`; excluyen `PENDIENTE` y `CANCELADO`. |
| Contraseñas | Mínimo 8 caracteres con al menos una letra y un número; se almacenan cifradas con BCrypt. |

## Documentación SDD

| Documento | Contenido |
|---|---|
| [`specs/001-tienda-zapatos/spec.md`](specs/001-tienda-zapatos/spec.md) | Especificación: 13 historias, 38 requisitos, 46 criterios, 6 NFR y 10 reglas de negocio |
| [`specs/001-tienda-zapatos/plan.md`](specs/001-tienda-zapatos/plan.md) | Plan técnico, arquitectura y fases |
| [`specs/001-tienda-zapatos/tasks.md`](specs/001-tienda-zapatos/tasks.md) | Desglose de tareas por sprint |
| [`specs/001-tienda-zapatos/constitution.md`](specs/001-tienda-zapatos/constitution.md) | Constitución y acuerdos del proyecto |
| [`docs/trazabilidad.md`](docs/trazabilidad.md) | Matriz criterio → código → prueba |
| [`docs/tablero.md`](docs/tablero.md) | Estado de los tres sprints y del product backlog |
| [`docs/actas/`](docs/actas/) | Actas de Review/Retrospective y decisión de release |

### Decisiones de arquitectura (ADRs)

- [ADR-001](docs/adr/ADR-001.md) — MVC con Thymeleaf para las pantallas.
- [ADR-002](docs/adr/ADR-002.md) — Sin capa `service`: reglas en las entidades.
- [ADR-003](docs/adr/ADR-003.md) — MySQL 8 con JPA y esquema validado (reemplazado por ADR-006).
- [ADR-004](docs/adr/ADR-004.md) — Seguridad por rol con Spring Security.
- [ADR-005](docs/adr/ADR-005.md) — PDF aislado en el paquete `pdf`.
- [ADR-006](docs/adr/ADR-006.md) — Cambio de MySQL 8 a PostgreSQL.

## Estado del proyecto

| Aspecto | Estado |
|---|---|
| Historias de usuario | 13/13 implementadas |
| Requisitos funcionales | 38/38 cubiertos |
| Criterios de aceptación con prueba | 46/46 |
| Suite de pruebas | 82 pruebas en verde |
| Esquema PostgreSQL | Validado con `ddl-auto=validate` en PostgreSQL 16.15 (25 nov 2026) |
| Release v1.0 | Pendiente de aprobación del Product Owner |
| NFR-ZAP-001 (catálogo < 2 s con 100 zapatos) | Medición formal pendiente |

El detalle de los pendientes y la recomendación técnica del equipo están en [`docs/actas/decision-release.md`](docs/actas/decision-release.md).

## Nota sobre la base de datos

El documento fuente del trabajo exigía **MySQL 8** (NFR-ZAP-004). El 25 de noviembre de 2026 el usuario decidió usar **PostgreSQL** para aprovechar el servicio local disponible y desplegar en Neon/Render. El cambio está registrado en [ADR-006](docs/adr/ADR-006.md) y NFR-ZAP-004 se reporta como **desviación autorizada**, no como cumplimiento literal del texto original.

- **Desarrollo:** PostgreSQL local en `localhost:5432`, base `tienda_zapatos`.
- **Producción:** Neon (PostgreSQL en la nube) configurado desde Render.
- **Pruebas:** H2 en memoria en modo PostgreSQL, sin tocar ninguna base real.

## Autores

| Nombre | Rol |
|---|---|
| Kevin Andréy Flórez Ramírez | Desarrollo |
| Juan Miguel Corales | Desarrollo / Scrum Master |
| Julián Barney Jaimes Rincón | Product Owner (docente) |

Proyecto académico de la **UTS** — 2026.
