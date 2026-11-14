-- =====================================================================
-- Tienda de Zapatos - base de datos MySQL / MariaDB
-- Ejecutar completo (phpMyAdmin, Workbench o: mysql -u root -p < tienda_zapatos.sql)
-- Los datos de demostración (5 usuarios y 14 zapatos) los crea la aplicación
-- al primer arranque (tienda.datos-demo=true), con las claves ya cifradas.
-- =====================================================================
CREATE DATABASE IF NOT EXISTS tienda_zapatos
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE tienda_zapatos;

CREATE TABLE IF NOT EXISTS usuarios (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  nombre     VARCHAR(100) NOT NULL,
  correo     VARCHAR(150) NOT NULL,
  password   VARCHAR(255) NOT NULL,
  telefono   VARCHAR(20),
  rol        VARCHAR(20)  NOT NULL,
  activo     BIT          NOT NULL DEFAULT 1,
  PRIMARY KEY (id),
  UNIQUE KEY uk_usuarios_correo (correo),
  CONSTRAINT ck_usuarios_rol CHECK (rol IN ('ADMINISTRADOR','GERENTE','VENDEDOR','ALMACENISTA','CLIENTE'))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS zapatos (
  id          BIGINT         NOT NULL AUTO_INCREMENT,
  referencia  VARCHAR(20)    NOT NULL,
  nombre      VARCHAR(120)   NOT NULL,
  marca       VARCHAR(60)    NOT NULL,
  categoria   VARCHAR(60)    NOT NULL,
  color       VARCHAR(40)    NOT NULL,
  precio      DECIMAL(12,2)  NOT NULL,
  descripcion VARCHAR(500),
  imagen      VARCHAR(300),
  activo      BIT            NOT NULL DEFAULT 1,
  PRIMARY KEY (id),
  UNIQUE KEY uk_zapatos_referencia (referencia)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tallas_stock (
  id        BIGINT NOT NULL AUTO_INCREMENT,
  zapato_id BIGINT NOT NULL,
  talla     INT    NOT NULL,
  stock     INT    NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_zapato_talla (zapato_id, talla),
  CONSTRAINT fk_tallas_zapato FOREIGN KEY (zapato_id) REFERENCES zapatos (id),
  CONSTRAINT ck_talla_rango CHECK (talla BETWEEN 34 AND 45),
  CONSTRAINT ck_stock_no_negativo CHECK (stock >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS pedidos (
  id          BIGINT        NOT NULL AUTO_INCREMENT,
  numero      VARCHAR(30)   NOT NULL,
  canal       VARCHAR(15)   NOT NULL,
  estado      VARCHAR(15)   NOT NULL,
  fecha       DATETIME(6)   NOT NULL,
  direccion   VARCHAR(250),
  metodo_pago VARCHAR(20)   NOT NULL,
  cliente_id  BIGINT,
  vendedor_id BIGINT,
  subtotal    DECIMAL(14,2) NOT NULL DEFAULT 0,
  iva         DECIMAL(14,2) NOT NULL DEFAULT 0,
  total       DECIMAL(14,2) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_pedidos_numero (numero),
  KEY ix_pedidos_fecha (fecha),
  CONSTRAINT fk_pedidos_cliente  FOREIGN KEY (cliente_id)  REFERENCES usuarios (id),
  CONSTRAINT fk_pedidos_vendedor FOREIGN KEY (vendedor_id) REFERENCES usuarios (id),
  CONSTRAINT ck_pedidos_canal  CHECK (canal IN ('WEB','PRESENCIAL')),
  CONSTRAINT ck_pedidos_estado CHECK (estado IN ('PENDIENTE','CONFIRMADO','ENVIADO','ENTREGADO','CANCELADO')),
  CONSTRAINT ck_pedidos_pago   CHECK (metodo_pago IN ('CONTRA_ENTREGA','TRANSFERENCIA','EFECTIVO','TARJETA'))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS detalles_pedido (
  id              BIGINT        NOT NULL AUTO_INCREMENT,
  pedido_id       BIGINT        NOT NULL,
  zapato_id       BIGINT        NOT NULL,
  talla_stock_id  BIGINT        NOT NULL,
  nombre_zapato   VARCHAR(120)  NOT NULL,
  talla           INT           NOT NULL,
  cantidad        INT           NOT NULL,
  precio_unitario DECIMAL(12,2) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_det_pedido FOREIGN KEY (pedido_id)      REFERENCES pedidos (id),
  CONSTRAINT fk_det_zapato FOREIGN KEY (zapato_id)      REFERENCES zapatos (id),
  CONSTRAINT fk_det_talla  FOREIGN KEY (talla_stock_id) REFERENCES tallas_stock (id),
  CONSTRAINT ck_det_cantidad CHECK (cantidad > 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS movimientos_inventario (
  id             BIGINT      NOT NULL AUTO_INCREMENT,
  talla_stock_id BIGINT      NOT NULL,
  tipo           VARCHAR(20) NOT NULL,
  cantidad       INT         NOT NULL,
  fecha          DATETIME(6) NOT NULL,
  usuario_id     BIGINT      NOT NULL,
  referencia     VARCHAR(30),
  PRIMARY KEY (id),
  CONSTRAINT fk_mov_talla   FOREIGN KEY (talla_stock_id) REFERENCES tallas_stock (id),
  CONSTRAINT fk_mov_usuario FOREIGN KEY (usuario_id)     REFERENCES usuarios (id),
  CONSTRAINT ck_mov_tipo CHECK (tipo IN ('ENTRADA','AJUSTE','VENTA','DEVOLUCION'))
) ENGINE=InnoDB;

-- Base separada para las pruebas automáticas (mvn test); Hibernate la recrea en cada ejecución.
CREATE DATABASE IF NOT EXISTS tienda_zapatos_test
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
