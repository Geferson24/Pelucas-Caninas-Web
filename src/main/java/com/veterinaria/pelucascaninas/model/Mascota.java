package com.veterinaria.pelucascaninas.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Mascota {
    private final String nombre;
    private final String imagen;
}
