package com.veterinaria.pelucascaninas;

import com.veterinaria.pelucascaninas.repository.ServicioRepository;
import com.veterinaria.pelucascaninas.service.CatalogoService;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ServicioAdminTest {
    @LocalServerPort int port;
    @Autowired ServicioRepository servicios;
    @Autowired CatalogoService catalogo;
    @Autowired JdbcTemplate jdbc;
    private final HttpClient client = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }
    private HttpResponse<String> post(String path, Map<String, String> datos) throws Exception {
        var body = datos.entrySet().stream().map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8)).collect(Collectors.joining("&"));
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }
    private HttpResponse<String> seguir(HttpResponse<String> response) throws Exception {
        var destino = response.uri().resolve(response.headers().firstValue("location").orElseThrow());
        return client.send(HttpRequest.newBuilder(destino).GET().build(), HttpResponse.BodyHandlers.ofString());
    }
    private String unique() { return "Servicio prueba " + UUID.randomUUID().toString().substring(0, 8); }
    private HashMap<String, String> formulario(String nombre) {
        return new HashMap<>(Map.of("nombre", nombre, "descripcion", "Primer beneficio; Segundo beneficio\nTercer beneficio",
                "precio", "45.50", "duracionMinutos", "60", "imagen", "/img/basico.jpg", "tipo", "PLAN",
                "destacado", "true", "estado", "ACTIVO"));
    }
    private int idPorNombre(String nombre) {
        return servicios.findAll().stream().filter(s -> s.getNombre().equals(nombre)).findFirst().orElseThrow().getId();
    }
    private void guardado(String path, Map<String, String> datos, String mensaje) throws Exception {
        var response = post(path, datos);
        assertThat(response.statusCode()).isEqualTo(302);
        assertThat(seguir(response).body()).contains(mensaje);
    }

    @Test
    void crudPublicacionYReservaUsanMySql() throws Exception {
        String nombre = unique();
        Integer id = null;
        try {
            var datos = formulario(nombre);
            guardado("/admin/servicios", datos, "Servicio registrado.");
            id = idPorNombre(nombre);
            final int servicioId = id;
            assertThat(servicios.findById(id).orElseThrow().isDestacado()).isTrue();
            var edicion = get("/admin/servicios/" + id + "/editar");
            assertThat(edicion.statusCode()).isEqualTo(200);
            assertThat(edicion.body()).contains(nombre, "value=\"45.50\"", "value=\"60\"",
                    "/img/basico.jpg", "checked=\"checked\"", "Primer beneficio");
            assertThat(get("/servicios").body()).contains(nombre, "S/ 45.50", "Duración estimada: 60 min");
            assertThat(catalogo.listarPlanes().stream().filter(p -> p.getId() == servicioId).findFirst().orElseThrow()
                    .getBeneficios()).containsExactly("Primer beneficio", "Segundo beneficio", "Tercer beneficio");
            assertThat(get("/").body()).contains("value=\"" + nombre + "\"");

            datos.put("tipo", "ADICIONAL"); datos.put("precio", ""); datos.put("duracionMinutos", "");
            datos.put("imagen", ""); datos.remove("destacado");
            guardado("/admin/servicios/" + id, datos, "Servicio actualizado.");
            var almacenado = servicios.findById(id).orElseThrow();
            assertThat(almacenado.getPrecio()).isNull();
            assertThat(almacenado.getDuracionMinutos()).isNull();
            assertThat(almacenado.isDestacado()).isFalse();
            assertThat(catalogo.listarPlanes()).noneMatch(p -> p.getId() == servicioId);
            assertThat(catalogo.listarServicios()).anyMatch(s -> s.getId() == servicioId && s.getImagen().equals("/img/perritos.png"));
            assertThat(get("/servicios").body()).contains(nombre, "Consultar precio").doesNotContain("S/ null");

            datos.put("estado", "INACTIVO");
            guardado("/admin/servicios/" + id, datos, "Servicio actualizado.");
            assertThat(get("/servicios").body()).doesNotContain(nombre);
            assertThat(get("/").body()).doesNotContain(nombre);
            assertThat(get("/admin/servicios").body()).contains(nombre, "Inactivo");
            var borrado = post("/admin/servicios/" + id + "/eliminar", Map.of());
            assertThat(borrado.statusCode()).isEqualTo(302);
            assertThat(seguir(borrado).body()).contains("Servicio eliminado.");
            assertThat(servicios.existsById(id)).isFalse();
        } finally {
            if (id != null && servicios.existsById(id)) servicios.deleteById(id);
        }
    }

    @Test
    void validacionesYDuplicadosConservanFormularioSinGuardar() throws Exception {
        long total = servicios.count();
        var datos = formulario(unique());
        datos.put("precio", "-1"); datos.put("duracionMinutos", "0");
        var response = post("/admin/servicios", datos);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains(datos.get("nombre"), "El precio no puede ser negativo", "La duración debe ser mayor que cero");
        datos.put("precio", "abc"); datos.put("duracionMinutos", "2.5");
        assertThat(post("/admin/servicios", datos).body()).contains("Ingresa un precio válido", "Ingresa una duración entera válida");
        datos = formulario("Baño Básico");
        assertThat(post("/admin/servicios", datos).body()).contains("Ya existe un servicio con ese nombre");
        datos = formulario(" "); datos.put("tipo", "OTRO"); datos.put("estado", "OTRO"); datos.put("imagen", "https://ejemplo.com/foto.jpg");
        assertThat(post("/admin/servicios", datos).body()).contains("Escribe el nombre", "Selecciona un tipo válido", "Selecciona un estado válido", "Usa una imagen local");
        assertThat(servicios.count()).isEqualTo(total);
    }

    @Test
    void servicioConCitasNoSeElimina() throws Exception {
        Integer clienteId = null, mascotaId = null, citaId = null;
        try {
            jdbc.update("INSERT INTO cliente(nombres,telefono) VALUES (?,?)", unique(), "999999999");
            clienteId = jdbc.queryForObject("SELECT MAX(id_cliente) FROM cliente", Integer.class);
            jdbc.update("INSERT INTO mascota(id_cliente,nombre) VALUES (?,?)", clienteId, "Mascota de prueba");
            mascotaId = jdbc.queryForObject("SELECT MAX(id_mascota) FROM mascota", Integer.class);
            jdbc.update("INSERT INTO cita(id_mascota,id_servicio,fecha) VALUES (?,1,'2030-01-01 10:00:00')", mascotaId);
            citaId = jdbc.queryForObject("SELECT MAX(id_cita) FROM cita", Integer.class);
            var response = post("/admin/servicios/1/eliminar", Map.of());
            assertThat(response.statusCode()).isEqualTo(302);
            assertThat(seguir(response).body()).contains("tiene citas asociadas");
            assertThat(servicios.existsById(1)).isTrue();
        } finally {
            if (citaId != null) jdbc.update("DELETE FROM cita WHERE id_cita=?", citaId);
            if (mascotaId != null) jdbc.update("DELETE FROM mascota WHERE id_mascota=?", mascotaId);
            if (clienteId != null) jdbc.update("DELETE FROM cliente WHERE id_cliente=?", clienteId);
        }
    }

    @Test
    void estadosVaciosYRegistrosInexistentes() throws Exception {
        var estados = servicios.findAll().stream().collect(Collectors.toMap(s -> s.getId(), s -> s.getEstado()));
        try {
            jdbc.update("UPDATE servicio SET estado='INACTIVO'");
            var pagina = get("/servicios");
            assertThat(pagina.statusCode()).isEqualTo(200);
            assertThat(pagina.body()).contains("Pronto tendremos servicios disponibles")
                    .doesNotContain("class=\"service-plan ", "class=\"extra-service\"");
            assertThat(get("/").body()).doesNotContain("<option value=");
        } finally {
            estados.forEach((id, estado) -> jdbc.update("UPDATE servicio SET estado=? WHERE id_servicio=?", estado, id));
        }
        assertThat(get("/admin/servicios/999999/editar").statusCode()).isEqualTo(404);
        assertThat(post("/admin/servicios/999999", formulario(unique())).statusCode()).isEqualTo(404);
        assertThat(post("/admin/servicios/999999/eliminar", Map.of()).statusCode()).isEqualTo(404);
        assertThat(get("/admin/servicios/1/eliminar").statusCode()).isEqualTo(405);
    }
}
