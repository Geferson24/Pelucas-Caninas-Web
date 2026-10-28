package com.veterinaria.pelucascaninas;

import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ModulosAdminTest {
    @LocalServerPort int port;
    @Autowired JdbcTemplate jdbc;
    private final HttpClient client = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
    private final Map<String, List<Integer>> creados = new LinkedHashMap<>();
    private static final Map<String, String> TABLAS = Map.of("usuarios", "usuario", "clientes", "cliente", "mascotas", "mascota",
            "citas", "cita", "pedidos", "pedido", "detalles-pedido", "detalle_pedido", "reclamos", "reclamo");
    private static final Map<String, String> CLAVES = Map.of("usuario", "id_usuario", "cliente", "id_cliente", "mascota", "id_mascota",
            "cita", "id_cita", "pedido", "id_pedido", "detalle_pedido", "id_detalle", "reclamo", "id_reclamo");
    private String unico() { return "test-" + UUID.randomUUID().toString().substring(0, 10); }

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }
    private HttpResponse<String> post(String path, Map<String, String> campos) throws Exception {
        var body = campos.entrySet().stream().map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8)).collect(Collectors.joining("&"));
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }
    private String seguir(HttpResponse<String> response) throws Exception {
        assertThat(response.statusCode()).isEqualTo(302);
        var destino = response.uri().resolve(response.headers().firstValue("location").orElseThrow());
        return client.send(HttpRequest.newBuilder(destino).GET().build(), HttpResponse.BodyHandlers.ofString()).body();
    }
    private int crear(String ruta, Map<String, String> datos) throws Exception {
        var response = post("/admin/" + ruta, datos);
        assertThat(response.statusCode()).withFailMessage(response.body()).isEqualTo(302);
        seguir(response);
        var tabla = TABLAS.get(ruta);
        int id = jdbc.queryForObject("SELECT MAX(" + CLAVES.get(tabla) + ") FROM " + tabla, Integer.class);
        creados.computeIfAbsent(tabla, key -> new ArrayList<>()).add(id);
        return id;
    }
    private Map<String, String> usuario(String correo, boolean activo, String contrasena) {
        return Map.of("nombre", "Familia " + unico(), "correo", correo, "rol", "CLIENTE", "activo", String.valueOf(activo), "contrasena", contrasena);
    }
    private Map<String, String> cliente(String nombre, Integer usuario) {
        return Map.of("nombres", nombre, "apellidos", "Prueba", "telefono", "+51 999 888 777", "correo", "familia@example.com", "idUsuario", usuario == null ? "" : usuario.toString());
    }
    private Map<String, String> pedido(int cliente) {
        return Map.of("idCliente", "" + cliente, "fecha", "2020-01-02T10:20:30", "estado", "PENDIENTE", "canal", "LOCAL", "observaciones", "Pedido de prueba");
    }
    private Map<String, String> detalle(int pedido, int producto, int cantidad, String precio) {
        return Map.of("idPedido", "" + pedido, "idProducto", "" + producto, "cantidad", "" + cantidad, "precioUnitario", precio);
    }
    private java.math.BigDecimal total(int pedido) {
        return jdbc.queryForObject("SELECT total FROM pedido WHERE id_pedido=?", java.math.BigDecimal.class, pedido);
    }
    @AfterEach
    void limpiar() {
        for (String tabla : List.of("reclamo", "cita", "detalle_pedido", "pedido", "mascota", "cliente", "usuario"))
            for (Integer id : creados.getOrDefault(tabla, List.of()))
                jdbc.update("DELETE FROM " + tabla + " WHERE " + CLAVES.get(tabla) + "=?", id);
    }

    @ParameterizedTest
    @ValueSource(strings = {"usuarios", "clientes", "mascotas", "citas", "pedidos", "detalles-pedido", "reclamos"})
    void paginasListanYOfrecenFormularioSinExponerHashes(String ruta) throws Exception {
        for (String path : List.of("/admin/" + ruta, "/admin/" + ruta + "/nuevo")) {
            var response = get(path);
            assertThat(response.statusCode()).withFailMessage(response.body()).isEqualTo(200);
            assertThat(response.body()).contains("admin-header", "admin-footer", "Administrador").doesNotContain("th:replace", "th:each", "passwordHash", "password_hash");
        }
        assertThat(get("/admin/" + ruta + "/9999999/editar").statusCode()).isEqualTo(404);
        assertThat(get("/admin/" + ruta + "/1/eliminar").statusCode()).isEqualTo(405);
    }

    @ParameterizedTest
    @ValueSource(strings = {"usuarios", "clientes", "mascotas", "citas", "pedidos", "detalles-pedido", "reclamos"})
    void rechazaFormulariosVaciosSinInsertar(String ruta) throws Exception {
        var tabla = TABLAS.get(ruta);
        var antes = jdbc.queryForObject("SELECT COUNT(*) FROM " + tabla, Long.class);
        var response = post("/admin/" + ruta, Map.of());
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("Revisa los campos indicados");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM " + tabla, Long.class)).isEqualTo(antes);
    }

    @Test
    void crudCompletoDeLosSieteModulosYFormulariosPrecargados() throws Exception {
        String correo = unico() + "@example.com";
        int u = crear("usuarios", usuario(correo, true, "Clave de prueba 2026!"));
        int c = crear("clientes", cliente("Familia inicial", u));
        int m = crear("mascotas", Map.of("idCliente", "" + c, "nombre", "Luna inicial", "especie", "PERRO", "raza", "Yorkshire", "fechaNacimiento", "2019-02-03", "observaciones", "Tranquila"));
        int cita = crear("citas", Map.of("idMascota", "" + m, "idServicio", "1", "fecha", "2020-01-02T11:22:33", "estado", "PENDIENTE", "precioAcordado", "35.50", "observaciones", "Cita inicial"));
        int p = crear("pedidos", pedido(c));
        int d = crear("detalles-pedido", detalle(p, 1, 2, "10.50"));
        int r = crear("reclamos", Map.of("idCliente", "" + c, "nombres", "Reclamante inicial", "documento", "12345678", "correo", correo, "telefono", "999888777", "fecha", "2020-01-02T12:22:33", "tipo", "RECLAMO", "descripcion", "Descripción inicial", "solicitud", "Revisión inicial", "estado", "PENDIENTE"));
        var ids = Map.of("usuarios", u, "clientes", c, "mascotas", m, "citas", cita, "pedidos", p, "detalles-pedido", d, "reclamos", r);
        for (var entry : ids.entrySet()) {
            var html = get("/admin/" + entry.getKey() + "/" + entry.getValue() + "/editar");
            assertThat(html.statusCode()).isEqualTo(200);
            assertThat(html.body()).contains("/admin/" + entry.getKey() + "/" + entry.getValue());
        }
        assertThat(get("/admin/mascotas/" + m + "/editar").body()).contains("Yorkshire", "2019-02-03", "Tranquila");
        assertThat(get("/admin/citas/" + cita + "/editar").body()).contains("2020-01-02T11:22:33", "35.50", "Cita inicial");
        assertThat(org.springframework.web.util.HtmlUtils.htmlUnescape(get("/admin/reclamos/" + r + "/editar").body()))
                .contains("12345678", "Descripción inicial", "Revisión inicial");
        assertThat(total(p)).isEqualByComparingTo("21.00");
        var usuarioEditado = new HashMap<>(usuario(correo, true, ""));
        usuarioEditado.put("nombre", "Cuenta editada");
        usuarioEditado.put("fechaRegistro", "2099-01-01T00:00:00");
        assertThat(seguir(post("/admin/usuarios/" + u, usuarioEditado))).contains("Cuenta editada");
        assertThat(seguir(post("/admin/clientes/" + c, cliente("Familia editada", u)))).contains("Familia editada");
        assertThat(seguir(post("/admin/mascotas/" + m, Map.of("idCliente", "" + c, "nombre", "Luna editada", "especie", "PERRO", "raza", "Mestiza", "fechaNacimiento", "2019-03-04", "observaciones", "Datos actualizados")))).contains("Luna editada");
        assertThat(seguir(post("/admin/citas/" + cita, Map.of("idMascota", "" + m, "idServicio", "2", "fecha", "2020-03-04T14:15:16", "estado", "CONFIRMADA", "precioAcordado", "42.00", "observaciones", "Cita editada")))).contains("Cita editada");
        var pedidoEditado = new HashMap<>(pedido(c)); pedidoEditado.put("estado", "CONFIRMADO"); pedidoEditado.put("total", "9999.99");
        assertThat(seguir(post("/admin/pedidos/" + p, pedidoEditado))).contains("CONFIRMADO");
        assertThat(total(p)).isEqualByComparingTo("21.00");
        assertThat(seguir(post("/admin/detalles-pedido/" + d, detalle(p, 2, 3, "12.00")))).contains("36.00");
        assertThat(total(p)).isEqualByComparingTo("36.00");
        assertThat(seguir(post("/admin/reclamos/" + r, Map.of("nombres", "Reclamante editado", "documento", "87654321", "correo", correo, "fecha", "2020-04-05T11:12:13", "tipo", "QUEJA", "descripcion", "Descripción editada", "solicitud", "Solución editada", "estado", "ATENDIDO")))).contains("Reclamante editado");
        assertThat(seguir(post("/admin/usuarios/" + u + "/eliminar", Map.of()))).contains("asociado a un cliente");
        assertThat(seguir(post("/admin/clientes/" + c + "/eliminar", Map.of()))).contains("mascotas, pedidos o reclamos");
        assertThat(seguir(post("/admin/mascotas/" + m + "/eliminar", Map.of()))).contains("citas asociadas");
        assertThat(seguir(post("/admin/pedidos/" + p + "/eliminar", Map.of()))).contains("contiene detalles");
        for (String ruta : List.of("reclamos", "citas", "detalles-pedido", "pedidos", "mascotas", "clientes", "usuarios")) {
            assertThat(seguir(post("/admin/" + ruta + "/" + ids.get(ruta) + "/eliminar", Map.of()))).contains("Registro eliminado.");
            assertThat(get("/admin/" + ruta + "/" + ids.get(ruta) + "/editar").statusCode()).isEqualTo(404);
        }
    }

    @Test
    void detallesRecalculanAmbosPedidosYReviertenDuplicadosODesbordamientos() throws Exception {
        int c = crear("clientes", cliente(unico(), null));
        int p1 = crear("pedidos", pedido(c)), p2 = crear("pedidos", pedido(c));
        int d1 = crear("detalles-pedido", detalle(p1, 1, 2, "10.00"));
        crear("detalles-pedido", detalle(p1, 2, 1, "5.00"));
        var duplicado = post("/admin/detalles-pedido", detalle(p1, 1, 1, "1.00"));
        assertThat(duplicado.statusCode()).isEqualTo(200);
        assertThat(duplicado.body()).contains("ya contiene este producto");
        assertThat(total(p1)).isEqualByComparingTo("25.00");
        assertThat(post("/admin/detalles-pedido/" + d1, detalle(p2, 1, 3, "7.00")).statusCode()).isEqualTo(302);
        assertThat(total(p1)).isEqualByComparingTo("5.00");
        assertThat(total(p2)).isEqualByComparingTo("21.00");
        var filtro = get("/admin/detalles-pedido?pedido=" + p1);
        assertThat(filtro.body()).contains("Añadir un producto", "/" + creados.get("detalle_pedido").getLast() + "/editar").doesNotContain("/" + d1 + "/editar");
        var porSubtotal = post("/admin/detalles-pedido", detalle(p2, 3, Integer.MAX_VALUE, "99999999.99"));
        assertThat(porSubtotal.statusCode()).isEqualTo(200);
        assertThat(porSubtotal.body()).contains("El subtotal no puede superar");
        var porTotal = post("/admin/detalles-pedido", detalle(p2, 3, 100, "99999999.99"));
        assertThat(porTotal.statusCode()).isEqualTo(200);
        assertThat(porTotal.body()).contains("total del pedido supera");
        assertThat(total(p2)).isEqualByComparingTo("21.00");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM detalle_pedido WHERE id_pedido=?", Long.class, p2)).isEqualTo(1);
        assertThat(post("/admin/detalles-pedido/" + d1 + "/eliminar", Map.of()).statusCode()).isEqualTo(302);
        assertThat(total(p2)).isEqualByComparingTo("0.00");
    }

    @Test
    void cuentasNoMuestranContrasenasYConservanElHashAlEditar() throws Exception {
        String correo = unico() + "@example.com", clave = "Clave privada de prueba!";
        var sinClave = post("/admin/usuarios", usuario(correo, true, ""));
        assertThat(sinClave.statusCode()).isEqualTo(200);
        assertThat(sinClave.body()).contains("Escribe una contraseña para activar");
        int u = crear("usuarios", usuario(correo, true, clave));
        String hash = jdbc.queryForObject("SELECT password_hash FROM usuario WHERE id_usuario=?", String.class, u);
        assertThat(hash).startsWith("pbkdf2_sha256$600000$").doesNotContain(clave);
        assertThat(get("/admin/usuarios").body()).doesNotContain(hash, clave);
        assertThat(get("/admin/usuarios/" + u + "/editar").body()).doesNotContain(hash, clave);
        assertThat(post("/admin/usuarios/" + u, usuario(correo, true, "")).statusCode()).isEqualTo(302);
        assertThat(jdbc.queryForObject("SELECT password_hash FROM usuario WHERE id_usuario=?", String.class, u)).isEqualTo(hash);
        var invalida = post("/admin/usuarios/" + u, usuario(correo, true, "corta"));
        assertThat(invalida.statusCode()).isEqualTo(200);
        assertThat(invalida.body()).doesNotContain("value=\"corta\"", hash);
        var correoDuplicado = post("/admin/usuarios", usuario(correo.toUpperCase(Locale.ROOT), false, ""));
        assertThat(correoDuplicado.statusCode()).isEqualTo(200);
        assertThat(correoDuplicado.body()).contains("Ya existe una cuenta");
        int c = crear("clientes", cliente(unico(), u));
        var vinculoDuplicado = post("/admin/clientes", cliente(unico(), u));
        assertThat(vinculoDuplicado.statusCode()).isEqualTo(200);
        assertThat(vinculoDuplicado.body()).contains("vinculada a otro cliente");
        assertThat(jdbc.queryForObject("SELECT id_usuario FROM cliente WHERE id_cliente=?", Integer.class, c)).isEqualTo(u);
        assertThat(post("/admin/usuarios/" + u, usuario(correo, true, clave)).statusCode()).isEqualTo(302);
        assertThat(jdbc.queryForObject("SELECT password_hash FROM usuario WHERE id_usuario=?", String.class, u))
                .startsWith("pbkdf2_sha256$600000$").isNotEqualTo(hash);
    }

    @Test
    void fechasYFormatosInvalidosSeRechazanSinPerderLosDatosDelFormulario() throws Exception {
        int c = crear("clientes", cliente(unico(), null));
        var futuro = new HashMap<>(pedido(c)); futuro.put("fecha", "2999-01-01T00:00:00");
        var response = post("/admin/pedidos", futuro);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("La fecha no puede estar en el futuro", "Pedido de prueba");
        var invalida = new HashMap<>(pedido(c)); invalida.put("fecha", "fecha-invalida");
        assertThat(post("/admin/pedidos", invalida).statusCode()).isEqualTo(200);
        var nacimientoFuturo = post("/admin/mascotas", Map.of("idCliente", "" + c, "nombre", "Pelusa", "especie", "PERRO", "fechaNacimiento", "2999-01-01"));
        assertThat(nacimientoFuturo.statusCode()).isEqualTo(200);
        assertThat(nacimientoFuturo.body()).contains("fecha de nacimiento no puede ser futura");
        assertThat(post("/admin/clientes", cliente(unico(), null)).statusCode()).isEqualTo(302);
        int extra = jdbc.queryForObject("SELECT MAX(id_cliente) FROM cliente", Integer.class);
        creados.get("cliente").add(extra);
        var numericoInvalido = detalle(999999, 1, 1, "importe");
        assertThat(post("/admin/detalles-pedido", numericoInvalido).statusCode()).isEqualTo(200);
    }

    @ParameterizedTest
    @ValueSource(strings = {"clientes", "mascotas", "citas", "pedidos", "detalles-pedido", "reclamos"})
    void rechazaClavesForaneasInexistentes(String ruta) throws Exception {
        Map<String, String> datos = switch (ruta) {
            case "clientes" -> cliente(unico(), 99999999);
            case "mascotas" -> Map.of("idCliente", "99999999", "nombre", "Pelusa", "especie", "PERRO");
            case "citas" -> Map.of("idMascota", "99999999", "idServicio", "1", "fecha", "2020-01-01T12:00:00", "estado", "PENDIENTE");
            case "pedidos" -> pedido(99999999);
            case "detalles-pedido" -> detalle(99999999, 1, 1, "1.00");
            default -> Map.of("idCliente", "99999999", "nombres", "Prueba", "documento", "12345678", "correo", "prueba@example.com", "fecha", "2020-01-01T12:00:00", "tipo", "QUEJA", "descripcion", "Detalle", "solicitud", "Solución", "estado", "PENDIENTE");
        };
        var response = post("/admin/" + ruta, datos);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("Revisa los campos indicados").doesNotContain("Whitelabel Error Page");
    }
}
