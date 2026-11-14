# Trazabilidad HU → código → pruebas

| HU | Tema | Controlador | Pruebas |
|---|---|---|---|
| HU-01 | Iniciar/cerrar sesión | SecurityConfig, AuthController | TiendaIntegracionTest ac001_1, ac002_1, ac002_2 |
| HU-02 | Registro de cliente | AuthController, PoliticaContrasena | ac004_1, ac005_1, ac006_1; PoliticaContrasenaTest |
| HU-03 | Usuarios y roles | UsuarioController | ac007_1, ac008_1, ac009_1, ac010_1 |
| HU-04 | Gestión de zapatos | ZapatoController | ac011_1, ac012_1, ac013_1 |
| HU-05 | Catálogo con filtros | CatalogoController, ZapatoSpecs | ac014_1, ac015_1, ac016_1 |
| HU-06 | Inventario por talla | InventarioController, TallaStock | ac017_1, ac018_1; TallaStockTest |
| HU-07 | Carrito | CarritoController, Carrito | CarritoTest |
| HU-08 | Compra en línea | PedidoController, Pedido | PedidoTest |
| HU-09 | Venta presencial | VentaController | ac026_1, ac026_2 |
| HU-10 | Estados del pedido | GestionPedidoController, EstadoPedido | ac028_1, ac030_1; PedidoTest |
| HU-11 | Historial del cliente | PedidoController (`/mis-pedidos`) | ac031_2 |
| HU-12 | Factura PDF | FacturaController, GeneradorPdf | ac033_1, ac034_1 |
| HU-13 | Reporte de ventas PDF | ReporteController, ResumenVentas | ac037_1, ac038_1; ResumenVentasTest |
