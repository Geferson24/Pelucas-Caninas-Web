package com.veterinaria.pelucascaninas.service;

import com.veterinaria.pelucascaninas.dto.ProductoFormulario;
import com.veterinaria.pelucascaninas.entity.ProductoEntity;
import com.veterinaria.pelucascaninas.model.Producto;
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
public class ProductoService {
    private final ProductoRepository repository;
    private final CategoriaProductoService categorias;

    public List<ProductoEntity> listar() {
        return repository.findAllByOrderByIdAsc();
    }

    public long contar() {
        return repository.count();
    }

    public List<ProductoEntity> listarRecientes() {
        return repository.findTop3ByOrderByIdDesc();
    }

    private ProductoEntity buscar(Integer id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public List<Producto> catalogo() {
        return repository.findByEstadoOrderByIdAsc("ACTIVO").stream().map(this::vista).toList();
    }

    private Producto vista(ProductoEntity p) {
        return new Producto(p.getId(), p.getNombre(), p.getDescripcion(), p.getPrecio(),
                p.getCategoria().getCodigo(), p.getImagen() == null || p.getImagen().isBlank()
                        ? "/img/perritos.png"
                        : p.getImagen());
    }

    public ProductoFormulario formulario(Integer id) {
        var p = buscar(id);
        var form = new ProductoFormulario();
        form.setNombre(p.getNombre());
        form.setDescripcion(p.getDescripcion());
        form.setIdCategoria(p.getCategoria().getId());
        form.setPrecio(p.getPrecio());
        form.setStock(p.getStock());
        form.setImagen(p.getImagen());
        form.setEstado(p.getEstado());
        return form;
    }

    @Transactional
    public void guardar(Integer id, ProductoFormulario form) {
        var p = id == null ? new ProductoEntity() : buscar(id);
        var categoria = categorias.buscar(form.getIdCategoria());
        p.setNombre(form.getNombre().trim());
        p.setDescripcion(form.getDescripcion());
        p.setCategoria(categoria);
        p.setPrecio(form.getPrecio());
        p.setStock(form.getStock());
        p.setImagen(form.getImagen() == null || form.getImagen().isBlank() ? null : form.getImagen());
        p.setEstado(form.getEstado());
        repository.saveAndFlush(p);
    }

    @Transactional
    public void eliminar(Integer id) {
        repository.delete(buscar(id));
        repository.flush();
    }
}
