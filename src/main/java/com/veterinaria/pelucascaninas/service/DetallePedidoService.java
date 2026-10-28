package com.veterinaria.pelucascaninas.service;

import com.veterinaria.pelucascaninas.dto.DetallePedidoFormulario;
import com.veterinaria.pelucascaninas.entity.DetallePedidoEntity;
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
public class DetallePedidoService {
    private final DetallePedidoRepository repository;
    private final PedidoRepository pedidos;
    private final ProductoRepository productos;

    public List<DetallePedidoEntity> listar() { return repository.findAllByOrderByIdDesc(); }
    public long contar() { return repository.count(); }

    public DetallePedidoEntity buscar(Integer id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public DetallePedidoFormulario formulario(Integer id) {
        var registro = buscar(id);
        var form = new DetallePedidoFormulario();
        form.setIdPedido(registro.getPedido() == null ? null : registro.getPedido().getId());
        form.setIdProducto(registro.getProducto() == null ? null : registro.getProducto().getId());
        form.setCantidad(registro.getCantidad());
        form.setPrecioUnitario(registro.getPrecioUnitario());
        return form;
    }

    @Transactional
    public void guardar(Integer id, DetallePedidoFormulario form) {
        var registro = id == null ? new DetallePedidoEntity() : repository.bloquearPorId(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Integer anterior = registro.getPedido() == null ? null : registro.getPedido().getId();
        var ids = new java.util.TreeSet<Integer>();
        ids.add(form.getIdPedido());
        if (anterior != null) ids.add(anterior);
        for (Integer pedidoId : ids) pedidos.bloquearPorId(pedidoId)
                .orElseThrow(() -> new FormularioInvalidoException("idPedido", "El pedido ya no existe."));
        if (repository.existsByPedidoIdAndProductoIdAndIdNot(form.getIdPedido(), form.getIdProducto(), id == null ? 0 : id))
            throw new FormularioInvalidoException("idProducto", "El pedido ya contiene este producto. Edita el detalle existente.");

        registro.setPedido(pedidos.findById(form.getIdPedido()).orElseThrow(() -> new FormularioInvalidoException("idPedido", "El registro seleccionado ya no existe.")));
        registro.setProducto(productos.findById(form.getIdProducto()).orElseThrow(() -> new FormularioInvalidoException("idProducto", "El registro seleccionado ya no existe.")));
        registro.setCantidad(form.getCantidad());
        registro.setPrecioUnitario(form.getPrecioUnitario());

        repository.saveAndFlush(registro);
        for (Integer pedidoId : ids) actualizarTotal(pedidoId);
    }

    @Transactional
    public void eliminar(Integer id) {
        var registro = repository.bloquearPorId(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Integer pedidoId = registro.getPedido().getId();
        pedidos.bloquearPorId(pedidoId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        repository.delete(registro);
        repository.flush();
        actualizarTotal(pedidoId);
    }

    public List<DetallePedidoEntity> listarPorPedido(Integer id) {
        pedidos.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return repository.findByPedidoIdOrderByIdDesc(id);
    }

    private void actualizarTotal(Integer id) {
        var pedido = pedidos.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var total = repository.totalPorPedido(id);
        if (total.compareTo(new java.math.BigDecimal("9999999999.99")) > 0)
            throw new FormularioInvalidoException("cantidad", "El total del pedido supera el máximo permitido.");
        pedido.setTotal(total);
        pedidos.saveAndFlush(pedido);
    }

}
