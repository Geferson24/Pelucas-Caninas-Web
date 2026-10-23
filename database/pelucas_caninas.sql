-- PELUCAS CANINAS - Instalación inicial
-- MySQL 8.0.16 o posterior. Codificación UTF-8.
-- Ejecutar el archivo completo en MySQL Workbench, en una instalación nueva.
-- No elimina bases, tablas ni registros existentes.
-- Si ya existen estas tablas, DETENERSE: este archivo no es una migración.
-- No configura Spring Security ni crea usuarios del servidor MySQL.
SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS pelucas_caninas
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE pelucas_caninas;

-- Un usuario tiene un solo rol. El administrador no necesita ser cliente.
-- password_hash admite NULL únicamente mientras la cuenta esté desactivada.
-- Al implementar autenticación, generar el hash en Java con PasswordEncoder.
CREATE TABLE usuario (
  id_usuario INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(120) NOT NULL,
  correo VARCHAR(120) NOT NULL,
  password_hash VARCHAR(255) CHARACTER SET ascii COLLATE ascii_bin NULL,
  rol VARCHAR(20) NOT NULL DEFAULT 'CLIENTE',
  activo BOOLEAN NOT NULL DEFAULT FALSE,
  fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_usuario_correo UNIQUE (correo),
  CONSTRAINT ck_usuario_nombre CHECK (CHAR_LENGTH(TRIM(nombre)) > 0),
  CONSTRAINT ck_usuario_correo CHECK (CHAR_LENGTH(TRIM(correo)) > 0),
  CONSTRAINT ck_usuario_rol CHECK (rol IN ('ADMIN', 'CLIENTE')),
  CONSTRAINT ck_usuario_activo CHECK (activo IN (0, 1)),
  CONSTRAINT ck_usuario_hash CHECK (password_hash IS NULL OR CHAR_LENGTH(TRIM(password_hash)) > 0),
  CONSTRAINT ck_usuario_acceso CHECK (activo = 0 OR password_hash IS NOT NULL)
) ENGINE=InnoDB;

-- Puede haber clientes presenciales sin cuenta. Máximo un cliente por usuario.
-- correo es un dato de contacto; el correo de acceso pertenece a usuario.
CREATE TABLE cliente (
  id_cliente INT AUTO_INCREMENT PRIMARY KEY,
  id_usuario INT NULL,
  nombres VARCHAR(80) NOT NULL,
  apellidos VARCHAR(80) NULL,
  telefono VARCHAR(20) NOT NULL,
  correo VARCHAR(120) NULL,
  fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_cliente_usuario UNIQUE (id_usuario),
  CONSTRAINT fk_cliente_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT ck_cliente_nombres CHECK (CHAR_LENGTH(TRIM(nombres)) > 0),
  CONSTRAINT ck_cliente_telefono CHECK (CHAR_LENGTH(TRIM(telefono)) > 0)
) ENGINE=InnoDB;

CREATE TABLE mascota (
  id_mascota INT AUTO_INCREMENT PRIMARY KEY,
  id_cliente INT NOT NULL,
  nombre VARCHAR(80) NOT NULL,
  especie VARCHAR(30) NOT NULL DEFAULT 'PERRO',
  raza VARCHAR(80) NULL,
  fecha_nacimiento DATE NULL,
  observaciones VARCHAR(500) NULL,
  CONSTRAINT fk_mascota_cliente FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT ck_mascota_nombre CHECK (CHAR_LENGTH(TRIM(nombre)) > 0),
  CONSTRAINT ck_mascota_especie CHECK (CHAR_LENGTH(TRIM(especie)) > 0)
) ENGINE=InnoDB;

CREATE TABLE categoria_producto (
  id_categoria INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(80) NOT NULL,
  codigo VARCHAR(40) NOT NULL COMMENT 'Identificador para filtros del catálogo',
  descripcion VARCHAR(255) NULL,
  CONSTRAINT uk_categoria_nombre UNIQUE (nombre),
  CONSTRAINT uk_categoria_codigo UNIQUE (codigo),
  CONSTRAINT ck_categoria_nombre CHECK (CHAR_LENGTH(TRIM(nombre)) > 0),
  CONSTRAINT ck_categoria_codigo CHECK (CHAR_LENGTH(TRIM(codigo)) > 0)
) ENGINE=InnoDB;

CREATE TABLE producto (
  id_producto INT AUTO_INCREMENT PRIMARY KEY,
  id_categoria INT NOT NULL,
  nombre VARCHAR(120) NOT NULL,
  descripcion TEXT NULL,
  precio DECIMAL(10,2) NOT NULL,
  stock INT NOT NULL DEFAULT 0,
  imagen VARCHAR(255) NULL,
  estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
  CONSTRAINT fk_producto_categoria FOREIGN KEY (id_categoria) REFERENCES categoria_producto (id_categoria)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT ck_producto_nombre CHECK (CHAR_LENGTH(TRIM(nombre)) > 0),
  CONSTRAINT ck_producto_precio CHECK (precio >= 0),
  CONSTRAINT ck_producto_stock CHECK (stock >= 0),
  CONSTRAINT ck_producto_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
) ENGINE=InnoDB;

-- Los planes de baño y los servicios adicionales se guardan en esta tabla.
-- NULL en precio/duración significa pendiente de definir, no servicio gratuito.
CREATE TABLE servicio (
  id_servicio INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(120) NOT NULL,
  descripcion TEXT NULL,
  precio DECIMAL(10,2) NULL,
  duracion_minutos INT NULL,
  imagen VARCHAR(255) NULL,
  tipo VARCHAR(20) NOT NULL DEFAULT 'ADICIONAL',
  destacado BOOLEAN NOT NULL DEFAULT FALSE,
  estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
  CONSTRAINT uk_servicio_nombre UNIQUE (nombre),
  CONSTRAINT ck_servicio_precio CHECK (precio IS NULL OR precio >= 0),
  CONSTRAINT ck_servicio_duracion CHECK (duracion_minutos IS NULL OR duracion_minutos > 0),
  CONSTRAINT ck_servicio_nombre CHECK (CHAR_LENGTH(TRIM(nombre)) > 0),
  CONSTRAINT ck_servicio_tipo CHECK (tipo IN ('PLAN', 'ADICIONAL')),
  CONSTRAINT ck_servicio_destacado CHECK (destacado IN (0, 1)),
  CONSTRAINT ck_servicio_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
) ENGINE=InnoDB;

-- Una cita corresponde a una mascota y a un servicio.
-- El dueño se obtiene desde mascota.id_cliente, evitando un cliente duplicado.
-- La disponibilidad y los cruces de horarios se validarán en la aplicación.
CREATE TABLE cita (
  id_cita INT AUTO_INCREMENT PRIMARY KEY,
  id_mascota INT NOT NULL,
  id_servicio INT NOT NULL,
  fecha DATETIME NOT NULL,
  estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
  precio_acordado DECIMAL(10,2) NULL,
  observaciones VARCHAR(500) NULL,
  fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_cita_mascota FOREIGN KEY (id_mascota) REFERENCES mascota (id_mascota)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT fk_cita_servicio FOREIGN KEY (id_servicio) REFERENCES servicio (id_servicio)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT ck_cita_estado CHECK (estado IN ('PENDIENTE', 'CONFIRMADA', 'EN_ATENCION', 'COMPLETADA', 'CANCELADA')),
  CONSTRAINT ck_cita_precio CHECK (precio_acordado IS NULL OR precio_acordado >= 0),
  INDEX ix_cita_fecha_estado (fecha, estado)
) ENGINE=InnoDB;

CREATE TABLE pedido (
  id_pedido INT AUTO_INCREMENT PRIMARY KEY,
  id_cliente INT NOT NULL,
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
  total DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  canal VARCHAR(20) NOT NULL DEFAULT 'WEB',
  observaciones VARCHAR(500) NULL,
  CONSTRAINT fk_pedido_cliente FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT ck_pedido_total CHECK (total >= 0),
  CONSTRAINT ck_pedido_estado CHECK (estado IN ('PENDIENTE', 'CONFIRMADO', 'ENTREGADO', 'CANCELADO')),
  CONSTRAINT ck_pedido_canal CHECK (canal IN ('WEB', 'LOCAL', 'WHATSAPP')),
  INDEX ix_pedido_fecha_estado (fecha, estado)
) ENGINE=InnoDB;

-- Se conserva el precio de la venta, aunque luego cambie producto.precio.
-- El subtotal se calcula automáticamente: no debe enviarse en INSERT/UPDATE.
CREATE TABLE detalle_pedido (
  id_detalle INT AUTO_INCREMENT PRIMARY KEY,
  id_pedido INT NOT NULL,
  id_producto INT NOT NULL,
  cantidad INT NOT NULL,
  precio_unitario DECIMAL(10,2) NOT NULL,
  subtotal DECIMAL(12,2) GENERATED ALWAYS AS (cantidad * precio_unitario) STORED,
  CONSTRAINT uk_detalle_pedido_producto UNIQUE (id_pedido, id_producto),
  CONSTRAINT fk_detalle_pedido FOREIGN KEY (id_pedido) REFERENCES pedido (id_pedido)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto) REFERENCES producto (id_producto)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT ck_detalle_cantidad CHECK (cantidad > 0),
  CONSTRAINT ck_detalle_precio CHECK (precio_unitario >= 0)
) ENGINE=InnoDB;

-- Datos propios del reclamo: también admite personas sin registro como cliente.
-- Conserva los datos de contacto tal como se declararon al presentarlo.
CREATE TABLE reclamo (
  id_reclamo INT AUTO_INCREMENT PRIMARY KEY,
  id_cliente INT NULL,
  nombres VARCHAR(160) NOT NULL,
  documento VARCHAR(20) NOT NULL,
  correo VARCHAR(120) NOT NULL,
  telefono VARCHAR(20) NULL,
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  tipo VARCHAR(20) NOT NULL,
  descripcion TEXT NOT NULL,
  solicitud TEXT NOT NULL,
  estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
  CONSTRAINT fk_reclamo_cliente FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT ck_reclamo_nombre CHECK (CHAR_LENGTH(TRIM(nombres)) > 0),
  CONSTRAINT ck_reclamo_documento CHECK (CHAR_LENGTH(TRIM(documento)) > 0),
  CONSTRAINT ck_reclamo_correo CHECK (CHAR_LENGTH(TRIM(correo)) > 0),
  CONSTRAINT ck_reclamo_descripcion CHECK (CHAR_LENGTH(TRIM(descripcion)) > 0),
  CONSTRAINT ck_reclamo_solicitud CHECK (CHAR_LENGTH(TRIM(solicitud)) > 0),
  CONSTRAINT ck_reclamo_tipo CHECK (tipo IN ('RECLAMO', 'QUEJA')),
  CONSTRAINT ck_reclamo_estado CHECK (estado IN ('PENDIENTE', 'EN_PROCESO', 'ATENDIDO'))
) ENGINE=InnoDB;

-- DATOS INICIALES: los precios e imágenes provienen de CatalogoService.java.
-- Stock real aún desconocido: todos los productos comienzan en cero.
START TRANSACTION;

INSERT INTO categoria_producto (id_categoria, nombre, codigo, descripcion) VALUES
  (1, 'Aseo', 'aseo', 'Productos para el baño y cuidado del pelaje.'),
  (2, 'Higiene', 'higiene', 'Productos de limpieza e higiene para mascotas.'),
  (3, 'Juguetes', 'juguetes', 'Artículos de juego y entretenimiento.'),
  (4, 'Accesorios', 'accesorios', 'Correas, arneses y otros accesorios.');

INSERT INTO producto (id_producto, id_categoria, nombre, descripcion, precio, stock, imagen) VALUES
  (1, 1, 'Shampoo Bexter', 'Shampoo recomendado para perros mayores a 1 año con sensibilidad en la dermis.', 25.00, 0, '/img/bexter.jpg'),
  (2, 1, 'Kit de aseo 6 piezas', 'Guantes de lavado para perros, cepillo de baño para perros.', 34.00, 0, '/img/augwind.jpg'),
  (3, 2, 'Toallitas humedas (400 unidades)', 'Hipoalergénicas, limpiadoras y desodorizantes.', 52.00, 0, '/img/immhabi.jpg'),
  (4, 3, 'Juguetes para perros', 'Juguetes interactivos para perros de tira y afloja.', 41.00, 0, '/img/ALLRIER.jpg'),
  (5, 2, 'Bolsas de basura', '100% a prueba de fugas y extra gruesas, paquete de 540 bolsas.', 58.00, 0, '/img/Greener.jpg'),
  (6, 4, 'Arnés táctico', 'Resistente para perros grandes, ajustable, antitirones y reflectante.', 99.00, 0, '/img/beebiepet.jpg'),
  (7, 4, 'Correa', 'Transpirable, diseño cómodo para todo tipo de mascota.', 34.00, 0, '/img/Matilor.jpg'),
  (8, 1, 'Cepillo de aseo 3 en 1', 'Cepillo de champú y estropajo con mango ajustable.', 30.00, 0, '/img/comotech.jpg'),
  (9, 1, 'Removedor de pelo', 'Rodillo reutilizable para retirar pelos de perros y gatos.', 85.00, 0, '/img/Chomchom.jpg'),
  (10, 3, 'Pelota chirriante', 'Juguete interactivo para perros masticadores fuertes.', 23.00, 0, '/img/dissknic.jpg');

INSERT INTO servicio (nombre, descripcion, precio, imagen, tipo, destacado) VALUES
  ('Baño Básico', '2 lavados con shampoo hipoalergénico; corte higiénico; corte y limado de uñas; limpieza y depilación de oídos; despejado de ojos y almohadillas; perfume y lazo decorativo.', 26.00, '/img/basico.jpg', 'PLAN', FALSE),
  ('Baño Premium', '2 lavados con shampoo hipoalergénico o antipulgas; acondicionador; corte higiénico; corte y limado de uñas; limpieza e hidratación de almohadillas; retoque de carita; cepillado de dientes; limpieza de glándulas perianales; perfume y lazo decorativo.', 37.00, '/img/premium.png', 'PLAN', TRUE),
  ('Baño y Corte', 'Baño básico o premium; corte de raza o adaptado; corte según estructura física y pelaje; corte con máquina, tijera o ambas técnicas; perfume y lazo decorativo.', 22.00, '/img/corte.jpg', 'PLAN', FALSE),
  ('Cepillado de dientes', NULL, NULL, '/img/cepillado.jpg', 'ADICIONAL', FALSE),
  ('Limpieza de oídos', NULL, NULL, '/img/oidos.png', 'ADICIONAL', FALSE),
  ('Limpieza de glándulas perianales', NULL, NULL, '/img/glandulas.png', 'ADICIONAL', FALSE),
  ('Corte y limado de uñas', NULL, NULL, '/img/unas.jpg', 'ADICIONAL', FALSE);

-- Correo de ejemplo: reemplazar por el del administrador real.
-- Esta cuenta NO permite iniciar sesión: no tiene contraseña y está desactivada.
-- Antes de activarla, asignar un hash generado con PasswordEncoder en Java.
INSERT INTO usuario (nombre, correo, password_hash, rol, activo) VALUES
  ('Administrador', 'admin@pelucascaninas.example', NULL, 'ADMIN', FALSE);

COMMIT;

-- Comprobaciones de instalación (no muestran contraseñas).
SHOW TABLES;
SELECT id_usuario, nombre, correo, rol, activo FROM usuario;
SELECT c.nombre AS categoria, COUNT(p.id_producto) AS productos
FROM categoria_producto c
LEFT JOIN producto p ON p.id_categoria = c.id_categoria
GROUP BY c.id_categoria, c.nombre
ORDER BY c.id_categoria;
select * from reclamo
-- REGLAS PARA LA FUTURA CAPA DE SERVICIOS:
-- 1. Guardar pedido, detalles, total y descuento de stock en una transacción.
--    total = SUM(detalle_pedido.subtotal). No hay triggers que lo sincronicen.
-- 2. Validar stock disponible con bloqueo/control de concurrencia al vender.
-- 3. Validar email, teléfono, fecha de nacimiento y disponibilidad de citas.
-- 4. Un cliente con cuenta debe vincularse a un usuario con rol CLIENTE.
-- 5. Desactivar productos/servicios utilizados en lugar de borrar su historial.
-- 6. Spring Security aplicará los permisos ADMIN/CLIENTE en el cuarto avance.
