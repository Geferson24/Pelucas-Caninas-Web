package com.veterinaria.pelucascaninas.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
public class ClienteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Positive
    @Column(name = "id_cliente")
    private Integer id;

    // Un cliente presencial puede no tener cuenta de usuario.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", unique = true)
    private UsuarioEntity usuario;

    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String nombres;

    @Size(max = 80)
    @Column(length = 80)
    private String apellidos;

    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = "\\+?[0-9][0-9 ()-]*")
    @Column(nullable = false, length = 20)
    private String telefono;

    @Email
    @Size(max = 120)
    @Column(length = 120)
    private String correo;

    @NotNull
    @PastOrPresent
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();
}
