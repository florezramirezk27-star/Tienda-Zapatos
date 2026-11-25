# Acta — Sprint Review 1

> **BORRADOR.** Esta acta es una plantilla. No afirma que la reunión haya ocurrido ni inventa asistentes, demostraciones o decisiones. Se completa y firma después del evento real.

| Campo | Valor |
|---|---|
| Evento | Sprint Review 1 |
| Fecha prevista | 20 de noviembre de 2026 |
| Hora | Por acordar con el docente |
| Duración | 45 min |
| Lugar / enlace | Por definir |
| Sprint | 1 (17–20 de noviembre de 2026) |
| Meta del sprint | Especificación v1.0 aprobada y los 5 roles con acceso según su menú; usuarios y catálogo operativos. |
| Puntos comprometidos | 13 |
| Estado del acta | Borrador — pendiente de completar |

## Asistentes

| Nombre | Rol Scrum | Asistió | Firma |
|---|---|---|---|
| Julián Barney Jaimes Rincón | Product Owner (docente) | ☐ | |
| Kevin Andréy Flórez Ramírez | Developer | ☐ | |
| Juan Miguel Corales | Scrum Master / Developer | ☐ | |
| | Invitado | ☐ | |

## Orden del día

1. Saludo y objetivo de la Review (5 min).
2. Demostración del incremento (25 min).
3. Revisión del Product Backlog y feedback del PO (10 min).
4. Acuerdos y próximos pasos (5 min).

## Incremento a demostrar

| Historia | Guion de demostración | Criterios que se muestran | Estado técnico |
|---|---|---|---|
| HU-01 | Iniciar sesión con cada rol y cerrar sesión; volver con «atrás». | AC-001.1, AC-002.1, AC-002.2, AC-003.1 | Implementada y probada |
| HU-02 | Registro válido, correo duplicado y contraseña corta. | AC-004.1, AC-005.1, AC-006.1, AC-006.2 | Implementada y probada |
| HU-03 | Crear un VENDEDOR, desactivar un ALMACENISTA, intentar 403 y último administrador. | AC-007.1, AC-008.1, AC-009.1, AC-010.1 | Implementada y probada |
| HU-04 | Crear zapato con referencia y 12 tallas, precio 0, desactivar conservando venta. | AC-011.1, AC-012.1, AC-013.1 | Implementada y probada |
| E-01 | Mostrar constitución, especificación, plan y tareas. | Puerta 1 | Documentada |

## Evidencia técnica disponible

- Suite: **82 pruebas, 0 fallos, 0 errores** al 25 de noviembre de 2026 (`mvn -B test`).
- Criterios del Sprint 1 con prueba: 15/15.
- Estructura sin carpeta `service`, sin MongoDB y con PostgreSQL configurado (ADR-006; el DOCX pedía MySQL 8).
- Documentos: `specs/001-tienda-zapatos/constitution.md`, `spec.md`, `plan.md`, `tasks.md`.

Esta evidencia es un insumo para la demostración; no sustituye la reunión.

## Resultados de la demostración

| Punto | Resultado observado | Aceptado por el PO |
|---|---|---|
| Login de los 5 roles | | ☐ |
| Registro y validaciones | | ☐ |
| Gestión de usuarios y 403 | | ☐ |
| Catálogo y desactivación | | ☐ |
| Especificación v1.0 | | ☐ |

## Feedback y decisiones del Product Owner

> Espacio para escribir textualmente lo que diga el PO. No completar con suposiciones.

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
