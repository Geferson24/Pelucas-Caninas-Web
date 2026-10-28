package com.veterinaria.pelucascaninas.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClienteFormulario {
    @Positive(message = "Selecciona una cuenta válida.")
    private Integer idUsuario;

    @NotBlank(message = "Completa este campo.")
    @Size(max = 80, message = "Admite hasta 80 caracteres.")
    private String nombres;

    @Size(max = 80, message = "Admite hasta 80 caracteres.")
    private String apellidos;

    @NotBlank(message = "Completa este campo.")
    @Size(max = 20, message = "Admite hasta 20 caracteres.")
    @Pattern(regexp = "\\+?[0-9][0-9 ()-]*", message = "Usa números, espacios, paréntesis o guiones.")
    private String telefono;

    @Email(message = "Escribe un correo válido.")
    @Size(max = 120, message = "El correo admite hasta 120 caracteres.")
    private String correo;
}
