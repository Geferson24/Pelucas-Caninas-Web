package com.veterinaria.pelucascaninas.model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Servicio {
    private final int id;
    private final String nombre;
    private final String imagen;

    public String getEnlaceWhatsapp() {
        return "https://wa.me/51937317249?text=" + URLEncoder.encode(
                "Hola, quiero información acerca del servicio " + nombre + ".", StandardCharsets.UTF_8);
    }
}
