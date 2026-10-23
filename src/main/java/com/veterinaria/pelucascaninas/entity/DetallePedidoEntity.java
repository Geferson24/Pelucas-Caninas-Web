package com.veterinaria.pelucascaninas.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

@Entity
@Table(name = "detalle_pedido", uniqueConstraints = @UniqueConstraint(name = "uk_detalle_pedido_producto", columnNames = {
        "id_pedido", "id_producto" }))
@Getter
@Setter
@NoArgsConstructor
public class DetallePedidoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Positive
    @Column(name = "id_detalle")
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pedido", nullable = false)
    private PedidoEntity pedido;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_producto", nullable = false)
    private ProductoEntity producto;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer cantidad;

    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    // MySQL genera este valor en cada INSERT/UPDATE. Hibernate solo lo lee.
    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    @Generated(event = { EventType.INSERT, EventType.UPDATE })
    @Column(precision = 12, scale = 2, insertable = false, updatable = false)
    @Setter(lombok.AccessLevel.NONE)
    private BigDecimal subtotal;
}
