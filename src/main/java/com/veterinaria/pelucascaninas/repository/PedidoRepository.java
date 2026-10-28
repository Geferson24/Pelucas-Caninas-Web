package com.veterinaria.pelucascaninas.repository;

import com.veterinaria.pelucascaninas.entity.PedidoEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PedidoRepository extends JpaRepository<PedidoEntity, Integer> {
    @EntityGraph(attributePaths = "cliente")
    List<PedidoEntity> findAllByOrderByIdDesc();

    @Override
    @EntityGraph(attributePaths = "cliente")
    Optional<PedidoEntity> findById(Integer id);

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PedidoEntity p where p.id = :id")
    Optional<PedidoEntity> bloquearPorId(@Param("id") Integer id);
}
