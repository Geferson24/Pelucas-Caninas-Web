package com.veterinaria.pelucascaninas.service;

import com.veterinaria.pelucascaninas.dto.MascotaFormulario;
import com.veterinaria.pelucascaninas.entity.MascotaEntity;
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
public class MascotaService {
    private final MascotaRepository repository;
    private final ClienteRepository clientes;

    public List<MascotaEntity> listar() { return repository.findAllByOrderByIdDesc(); }
    public long contar() { return repository.count(); }

    public MascotaEntity buscar(Integer id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public MascotaFormulario formulario(Integer id) {
        var registro = buscar(id);
        var form = new MascotaFormulario();
        form.setIdCliente(registro.getCliente() == null ? null : registro.getCliente().getId());
        form.setNombre(registro.getNombre());
        form.setEspecie(registro.getEspecie());
        form.setRaza(registro.getRaza());
        form.setFechaNacimiento(registro.getFechaNacimiento());
        form.setObservaciones(registro.getObservaciones());
        return form;
    }

    @Transactional
    public void guardar(Integer id, MascotaFormulario form) {
        var registro = id == null ? new MascotaEntity() : buscar(id);

        registro.setCliente(clientes.findById(form.getIdCliente()).orElseThrow(() -> new FormularioInvalidoException("idCliente", "El registro seleccionado ya no existe.")));
        registro.setNombre(form.getNombre().trim());
        registro.setEspecie(form.getEspecie().trim());
        registro.setRaza(form.getRaza() == null || form.getRaza().isBlank() ? null : form.getRaza().trim());
        registro.setFechaNacimiento(form.getFechaNacimiento());
        registro.setObservaciones(form.getObservaciones() == null || form.getObservaciones().isBlank() ? null : form.getObservaciones().trim());

        repository.saveAndFlush(registro);

    }

    @Transactional
    public void eliminar(Integer id) {
        repository.delete(buscar(id));
        repository.flush();
    }

}
