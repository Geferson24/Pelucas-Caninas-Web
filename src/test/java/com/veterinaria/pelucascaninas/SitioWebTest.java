package com.veterinaria.pelucascaninas;

import com.veterinaria.pelucascaninas.service.CatalogoService;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SitioWebTest {
    @LocalServerPort
    int port;
    @Autowired
    CatalogoService catalogo;
    private final HttpClient client = HttpClient.newHttpClient();

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Accept", "text/html").GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private long contarClase(String html, String clase) {
        return Pattern.compile("\\bclass=\"([^\"]*)\"").matcher(html).results()
                .filter(match -> Arrays.asList(match.group(1).split("\\s+")).contains(clase)).count();
    }

    @ParameterizedTest
    @ValueSource(strings = { "/", "/productos", "/servicios", "/nosotros", "/contacto", "/libro-reclamaciones",
            "/terminos", "/politica-privacidad", "/login", "/admin", "/admin/categorias",
            "/admin/categorias/nueva", "/admin/categorias/1/editar", "/admin/productos",
            "/admin/productos/nuevo", "/admin/productos/1/editar", "/admin/servicios",
            "/admin/servicios/nuevo", "/admin/servicios/1/editar" })
    void rutasCompartenFragmentosYRecursos(String path) throws Exception {
        var response = get(path);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("navbarPelucas", "footer-nuevo", "/css/style-figma.css", "/js/app.js");
        assertThat(response.body()).doesNotContain("th:replace", "th:each", "/vendor/", "../../static", "@{");
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 })
    void fichasRetiradasResponden404AunqueElProductoExista(int id) throws Exception {
        var response = get("/productos/" + id);
        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.body()).contains("Esta página se nos escapó", "navbarPelucas", "footer-nuevo");
    }

    @ParameterizedTest
    @ValueSource(strings = { "/no-existe", "/productos/999", "/productos/texto", "/otra/ruta",
            "/index.html", "/productos.html", "/servicios.html", "/nosotros.html", "/form-contacto.html",
            "/libro.html", "/politica-privacidad.html", "/terminos.html", "/detalle-producto?id=3" })
    void paginasInexistentesResponden404(String path) throws Exception {
        var response = get(path);
        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.body()).contains("Esta página se nos escapó", "Volver al inicio", "navbarPelucas",
                "footer-nuevo");
        assertThat(response.body()).doesNotContain("/vendor/", "th:replace");
    }

    @Test
    void catalogoYGaleriaRepitenDatos() throws Exception {
        assertThat(contarClase(get("/productos").body(), "product-card")).isEqualTo(10);
        var inicio = get("/").body();
        assertThat(contarClase(inicio, "hero")).isEqualTo(1);
        assertThat(contarClase(inicio, "pet-slide")).isEqualTo(3);
        assertThat(inicio).contains("Mascota Bianca", "Mascota Valentino");
        var datosMascotas = Pattern.compile("(?s)<template\\b[^>]*\\bid=\"pet-data\"[^>]*>(.*?)</template>")
                .matcher(inicio);
        assertThat(datosMascotas.find()).isTrue();
        assertThat(Pattern.compile("<img\\b").matcher(datosMascotas.group(1)).results().count()).isEqualTo(12);
    }

    @Test
    void condicionalDestacaSoloPlanPremium() throws Exception {
        var html = get("/servicios").body();
        assertThat(html.split("El más elegido", -1).length - 1).isEqualTo(1);
        assertThat(contarClase(html, "service-plan")).isEqualTo(3);
        assertThat(contarClase(html, "is-featured")).isEqualTo(1);
        assertThat(html).contains("Baño Premium", "S/ 37.00");
        for (var plan : catalogo.listarPlanes()) {
            assertThat(html).contains(plan.getEnlaceWhatsapp().replace("&", "&amp;"));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "/", "/servicios", "/productos", "/nosotros", "/contacto", "/libro-reclamaciones" })
    void navegacionConservaPaginasIndependientes(String path) throws Exception {
        var html = get(path).body();
        for (var destino : new String[] { "/servicios", "/productos", "/nosotros", "/contacto", "/libro-reclamaciones" }) {
            assertThat(html).contains("href=\"" + destino + "\"");
        }
        assertThat(html).doesNotContain("href=\"#servicios\"", "href=\"#productos\"", "href=\"#nosotros\"");
    }

    @Test
    void catalogoNoEnlazaPaginasDeDetalle() throws Exception {
        var html = get("/productos").body();
        assertThat(Pattern.compile("href=\"/productos/\\d+\"").matcher(html).find()).isFalse();
        assertThat(contarClase(html, "btn-add-cart")).isEqualTo(10);
        assertThat(html).contains("id=\"buscadorProductos\"", "data-filter=\"all\"", "data-precio=", "data-categoria=");
    }

    @Test
    void preguntasFrecuentesSiguenAlMapaSinRecomendaciones() throws Exception {
        var html = get("/").body();
        assertThat(html).contains("id=\"reserva\"", "id=\"preguntas-frecuentes\"", "www.google.com/maps/embed");
        assertThat(html).doesNotContain("Recomendaciones para tu mascota", "class=\"recomendaciones");
        assertThat(html.indexOf("www.google.com/maps/embed")).isLessThan(html.indexOf("id=\"preguntas-frecuentes\""));
        assertThat(Pattern.compile("(?s)<section\\b[^>]*\\bid=\"reserva\"[^>]*>.*?</section>\\s*(?:<!--.*?-->\\s*)?<section\\b[^>]*\\bid=\"preguntas-frecuentes\"")
                .matcher(html).find()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = { "/css/style-general.css", "/css/style-figma.css", "/css/style-error.css", "/js/app.js", "/img/bexter.jpg" })
    void recursosLocalesResponden(String path) throws Exception {
        assertThat(get(path).statusCode()).isEqualTo(200);
    }

    @Test
    void loginPendienteInformaSinRepetirCredenciales() throws Exception {
        var navegador = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
        var uri = URI.create("http://localhost:" + port + "/login");
        var envio = navegador.send(HttpRequest.newBuilder(uri)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "username=admin%40example.com&password=clave-de-prueba"))
                .build(), HttpResponse.BodyHandlers.ofString());
        assertThat(envio.statusCode()).isEqualTo(302);
        var destino = URI.create(envio.headers().firstValue("location").orElseThrow());
        assertThat(destino.getPath().split(";", 2)[0]).isEqualTo("/login");

        var pagina = navegador.send(HttpRequest.newBuilder(uri.resolve(destino)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(pagina.statusCode()).isEqualTo(200);
        assertThat(pagina.body()).contains("El inicio de sesión todavía no está habilitado",
                "method=\"post\"", "type=\"password\"", "autocomplete=\"current-password\"");
        assertThat(pagina.body()).doesNotContain("admin@example.com", "clave-de-prueba");
    }

}
