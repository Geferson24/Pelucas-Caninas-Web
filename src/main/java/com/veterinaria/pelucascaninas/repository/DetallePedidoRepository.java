package com.veterinaria.pelucascaninas.repository;

import com.veterinaria.pelucascaninas.entity.DetallePedidoEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface DetallePedidoRepository extends JpaRepository<DetallePedidoEntity, Integer> {
    @EntityGraph(attributePaths = {"pedido.cliente", "producto"})
    List<DetallePedidoEntity> findAllByOrderByIdDesc();

    @Override
    @EntityGraph(attributePaths = {"pedido.cliente", "producto"})
    Optional<DetallePedidoEntity> findById(Integer id);

    boolean existsByPedidoIdAndProductoIdAndIdNot(Integer pedidoId, Integer productoId, Integer id);

    @EntityGraph(attributePaths = {"pedido.cliente", "producto"})
    List<DetallePedidoEntity> findByPedidoIdOrderByIdDesc(Integer pedidoId);

    @Query("select coalesce(sum(d.subtotal), 0) from DetallePedidoEntity d where d.pedido.id = :id")
    java.math.BigDecimal totalPorPedido(@Param("id") Integer id);

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DetallePedidoEntity d where d.id = :id")
    Optional<DetallePedidoEntity> bloquearPorId(@Param("id") Integer id);
}
