package com.veterinaria.pelucascaninas.repository;

import com.veterinaria.pelucascaninas.entity.ReclamoEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ReclamoRepository extends JpaRepository<ReclamoEntity, Integer> {
    @EntityGraph(attributePaths = "cliente")
    List<ReclamoEntity> findAllByOrderByIdDesc();

    @Override
    @EntityGraph(attributePaths = "cliente")
    Optional<ReclamoEntity> findById(Integer id);

    
}
