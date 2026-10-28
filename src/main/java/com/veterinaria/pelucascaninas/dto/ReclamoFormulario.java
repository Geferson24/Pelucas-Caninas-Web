package com.veterinaria.pelucascaninas.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReclamoFormulario {
    @Positive(message = "Selecciona un cliente válido.")
    private Integer idCliente;

    @NotBlank(message = "Completa este campo.")
    @Size(max = 160, message = "Admite hasta 160 caracteres.")
    private String nombres;

    @NotBlank(message = "Completa este campo.")
    @Size(max = 20, message = "Admite hasta 20 caracteres.")
    private String documento;

    @NotBlank(message = "Completa este campo.")
    @Email(message = "Escribe un correo válido.")
    @Size(max = 120, message = "El correo admite hasta 120 caracteres.")
    private String correo;

    @Size(max = 20, message = "Admite hasta 20 caracteres.")
    @Pattern(regexp = "^$|\\+?[0-9][0-9 ()-]*", message = "Revisa el formato del teléfono.")
    private String telefono;

    @NotNull(message = "Indica la fecha y hora.")
    @PastOrPresent(message = "La fecha no puede estar en el futuro.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fecha = LocalDateTime.now().withNano(0);

    @NotBlank(message = "Selecciona una opción.")
    @Pattern(regexp = "RECLAMO|QUEJA", message = "Selecciona una opción válida.")
    private String tipo = "RECLAMO";

    @NotBlank(message = "Completa este campo.")
    @Size(max = 5000, message = "Admite hasta 5000 caracteres.")
    private String descripcion;

    @NotBlank(message = "Completa este campo.")
    @Size(max = 5000, message = "Admite hasta 5000 caracteres.")
    private String solicitud;

    @NotBlank(message = "Selecciona una opción.")
    @Pattern(regexp = "PENDIENTE|EN_PROCESO|ATENDIDO", message = "Selecciona una opción válida.")
    private String estado = "PENDIENTE";
}
