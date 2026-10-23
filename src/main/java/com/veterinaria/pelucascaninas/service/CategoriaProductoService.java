package com.veterinaria.pelucascaninas.service;

import com.veterinaria.pelucascaninas.dto.CategoriaFormulario;
import com.veterinaria.pelucascaninas.entity.CategoriaProductoEntity;
import com.veterinaria.pelucascaninas.repository.CategoriaProductoRepository;
import com.veterinaria.pelucascaninas.repository.ProductoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoriaProductoService {
    private final CategoriaProductoRepository repository;
    private final ProductoRepository productos;

    public List<CategoriaProductoEntity> listar() {
        return repository.findAllByOrderByNombreAsc();
    }

    public long contar() {
        return repository.count();
    }

    public CategoriaProductoEntity buscar(Integer id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public CategoriaFormulario formulario(Integer id) {
        var categoria = buscar(id);
        var form = new CategoriaFormulario();
        form.setNombre(categoria.getNombre());
        form.setCodigo(categoria.getCodigo());
        form.setDescripcion(categoria.getDescripcion());
        return form;
    }

    public boolean nombreOcupado(String nombre, Integer id) {
        return repository.existsByNombreIgnoreCaseAndIdNot(nombre.trim(), id == null ? 0 : id);
    }

    public boolean codigoOcupado(String codigo, Integer id) {
        return repository.existsByCodigoIgnoreCaseAndIdNot(codigo.trim(), id == null ? 0 : id);
    }

    @Transactional
    public void guardar(Integer id, CategoriaFormulario form) {
        var categoria = id == null ? new CategoriaProductoEntity() : buscar(id);
        if (nombreOcupado(form.getNombre(), id) || codigoOcupado(form.getCodigo(), id)) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre o código.");
        }
        categoria.setNombre(form.getNombre().trim());
        categoria.setCodigo(form.getCodigo().trim());
        categoria.setDescripcion(form.getDescripcion());
        repository.saveAndFlush(categoria);
    }

    @Transactional
    public void eliminar(Integer id) {
        var categoria = buscar(id);
        if (productos.existsByCategoriaId(id)) {
            throw new IllegalArgumentException("No se puede eliminar la categoría porque tiene productos asociados.");
        }
        repository.delete(categoria);
        repository.flush();
    }
}
