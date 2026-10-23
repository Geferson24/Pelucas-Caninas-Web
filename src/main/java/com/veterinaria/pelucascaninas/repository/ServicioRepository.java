package com.veterinaria.pelucascaninas.repository;

import com.veterinaria.pelucascaninas.entity.ServicioEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicioRepository extends JpaRepository<ServicioEntity, Integer> {
    List<ServicioEntity> findAllByOrderByIdAsc();

    List<ServicioEntity> findByEstadoOrderByIdAsc(String estado);

    List<ServicioEntity> findByTipoAndEstadoOrderByIdAsc(String tipo, String estado);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Integer id);
}
