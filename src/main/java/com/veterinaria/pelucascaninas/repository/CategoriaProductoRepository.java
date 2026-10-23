package com.veterinaria.pelucascaninas.repository;

import com.veterinaria.pelucascaninas.entity.CategoriaProductoEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaProductoRepository extends JpaRepository<CategoriaProductoEntity, Integer> {
    List<CategoriaProductoEntity> findAllByOrderByNombreAsc();

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Integer id);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Integer id);
}
