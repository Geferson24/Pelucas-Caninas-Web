package com.veterinaria.pelucascaninas.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CitaFormulario {
    @NotNull(message = "Selecciona un registro.")
    @Positive(message = "Selecciona un registro válido.")
    private Integer idMascota;

    @NotNull(message = "Selecciona un registro.")
    @Positive(message = "Selecciona un registro válido.")
    private Integer idServicio;

    @NotNull(message = "Indica la fecha y hora.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fecha;

    @NotBlank(message = "Selecciona una opción.")
    @Pattern(regexp = "PENDIENTE|CONFIRMADA|EN_ATENCION|COMPLETADA|CANCELADA", message = "Selecciona una opción válida.")
    private String estado = "PENDIENTE";

    @DecimalMin(value = "0.00", message = "El importe no puede ser negativo.")
    @Digits(integer = 8, fraction = 2, message = "Admite hasta 8 enteros y 2 decimales.")
    private BigDecimal precioAcordado;

    @Size(max = 500, message = "Admite hasta 500 caracteres.")
    private String observaciones;
}
