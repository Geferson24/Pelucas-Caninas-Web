package com.veterinaria.pelucascaninas.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PedidoFormulario {
    @NotNull(message = "Selecciona un registro.")
    @Positive(message = "Selecciona un registro válido.")
    private Integer idCliente;

    @NotNull(message = "Indica la fecha y hora.")
    @PastOrPresent(message = "La fecha no puede estar en el futuro.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fecha = LocalDateTime.now().withNano(0);

    @NotBlank(message = "Selecciona una opción.")
    @Pattern(regexp = "PENDIENTE|CONFIRMADO|ENTREGADO|CANCELADO", message = "Selecciona una opción válida.")
    private String estado = "PENDIENTE";

    @NotBlank(message = "Selecciona una opción.")
    @Pattern(regexp = "WEB|LOCAL|WHATSAPP", message = "Selecciona una opción válida.")
    private String canal = "LOCAL";

    @Size(max = 500, message = "Admite hasta 500 caracteres.")
    private String observaciones;
}
