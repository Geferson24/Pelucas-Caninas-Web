package com.veterinaria.pelucascaninas.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioFormulario {
    @NotBlank(message = "Completa este campo.")
    @Size(max = 120, message = "Admite hasta 120 caracteres.")
    private String nombre;

    @NotBlank(message = "Completa este campo.")
    @Email(message = "Escribe un correo válido.")
    @Size(max = 120, message = "El correo admite hasta 120 caracteres.")
    private String correo;

    @Size(max = 128, message = "La contraseña admite hasta 128 caracteres.")
    @Pattern(regexp = "^$|(?s:(?=.*\\S).{8,128})", message = "Usa entre 8 y 128 caracteres y evita una contraseña de solo espacios.")
    private String contrasena = "";

    @NotBlank(message = "Selecciona una opción.")
    @Pattern(regexp = "CLIENTE|ADMIN", message = "Selecciona una opción válida.")
    private String rol = "CLIENTE";

    @NotNull(message = "Indica el estado de la cuenta.")
    private Boolean activo = false;
}
