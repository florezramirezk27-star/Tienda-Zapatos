# Especificación de Requisitos — Tienda de Zapatos v1.0

| Campo | Valor |
|---|---|
| Producto | Tienda de Zapatos (STEP Calzado) |
| Especificación | `001-tienda-zapatos` |
| Versión | 1.0 |
| Fecha | 25 de noviembre de 2026 |
| Estado | Aprobable — Puerta 1 |
| Autores | Kevin Andréy Flórez Ramírez y Juan Miguel Corales |
| Product Owner | Docente Julián Barney Jaimes Rincón |
| Base normativa | ISO/IEC/IEEE 29148:2018, EARS y BDD |
| Historias | 13 |
| Requisitos funcionales | 38 |
| Criterios de aceptación | 46 |
| Requisitos no funcionales | 6 |
| Reglas de negocio | 10 |
| Roles | 5 |

---

## 1. Contexto y problema

Una tienda pequeña de calzado vende por internet y en su punto de venta. No tiene certeza de cuántos pares quedan de cada talla, quién confirmó un pedido ni si el cliente recibió su comprobante. El resultado es sobreventa, inventario desactualizado y clientes sin respuesta.

Tienda de Zapatos centraliza el catálogo, el inventario por talla, el carrito, los pedidos web y presenciales, los estados del pedido, las facturas y el reporte de ventas. El sistema se usa desde el navegador de un computador o de un celular, sin instalar una aplicación.

## 2. Objetivo del producto

> «Que ningún par de zapatos se venda sin existir en el inventario: todo pedido queda registrado, descuenta el stock por talla, lo gestiona el rol que corresponde y se respalda con una factura en PDF.»

El trabajo se ejecuta en tres sprints de una semana, del 17 de noviembre al 4 de diciembre de 2026. Los datos son ficticios y el tratamiento de datos personales sigue la Ley 1581 de 2012.

## 3. Alcance

### Incluido

- Catálogo público con paginación, búsqueda y filtros.
- Registro de clientes, inicio y cierre de sesión.
- Gestión de usuarios y de los 5 roles.
- Gestión del catálogo de zapatos y de la imagen.
- Inventario por talla con entradas, ajustes y bitácora.
- Carrito en la sesión del cliente.
- Pedido web con IVA, dirección, método de pago y descuento de stock.
- Venta presencial con cliente de mostrador.
- Cambio de estado y cancelación con devolución de stock.
- Historial y detalle de pedidos del cliente.
- Factura en PDF y reporte de ventas en PDF.
- Seguridad por rol, CSRF, BCrypt y protección de datos.

### Fuera de alcance

- Aplicación móvil nativa Android o iOS.
- Pasarela de pagos real; el pago se registra como contra entrega o transferencia informativa.
- Integración con empresas de transporte o cálculo de fletes.
- Facturación electrónica ante la DIAN.
- Notificaciones por correo o SMS.
- Multiempresa, multimoneda o idiomas distintos del español.

## 4. Actores y roles

| Rol | Descripción | Permisos principales |
|---|---|---|
| Administrador | Controla usuarios y roles | Gestionar usuarios, catálogo, inventario y consultar pedidos |
| Gerente | Analiza las ventas | Consultar pedidos e inventario y generar el reporte de ventas |
| Vendedor | Atiende el punto de venta | Registrar ventas presenciales, confirmar/entregar pedidos y descargar facturas |
| Almacenista | Administra bodega y catálogo | Gestionar catálogo e inventario y enviar pedidos confirmados |
| Cliente | Compra en línea | Catálogo, carrito, pedido, historial y facturas propias |
| Visitante | Persona sin cuenta | Ver catálogo y registrarse; no es un rol del sistema |

### Matriz de permisos

| Función | Admin. | Gerente | Vendedor | Almacenista | Cliente |
|---|---:|---:|---:|---:|---:|
| Ver catálogo y filtrar (HU-05) | Sí | Sí | Sí | Sí | Sí |
| Gestionar usuarios y roles (HU-03) | Sí | — | — | — | — |
| Gestionar catálogo (HU-04) | Sí | — | — | Sí | — |
| Gestionar inventario (HU-06) | — | — | — | Sí | — |
| Carrito y pedido (HU-07, HU-08) | — | — | — | — | Sí |
| Venta presencial (HU-09) | — | — | Sí | — | — |
| Cambiar estado de pedidos (HU-10) | — | — | Sí | Sí | — |
| Ver mis pedidos (HU-11) | — | — | — | — | Sí |
| Descargar factura PDF (HU-12) | — | — | Sí | — | Sí (solo propias) |
| Reporte de ventas PDF (HU-13) | — | Sí | — | — | — |

Cualquier acción fuera de esta matriz se rechaza con HTTP 403.

## 5. Glosario

| Término | Definición |
|---|---|
| Talla | Numeración europea de calzado, de 34 a 45. Cada combinación zapato-talla tiene su propio stock. |
| Stock | Pares disponibles de una talla. Nunca puede ser negativo. |
| Pedido web | Compra hecha por un cliente autenticado; nace `PENDIENTE`. |
| Venta presencial | Pedido con canal `PRESENCIAL`; nace `ENTREGADO` y puede no tener cliente registrado. |
| Cliente de mostrador | Comprador sin cuenta, asociado al texto «Cliente de mostrador». |
| Facturable | Pedido `CONFIRMADO`, `ENVIADO` o `ENTREGADO`. |
| IVA | Impuesto del 19 % sobre el subtotal. |
| Total | Subtotal + IVA. No incluye costos de envío. |
| Movimiento | Registro de entrada, ajuste, venta o devolución de inventario. |
| DoR / DoD | Definition of Ready / Definition of Done. |

---

## 6. Requisitos funcionales y criterios de aceptación

Los requisitos se numeran `REQ-ZAP-001` a `REQ-ZAP-038` y los criterios `AC-NNN.M`. Cada criterio es verificable con una prueba automática.

## Sprint 1

### E-01 Habilitador: constitución, especificación y base del proyecto

**Puntos:** 3 · **Sprint:** 1 · **Hace:** Corales y Kevin · **Valida:** Docente (PO)

Se considera terminado cuando la especificación está aprobada, el proyecto Spring Boot compila, se conecta a PostgreSQL (desviación ADR-006; el DOCX pedía MySQL) y tiene la estructura MVC sin carpeta `service`. No tiene criterios funcionales propios; se verifica con la compilación, el arranque y la lista de cumplimiento de la constitución.

### HU-01 Iniciar y cerrar sesión

**Rol:** Administrador, Gerente, Vendedor, Almacenista y Cliente · **Sprint:** 1 · **Puntos:** 2 · **Hace:** Kevin · **Valida:** Corales

**Historia.** Como usuario registrado quiero iniciar y cerrar sesión con mi correo y contraseña para acceder solo a las opciones que mi rol permite.

**Requisitos EARS**

| ID | Patrón | Requisito |
|---|---|---|
| REQ-ZAP-001 | Evento | Cuando un usuario envía un correo y una contraseña válidos, el sistema deberá iniciar su sesión y mostrar el menú de su rol. |
| REQ-ZAP-002 | No deseado | Si las credenciales no son válidas, entonces el sistema deberá rechazar el acceso con el mensaje «Correo o contraseña incorrectos», sin indicar cuál dato falló. |
| REQ-ZAP-003 | Evento | Cuando el usuario cierra sesión, el sistema deberá invalidar la sesión y redirigirlo al catálogo público. |

**Criterios de aceptación**

**AC-001.1 Inicio de sesión exitoso**
- Dado un vendedor activo con su correo y contraseña correctos.
- Cuando envía el formulario de inicio de sesión.
- Entonces el sistema muestra el panel de ventas.
- Y el menú solo contiene las opciones del rol Vendedor.

**AC-002.1 Contraseña incorrecta**
- Dado un usuario registrado y activo.
- Cuando envía una contraseña incorrecta.
- Entonces el sistema permanece en la pantalla de inicio de sesión.
- Y muestra «Correo o contraseña incorrectos».

**AC-002.2 Cuenta desactivada (borde)**
- Dado un usuario con estado INACTIVO.
- Cuando envía su correo y contraseña correctos.
- Entonces el sistema rechaza el acceso con el mismo mensaje genérico.

**AC-003.1 Cierre de sesión**
- Dada una sesión iniciada.
- Cuando el usuario pulsa «Cerrar sesión».
- Entonces el sistema lo lleva al catálogo público.
- Y al volver con el botón «atrás» le pide iniciar sesión.

### HU-02 Registrarme como cliente

**Rol:** Visitante (futuro Cliente) · **Sprint:** 1 · **Puntos:** 2 · **Hace:** Corales · **Valida:** Kevin

**Historia.** Como visitante quiero registrarme con mis datos para poder comprar zapatos en la tienda.

**Requisitos EARS**

| ID | Patrón | Requisito |
|---|---|---|
| REQ-ZAP-004 | Evento | Cuando un visitante envía nombre, correo, teléfono y contraseña válidos, el sistema deberá crear una cuenta con el rol CLIENTE. |
| REQ-ZAP-005 | No deseado | Si el correo ya está registrado, entonces el sistema deberá rechazar el registro e indicar que el correo ya existe. |
| REQ-ZAP-006 | Ubicuo | El sistema deberá almacenar las contraseñas cifradas con BCrypt y exigir un mínimo de 8 caracteres con al menos una letra y un número. |

**Criterios de aceptación**

**AC-004.1 Registro exitoso**
- Dado un visitante sin cuenta.
- Cuando envía el formulario con datos válidos.
- Entonces el sistema crea un usuario con rol CLIENTE y estado ACTIVO.
- Y lo lleva a la pantalla de inicio de sesión.

**AC-005.1 Correo duplicado**
- Dado un cliente ya registrado con el correo `ana@correo.test`.
- Cuando otro visitante intenta registrarse con ese mismo correo.
- Entonces el sistema rechaza el registro.
- Y muestra «El correo ya está registrado».

**AC-006.1 Contraseña demasiado corta**
- Dado un formulario con la contraseña «abc123» (6 caracteres).
- Cuando el visitante lo envía.
- Entonces el sistema lo rechaza.
- Y muestra «La contraseña debe tener mínimo 8 caracteres, con letras y números».

**AC-006.2 Contraseña en el límite (borde)**
- Dado un formulario con la contraseña «abcdefg1» (8 caracteres, letras y un número).
- Cuando el visitante lo envía.
- Entonces el sistema acepta la contraseña.
- Y la guarda cifrada y no en texto plano.

### HU-03 Gestionar usuarios y roles

**Rol:** Administrador · **Sprint:** 1 · **Puntos:** 3 · **Hace:** Kevin · **Valida:** Corales

**Historia.** Como administrador quiero crear usuarios, asignarles uno de los 5 roles y desactivar cuentas para controlar quién puede hacer qué en el sistema.

**Requisitos EARS**

| ID | Patrón | Requisito |
|---|---|---|
| REQ-ZAP-007 | Evento | Cuando el administrador crea un usuario, el sistema deberá asignarle exactamente uno de los 5 roles: ADMINISTRADOR, GERENTE, VENDEDOR, ALMACENISTA o CLIENTE. |
| REQ-ZAP-008 | Evento | Cuando el administrador desactiva una cuenta, el sistema deberá impedir su inicio de sesión sin borrar su historial. |
| REQ-ZAP-009 | No deseado | Si un usuario que no tiene el rol ADMINISTRADOR intenta entrar a la gestión de usuarios, entonces el sistema deberá rechazar la acción con el código HTTP 403. |
| REQ-ZAP-010 | No deseado | Si el administrador intenta desactivar la única cuenta de administrador activa, entonces el sistema deberá rechazar la acción. |

**Criterios de aceptación**

**AC-007.1 Crear un vendedor**
- Dado un administrador con sesión iniciada.
- Cuando crea un usuario eligiendo el rol VENDEDOR.
- Entonces el usuario queda guardado con ese rol.
- Y puede iniciar sesión y ve solo el menú del Vendedor.

**AC-008.1 Desactivar una cuenta**
- Dado un almacenista activo con pedidos despachados en su historial.
- Cuando el administrador lo desactiva.
- Entonces el almacenista ya no puede iniciar sesión.
- Y sus movimientos de inventario siguen visibles.

**AC-009.1 Acceso no autorizado**
- Dado un almacenista con sesión iniciada.
- Cuando intenta abrir la pantalla de gestión de usuarios.
- Entonces el sistema responde con HTTP 403.

**AC-010.1 Único administrador (borde)**
- Dado un solo administrador activo en el sistema.
- Cuando intenta desactivar su propia cuenta.
- Entonces el sistema rechaza la acción.
- Y muestra «Debe existir al menos un administrador activo».

### HU-04 Gestionar el catálogo de zapatos

**Rol:** Almacenista y Administrador · **Sprint:** 1 · **Puntos:** 3 · **Hace:** Corales · **Valida:** Kevin

**Historia.** Como almacenista quiero registrar, editar y desactivar zapatos con su marca, categoría, color, precio e imagen para mantener el catálogo actualizado.

**Requisitos EARS**

| ID | Patrón | Requisito |
|---|---|---|
| REQ-ZAP-011 | Evento | Cuando un Administrador o un Almacenista guarda un zapato con nombre, marca, categoría, color y precio válidos, el sistema deberá crearlo con una referencia única. |
| REQ-ZAP-012 | No deseado | Si el precio es menor o igual a cero, entonces el sistema deberá rechazar el guardado e indicar el error en el campo precio. |
| REQ-ZAP-013 | Evento | Cuando un Administrador o un Almacenista desactiva un zapato, el sistema deberá ocultarlo del catálogo público y conservar sus ventas históricas. |

**Criterios de aceptación**

**AC-011.1 Crear un zapato**
- Dado un almacenista con sesión iniciada.
- Cuando guarda un zapato con nombre, marca, categoría, color y precio de $120.000.
- Entonces el sistema lo crea con una referencia única.
- Y aparece en la lista de zapatos del almacén.

**AC-012.1 Precio en el límite (borde)**
- Dado el formulario de un zapato con todos los datos correctos.
- Cuando el precio es 0.
- Entonces el sistema rechaza el guardado.
- Y muestra «El precio debe ser mayor que cero» (con precio 1 el guardado es aceptado).

**AC-013.1 Desactivar un zapato**
- Dado un zapato activo que ya tiene ventas registradas.
- Cuando el almacenista lo desactiva.
- Entonces el zapato deja de aparecer en el catálogo público.
- Y los pedidos anteriores que lo incluyen no cambian.

## Sprint 2

### HU-05 Consultar el catálogo con filtros

**Rol:** Visitante y Cliente · **Sprint:** 2 · **Puntos:** 3 · **Hace:** Corales · **Valida:** Kevin

**Historia.** Como visitante o cliente quiero ver el catálogo y filtrarlo por marca, categoría, talla y precio para encontrar rápido los zapatos que me interesan.

**Requisitos EARS**

| ID | Patrón | Requisito |
|---|---|---|
| REQ-ZAP-014 | Ubicuo | El sistema deberá mostrar el catálogo público de zapatos activos con imagen, nombre, marca y precio, paginado de 12 en 12. |
| REQ-ZAP-015 | Evento | Cuando el usuario aplica filtros de marca, categoría, talla o rango de precio, el sistema deberá mostrar solo los zapatos que cumplan todos los filtros aplicados. |
| REQ-ZAP-016 | No deseado | Si ningún zapato cumple los filtros, entonces el sistema deberá mostrar el mensaje «No encontramos zapatos con esos filtros». |

**Criterios de aceptación**

**AC-014.1 Paginación (borde)**
- Dados 13 zapatos activos en el catálogo.
- Cuando el visitante abre el catálogo.
- Entonces la primera página muestra 12 zapatos.
- Y la segunda página muestra 1 zapato.

**AC-015.1 Filtro combinado**
- Dados zapatos de las marcas Nike y Adidas en varias tallas.
- Cuando el usuario filtra por marca Nike y talla 40.
- Entonces solo aparecen zapatos Nike que tengan la talla 40 con stock.

**AC-016.1 Sin resultados**
- Dado que ningún zapato de la marca Puma tiene precio menor a $50.000.
- Cuando el usuario aplica ese filtro.
- Entonces el sistema muestra «No encontramos zapatos con esos filtros».

### HU-06 Gestionar el inventario por talla

**Rol:** Almacenista · **Sprint:** 2 · **Puntos:** 3 · **Hace:** Kevin · **Valida:** Corales

**Historia.** Como almacenista quiero registrar entradas y ajustes de mercancía por talla para que el stock de la tienda sea siempre correcto.

**Requisitos EARS**

| ID | Patrón | Requisito |
|---|---|---|
| REQ-ZAP-017 | Evento | Cuando el almacenista registra una entrada de mercancía con zapato, talla y una cantidad mayor que cero, el sistema deberá sumar esa cantidad al stock de la talla y guardar el movimiento. |
| REQ-ZAP-018 | No deseado | Si un ajuste dejaría el stock de una talla en un valor negativo, entonces el sistema deberá rechazar el ajuste. |
| REQ-ZAP-019 | Estado | Mientras el stock de una talla sea 0, el sistema deberá mostrarla como «Agotada» en el catálogo y no permitir agregarla al carrito. |

**Criterios de aceptación**

**AC-017.1 Entrada de mercancía**
- Dado un zapato con 4 pares en talla 40.
- Cuando el almacenista registra una entrada de 10 pares en talla 40.
- Entonces el stock de la talla 40 queda en 14.
- Y se guarda un movimiento con usuario, fecha y cantidad.

**AC-018.1 Ajuste que deja stock negativo**
- Dada una talla con stock 4.
- Cuando el almacenista registra un ajuste de -10.
- Entonces el sistema rechaza el ajuste.
- Y el stock sigue en 4.

**AC-018.2 Ajuste hasta cero (borde)**
- Dada una talla con stock 4.
- Cuando el almacenista registra un ajuste de -4.
- Entonces el sistema lo acepta.
- Y el stock queda en 0.

**AC-019.1 Talla agotada**
- Dada una talla con stock 0.
- Cuando un cliente ve el zapato en el catálogo.
- Entonces la talla aparece como «Agotada».
- Y el botón de agregar al carrito está deshabilitado para esa talla.

### HU-07 Armar mi carrito de compras

**Rol:** Cliente · **Sprint:** 2 · **Puntos:** 3 · **Hace:** Corales · **Valida:** Kevin

**Historia.** Como cliente quiero agregar zapatos con su talla y cantidad a un carrito para revisar mi compra antes de pedir.

**Requisitos EARS**

| ID | Patrón | Requisito |
|---|---|---|
| REQ-ZAP-020 | Evento | Cuando el cliente agrega un zapato con talla y cantidad al carrito, el sistema deberá guardarlo en el carrito de su sesión y mostrar el subtotal. |
| REQ-ZAP-021 | No deseado | Si la cantidad solicitada supera el stock de la talla, entonces el sistema deberá rechazar la adición e indicar el stock disponible. |
| REQ-ZAP-022 | Evento | Cuando el cliente elimina un ítem o cambia su cantidad, el sistema deberá recalcular el total del carrito. |

**Criterios de aceptación**

**AC-020.1 Agregar al carrito**
- Dado un cliente con sesión iniciada y un zapato de $120.000 con stock suficiente.
- Cuando agrega 2 pares en talla 39.
- Entonces el carrito muestra el ítem con cantidad 2.
- Y el subtotal es $240.000.

**AC-021.1 Cantidad mayor al stock**
- Dada una talla con stock 3.
- Cuando el cliente intenta agregar 4 pares.
- Entonces el sistema rechaza la adición.
- Y muestra «Disponible: 3».

**AC-021.2 Cantidad igual al stock (borde)**
- Dada una talla con stock 3.
- Cuando el cliente agrega 3 pares.
- Entonces el sistema acepta la adición.

**AC-022.1 Eliminar un ítem**
- Dado un carrito con dos ítems.
- Cuando el cliente elimina uno.
- Entonces el carrito muestra solo el otro ítem.
- Y el total se recalcula.

### HU-08 Realizar un pedido

**Rol:** Cliente · **Sprint:** 2 · **Puntos:** 4 · **Hace:** Kevin · **Valida:** Corales

**Historia.** Como cliente quiero confirmar mi carrito con una dirección de entrega y un método de pago para que la tienda prepare mi pedido.

**Requisitos EARS**

| ID | Patrón | Requisito |
|---|---|---|
| REQ-ZAP-023 | Evento | Cuando el cliente confirma un carrito con dirección de entrega y método de pago, el sistema deberá crear el pedido en estado PENDIENTE con un número único y descontar el stock de cada talla. |
| REQ-ZAP-024 | No deseado | Si algún ítem ya no tiene stock suficiente al confirmar, entonces el sistema deberá rechazar el pedido completo y dejar el stock sin cambios. |
| REQ-ZAP-025 | Ubicuo | El sistema deberá calcular el IVA del 19 % sobre el subtotal y mostrar el total del pedido antes de confirmarlo. |

**Criterios de aceptación**

**AC-023.1 Pedido exitoso**
- Dado un cliente con 2 pares en talla 39 en su carrito.
- Cuando confirma con dirección de entrega y pago contra entrega.
- Entonces el sistema crea un pedido PENDIENTE con número único.
- Y el stock de la talla 39 baja en 2 y el carrito queda vacío.

**AC-023.2 Carrito vacío (borde)**
- Dado un cliente con el carrito vacío.
- Cuando intenta confirmar el pedido.
- Entonces el sistema rechaza la confirmación.
- Y muestra «Tu carrito está vacío».

**AC-024.1 Dos clientes por el último par**
- Dada una talla con stock 1 y dos clientes con ese par en su carrito.
- Cuando ambos confirman casi al mismo tiempo.
- Entonces solo uno de los pedidos se crea.
- Y el otro recibe «Stock insuficiente» y el stock queda en 0.

**AC-025.1 Cálculo del IVA**
- Dado un carrito con subtotal de $200.000.
- Cuando el cliente llega a la pantalla de confirmación.
- Entonces el sistema muestra IVA de $38.000.
- Y muestra un total de $238.000.

## Sprint 3

### HU-09 Registrar una venta presencial

**Rol:** Vendedor · **Sprint:** 3 · **Puntos:** 3 · **Hace:** Corales · **Valida:** Kevin

**Historia.** Como vendedor quiero registrar las ventas hechas en la tienda física para que el stock y los reportes incluyan todas las ventas.

**Requisitos EARS**

| ID | Patrón | Requisito |
|---|---|---|
| REQ-ZAP-026 | Evento | Cuando el vendedor registra una venta con productos, tallas y cantidades, el sistema deberá crear un pedido con canal PRESENCIAL en estado ENTREGADO y descontar el stock. |
| REQ-ZAP-027 | Opcional | Donde la venta sea presencial, el sistema deberá permitir registrarla sin cuenta de cliente como «Cliente de mostrador». |

**Criterios de aceptación**

**AC-026.1 Venta presencial**
- Dado un vendedor con sesión iniciada y un par en talla 41 con stock 5.
- Cuando registra la venta de 1 par en talla 41.
- Entonces el sistema crea un pedido PRESENCIAL en estado ENTREGADO.
- Y el stock de la talla 41 queda en 4.

**AC-026.2 Talla agotada (borde)**
- Dada una talla con stock 0.
- Cuando el vendedor intenta venderla.
- Entonces el sistema rechaza la venta.
- Y muestra «Stock insuficiente».

**AC-027.1 Cliente de mostrador**
- Dado un comprador sin cuenta en la tienda.
- Cuando el vendedor registra la venta sin seleccionar cliente.
- Entonces el pedido queda asociado a «Cliente de mostrador».

### HU-10 Gestionar el estado de los pedidos

**Rol:** Vendedor y Almacenista · **Sprint:** 3 · **Puntos:** 2 · **Hace:** Corales · **Valida:** Kevin

**Historia.** Como vendedor o almacenista quiero cambiar el estado de un pedido para que el cliente sepa en qué paso va su compra.

**Requisitos EARS**

| ID | Patrón | Requisito |
|---|---|---|
| REQ-ZAP-028 | Ubicuo | El sistema deberá permitir únicamente las transiciones de estado PENDIENTE → CONFIRMADO → ENVIADO → ENTREGADO, donde el Vendedor confirma y entrega, y el Almacenista envía. |
| REQ-ZAP-029 | No deseado | Si se intenta una transición no permitida, entonces el sistema deberá rechazarla y conservar el estado actual. |
| REQ-ZAP-030 | Evento | Cuando un pedido PENDIENTE o CONFIRMADO se cancela, el sistema deberá cambiarlo a CANCELADO y devolver el stock de sus ítems. |

**Criterios de aceptación**

**AC-028.1 Confirmar un pedido**
- Dado un pedido en estado PENDIENTE.
- Cuando el vendedor lo confirma.
- Entonces el pedido pasa a CONFIRMADO.

**AC-029.1 Transición inválida**
- Dado un pedido en estado ENTREGADO.
- Cuando alguien intenta cambiarlo a PENDIENTE.
- Entonces el sistema rechaza el cambio.
- Y el pedido sigue ENTREGADO.

**AC-030.1 Cancelar devuelve el stock**
- Dado un pedido CONFIRMADO con 2 pares en talla 40 y stock actual 3.
- Cuando el vendedor lo cancela.
- Entonces el pedido queda CANCELADO.
- Y el stock de la talla 40 sube a 5.

**AC-030.2 Cancelar un pedido enviado (borde)**
- Dado un pedido en estado ENVIADO.
- Cuando el vendedor intenta cancelarlo.
- Entonces el sistema rechaza la cancelación.

### HU-11 Consultar mi historial de pedidos

**Rol:** Cliente · **Sprint:** 3 · **Puntos:** 2 · **Hace:** Corales · **Valida:** Kevin

**Historia.** Como cliente quiero ver mis pedidos y su estado para saber qué compré y cuándo llegará.

**Requisitos EARS**

| ID | Patrón | Requisito |
|---|---|---|
| REQ-ZAP-031 | Ubicuo | El sistema deberá mostrar a cada cliente solo sus propios pedidos, ordenados del más reciente al más antiguo. |
| REQ-ZAP-032 | Evento | Cuando el cliente abre uno de sus pedidos, el sistema deberá mostrar los ítems, tallas, cantidades, totales, estado y fechas. |

**Criterios de aceptación**

**AC-031.1 Solo mis pedidos**
- Dados dos clientes con pedidos distintos.
- Cuando el primero abre «Mis pedidos».
- Entonces ve únicamente los suyos.
- Y aparecen del más reciente al más antiguo.

**AC-031.2 Pedido ajeno (borde)**
- Dado un pedido que pertenece a otro cliente.
- Cuando un cliente intenta abrirlo escribiendo su dirección en el navegador.
- Entonces el sistema responde con HTTP 403.

**AC-032.1 Detalle del pedido**
- Dado un pedido propio con 2 ítems.
- Cuando el cliente lo abre.
- Entonces ve cada ítem con talla, cantidad y precio.
- Y ve el subtotal, el IVA, el total y el estado.

### HU-12 Generar la factura en PDF

**Rol:** Cliente y Vendedor · **Sprint:** 3 · **Puntos:** 3 · **Hace:** Kevin · **Valida:** Corales

**Historia.** Como cliente o vendedor quiero descargar la factura de un pedido en PDF para tener un comprobante de la compra.

**Requisitos EARS**

| ID | Patrón | Requisito |
|---|---|---|
| REQ-ZAP-033 | Evento | Cuando el cliente dueño del pedido o un Vendedor solicita la factura de un pedido CONFIRMADO, ENVIADO o ENTREGADO, el sistema deberá generar un PDF con la API de PDF que incluya número, fecha, datos del cliente, ítems, IVA y total. |
| REQ-ZAP-034 | No deseado | Si el pedido está PENDIENTE o CANCELADO, entonces el sistema deberá rechazar la generación e indicar que la factura solo existe para pedidos confirmados. |
| REQ-ZAP-035 | No deseado | Si la API de PDF falla, entonces el sistema deberá mostrar un mensaje de error y dejar el pedido sin cambios. |

**Criterios de aceptación**

**AC-033.1 Factura de un pedido confirmado**
- Dado un pedido CONFIRMADO de $238.000 con IVA de $38.000.
- Cuando el cliente pulsa «Descargar factura».
- Entonces el sistema descarga un archivo PDF.
- Y el PDF muestra el número del pedido, los ítems, el IVA y el total de $238.000.

**AC-034.1 Pedido pendiente**
- Dado un pedido en estado PENDIENTE.
- Cuando el cliente solicita la factura.
- Entonces el sistema rechaza la solicitud.
- Y muestra «La factura solo está disponible para pedidos confirmados».

**AC-035.1 Falla de la API de PDF**
- Dada la API de PDF que no responde.
- Cuando el cliente solicita la factura de un pedido confirmado.
- Entonces el sistema muestra «No fue posible generar la factura, intenta de nuevo».
- Y el estado del pedido no cambia.

### HU-13 Obtener el reporte de ventas en PDF

**Rol:** Gerente · **Sprint:** 3 · **Puntos:** 3 · **Hace:** Kevin · **Valida:** Corales

**Historia.** Como gerente quiero generar un reporte de ventas en PDF por rango de fechas para tomar decisiones sobre qué zapatos comprar y vender.

**Requisitos EARS**

| ID | Patrón | Requisito |
|---|---|---|
| REQ-ZAP-036 | Evento | Cuando el Gerente selecciona un rango de fechas y solicita el reporte, el sistema deberá generar un PDF con el total de pedidos, las unidades vendidas, los ingresos y los 5 zapatos más vendidos. |
| REQ-ZAP-037 | No deseado | Si la fecha inicial es posterior a la fecha final, entonces el sistema deberá rechazar la solicitud e indicar el error. |
| REQ-ZAP-038 | No deseado | Si no existen ventas en el rango seleccionado, entonces el sistema deberá generar el reporte con el mensaje «Sin ventas en el rango seleccionado». |

**Criterios de aceptación**

**AC-036.1 Reporte con ventas**
- Dado en el rango 3 pedidos válidos de $238.000, $119.000 y $357.000, y 1 pedido CANCELADO de $500.000.
- Cuando el gerente solicita el reporte.
- Entonces el PDF muestra 3 pedidos e ingresos de $714.000.
- Y el pedido cancelado no aparece en los totales.

**AC-037.1 Fechas invertidas (borde)**
- Dada una fecha inicial del 10 de diciembre y una final del 1 de diciembre.
- Cuando el gerente solicita el reporte.
- Entonces el sistema rechaza la solicitud.
- Y muestra «La fecha inicial no puede ser posterior a la final».

**AC-038.1 Rango sin ventas**
- Dado un rango de fechas sin pedidos válidos.
- Cuando el gerente solicita el reporte.
- Entonces el PDF se genera con el mensaje «Sin ventas en el rango seleccionado».

---

## 7. Requisitos no funcionales

| ID | Patrón | Requisito | Verificación |
|---|---|---|---|
| NFR-ZAP-001 | Ubicuo | El sistema deberá mostrar la primera página del catálogo en menos de 2 segundos con 100 zapatos cargados, medido con las herramientas del navegador. | Prueba manual de rendimiento con 100 registros y paginación de 12; revisión en Sprint Review. |
| NFR-ZAP-002 | Ubicuo | El sistema deberá proteger cada ruta con Spring Security según el rol y mantener activa la protección CSRF en todos los formularios. | Pruebas de roles y de CSRF en MockMvc; revisión de `SecurityConfig`. |
| NFR-ZAP-003 | Ubicuo | El sistema deberá poder usarse desde un navegador de computador y de celular sin instalar ninguna aplicación. | Diseño responsive con Bootstrap 5; revisión manual en anchos de escritorio y móvil. |
| NFR-ZAP-004 | Ubicuo | El sistema deberá guardar todos sus datos en MySQL mediante JPA y no deberá usar MongoDB. | **Desviación autorizada por el usuario (ADR-006):** se usa PostgreSQL; ver `docs/trazabilidad.md`. |
| NFR-ZAP-005 | Ubicuo | El sistema deberá tratar los datos personales según la Ley 1581 de 2012 y no registrar contraseñas en los logs. | Revisión de logs y de `DatosIniciales`; BCrypt; mensajes genéricos. |
| NFR-ZAP-006 | Ubicuo | El código deberá seguir el modelo MVC y no deberá contener ninguna carpeta ni paquete llamado `service`. | Revisión de la estructura de paquetes y de la compilación. |

> **Nota de desviación (NFR-ZAP-004):** el texto anterior reproduce el requisito literal del DOCX. El 25 de noviembre de 2026 el usuario decidió usar PostgreSQL para aprovechar el servicio local, cambiando `pom.xml`, la configuración y el esquema SQL. La decisión y sus consecuencias están en [ADR-006](../../docs/adr/ADR-006.md); el estado real se registra en `docs/trazabilidad.md` como «desviación autorizada», no como cumplimiento.

## 8. Reglas de negocio

| ID | Regla | Dónde se cumple |
|---|---|---|
| RN-01 | Cada talla de cada zapato tiene su propio stock y nunca puede ser negativo. | `TallaStock`, `Pedido`, `InventarioController` |
| RN-02 | Las tallas válidas van de la 34 a la 45 (numeración europea). | `TallaStock`, `Zapato.crearTallasVacias()` |
| RN-03 | Los precios están en pesos colombianos (COP) y no incluyen IVA; el IVA es del 19 %. | `Pedido`, `Carrito`, `Dinero` |
| RN-04 | Cada usuario tiene exactamente un rol y siempre debe existir al menos un administrador activo. | `Usuario`, `UsuarioController` |
| RN-05 | Estados del pedido: PENDIENTE, CONFIRMADO, ENVIADO, ENTREGADO y CANCELADO. Las ventas presenciales nacen en ENTREGADO. | `EstadoPedido`, `Pedido`, `VentaController` |
| RN-06 | Solo se cancelan pedidos PENDIENTE o CONFIRMADO, y al cancelar se devuelve el stock. | `EstadoPedido`, `Pedido`, `GestionPedidoController` |
| RN-07 | La factura solo existe para pedidos CONFIRMADO, ENVIADO o ENTREGADO. | `EstadoPedido.isFacturable()`, `FacturaController` |
| RN-08 | Un zapato desactivado no aparece en el catálogo, pero conserva su historial de ventas. | `Zapato.activo`, `CatalogoController`, `DetallePedido` |
| RN-09 | Un cliente solo ve y descarga lo que pertenece a sus propios pedidos. | `PedidoController`, `FacturaController` |
| RN-10 | Los reportes cuentan los pedidos CONFIRMADO, ENVIADO y ENTREGADO, y excluyen los PENDIENTE y los CANCELADO. | `ReporteController`, `PedidoRepository.ventasEntre` |

## 9. Restricciones técnicas

| Restricción | Cumplimiento |
|---|---|
| Aplicación web, no móvil | Thymeleaf y Bootstrap 5 con diseño adaptable; no hay app Android ni iOS. |
| Spring Boot con MVC | `@Controller`, plantillas Thymeleaf y entidades JPA. |
| Sin carpeta `service` | No existe el paquete; la lógica vive en entidades, controladores y repositorios (ADR-002). |
| JPA y PostgreSQL (desviación de MySQL 8, ADR-006) | `org.postgresql:postgresql`, dialecto PostgreSQL y esquema en `database/tienda_zapatos.sql`. |
| Sin MongoDB | No hay dependencia ni configuración de MongoDB. |
| API de PDF aislada | Paquete `com.tiendazapatos.pdf` con `GeneradorPdf` (ADR-005). |
| 5 roles | Spring Security con `ADMINISTRADOR`, `GERENTE`, `VENDEDOR`, `ALMACENISTA`, `CLIENTE`. |
| Tres sprints de una semana | 17–20 nov, 23–27 nov y 30 nov–4 dic de 2026. |

## 10. Resumen de cobertura

| HU | Requisitos | Criterios | Sprint |
|---|---|---|---:|
| HU-01 | REQ-ZAP-001 a 003 | AC-001.1, AC-002.1, AC-002.2, AC-003.1 | 1 |
| HU-02 | REQ-ZAP-004 a 006 | AC-004.1, AC-005.1, AC-006.1, AC-006.2 | 1 |
| HU-03 | REQ-ZAP-007 a 010 | AC-007.1, AC-008.1, AC-009.1, AC-010.1 | 1 |
| HU-04 | REQ-ZAP-011 a 013 | AC-011.1, AC-012.1, AC-013.1 | 1 |
| HU-05 | REQ-ZAP-014 a 016 | AC-014.1, AC-015.1, AC-016.1 | 2 |
| HU-06 | REQ-ZAP-017 a 019 | AC-017.1, AC-018.1, AC-018.2, AC-019.1 | 2 |
| HU-07 | REQ-ZAP-020 a 022 | AC-020.1, AC-021.1, AC-021.2, AC-022.1 | 2 |
| HU-08 | REQ-ZAP-023 a 025 | AC-023.1, AC-023.2, AC-024.1, AC-025.1 | 2 |
| HU-09 | REQ-ZAP-026 a 027 | AC-026.1, AC-026.2, AC-027.1 | 3 |
| HU-10 | REQ-ZAP-028 a 030 | AC-028.1, AC-029.1, AC-030.1, AC-030.2 | 3 |
| HU-11 | REQ-ZAP-031 a 032 | AC-031.1, AC-031.2, AC-032.1 | 3 |
| HU-12 | REQ-ZAP-033 a 035 | AC-033.1, AC-034.1, AC-035.1 | 3 |
| HU-13 | REQ-ZAP-036 a 038 | AC-036.1, AC-037.1, AC-038.1 | 3 |
| **Total** | **38** | **46** | **3 sprints** |

## 11. Supuestos y dependencias

1. PostgreSQL está disponible en `localhost:5432` con la base `tienda_zapatos`; las credenciales se pasan por `DB_USER` y `DB_PASSWORD` (desviación ADR-006 respecto al DOCX, que pedía MySQL 8 en `3306`).
2. El esquema se ejecuta una vez con `database/tienda_zapatos.sql`; la aplicación valida el esquema al arrancar.
3. Las pruebas automáticas no requieren el servidor PostgreSQL: usan H2 en memoria en modo PostgreSQL.
4. El docente actúa como Product Owner y aprueba la especificación y la decisión de release.
5. La fecha del cronograma es una propuesta sujeta al calendario académico; el lunes 16 de noviembre es festivo, por eso el Sprint 1 tiene cuatro días hábiles.
6. La tabla 1 del documento fuente dice «3 sprints de 3 semanas», pero el cronograma y las fechas reales corresponden a tres sprints de una semana. Se adopta el cronograma como fuente de verdad.

## 12. Control de cambios

| Versión | Fecha | Cambio | Aprobó |
|---|---|---|---|
| 1.0 | 25 nov 2026 | Especificación inicial con 13 HU, 38 REQ, 46 AC, 6 NFR y 10 RN. | Pendiente Puerta 1 (PO) |
| 1.1 | 25 nov 2026 | Desviación autorizada: MySQL 8 → PostgreSQL (ADR-006). Sin cambios en HU, REQ, AC ni RN. | Usuario (Kevin) |

Cualquier cambio de alcance se registra primero aquí, se refleja en `plan.md`, `tasks.md` y `docs/trazabilidad.md`, y se somete a la Puerta del cambio.
