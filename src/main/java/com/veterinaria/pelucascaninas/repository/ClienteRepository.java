package com.veterinaria.pelucascaninas.repository;

import com.veterinaria.pelucascaninas.entity.ClienteEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ClienteRepository extends JpaRepository<ClienteEntity, Integer> {
    @EntityGraph(attributePaths = "usuario")
    List<ClienteEntity> findAllByOrderByIdDesc();

    @Override
    @EntityGraph(attributePaths = "usuario")
    Optional<ClienteEntity> findById(Integer id);

    boolean existsByUsuarioIdAndIdNot(Integer usuarioId, Integer id);
}
