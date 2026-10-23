# Base de datos de Pelucas Caninas

Archivo de instalación: `pelucas_caninas.sql`. Requiere MySQL 8.0.16 o posterior
(recomendado MySQL 8.4). Usa InnoDB, claves foráneas, restricciones CHECK y utf8mb4.

## Importar en MySQL Workbench

1. Abre tu conexión al servidor MySQL.
2. Selecciona **File → Open SQL Script** y abre `pelucas_caninas.sql`.
3. Ejecuta el archivo completo con el botón del rayo (sin seleccionar un fragmento).
4. Actualiza **Schemas**. Aparecerá `pelucas_caninas` con diez tablas.

Es una instalación inicial, no una migración. No contiene DROP ni borra datos.
Si ya existen tablas con estos nombres, no lo vuelvas a ejecutar: detente ante el
primer error. CREATE TABLE no es transaccional; si falla la instalación, revisa el
error antes de continuar. Los datos iniciales sí se insertan en una transacción.

## Tablas y decisiones

| Tabla | Uso |
|---|---|
| usuario | Cuenta de acceso; un rol ADMIN o CLIENTE por cuenta |
| cliente | Contacto del cliente; vínculo opcional y único con usuario |
| mascota | Mascota de un cliente; fecha de nacimiento opcional |
| categoria_producto | Categorías del catálogo con código para los filtros |
| producto | Precio, stock, imagen y estado del producto |
| servicio | Planes de baño y servicios adicionales |
| cita | Una mascota y un servicio por cita |
| pedido | Compra de productos de un cliente |
| detalle_pedido | Cantidad, precio de venta y subtotal calculado |
| reclamo | Reclamo o queja, incluso de una persona sin cuenta |

El administrador es un usuario con rol ADMIN y no necesita tener registro de
cliente. Los clientes presenciales pueden existir sin usuario. No se incluye una
tabla independiente de administradores ni de roles: cada cuenta tiene un solo rol.

La fecha de nacimiento reemplaza la edad; se admite NULL si se desconoce. El dueño
de una cita se obtiene mediante la mascota. Si después se necesitan varios
servicios por cita o registrar otra persona que reserva, deberá ampliarse el modelo.

Los pedidos solo manejan productos, sin descuentos, envío ni cobros de servicios.
El precio unitario del detalle conserva el importe de la venta. El subtotal es una
columna generada; el futuro servicio de pedidos deberá actualizar el total y el
stock dentro de una transacción. Las claves foráneas restringen la eliminación de
registros relacionados; para productos y servicios con historial se usa INACTIVO.

## Datos incluidos

- Cuatro categorías y los diez productos actuales de `CatalogoService.java`.
- Tres planes y cuatro servicios adicionales del sitio.
- Stock inicial cero, porque todavía no se ha registrado el inventario real.
- Precios y duraciones desconocidos como NULL; no significan gratuidad.
- Una cuenta ADMIN de ejemplo, desactivada y sin contraseña.

El correo de ejemplo es `admin@pelucascaninas.example`. Se debe cambiar por el real.
La columna password_hash no guarda contraseñas en texto plano. Al implementar
Spring Security, se generará un hash mediante PasswordEncoder y se activará la
cuenta. Una restricción impide activar una cuenta sin hash, aunque la validez del
hash debe comprobarse en la aplicación. No hay credenciales de acceso funcionales.

## Alcance del tercer avance

El CRUD administrativo puede comenzar por categoria_producto y producto. Los
roles quedan almacenados, pero la base de datos por sí sola no protege las rutas
del panel. Spring Security se incorporará en el cuarto avance. Este script no
modifica los controladores ni conecta todavía el catálogo de Spring Boot a MySQL.

Referencia de sintaxis: [MySQL — CHECK Constraints](https://dev.mysql.com/doc/refman/8.4/en/create-table-check-constraints.html).
