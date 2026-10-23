package com.veterinaria.pelucascaninas.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reclamo")
@Getter
@Setter
@NoArgsConstructor
public class ReclamoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Positive
    @Column(name = "id_reclamo")
    private Integer id;

    // Se puede reclamar sin estar registrado como cliente.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente")
    private ClienteEntity cliente;

    @NotBlank
    @Size(max = 160)
    @Column(nullable = false, length = 160)
    private String nombres;

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String documento;

    @NotBlank
    @Email
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String correo;

    @Size(max = 20)
    @Pattern(regexp = "^$|\\+?[0-9][0-9 ()-]*")
    @Column(length = 20)
    private String telefono;

    @NotNull
    @PastOrPresent
    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @NotBlank
    @Pattern(regexp = "RECLAMO|QUEJA")
    @Column(nullable = false, length = 20)
    private String tipo;

    @NotBlank
    @Size(max = 5000)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @NotBlank
    @Size(max = 5000)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String solicitud;

    @NotBlank
    @Pattern(regexp = "PENDIENTE|EN_PROCESO|ATENDIDO")
    @Column(nullable = false, length = 20)
    private String estado = "PENDIENTE";
}
