package com.veterinaria.pelucascaninas.repository;

import com.veterinaria.pelucascaninas.entity.ProductoEntity;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<ProductoEntity, Integer> {
    @EntityGraph(attributePaths = "categoria")
    List<ProductoEntity> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = "categoria")
    List<ProductoEntity> findByEstadoOrderByIdAsc(String estado);

    boolean existsByCategoriaId(Integer idCategoria);
}
