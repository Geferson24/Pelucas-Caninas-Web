package com.veterinaria.pelucascaninas.service;

import com.veterinaria.pelucascaninas.dto.CitaFormulario;
import com.veterinaria.pelucascaninas.entity.CitaEntity;
import com.veterinaria.pelucascaninas.repository.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CitaService {
    private final CitaRepository repository;
    private final MascotaRepository mascotas;
    private final ServicioRepository servicios;

    public List<CitaEntity> listar() { return repository.findAllByOrderByIdDesc(); }
    public long contar() { return repository.count(); }

    public CitaEntity buscar(Integer id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public CitaFormulario formulario(Integer id) {
        var registro = buscar(id);
        var form = new CitaFormulario();
        form.setIdMascota(registro.getMascota() == null ? null : registro.getMascota().getId());
        form.setIdServicio(registro.getServicio() == null ? null : registro.getServicio().getId());
        form.setFecha(registro.getFecha());
        form.setEstado(registro.getEstado());
        form.setPrecioAcordado(registro.getPrecioAcordado());
        form.setObservaciones(registro.getObservaciones());
        return form;
    }

    @Transactional
    public void guardar(Integer id, CitaFormulario form) {
        var registro = id == null ? new CitaEntity() : buscar(id);

        registro.setMascota(mascotas.findById(form.getIdMascota()).orElseThrow(() -> new FormularioInvalidoException("idMascota", "El registro seleccionado ya no existe.")));
        registro.setServicio(servicios.findById(form.getIdServicio()).orElseThrow(() -> new FormularioInvalidoException("idServicio", "El registro seleccionado ya no existe.")));
        registro.setFecha(form.getFecha());
        registro.setEstado(form.getEstado());
        registro.setPrecioAcordado(form.getPrecioAcordado());
        registro.setObservaciones(form.getObservaciones() == null || form.getObservaciones().isBlank() ? null : form.getObservaciones().trim());

        repository.saveAndFlush(registro);

    }

    @Transactional
    public void eliminar(Integer id) {
        repository.delete(buscar(id));
        repository.flush();
    }

}
