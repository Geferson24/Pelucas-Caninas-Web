package com.veterinaria.pelucascaninas.repository;

import com.veterinaria.pelucascaninas.entity.MascotaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface MascotaRepository extends JpaRepository<MascotaEntity, Integer> {
    @EntityGraph(attributePaths = "cliente")
    List<MascotaEntity> findAllByOrderByIdDesc();

    @Override
    @EntityGraph(attributePaths = "cliente")
    Optional<MascotaEntity> findById(Integer id);

    
}
