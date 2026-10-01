package com.veterinaria.pelucascaninas.model;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Producto {
    private final int id;
    private final String nombre;
    private final String descripcion;
    private final BigDecimal precio;
    private final String categoria;
    private final String imagen;
}
