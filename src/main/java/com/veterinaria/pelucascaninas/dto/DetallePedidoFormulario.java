package com.veterinaria.pelucascaninas.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetallePedidoFormulario {
    @NotNull(message = "Selecciona un registro.")
    @Positive(message = "Selecciona un registro válido.")
    private Integer idPedido;

    @NotNull(message = "Selecciona un registro.")
    @Positive(message = "Selecciona un registro válido.")
    private Integer idProducto;

    @NotNull(message = "Indica la cantidad.")
    @Positive(message = "La cantidad debe ser mayor que cero.")
    private Integer cantidad = 1;

    @NotNull(message = "Indica el precio unitario.")
    @DecimalMin(value = "0.00", message = "El importe no puede ser negativo.")
    @Digits(integer = 8, fraction = 2, message = "Admite hasta 8 enteros y 2 decimales.")
    private BigDecimal precioUnitario;

    @AssertTrue(message = "El subtotal no puede superar S/ 9999999999.99.")
    public boolean isSubtotalValido() {
        return cantidad == null || precioUnitario == null || precioUnitario.multiply(BigDecimal.valueOf(cantidad))
                .compareTo(new BigDecimal("9999999999.99")) <= 0;
    }
}
