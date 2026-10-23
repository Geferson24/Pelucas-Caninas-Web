package com.veterinaria.pelucascaninas.service;

import com.veterinaria.pelucascaninas.dto.ServicioFormulario;
import com.veterinaria.pelucascaninas.entity.ServicioEntity;
import com.veterinaria.pelucascaninas.model.Plan;
import com.veterinaria.pelucascaninas.model.Servicio;
import com.veterinaria.pelucascaninas.repository.ServicioRepository;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServicioService {
    private final ServicioRepository repository;

    public List<ServicioEntity> listar() {
        return repository.findAllByOrderByIdAsc();
    }

    public List<ServicioEntity> activos() {
        return repository.findByEstadoOrderByIdAsc("ACTIVO");
    }

    public long contar() {
        return repository.count();
    }

    private ServicioEntity buscar(Integer id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public boolean nombreOcupado(String nombre, Integer id) {
        return repository.existsByNombreIgnoreCaseAndIdNot(nombre.trim(), id == null ? 0 : id);
    }

    public ServicioFormulario formulario(Integer id) {
        var s = buscar(id);
        var form = new ServicioFormulario();
        form.setNombre(s.getNombre());
        form.setDescripcion(s.getDescripcion());
        form.setPrecio(s.getPrecio());
        form.setDuracionMinutos(s.getDuracionMinutos());
        form.setImagen(s.getImagen());
        form.setTipo(s.getTipo());
        form.setDestacado(s.isDestacado());
        form.setEstado(s.getEstado());
        return form;
    }

    @Transactional
    public void guardar(Integer id, ServicioFormulario form) {
        var s = id == null ? new ServicioEntity() : buscar(id);
        if (nombreOcupado(form.getNombre(), id)) {
            throw new IllegalArgumentException("Ya existe un servicio con ese nombre.");
        }
        s.setNombre(form.getNombre().trim());
        s.setDescripcion(form.getDescripcion());
        s.setPrecio(form.getPrecio());
        s.setDuracionMinutos(form.getDuracionMinutos());
        s.setImagen(form.getImagen() == null || form.getImagen().isBlank() ? null : form.getImagen());
        s.setTipo(form.getTipo());
        s.setDestacado(form.isDestacado());
        s.setEstado(form.getEstado());
        repository.saveAndFlush(s);
    }

    @Transactional
    public void eliminar(Integer id) {
        repository.delete(buscar(id));
        repository.flush();
    }

    public List<Plan> planes() {
        return repository.findByTipoAndEstadoOrderByIdAsc("PLAN", "ACTIVO").stream()
                .map(s -> new Plan(s.getId(), s.getNombre(), beneficios(s.getDescripcion()), s.getPrecio(),
                        imagen(s), s.isDestacado(), s.getDuracionMinutos()))
                .toList();
    }

    public List<Servicio> adicionales() {
        return repository.findByTipoAndEstadoOrderByIdAsc("ADICIONAL", "ACTIVO").stream()
                .map(s -> new Servicio(s.getId(), s.getNombre(), imagen(s), s.getDescripcion(), s.getPrecio(),
                        s.getDuracionMinutos(), s.isDestacado()))
                .toList();
    }

    private String imagen(ServicioEntity s) {
        return s.getImagen() == null || s.getImagen().isBlank() ? "/img/perritos.png" : s.getImagen();
    }

    private List<String> beneficios(String descripcion) {
        return descripcion == null ? List.of()
                : Arrays.stream(descripcion.split("[;\\r\\n]+"))
                        .map(String::trim).filter(texto -> !texto.isEmpty()).toList();
    }
}
