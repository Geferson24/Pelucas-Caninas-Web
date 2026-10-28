package com.veterinaria.pelucascaninas.service;

import com.veterinaria.pelucascaninas.dto.PedidoFormulario;
import com.veterinaria.pelucascaninas.entity.PedidoEntity;
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
public class PedidoService {
    private final PedidoRepository repository;
    private final ClienteRepository clientes;
    private final DetallePedidoRepository detalles;

    public List<PedidoEntity> listar() { return repository.findAllByOrderByIdDesc(); }
    public long contar() { return repository.count(); }

    public PedidoEntity buscar(Integer id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public PedidoFormulario formulario(Integer id) {
        var registro = buscar(id);
        var form = new PedidoFormulario();
        form.setIdCliente(registro.getCliente() == null ? null : registro.getCliente().getId());
        form.setFecha(registro.getFecha());
        form.setEstado(registro.getEstado());
        form.setCanal(registro.getCanal());
        form.setObservaciones(registro.getObservaciones());
        return form;
    }

    @Transactional
    public void guardar(Integer id, PedidoFormulario form) {
        var registro = id == null ? new PedidoEntity() : repository.bloquearPorId(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        registro.setCliente(clientes.findById(form.getIdCliente()).orElseThrow(() -> new FormularioInvalidoException("idCliente", "El registro seleccionado ya no existe.")));
        registro.setFecha(form.getFecha());
        registro.setEstado(form.getEstado());
        registro.setCanal(form.getCanal());
        registro.setObservaciones(form.getObservaciones() == null || form.getObservaciones().isBlank() ? null : form.getObservaciones().trim());
        registro.setTotal(id == null ? java.math.BigDecimal.ZERO : detalles.totalPorPedido(id));
        repository.saveAndFlush(registro);

    }

    @Transactional
    public void eliminar(Integer id) {
        repository.delete(buscar(id));
        repository.flush();
    }

}
