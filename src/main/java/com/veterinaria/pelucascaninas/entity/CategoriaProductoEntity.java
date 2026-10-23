package com.veterinaria.pelucascaninas.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categoria_producto")
@Getter
@Setter
@NoArgsConstructor
public class CategoriaProductoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Positive
    @Column(name = "id_categoria")
    private Integer id;
    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, unique = true, length = 80)
    private String nombre;
    @NotBlank
    @Size(max = 40)
    @Pattern(regexp = "(?!all$)[a-z0-9]+(?:-[a-z0-9]+)*")
    @Column(nullable = false, unique = true, length = 40)
    private String codigo;
    @Size(max = 255)
    @Column(length = 255)
    private String descripcion;
}
