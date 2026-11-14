# Tienda de Zapatos

Tienda web de zapatos (Scrum + SDD) — Spring Boot 3.5 · Java 21 · MVC con Thymeleaf · JPA/Hibernate · MySQL · PDF con OpenPDF.
Autores: Kevin Andréy Flórez Ramírez y Corales — UTS.

## Requisitos
- JDK 21 y Maven 3.9+
- MySQL 8 en `localhost:3306`

## Puesta en marcha
1. Crear la base y las tablas: ejecutar `database/tienda_zapatos.sql` (phpMyAdmin/Workbench, o `mysql -u root -p < database/tienda_zapatos.sql`).
   También crea `tienda_zapatos_test`, que usan las pruebas.
2. Credenciales de MySQL por variables de entorno (por defecto `root` sin clave):
   `export DB_USER=root DB_PASSWORD=tu_clave`
3. Arrancar: `mvn spring-boot:run` y abrir http://localhost:8080

Pruebas (usan la base MySQL `tienda_zapatos_test`, que se recrea sola): `mvn test`

Si usas Eclipse/STS ya no necesitas Lombok: los getters y setters están escritos en el código.

## Usuarios de demostración
Con `tienda.datos-demo=true` (en `application.properties`) se crean al primer arranque, con clave **Zapatos2026**:

| Rol | Correo |
|---|---|
| Administrador | admin@tienda.test |
| Gerente | gerente@tienda.test |
| Vendedor | vendedor@tienda.test |
| Almacenista | almacenista@tienda.test |
| Cliente | cliente@tienda.test |

Además se cargan 14 zapatos. Ponga `tienda.datos-demo=false` y cambie las claves antes de usarlo en serio.

## Arquitectura (MVC sin carpeta `service`, ADR-002)
`controller` → `model` (entidades con las reglas de negocio) → `repository` (Spring Data JPA).
`pdf` aísla la API de PDF (facturas y reporte de ventas). `config` tiene la seguridad y los datos iniciales.
Las transacciones se declaran en los controladores; el stock se protege con bloqueo pesimista.

## Reglas clave
IVA 19 %, tallas 34–45, estados PENDIENTE→CONFIRMADO→ENVIADO→ENTREGADO (CANCELADO devuelve stock),
factura solo para pedidos confirmados o posteriores, contraseña de 8+ caracteres con letra y número.

Trazabilidad historia → código → prueba: `docs/trazabilidad.md`.
