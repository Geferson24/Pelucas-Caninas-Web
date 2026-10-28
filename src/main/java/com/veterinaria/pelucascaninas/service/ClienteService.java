package com.veterinaria.pelucascaninas.service;

import com.veterinaria.pelucascaninas.dto.ClienteFormulario;
import com.veterinaria.pelucascaninas.entity.ClienteEntity;
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
public class ClienteService {
    private final ClienteRepository repository;
    private final UsuarioRepository usuarios;

    public List<ClienteEntity> listar() { return repository.findAllByOrderByIdDesc(); }
    public long contar() { return repository.count(); }

    public ClienteEntity buscar(Integer id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public ClienteFormulario formulario(Integer id) {
        var registro = buscar(id);
        var form = new ClienteFormulario();
        form.setIdUsuario(registro.getUsuario() == null ? null : registro.getUsuario().getId());
        form.setNombres(registro.getNombres());
        form.setApellidos(registro.getApellidos());
        form.setTelefono(registro.getTelefono());
        form.setCorreo(registro.getCorreo());
        return form;
    }

    @Transactional
    public void guardar(Integer id, ClienteFormulario form) {
        var registro = id == null ? new ClienteEntity() : buscar(id);
        if (form.getIdUsuario() != null && repository.existsByUsuarioIdAndIdNot(form.getIdUsuario(), id == null ? 0 : id))
            throw new FormularioInvalidoException("idUsuario", "Esta cuenta ya está vinculada a otro cliente.");
        registro.setUsuario(form.getIdUsuario() == null ? null : usuarios.findById(form.getIdUsuario()).orElseThrow(() -> new FormularioInvalidoException("idUsuario", "El registro seleccionado ya no existe.")));
        registro.setNombres(form.getNombres().trim());
        registro.setApellidos(form.getApellidos() == null || form.getApellidos().isBlank() ? null : form.getApellidos().trim());
        registro.setTelefono(form.getTelefono().trim());
        registro.setCorreo(form.getCorreo() == null || form.getCorreo().isBlank() ? null : form.getCorreo().trim());

        repository.saveAndFlush(registro);

    }

    @Transactional
    public void eliminar(Integer id) {
        repository.delete(buscar(id));
        repository.flush();
    }

}
