package com.veterinaria.pelucascaninas.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServicioFormulario {
    @NotBlank(message = "Escribe el nombre del servicio.")
    @Size(max = 120, message = "El nombre admite hasta 120 caracteres.")
    private String nombre;
    @Size(max = 5000, message = "La descripción admite hasta 5000 caracteres.")
    private String descripcion;
    @DecimalMin(value = "0.00", message = "El precio no puede ser negativo.")
    @Digits(integer = 8, fraction = 2, message = "Usa hasta 8 dígitos enteros y 2 decimales.")
    private BigDecimal precio;
    @Positive(message = "La duración debe ser mayor que cero.")
    private Integer duracionMinutos;
    @Size(max = 255, message = "La ruta admite hasta 255 caracteres.")
    @Pattern(regexp = "^$|^/img/[\\p{L}\\p{N} _.-]+\\.(?i:png|jpe?g|webp|gif|avif)$", message = "Usa una imagen local, por ejemplo /img/basico.jpg.")
    private String imagen;
    @NotBlank(message = "Selecciona el tipo de servicio.")
    @Pattern(regexp = "PLAN|ADICIONAL", message = "Selecciona un tipo válido.")
    private String tipo = "ADICIONAL";
    private boolean destacado;
    @NotBlank(message = "Selecciona el estado.")
    @Pattern(regexp = "ACTIVO|INACTIVO", message = "Selecciona un estado válido.")
    private String estado = "ACTIVO";
}
