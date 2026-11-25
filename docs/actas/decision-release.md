# Decisión de Release — Tienda de Zapatos v1.0

> **PENDIENTE.** Este documento no declara aprobado el release. Registra el estado de los criterios y deja la decisión al Product Owner después de la Sprint Review 3. No se inventa una aprobación.

| Campo | Valor |
|---|---|
| Producto | Tienda de Zapatos (STEP Calzado) |
| Versión candidata | 1.0 |
| Fecha de evaluación | 25 de noviembre de 2026 |
| Fecha prevista de decisión | 4 de diciembre de 2026 |
| Product Owner | Docente Julián Barney Jaimes Rincón |
| Estado | **Pendiente de decisión** |

## 1. Checklist de release

| # | Criterio | Evidencia | Estado |
|---|---|---|---|
| 1 | 13 historias implementadas | Código y `docs/tablero.md` | Cumple |
| 2 | 38 requisitos funcionales cubiertos | `spec.md` | Cumple |
| 3 | 46 criterios con al menos una prueba automática | `docs/trazabilidad.md` | Cumple |
| 4 | Suite completa verde | 82 pruebas, 0 fallos, 0 errores | Cumple |
| 5 | 6 NFR verificados | `plan.md`, pruebas y revisión | Cumple con medición NFR-001 pendiente |
| 6 | 10 reglas de negocio implementadas | Entidades y controladores | Cumple |
| 7 | 5 roles con rutas protegidas y 403 | `SecurityConfig`, pruebas | Cumple |
| 8 | Sin carpeta ni paquete `service` | Revisión de estructura | Cumple |
| 9 | Sin MongoDB; JPA con base de datos | `pom.xml` (`org.postgresql:postgresql`), `application.properties` | **Desviación (ADR-006):** PostgreSQL en lugar del MySQL 8 del DOCX |
| 10 | PDF aislado en el paquete `pdf` | `GeneradorPdf` | Cumple |
| 11 | Contraseñas BCrypt y sin claves en logs | `DatosIniciales`, revisión | Cumple |
| 12 | Esquema ejecutado en PostgreSQL real (ADR-006) | Arranque con `validate` OK en PostgreSQL 16.15, 25 nov 2026 | **Hecho** |
| 13 | Revisión de par y PR por historia | — | **Pendiente** |
| 14 | Actas de Review y Retrospective firmadas | `docs/actas/` | **Pendiente** |
| 15 | NFR-ZAP-001 medido con 100 zapatos | — | **Pendiente** |
| 16 | Aprobación del Product Owner | — | **Pendiente** |

## 2. Riesgos residuales

| Riesgo | Impacto | Mitigación antes del release | Responsable |
|---|---|---|---|
| El script no coincide con las entidades en PostgreSQL real | Alto | Verificado: arranque con `validate` OK en PostgreSQL 16.15 | Kevin |
| No hay revisión de par registrada | Medio | Revisar los cambios y dejar constancia del PR | Kevin y Corales |
| NFR-001 sin medición formal | Bajo | Cargar 100 zapatos y medir en navegador | Corales |
| Actas sin firmar | Medio | Completar y firmar en la Review 3 | PO y equipo |

## 3. Recomendación técnica del equipo

> El equipo considera que el incremento está **funcionalmente completo y probado** en el entorno de pruebas (H2 en modo PostgreSQL) y que el esquema quedó validado contra PostgreSQL 16.15. La recomendación es **aprobar el release condicionado** a completar la revisión de par y las actas, y a que el PO acepte o rechace la desviación ADR-006 del MySQL 8 exigido en el DOCX. La decisión final corresponde al Product Owner.

Esta recomendación es del equipo de desarrollo, no una aprobación del PO.

## 4. Decisión del Product Owner

Marcar una sola opción cuando la decisión sea real:

- ☐ **Aprobado** para publicación.
- ☐ **Aprobado con condiciones** (describirlas abajo).
- ☐ **Rechazado**; vuelve al Product Backlog.

Condiciones o motivo:

> 
 
## 5. Firma

| Nombre | Rol | Fecha | Firma |
|---|---|---|---|
| Julián Barney Jaimes Rincón | Product Owner | | |
| Kevin Andréy Flórez Ramírez | Developer | | |
| Juan Miguel Corales | Scrum Master / Developer | | |
