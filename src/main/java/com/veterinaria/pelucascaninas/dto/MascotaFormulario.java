package com.veterinaria.pelucascaninas.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MascotaFormulario {
    @NotNull(message = "Selecciona un registro.")
    @Positive(message = "Selecciona un registro válido.")
    private Integer idCliente;

    @NotBlank(message = "Completa este campo.")
    @Size(max = 80, message = "Admite hasta 80 caracteres.")
    private String nombre;

    @NotBlank(message = "Completa este campo.")
    @Size(max = 30, message = "Admite hasta 30 caracteres.")
    private String especie = "PERRO";

    @Size(max = 80, message = "Admite hasta 80 caracteres.")
    private String raza;

    @PastOrPresent(message = "La fecha de nacimiento no puede ser futura.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaNacimiento;

    @Size(max = 500, message = "Admite hasta 500 caracteres.")
    private String observaciones;
}
