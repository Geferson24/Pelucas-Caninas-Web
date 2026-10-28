package com.veterinaria.pelucascaninas.repository;

import com.veterinaria.pelucascaninas.entity.CitaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface CitaRepository extends JpaRepository<CitaEntity, Integer> {
    @EntityGraph(attributePaths = {"mascota.cliente", "servicio"})
    List<CitaEntity> findAllByOrderByIdDesc();

    @Override
    @EntityGraph(attributePaths = {"mascota.cliente", "servicio"})
    Optional<CitaEntity> findById(Integer id);

    
}
