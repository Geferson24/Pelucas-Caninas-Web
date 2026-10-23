package com.veterinaria.pelucascaninas.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "producto")
@Getter
@Setter
@NoArgsConstructor
public class ProductoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Positive
    @Column(name = "id_producto")
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotNull
    @JoinColumn(name = "id_categoria", nullable = false)
    private CategoriaProductoEntity categoria;
    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String nombre;
    @Size(max = 5000)
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 8, fraction = 2)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;
    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer stock;
    @Size(max = 255)
    @Pattern(regexp = "^$|^/img/[\\p{L}\\p{N} _.-]+\\.(?i:png|jpe?g|webp|gif|avif)$")
    @Column(length = 255)
    private String imagen;
    @NotBlank
    @Pattern(regexp = "ACTIVO|INACTIVO")
    @Column(nullable = false, length = 20)
    private String estado;
}
