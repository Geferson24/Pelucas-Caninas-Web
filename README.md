# Pelucas Caninas — guía para el grupo

Proyecto académico con Spring Boot, Thymeleaf, Spring Data JPA y MySQL.
Esta guía permite ejecutar el proyecto en otra computadora desde cero.

## 1. Instalar los requisitos

- **JDK 25**. Verifica con `java -version` y `javac -version`: ambos deben indicar 25.
  Configura `JAVA_HOME` con la carpeta de tu JDK y agrega su carpeta `bin` al PATH.
- **MySQL Server 8.0.16 o superior**, iniciado en `localhost:3306`.
  MySQL Workbench es la herramienta gráfica; también necesitas el servidor instalado.
- **VS Code** con **Extension Pack for Java**. Spring Boot Extension Pack es opcional.
- **Git**, si vas a clonar el repositorio. También puedes descargarlo como ZIP.

No necesitas instalar Maven ni Node.js: el repositorio incluye Maven Wrapper.
La primera ejecución necesita conexión a Internet para descargar las dependencias.
Las fuentes, Bootstrap, Font Awesome y el mapa también usan servicios externos.

## 2. Descargar y abrir el proyecto

En GitHub, pulsa **Code → HTTPS**, copia la URL y ejecuta:

```powershell
git clone URL_DEL_REPOSITORIO
```

También puedes usar **Code → Download ZIP** y extraerlo. En VS Code selecciona
**Archivo → Abrir carpeta** y abre la carpeta que contiene `pom.xml`, `mvnw.cmd`,
`.mvn` y `src`. Espera a que Java termine de importar el proyecto Maven.

## 3. Crear la base de datos de tu computadora

Solo si todavía no tienes instalada la base de Pelucas Caninas:

1. Abre MySQL Workbench y conéctate a tu servidor local.
2. Usa **File → Open SQL Script** y selecciona `database/pelucas_caninas.sql`.
3. Ejecuta el archivo completo. Crea `pelucas_caninas`, sus diez tablas y los datos iniciales.
4. Actualiza la lista de esquemas. Para comprobar las tablas, ejecuta:

```sql
USE pelucas_caninas;
SHOW TABLES;
```

**Ejecuta el script de instalación una sola vez, en una base nueva.** Si ya tienes
las tablas, no lo vuelvas a importar: conserva tus registros existentes.
`spring.jpa.hibernate.ddl-auto=validate` comprueba el esquema; no lo crea ni modifica.
Cada compañero usa su propia instancia y sus propios datos locales.

## 4. Configurar el usuario y la contraseña de MySQL

En la terminal PowerShell de VS Code, dentro del proyecto:

```powershell
Copy-Item .env.example .env
```

Hazlo solo si no existe ya tu `.env`, para no reemplazar tu configuración.
Después abre `.env` en VS Code y completa estos valores:

```dotenv
DB_URL=jdbc:mysql://localhost:3306/pelucas_caninas?allowPublicKeyRetrieval=true&connectionTimeZone=America/Lima
DB_USER=root
DB_PASSWORD=TU_CONTRASEÑA_LOCAL
```

Usa tu contraseña de MySQL, no la de otro integrante. Si tu usuario no tiene
contraseña, deja `DB_PASSWORD=` vacío. Si usas otro usuario o puerto, modifica
`DB_USER` o `DB_URL`. `.env` está excluido de Git: comparte únicamente `.env.example`.

## 5. Ejecutar con un clic en VS Code (recomendado)

El repositorio incluye `.vscode/launch.json`, que carga `${workspaceFolder}/.env`.

1. Abre **Ejecutar y depurar** con **Ctrl+Shift+D**.
2. Selecciona **Spring Boot-PelucasCaninasApplication<pelucas-caninas>**.
3. Pulsa el triángulo verde o **F5**. **Ctrl+F5** ejecuta sin depuración.
4. Espera a que la consola indique que Tomcat inició en el puerto **8080**.
5. Abre [el sitio](http://localhost:8080) o [el administrador](http://localhost:8080/admin).

Para detenerlo, pulsa el botón rojo de la depuración. Si cambias `.env`, detén y
vuelve a iniciar la aplicación. Si la configuración no aparece, comprueba que
abriste la carpeta de `pom.xml` y que las extensiones Java terminaron de cargar.

### Alternativa: ejecutar desde una terminal PowerShell

Maven desde la terminal **no carga `.env` automáticamente**. Configura las
variables en esa misma terminal antes de arrancar:

```powershell
$env:DB_USER = 'root'
$env:DB_URL = 'jdbc:mysql://localhost:3306/pelucas_caninas?allowPublicKeyRetrieval=true&connectionTimeZone=America/Lima'
$credencial = Read-Host 'Contraseña de MySQL' -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $credencial).Password
.\mvnw.cmd spring-boot:run
```

Si MySQL no tiene contraseña, reemplaza las dos líneas de captura por
`$env:DB_PASSWORD = ''`. Detén el servidor con **Ctrl+C**.
En macOS/Linux, usa variables de entorno de tu terminal y `./mvnw spring-boot:run`;
si el wrapper no tiene permiso de ejecución, primero ejecuta `chmod +x mvnw`.

## 6. Comprobar que todo funciona

- `/`: inicio, galería de mascotas, mapa y preguntas frecuentes.
- `/productos`: catálogo, filtros, búsqueda y carrito.
- `/servicios`, `/nosotros`, `/contacto`: páginas públicas independientes.
- `/login`: formulario de inicio de sesión; la autenticación queda para el cuarto avance.
- `/admin`: acceso al panel; prueba crear una categoría y después un producto asociado.
- `/admin/servicios`: gestión de planes y cuidados adicionales.

Las consultas y reservas continúan mediante WhatsApp. El libro de reclamaciones
solo valida el formulario de ejemplo; todavía no registra reclamos en MySQL.

## Problemas frecuentes

| Mensaje o problema | Qué revisar |
| --- | --- |
| `Access denied for user` | Usuario y contraseña de tu MySQL en `.env`; reinicia la ejecución. |
| `Communications link failure` / conexión rechazada | MySQL Server debe estar iniciado y el puerto de `DB_URL` debe ser el correcto. |
| `Unknown database` / `Schema-validation: missing table` | Ejecuta el script completo solo si aún no instalaste la base y sus diez tablas. |
| `Port 8080 was already in use` | Detén la otra ejecución. Si cambias `server.port` en `application.properties`, abre la web con ese nuevo puerto. |
| Java no admite la versión 25 | Selecciona JDK 25 en **Java: Configure Java Runtime** y revisa `JAVA_HOME`. |
| Maven no descarga dependencias | Revisa tu conexión a Internet y vuelve a ejecutar el wrapper. |
| Faltan imágenes o estilos externos | Comprueba la conexión y recarga el navegador. Las imágenes locales vienen en el repositorio. |

Para recibir cambios del grupo, detén el proyecto y usa `git pull`. Si Git indica
cambios locales en conflicto, consérvalos y resuélvelos con tu grupo antes de continuar.

## Funcionalidades de este avance

- CRUD de categorías: `/admin/categorias`.
- CRUD de productos: `/admin/productos`, con selector de categoría.
- CRUD de servicios: `/admin/servicios`, para planes y servicios adicionales,
  con precio y duración opcionales, descripción, imagen, destacado y estado.
- Validaciones en servidor y formularios, mensajes de éxito/error y confirmación
  antes de eliminar. Las eliminaciones se realizan por POST.
- El catálogo público y sus filtros leen MySQL. Los productos INACTIVOS se ocultan.
- La página de servicios y las opciones de reserva también leen MySQL y ocultan
  los servicios INACTIVOS. Los precios sin definir se muestran como «Consultar
  precio». Los beneficios de un plan se separan por líneas o punto y coma.
- Una categoría con productos no se puede eliminar. Un producto con pedidos
  asociados tampoco; puede marcarse INACTIVO para conservar el historial.
- Los servicios con citas asociadas no se pueden eliminar; pueden desactivarse.
- La imagen se indica mediante una ruta local, por ejemplo `/img/bexter.jpg`.
  Debe existir en `src/main/resources/static/img`; no hay carga de archivos.
- Hibernate valida el esquema existente; no crea, borra ni modifica tablas.

El panel todavía es de acceso directo: los roles de la BD y el formulario de login
no autentican ni restringen rutas. Spring Security se incorporará en el cuarto
avance. La tabla usuario queda preparada en MySQL sin CRUD de cuentas en este paso.
La galería conserva las imágenes del sitio en memoria.
Las antiguas direcciones con `.html` y `/detalle-producto?id=...` se han retirado;
se utilizan las rutas actuales, por ejemplo `/productos` y `/servicios`.
La ficha individual `/productos/{id}` también se retiró: los productos se
consultan y se añaden al carrito desde el catálogo.

## Diseño público

Las páginas públicas adaptan el diseño de Figma Make a HTML/Thymeleaf. La
navegación conserva rutas independientes para servicios, productos, nosotros,
contacto y libro de reclamaciones. Los productos se añaden al carrito desde su
tarjeta, sin navegar a una ficha separada. El inicio conserva la galería de
mascotas y la ubicación; las preguntas frecuentes aparecen después del mapa y se
retiró la sección de recomendaciones.

Los estilos compartidos están en `style-figma.css` y su adaptación al proyecto en
`style-adaptaciones.css`. Las fotografías estáticas de la referencia se guardan en
`static/img/figma`; las imágenes de productos y servicios siguen viniendo de MySQL.
El menú móvil, el carrusel, los filtros, el carrito y el modo oscuro funcionan con
JavaScript, conservando las operaciones del administrador.

Los bloques aparecen con animaciones al bajar y vuelven a animarse al subir.
`scroll-reveal.js` coordina estos efectos y respeta la preferencia del sistema
de reducir movimiento. El login utiliza la imagen local `img-login.webp`.

Las páginas públicas cargan `style-general.css`, `style-figma.css` y
`style-adaptaciones.css`. Administración, documentos legales y errores agregan su CSS específico mediante el fragmento `head`. Las hojas del
diseño anterior se retiraron; se usa `null` cuando la página no necesita CSS propio.

## Estructura

`entity` contiene diez entidades JPA: usuario, cliente, mascota, categoria_producto,
producto, servicio, cita, pedido, detalle_pedido y reclamo. Cada una incluye las
validaciones de sus datos y los mapeos de sus claves foráneas. Los identificadores
se generan en MySQL y los booleanos primitivos no admiten null. El subtotal del
detalle de pedido es de solo lectura y MySQL lo calcula al insertar o actualizar.

`repository` accede a MySQL con Spring Data JPA. `service` implementa consultas y
operaciones transaccionales. `dto` contiene los formularios validados. `controller`
gestiona las rutas Thymeleaf. Estas cuatro capas y los CRUD HTML están implementados
para categorías, productos y servicios; las otras siete tablas tienen por ahora
su entidad. Los mapeos nuevos no activan el inicio de sesión ni el registro de
citas, pedidos o reclamos desde la web; Spring Security sigue pendiente para el
cuarto avance.
`model/Producto` es la vista de lectura utilizada por el catálogo público.

## Pruebas

Las pruebas HTTP verifican el sitio y el CRUD completo contra MySQL. Necesitan una
instancia/base **exclusiva para pruebas**, con el esquema y los datos iniciales del
script. No las apuntes a tu base de trabajo: prueban inserciones y eliminaciones.
El perfil test exige TEST_DB_URL explícitamente para evitar usar la conexión normal.

```powershell
$env:TEST_DB_URL = 'jdbc:mysql://localhost:33316/pelucas_caninas?allowPublicKeyRetrieval=true'
$env:TEST_DB_USER = 'root'
# Configura TEST_DB_PASSWORD si tu instancia de pruebas tiene contraseña.
.\mvnw.cmd test
```

El puerto 33316 es un ejemplo para una segunda instancia local; no se crea
automáticamente al ejecutar Maven.
