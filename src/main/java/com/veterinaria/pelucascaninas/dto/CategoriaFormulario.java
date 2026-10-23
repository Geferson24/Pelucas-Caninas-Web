package com.veterinaria.pelucascaninas.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoriaFormulario {
    @NotBlank(message = "Escribe el nombre de la categoría.")
    @Size(max = 80, message = "El nombre admite hasta 80 caracteres.")
    private String nombre;
    @NotBlank(message = "Escribe un código para la categoría.")
    @Size(max = 40, message = "El código admite hasta 40 caracteres.")
    @Pattern(regexp = "(?!all$)[a-z0-9]+(?:-[a-z0-9]+)*", message = "Usa minúsculas, números y guiones, sin espacios. El código all está reservado.")
    private String codigo;
    @Size(max = 255, message = "La descripción admite hasta 255 caracteres.")
    private String descripcion;
}
