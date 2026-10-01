package com.veterinaria.pelucascaninas;

import com.veterinaria.pelucascaninas.service.CatalogoService;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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

    @ParameterizedTest
    @ValueSource(strings = { "/", "/productos", "/servicios", "/nosotros", "/contacto", "/libro-reclamaciones",
            "/terminos", "/politica-privacidad" })
    void rutasCompartenFragmentosYRecursos(String path) throws Exception {
        var response = get(path);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("navbarPelucas", "footer-nuevo", "cdn.jsdelivr.net",
                "cdnjs.cloudflare.com");
        assertThat(response.body()).doesNotContain("th:replace", "th:each", "/vendor/", "../../static", "@{");
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 })
    void detallesUsanCatalogoUnico(int id) throws Exception {
        var producto = catalogo.buscarProducto(id).orElseThrow();
        var response = get("/productos/" + id);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains(producto.getNombre(), producto.getDescripcion(), producto.getImagen(),
                "navbarPelucas", "footer-nuevo");
        assertThat(response.body().split("class=\"card-producto\"", -1).length - 1).isEqualTo(9);
    }

    @ParameterizedTest
    @ValueSource(strings = { "/no-existe", "/productos/999", "/productos/texto", "/otra/ruta" })
    void paginasInexistentesResponden404(String path) throws Exception {
        var response = get(path);
        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.body()).contains("Esta página se nos escapó", "Volver al inicio", "navbarPelucas",
                "footer-nuevo");
        assertThat(response.body()).doesNotContain("/vendor/", "th:replace");
    }

    @Test
    void catalogoYGaleriaRepitenDatos() throws Exception {
        assertThat(get("/productos").body().split("class=\"tarjeta\"", -1).length - 1).isEqualTo(10);
        var inicio = get("/").body();
        assertThat(inicio.split("class=\"carousel-item", -1).length - 1).isEqualTo(3);
        assertThat(inicio).contains("Mascota Bianca", "Mascota Valentino");
    }

    @Test
    void condicionalDestacaSoloPlanPremium() throws Exception {
        var html = get("/servicios").body();
        assertThat(html.split("Más elegido", -1).length - 1).isEqualTo(1);
        assertThat(html.split("class=\"servicio-card", -1).length - 1).isEqualTo(3);
        assertThat(html).contains("Baño Premium", "S/ 37.00", "servicio-card destacado");
        for (var plan : catalogo.listarPlanes()) {
            assertThat(html).contains(plan.getEnlaceWhatsapp().replace("&", "&amp;"));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "/css/style-general.css", "/css/style-error.css", "/js/app.js", "/img/bexter.jpg" })
    void recursosLocalesResponden(String path) throws Exception {
        assertThat(get(path).statusCode()).isEqualTo(200);
    }

    @Test
    void enlacesAnterioresRedirigen() throws Exception {
        assertThat(get("/index.html").statusCode()).isEqualTo(302);
        var detail = get("/detalle-producto?id=3");
        assertThat(detail.statusCode()).isEqualTo(302);
        assertThat(detail.headers().firstValue("location").orElseThrow()).endsWith("/productos/3");
    }
}
