package com.veterinaria.pelucascaninas.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class UsuarioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Positive
    @Column(name = "id_usuario")
    private Integer id;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String nombre;

    @NotBlank
    @Email
    @Size(max = 120)
    @Column(nullable = false, unique = true, length = 120)
    private String correo;

    // Se admite null para las cuentas desactivadas del tercer avance.
    @Size(max = 255)
    @Pattern(regexp = "[\\x21-\\x7E]+")
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @NotBlank
    @Pattern(regexp = "ADMIN|CLIENTE")
    @Column(nullable = false, length = 20)
    private String rol = "CLIENTE";

    @Column(nullable = false)
    private boolean activo;

    @NotNull
    @PastOrPresent
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Transient
    @AssertTrue(message = "Una cuenta activa debe tener un hash de contraseña.")
    public boolean isAccesoValido() {
        return !activo || (passwordHash != null && !passwordHash.isBlank());
    }
}
