package com.veterinaria.pelucascaninas.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "servicio")
@Getter
@Setter
@NoArgsConstructor
public class ServicioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Positive
    @Column(name = "id_servicio")
    private Integer id;
    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, unique = true, length = 120)
    private String nombre;
    @Size(max = 5000)
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    @DecimalMin("0.00")
    @Digits(integer = 8, fraction = 2)
    @Column(precision = 10, scale = 2)
    private BigDecimal precio;
    @Positive
    @Column(name = "duracion_minutos")
    private Integer duracionMinutos;
    @Size(max = 255)
    @Pattern(regexp = "^$|^/img/[\\p{L}\\p{N} _.-]+\\.(?i:png|jpe?g|webp|gif|avif)$")
    @Column(length = 255)
    private String imagen;
    @NotBlank
    @Pattern(regexp = "PLAN|ADICIONAL")
    @Column(nullable = false, length = 20)
    private String tipo;
    @Column(nullable = false)
    private boolean destacado;
    @NotBlank
    @Pattern(regexp = "ACTIVO|INACTIVO")
    @Column(nullable = false, length = 20)
    private String estado;
}
