package com.veterinaria.pelucascaninas.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cita")
@Getter
@Setter
@NoArgsConstructor
public class CitaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Positive
    @Column(name = "id_cita")
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_mascota", nullable = false)
    private MascotaEntity mascota;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_servicio", nullable = false)
    private ServicioEntity servicio;

    // No se exige fecha futura en la entidad: también representa citas históricas.
    @NotNull
    @Column(nullable = false)
    private LocalDateTime fecha;

    @NotBlank
    @Pattern(regexp = "PENDIENTE|CONFIRMADA|EN_ATENCION|COMPLETADA|CANCELADA")
    @Column(nullable = false, length = 20)
    private String estado = "PENDIENTE";

    @DecimalMin("0.00")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "precio_acordado", precision = 10, scale = 2)
    private BigDecimal precioAcordado;

    @Size(max = 500)
    @Column(length = 500)
    private String observaciones;

    @NotNull
    @PastOrPresent
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();
}
