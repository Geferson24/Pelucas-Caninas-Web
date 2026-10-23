package com.veterinaria.pelucascaninas.model;

import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Getter
@RequiredArgsConstructor
public class Plan {
    private final int id;
    private final String nombre;
    private final List<String> beneficios;
    private final BigDecimal precio;
    private final String imagen;
    private final boolean destacado;
    private final Integer duracionMinutos;

    public String getEnlaceWhatsapp() {
        return "https://wa.me/51937317249?text=" + URLEncoder.encode(
                "Hola, quiero información acerca del servicio " + nombre + ".", StandardCharsets.UTF_8);
    }
}
