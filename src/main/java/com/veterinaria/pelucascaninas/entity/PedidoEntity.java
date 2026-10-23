package com.veterinaria.pelucascaninas.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pedido")
@Getter
@Setter
@NoArgsConstructor
public class PedidoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Positive
    @Column(name = "id_pedido")
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private ClienteEntity cliente;

    @NotNull
    @PastOrPresent
    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @NotBlank
    @Pattern(regexp = "PENDIENTE|CONFIRMADO|ENTREGADO|CANCELADO")
    @Column(nullable = false, length = 20)
    private String estado = "PENDIENTE";

    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @NotBlank
    @Pattern(regexp = "WEB|LOCAL|WHATSAPP")
    @Column(nullable = false, length = 20)
    private String canal = "WEB";

    @Size(max = 500)
    @Column(length = 500)
    private String observaciones;
}
