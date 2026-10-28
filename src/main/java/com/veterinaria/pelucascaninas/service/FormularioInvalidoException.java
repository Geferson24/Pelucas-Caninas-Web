package com.veterinaria.pelucascaninas.service;

import lombok.Getter;

/** Error de negocio que se presenta junto al campo correspondiente. */
@Getter
public class FormularioInvalidoException extends IllegalArgumentException {
    private final String campo;

    public FormularioInvalidoException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }
}
