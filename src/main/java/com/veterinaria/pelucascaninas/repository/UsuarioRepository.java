package com.veterinaria.pelucascaninas.repository;

import com.veterinaria.pelucascaninas.entity.UsuarioEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Integer> {
    List<UsuarioEntity> findAllByOrderByIdDesc();

    boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Integer id);
}
