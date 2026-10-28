package com.veterinaria.pelucascaninas.service;

import com.veterinaria.pelucascaninas.dto.ReclamoFormulario;
import com.veterinaria.pelucascaninas.entity.ReclamoEntity;
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
public class ReclamoService {
    private final ReclamoRepository repository;
    private final ClienteRepository clientes;

    public List<ReclamoEntity> listar() { return repository.findAllByOrderByIdDesc(); }
    public long contar() { return repository.count(); }

    public ReclamoEntity buscar(Integer id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public ReclamoFormulario formulario(Integer id) {
        var registro = buscar(id);
        var form = new ReclamoFormulario();
        form.setIdCliente(registro.getCliente() == null ? null : registro.getCliente().getId());
        form.setNombres(registro.getNombres());
        form.setDocumento(registro.getDocumento());
        form.setCorreo(registro.getCorreo());
        form.setTelefono(registro.getTelefono());
        form.setFecha(registro.getFecha());
        form.setTipo(registro.getTipo());
        form.setDescripcion(registro.getDescripcion());
        form.setSolicitud(registro.getSolicitud());
        form.setEstado(registro.getEstado());
        return form;
    }

    @Transactional
    public void guardar(Integer id, ReclamoFormulario form) {
        var registro = id == null ? new ReclamoEntity() : buscar(id);

        registro.setCliente(form.getIdCliente() == null ? null : clientes.findById(form.getIdCliente()).orElseThrow(() -> new FormularioInvalidoException("idCliente", "El registro seleccionado ya no existe.")));
        registro.setNombres(form.getNombres().trim());
        registro.setDocumento(form.getDocumento().trim());
        registro.setCorreo(form.getCorreo().trim());
        registro.setTelefono(form.getTelefono() == null || form.getTelefono().isBlank() ? null : form.getTelefono().trim());
        registro.setFecha(form.getFecha());
        registro.setTipo(form.getTipo());
        registro.setDescripcion(form.getDescripcion().trim());
        registro.setSolicitud(form.getSolicitud().trim());
        registro.setEstado(form.getEstado());

        repository.saveAndFlush(registro);

    }

    @Transactional
    public void eliminar(Integer id) {
        repository.delete(buscar(id));
        repository.flush();
    }

}
