# Matriz de Trazabilidad — Tienda de Zapatos v1.0

| Campo | Valor |
|---|---|
| Especificación | `specs/001-tienda-zapatos/spec.md` |
| Tareas | `specs/001-tienda-zapatos/tasks.md` |
| Plan | `specs/001-tienda-zapatos/plan.md` |
| Fecha | 25 de noviembre de 2026 |
| Criterios | 46 |
| Criterios con prueba | 46/46 |
| Pruebas de la suite | 82 |

Cada fila conecta un criterio de aceptación con su historia, sus requisitos, la tarea que lo produjo, el código que lo implementa y la prueba automática que lo verifica. Los nombres de prueba corresponden a métodos reales de `src/test/java/com/tiendazapatos/`.

## 1. Criterios de aceptación

| Criterio | HU | Requisito(s) | Tarea | Código | Prueba automática | Estado |
|---|---|---|---|---|---|---|
| AC-001.1 | HU-01 | REQ-ZAP-001 | T-004, T-005, T-016 | `SecurityConfig`, `AuthController`, `login.html` | `TiendaIntegracionTest.ac001_1_inicioDeSesionExitosoLlevaAlPanelDelRol` | Verde |
| AC-002.1 | HU-01 | REQ-ZAP-002 | T-004, T-005, T-016 | `SecurityConfig`, `login.html` | `TiendaIntegracionTest.ac002_1_contrasenaIncorrectaSeRechazaConMensajeGenerico`<br>`TiendaIntegracionTest.ac002_1_elLoginMuestraElMensajeGenericoExacto` | Verde |
| AC-002.2 | HU-01 | REQ-ZAP-002 | T-004, T-010, T-016 | `SecurityConfig`, `Usuario.activo` | `TiendaIntegracionTest.ac002_2_cuentaDesactivadaNoPuedeIniciarSesion` | Verde |
| AC-003.1 | HU-01 | REQ-ZAP-003 | T-004, T-016 | `SecurityConfig`, `layout.html` | `TiendaIntegracionTest.ac003_1_cerrarSesionInvalidaLaSesionYVuelveAlCatalogo` | Verde |
| AC-004.1 | HU-02 | REQ-ZAP-004 | T-006, T-016 | `AuthController.registro`, `RegistroForm` | `TiendaIntegracionTest.ac004_1_registroCreaUnClienteConLaContrasenaCifrada` | Verde |
| AC-005.1 | HU-02 | REQ-ZAP-005 | T-007, T-016 | `AuthController.registro` | `TiendaIntegracionTest.ac005_1_correoDuplicadoSeRechaza` | Verde |
| AC-006.1 | HU-02 | REQ-ZAP-006 | T-008, T-016 | `PoliticaContrasena`, `RegistroForm` | `TiendaIntegracionTest.ac006_1_contrasenaCortaSeRechazaConElMensajeExacto`<br>`PoliticaContrasenaTest.ac006_1_seisCaracteresSeRechaza` | Verde |
| AC-006.2 | HU-02 | REQ-ZAP-006 | T-008, T-016 | `PoliticaContrasena`, `AuthController` | `TiendaIntegracionTest.ac006_2_contrasenaEnElLimiteSeAceptaYCifra`<br>`PoliticaContrasenaTest.ac006_2_ochoCaracteresConLetrasYNumeroSeAcepta` | Verde |
| AC-007.1 | HU-03 | REQ-ZAP-007 | T-009, T-016 | `UsuarioController`, `UsuarioForm`, `Usuario.rol` | `TiendaIntegracionTest.ac007_1_elAdministradorCreaUnUsuarioConUnRol` | Verde |
| AC-008.1 | HU-03 | REQ-ZAP-008 | T-010, T-016 | `UsuarioController.desactivar`, `Usuario.activo`, `MovimientoInventario` | `TiendaIntegracionTest.ac008_1_desactivarUnaCuentaImpideElLoginYConservaSusMovimientos` | Verde |
| AC-009.1 | HU-03 | REQ-ZAP-009 | T-012, T-016 | `SecurityConfig`, `ManejadorErrores` | `TiendaIntegracionTest.ac009_1_unRolNoAutorizadoRecibe403` | Verde |
| AC-010.1 | HU-03 | REQ-ZAP-010 | T-011, T-016 | `UsuarioController.desactivar` | `TiendaIntegracionTest.ac010_1_noSeDesactivaAlUnicoAdministrador` | Verde |
| AC-011.1 | HU-04 | REQ-ZAP-011 | T-013, T-016 | `ZapatoController`, `Zapato`, `ZapatoForm` | `TiendaIntegracionTest.ac011_1_crearUnZapatoGeneraReferenciaUnicaYLasTallas` | Verde |
| AC-012.1 | HU-04 | REQ-ZAP-012 | T-014, T-016 | `ZapatoForm.precio`, `ZapatoController` | `TiendaIntegracionTest.ac012_1_precioCeroSeRechazaConMensajeYPrecioUnoSeAcepta` | Verde |
| AC-013.1 | HU-04 | REQ-ZAP-013 | T-015, T-016 | `ZapatoController.desactivar`, `Zapato.activo`, `DetallePedido` | `TiendaIntegracionTest.ac013_1_unZapatoDesactivadoSaleDelCatalogoPeroConservaSusVentas` | Verde |
| AC-014.1 | HU-05 | REQ-ZAP-014 | T-017, T-027 | `CatalogoController`, `ZapatoRepository`, `catalogo.html` | `TiendaIntegracionTest.ac014_1_elCatalogoPaginaDe12En12` | Verde |
| AC-015.1 | HU-05 | REQ-ZAP-015 | T-018, T-027 | `ZapatoSpecs`, `CatalogoController` | `TiendaIntegracionTest.ac015_1_losFiltrosSeCombinan` | Verde |
| AC-016.1 | HU-05 | REQ-ZAP-016 | T-019, T-027 | `CatalogoController`, `catalogo.html` | `TiendaIntegracionTest.ac016_1_sinResultadosMuestraElMensaje` | Verde |
| AC-017.1 | HU-06 | REQ-ZAP-017 | T-020, T-027 | `InventarioController`, `TallaStock.sumar`, `MovimientoInventario` | `TiendaIntegracionTest.ac017_1_laEntradaDeMercanciaSumaAlStockYGuardaElMovimiento`<br>`TallaStockTest.ac017_1_entradaDeMercanciaSumaAlStock` | Verde |
| AC-018.1 | HU-06 | REQ-ZAP-018 | T-020, T-027 | `TallaStock.ajustar`, `InventarioController` | `TiendaIntegracionTest.ac018_1_unAjusteQueDejaStockNegativoSeRechaza`<br>`TallaStockTest.ac018_1_ajusteQueDejaStockNegativoSeRechaza` | Verde |
| AC-018.2 | HU-06 | REQ-ZAP-018 | T-020, T-027 | `TallaStock.ajustar`, `InventarioController` | `TiendaIntegracionTest.ac018_2_unAjusteHastaCeroSeAcepta`<br>`TallaStockTest.ac018_2_ajusteHastaCeroEsValido` | Verde |
| AC-019.1 | HU-06 | REQ-ZAP-019 | T-021, T-027 | `TallaStock.agotada`, `zapato-detalle.html`, `app.js` | `TiendaVistasTest.ac019_1_laTallaAgotadaSeMuestraComoAgotadaYElBotonEstaDeshabilitado`<br>`TallaStockTest.ac019_1_tallaConStockCeroEstaAgotada` | Verde |
| AC-020.1 | HU-07 | REQ-ZAP-020 | T-022, T-027 | `Carrito.agregar`, `CarritoController`, `carrito.html` | `CarritoTest.ac020_1_agregarAlCarritoMuestraCantidadYSubtotal` | Verde |
| AC-021.1 | HU-07 | REQ-ZAP-021 | T-023, T-027 | `Carrito.agregar`, `CarritoController` | `CarritoTest.ac021_1_cantidadMayorAlStockSeRechaza` | Verde |
| AC-021.2 | HU-07 | REQ-ZAP-021 | T-023, T-027 | `Carrito.agregar`, `CarritoController` | `CarritoTest.ac021_2_cantidadIgualAlStockSeAcepta` | Verde |
| AC-022.1 | HU-07 | REQ-ZAP-022 | T-023, T-027 | `Carrito.eliminar`, `Carrito.actualizar`, `CarritoController` | `CarritoTest.ac022_1_eliminarUnItemRecalculaElTotal` | Verde |
| AC-023.1 | HU-08 | REQ-ZAP-023 | T-025, T-027 | `PedidoController.confirmar`, `Pedido`, `Carrito.vaciar` | `TiendaIntegracionTest.ac023_1_pedidoExitosoDescuentaStockYVaciaElCarrito`<br>`PedidoTest.ac023_1_crearPedidoDescuentaElStockYEmpiezaPendiente` | Verde |
| AC-023.2 | HU-08 | REQ-ZAP-023 | T-025, T-027 | `PedidoController.confirmar`, `checkout.html` | `TiendaIntegracionTest.ac023_2_carritoVacioRechazaLaConfirmacion` | Verde |
| AC-024.1 | HU-08 | REQ-ZAP-024 | T-026, T-027 | `PedidoController.confirmar`, `TallaStockRepository.bloquear` | `PedidoConcurrenciaTest.ac024_1_dosClientesPorElUltimoParSoloCreanUnPedido`<br>`PedidoTest.ac024_1_sinStockSuficienteElPedidoSeRechazaYElStockNoCambia` | Verde |
| AC-025.1 | HU-08 | REQ-ZAP-025 | T-024, T-027 | `Carrito.getIva/getTotal`, `checkout.html`, `Pedido` | `TiendaIntegracionTest.ac025_1_elCheckoutMuestraIvaYTotalYElPedidoGuardaElMismoTotal`<br>`PedidoTest.ac025_1_ivaDel19PorCientoSobreElSubtotal` | Verde |
| AC-026.1 | HU-09 | REQ-ZAP-026 | T-028, T-038 | `VentaController`, `Pedido` (canal PRESENCIAL, ENTREGADO) | `TiendaIntegracionTest.ac026_1_ventaPresencialDescuentaStockYQuedaEntregada` | Verde |
| AC-026.2 | HU-09 | REQ-ZAP-026 | T-028, T-038 | `VentaController`, `TallaStock.descontar` | `TiendaIntegracionTest.ac026_2_tallaAgotadaRechazaLaVenta` | Verde |
| AC-027.1 | HU-09 | REQ-ZAP-027 | T-029, T-038 | `Pedido.clienteMostrador`, `VentaController` | `PedidoTest.ventaDeMostradorNoTieneClienteYSeLlamaClienteDeMostrador` | Verde |
| AC-028.1 | HU-10 | REQ-ZAP-028 | T-030, T-038 | `EstadoPedido`, `GestionPedidoController.confirmar` | `TiendaIntegracionTest.ac028_1_elVendedorConfirmaUnPedidoPendiente`<br>`PedidoTest.ac028_1_pendientePasaAConfirmado` | Verde |
| AC-029.1 | HU-10 | REQ-ZAP-029 | T-030, T-038 | `EstadoPedido.permite`, `Pedido.cambiarEstado` | `PedidoTest.ac029_1_transicionInvalidaSeRechazaYConservaElEstado`<br>`PedidoTest.noSePuedeSaltarDePendienteAEntregado` | Verde |
| AC-030.1 | HU-10 | REQ-ZAP-030 | T-031, T-038 | `Pedido.cancelar`, `GestionPedidoController.cancelar` | `TiendaIntegracionTest.ac030_1_cancelarUnPedidoConfirmadoDevuelveElStock`<br>`PedidoTest.ac030_1_cancelarDevuelveElStock` | Verde |
| AC-030.2 | HU-10 | REQ-ZAP-030 | T-031, T-038 | `EstadoPedido`, `Pedido.cancelar` | `PedidoTest.ac030_2_pedidoEnviadoNoSePuedeCancelar` | Verde |
| AC-031.1 | HU-11 | REQ-ZAP-031 | T-032, T-038 | `PedidoController.misPedidos`, `PedidoRepository.findByClienteOrderByFechaDesc` | `TiendaIntegracionTest.ac031_1_soloMisPedidosYDelMasRecienteAlMasAntiguo` | Verde |
| AC-031.2 | HU-11 | REQ-ZAP-031 | T-033, T-038 | `PedidoController.detalle`, `SecurityConfig` | `TiendaIntegracionTest.ac031_2_unClienteNoPuedeAbrirElPedidoDeOtro` | Verde |
| AC-032.1 | HU-11 | REQ-ZAP-032 | T-033, T-038 | `PedidoController.detalle`, `pedido-detalle.html` | `TiendaIntegracionTest.ac032_1_elDetalleMuestraItemsTallasCantidadesYtotales` | Verde |
| AC-033.1 | HU-12 | REQ-ZAP-033 | T-034, T-038 | `FacturaController`, `GeneradorPdf.factura`, `EstadoPedido.isFacturable` | `TiendaIntegracionTest.ac033_1_unPedidoConfirmadoDescargaUnPdfConNumeroItemsIvaYTotal` | Verde |
| AC-034.1 | HU-12 | REQ-ZAP-034 | T-035, T-038 | `FacturaController`, `EstadoPedido.isFacturable` | `TiendaIntegracionTest.ac034_1_unPedidoPendienteNoTieneFactura` | Verde |
| AC-035.1 | HU-12 | REQ-ZAP-035 | T-035, T-038 | `GeneradorPdf`, `PdfGeneracionException`, `FacturaController` | `FacturaPdfFallbackTest.ac035_1_siLaApiDePdfFallaSeMuestraElMensajeYElPedidoNoCambia` | Verde |
| AC-036.1 | HU-13 | REQ-ZAP-036 | T-036, T-038 | `ResumenVentas.de`, `ReporteController`, `GeneradorPdf.reporte` | `TiendaIntegracionTest.ac036_1_elReporteSumaLosPedidosValidosYExcluyeLosCancelados`<br>`ResumenVentasTest.ac036_1_totalesYLosMasVendidos` | Verde |
| AC-037.1 | HU-13 | REQ-ZAP-037 | T-037, T-038 | `ReporteController`, `ReglaNegocioException` | `TiendaIntegracionTest.ac037_1_fechasInvertidasSeRechazan` | Verde |
| AC-038.1 | HU-13 | REQ-ZAP-038 | T-037, T-038 | `ResumenVentas`, `GeneradorPdf.reporte` | `TiendaIntegracionTest.ac038_1_unRangoSinVentasGeneraElPdfConElMensaje`<br>`ResumenVentasTest.ac038_1_sinVentasEnElRango` | Verde |

## 2. Cobertura por historia

| HU | Requisitos | Criterios | Criterios con prueba | Pruebas identificadas |
|---|---|---:|---:|---:|
| HU-01 | REQ-ZAP-001 a 003 | 4 | 4 | 5 |
| HU-02 | REQ-ZAP-004 a 006 | 4 | 4 | 6 |
| HU-03 | REQ-ZAP-007 a 010 | 4 | 4 | 4 |
| HU-04 | REQ-ZAP-011 a 013 | 3 | 3 | 3 |
| HU-05 | REQ-ZAP-014 a 016 | 3 | 3 | 3 |
| HU-06 | REQ-ZAP-017 a 019 | 4 | 4 | 8 |
| HU-07 | REQ-ZAP-020 a 022 | 4 | 4 | 4 |
| HU-08 | REQ-ZAP-023 a 025 | 4 | 4 | 7 |
| HU-09 | REQ-ZAP-026 a 027 | 3 | 3 | 3 |
| HU-10 | REQ-ZAP-028 a 030 | 4 | 4 | 7 |
| HU-11 | REQ-ZAP-031 a 032 | 3 | 3 | 3 |
| HU-12 | REQ-ZAP-033 a 035 | 3 | 3 | 3 |
| HU-13 | REQ-ZAP-036 a 038 | 3 | 3 | 5 |
| **Total** | **38** | **46** | **46** | **61 métodos** |

> Además de los 61 métodos vinculados directamente a criterios, la suite incluye pruebas de apoyo (por ejemplo, `sinSesionLasRutasProtegidasLlevanAlLogin`, `soloElAlmacenistaRegistraMovimientosDeInventario`, `elVisitanteNoVeElCarritoEnLaNavegacionPeroElClienteSi`). El total de la suite es **82 pruebas**.

## 3. Requisitos no funcionales

| NFR | Verificación | Evidencia | Estado |
|---|---|---|---|
| NFR-ZAP-001 | Primera página del catálogo < 2 s con 100 zapatos | Paginación de 12 y medición en navegador | Pendiente de medición formal |
| NFR-ZAP-002 | Rutas por rol y CSRF activo | `SecurityConfig`, `TiendaIntegracionTest.ac009_1`, `ac031_2`; todos los POST usan `csrf()` | Verde |
| NFR-ZAP-003 | Uso desde computador y celular sin instalar app | Thymeleaf + Bootstrap 5 + CSS responsive | Verde por revisión |
| NFR-ZAP-004 | Texto original: MySQL 8 con JPA, sin MongoDB | `pom.xml` (`org.postgresql:postgresql`), `application.properties` (`DB_URL`), sin MongoDB; arranque con `validate` OK en PostgreSQL 16.15; esquemas `tienda_zapatos.sql` (local) y `tienda_zapatos_neon.sql` (Render); [ADR-006](adr/ADR-006.md) | **Desviación autorizada (ADR-006)**: se usa PostgreSQL (local y Neon/Render); no cumple la redacción original del DOCX, pero el esquema quedó validado en el servidor real |
| NFR-ZAP-005 | Ley 1581 y sin contraseñas en logs | BCrypt, mensaje genérico, `DatosIniciales` sin clave, revisión de logs | Verde |
| NFR-ZAP-006 | MVC sin carpeta/paquete `service` | Estructura de paquetes y compilación | Verde |

## 4. Reglas de negocio

| Regla | Dónde se cumple | Prueba o evidencia | Estado |
|---|---|---|---|
| RN-01 | `TallaStock` rechaza stock negativo | `TallaStockTest`, AC-018.1, AC-018.2 | Verde |
| RN-02 | Tallas 34–45 | `Zapato.crearTallasVacias`, `TallaStock.TALLA_MIN/MAX` | Verde |
| RN-03 | COP sin IVA; IVA 19 % | `Pedido`, `Carrito`, `Dinero`, AC-025.1 | Verde |
| RN-04 | Un rol por usuario y al menos un administrador activo | `Usuario.rol`, `UsuarioController`, AC-010.1 | Verde |
| RN-05 | Estados y venta presencial ENTREGADO | `EstadoPedido`, `VentaController`, AC-026.1 | Verde |
| RN-06 | Cancelar solo PENDIENTE/CONFIRMADO y devolver stock | `Pedido.cancelar`, AC-030.1, AC-030.2 | Verde |
| RN-07 | Factura solo CONFIRMADO/ENVIADO/ENTREGADO | `EstadoPedido.isFacturable`, AC-034.1 | Verde |
| RN-08 | Zapato desactivado fuera del catálogo, historial intacto | AC-013.1 | Verde |
| RN-09 | Cliente solo ve/descarga lo propio | AC-031.2; prueba de factura ajena | Verde |
| RN-10 | Reporte solo con CONFIRMADO/ENVIADO/ENTREGADO | `ReporteController`, AC-036.1 | Verde |

## 5. Roles

| Rol | Rutas principales | Prueba de acceso | Estado |
|---|---|---|---|
| ADMINISTRADOR | `/admin/usuarios/**`, `/almacen/zapatos/**`, `/gestion/pedidos` | AC-007.1, AC-008.1, AC-009.1, AC-010.1 | Verde |
| GERENTE | `/reportes/ventas/**` | `soloElGerenteVeElReporte` | Verde |
| VENDEDOR | `/ventas/**`, `/gestion/pedidos/**`, `/facturas/**` | AC-026.1, AC-028.1, AC-033.1 | Verde |
| ALMACENISTA | `/almacen/zapatos/**`, `/almacen/inventario/**`, `/gestion/pedidos` (envío) | AC-017.1, AC-019.1, `elAlmacenistaNoPuedeConfirmarPeroSiEnviar` | Verde |
| CLIENTE | `/carrito/**`, `/pedidos/**`, `/mis-pedidos/**`, `/facturas/**` propias | AC-020.1, AC-023.1, AC-031.1, AC-031.2 | Verde |

## 6. Mantenimiento de la matriz

1. Un criterio nuevo se agrega a `spec.md`, se implementa, se prueba y se registra aquí en la misma tarea.
2. Si se renombra un método de prueba, se actualiza esta matriz y se vuelve a ejecutar `mvn -B test`.
3. Una fila sin prueba o sin código se marca como deuda y no puede pasar la Puerta 1 ni la revisión de release.
4. La matriz se revisa al final de cada sprint junto con la Definition of Done.
