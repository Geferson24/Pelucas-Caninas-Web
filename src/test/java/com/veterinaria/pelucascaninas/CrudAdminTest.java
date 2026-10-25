package com.veterinaria.pelucascaninas;

import com.veterinaria.pelucascaninas.repository.CategoriaProductoRepository;
import com.veterinaria.pelucascaninas.repository.ProductoRepository;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
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
class CrudAdminTest {
    @LocalServerPort int port;
    @Autowired CategoriaProductoRepository categorias;
    @Autowired ProductoRepository productos;
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
    private String unique() { return "test-" + UUID.randomUUID().toString().substring(0, 8); }
    private HttpResponse<String> seguir(HttpResponse<String> response) throws Exception {
        var destino = response.uri().resolve(response.headers().firstValue("location").orElseThrow());
        return client.send(HttpRequest.newBuilder(destino).GET().build(), HttpResponse.BodyHandlers.ofString());
    }
    private Map<String, String> producto(String nombre, int categoria, String estado) {
        return Map.of("nombre", nombre, "idCategoria", String.valueOf(categoria), "precio", "42.50",
                "stock", "7", "imagen", "/img/bexter.jpg", "descripcion", "Descripción de prueba", "estado", estado);
    }

    @Test
    void crudCompletoYVisibilidadPublica() throws Exception {
        var codigo = unique();
        Integer categoriaId = null, productoId = null;
        try {
            var creacionCategoria = post("/admin/categorias", Map.of("nombre", codigo, "codigo", codigo, "descripcion", "Prueba"));
            assertThat(creacionCategoria.statusCode()).isEqualTo(302);
            assertThat(seguir(creacionCategoria).body()).contains("Categoría registrada.");
            categoriaId = categorias.findAll().stream().filter(c -> c.getCodigo().equals(codigo)).findFirst().orElseThrow().getId();
            assertThat(get("/admin/categorias/" + categoriaId + "/editar").body()).contains(codigo, "Prueba");
            var actualizacionCategoria = post("/admin/categorias/" + categoriaId, Map.of("nombre", codigo + " editada", "codigo", codigo, "descripcion", "Actualizada"));
            assertThat(actualizacionCategoria.statusCode()).isEqualTo(302);
            assertThat(seguir(actualizacionCategoria).body()).contains("Categoría actualizada.");
            assertThat(categorias.findById(categoriaId).orElseThrow().getDescripcion()).isEqualTo("Actualizada");

            var creacionProducto = post("/admin/productos", producto(codigo, categoriaId, "ACTIVO"));
            assertThat(creacionProducto.statusCode()).isEqualTo(302);
            assertThat(seguir(creacionProducto).body()).contains("Producto registrado.");
            productoId = productos.findAll().stream().filter(p -> p.getNombre().equals(codigo)).findFirst().orElseThrow().getId();
            assertThat(get("/productos").body()).contains(codigo, "42.50");
            assertThat(get("/productos/" + productoId).statusCode()).isEqualTo(404);
            var edit = get("/admin/productos/" + productoId + "/editar");
            assertThat(edit.body()).contains("value=\"42.50\"", "value=\"7\"", "selected=\"selected\"", "/img/bexter.jpg");

            var eliminacionCategoria = post("/admin/categorias/" + categoriaId + "/eliminar", Map.of());
            assertThat(eliminacionCategoria.statusCode()).isEqualTo(302);
            assertThat(seguir(eliminacionCategoria).body()).contains("tiene productos asociados");
            assertThat(categorias.existsById(categoriaId)).isTrue();
            var actualizacionProducto = post("/admin/productos/" + productoId, producto(codigo, categoriaId, "INACTIVO"));
            assertThat(actualizacionProducto.statusCode()).isEqualTo(302);
            assertThat(seguir(actualizacionProducto).body()).contains("Producto actualizado.");
            assertThat(get("/productos").body()).doesNotContain("<h3>" + codigo + "</h3>");
            assertThat(get("/productos/" + productoId).statusCode()).isEqualTo(404);
            assertThat(get("/admin/productos").body()).contains(codigo, "Inactivo");

            assertThat(post("/admin/productos/" + productoId + "/eliminar", Map.of()).statusCode()).isEqualTo(302);
            assertThat(productos.existsById(productoId)).isFalse();
            assertThat(post("/admin/categorias/" + categoriaId + "/eliminar", Map.of()).statusCode()).isEqualTo(302);
            assertThat(categorias.existsById(categoriaId)).isFalse();
        } finally {
            if (productoId != null && productos.existsById(productoId)) productos.deleteById(productoId);
            if (categoriaId != null && categorias.existsById(categoriaId)) categorias.deleteById(categoriaId);
        }
    }

    @Test
    void erroresDeFormularioConservanDatosYNoPersisten() throws Exception {
        long total = productos.count();
        var nombre = unique();
        var datos = new java.util.HashMap<>(producto(nombre, 1, "ACTIVO"));
        datos.put("precio", "-2"); datos.put("stock", "-1");
        var response = post("/admin/productos", datos);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("El precio no puede ser negativo", "El stock no puede ser negativo", nombre);
        datos.put("precio", "texto"); datos.put("stock", "1.5");
        response = post("/admin/productos", datos);
        assertThat(response.body()).contains("Ingresa un precio válido", "Ingresa una cantidad entera válida");
        datos = new java.util.HashMap<>(producto(nombre, 999999, "ACTIVO"));
        assertThat(post("/admin/productos", datos).body()).contains("La categoría ya no existe");
        datos = new java.util.HashMap<>(producto(nombre, 1, "OTRO"));
        datos.put("imagen", "https://ejemplo.com/foto.jpg");
        assertThat(post("/admin/productos", datos).body()).contains("Selecciona un estado válido", "Usa una imagen local");
        assertThat(productos.count()).isEqualTo(total);
    }

    @Test
    void categoriaDuplicadaOVaciaNoSeGuarda() throws Exception {
        long total = categorias.count();
        var response = post("/admin/categorias", Map.of("nombre", "Aseo", "codigo", "aseo"));
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("Ya existe una categoría con ese nombre", "Ya existe una categoría con ese código");
        response = post("/admin/categorias", Map.of("nombre", " ", "codigo", "all"));
        assertThat(response.body()).contains("Escribe el nombre", "all está reservado");
        assertThat(categorias.count()).isEqualTo(total);
    }

    @Test
    void productoConPedidosNoSeElimina() throws Exception {
        Integer clienteId = null, pedidoId = null;
        try {
            jdbc.update("INSERT INTO cliente(nombres,telefono) VALUES (?,?)", unique(), "999999999");
            clienteId = jdbc.queryForObject("SELECT MAX(id_cliente) FROM cliente", Integer.class);
            jdbc.update("INSERT INTO pedido(id_cliente) VALUES (?)", clienteId);
            pedidoId = jdbc.queryForObject("SELECT MAX(id_pedido) FROM pedido", Integer.class);
            jdbc.update("INSERT INTO detalle_pedido(id_pedido,id_producto,cantidad,precio_unitario) VALUES (?,1,1,25)", pedidoId);
            var eliminacion = post("/admin/productos/1/eliminar", Map.of());
            assertThat(eliminacion.statusCode()).isEqualTo(302);
            assertThat(seguir(eliminacion).body()).contains("tiene pedidos asociados");
            assertThat(productos.existsById(1)).isTrue();
        } finally {
            if (pedidoId != null) {
                jdbc.update("DELETE FROM detalle_pedido WHERE id_pedido=?", pedidoId);
                jdbc.update("DELETE FROM pedido WHERE id_pedido=?", pedidoId);
            }
            if (clienteId != null) jdbc.update("DELETE FROM cliente WHERE id_cliente=?", clienteId);
        }
    }

    @Test
    void panelMuestraLosUltimosProductosInclusoSiEstanInactivos() throws Exception {
        var nombre = unique();
        Integer productoId = null;
        var totalAnterior = productos.count();
        try {
            assertThat(post("/admin/productos", producto(nombre, 1, "INACTIVO")).statusCode()).isEqualTo(302);
            productoId = jdbc.queryForObject("SELECT id_producto FROM producto WHERE nombre=?", Integer.class, nombre);
            var html = get("/admin").body();
            assertThat(html).contains(nombre, "Inactivo", "Productos recientes", "Aseo");
            var enlaces = java.util.regex.Pattern.compile("/admin/productos/(\\d+)/editar")
                    .matcher(html).results().map(m -> Integer.valueOf(m.group(1))).toList();
            assertThat(enlaces).hasSize(3).isSortedAccordingTo(java.util.Comparator.reverseOrder());
            assertThat(enlaces.getFirst()).isEqualTo(productoId);
            var conteos = java.util.regex.Pattern.compile("class=\"admin-count\"[^>]*>(\\d+)<")
                    .matcher(html).results().map(m -> Long.valueOf(m.group(1))).toList();
            assertThat(conteos.getFirst()).isEqualTo(totalAnterior + 1);
            assertThat(get("/productos").body()).doesNotContain(nombre);
        } finally {
            if (productoId != null) productos.deleteById(productoId);
        }
    }

    @Test
    void registrosInexistentesYEliminacionPorGet() throws Exception {
        assertThat(get("/admin/productos/999999/editar").statusCode()).isEqualTo(404);
        assertThat(get("/admin/categorias/999999/editar").statusCode()).isEqualTo(404);
        assertThat(get("/admin/productos/1/eliminar").statusCode()).isEqualTo(405);
        assertThat(get("/admin/categorias/1/eliminar").statusCode()).isEqualTo(405);
    }
}
