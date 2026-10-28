package com.veterinaria.pelucascaninas.service;

import com.veterinaria.pelucascaninas.dto.UsuarioFormulario;
import com.veterinaria.pelucascaninas.entity.UsuarioEntity;
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
public class UsuarioService {
    private final UsuarioRepository repository;
    private final ContrasenaService contrasenas;

    public List<UsuarioEntity> listar() { return repository.findAllByOrderByIdDesc(); }
    public long contar() { return repository.count(); }

    public UsuarioEntity buscar(Integer id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public UsuarioFormulario formulario(Integer id) {
        var registro = buscar(id);
        var form = new UsuarioFormulario();
        form.setNombre(registro.getNombre());
        form.setCorreo(registro.getCorreo());
        form.setRol(registro.getRol());
        form.setActivo(registro.isActivo());
        return form;
    }

    @Transactional
    public void guardar(Integer id, UsuarioFormulario form) {
        var registro = id == null ? new UsuarioEntity() : buscar(id);
        if (repository.existsByCorreoIgnoreCaseAndIdNot(form.getCorreo().trim(), id == null ? 0 : id))
            throw new FormularioInvalidoException("correo", "Ya existe una cuenta con ese correo.");
        if (form.getContrasena() != null && !form.getContrasena().isEmpty())
            registro.setPasswordHash(contrasenas.generarHash(form.getContrasena()));
        if (Boolean.TRUE.equals(form.getActivo()) && (registro.getPasswordHash() == null || registro.getPasswordHash().isBlank()))
            throw new FormularioInvalidoException("contrasena", "Escribe una contraseña para activar esta cuenta.");
        registro.setNombre(form.getNombre().trim());
        registro.setCorreo(form.getCorreo().trim().toLowerCase(java.util.Locale.ROOT));
        registro.setRol(form.getRol());
        registro.setActivo(form.getActivo());

        repository.saveAndFlush(registro);

    }

    @Transactional
    public void eliminar(Integer id) {
        repository.delete(buscar(id));
        repository.flush();
    }

}
