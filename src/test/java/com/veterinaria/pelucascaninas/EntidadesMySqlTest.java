package com.veterinaria.pelucascaninas;

import com.veterinaria.pelucascaninas.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import jakarta.validation.ConstraintViolationException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import static com.veterinaria.pelucascaninas.fixture.EntidadesDePrueba.*;
import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class EntidadesMySqlTest {
    @PersistenceContext EntityManager entityManager;

    @Test
    void mapeaLasDiezTablasYRecuperaTodasLasRelaciones() {
        assertThat(entityManager.getMetamodel().getEntities())
                .extracting(e -> e.getJavaType().getAnnotation(Table.class).name())
                .containsExactlyInAnyOrder("usuario", "cliente", "mascota", "categoria_producto", "producto",
                        "servicio", "cita", "pedido", "detalle_pedido", "reclamo");

        var usuario = usuario();
        entityManager.persist(usuario);
        var cliente = cliente();
        cliente.setUsuario(usuario);
        entityManager.persist(cliente);
        var mascota = mascota();
        mascota.setCliente(cliente);
        entityManager.persist(mascota);
        var categoria = categoria();
        entityManager.persist(categoria);
        var producto = producto();
        producto.setCategoria(categoria);
        entityManager.persist(producto);
        var servicio = servicio();
        entityManager.persist(servicio);
        var cita = cita();
        cita.setMascota(mascota);
        cita.setServicio(servicio);
        entityManager.persist(cita);
        var pedido = pedido();
        pedido.setCliente(cliente);
        entityManager.persist(pedido);
        var detalle = detalle();
        detalle.setPedido(pedido);
        detalle.setProducto(producto);
        entityManager.persist(detalle);
        var reclamo = reclamo();
        reclamo.setCliente(cliente);
        entityManager.persist(reclamo);
        entityManager.flush();
        entityManager.clear();

        var citaGuardada = entityManager.find(CitaEntity.class, cita.getId());
        assertThat(citaGuardada.getMascota().getCliente().getUsuario().getCorreo()).isEqualTo(usuario.getCorreo());
        assertThat(citaGuardada.getServicio().getNombre()).isEqualTo(servicio.getNombre());
        assertThat(citaGuardada.getFecha()).isEqualTo(cita.getFecha());
        assertThat(citaGuardada.getPrecioAcordado()).isNull();
        var detalleGuardado = entityManager.find(DetallePedidoEntity.class, detalle.getId());
        assertThat(detalleGuardado.getProducto().getCategoria().getCodigo()).isEqualTo(categoria.getCodigo());
        assertThat(detalleGuardado.getPedido().getCliente().getId()).isEqualTo(cliente.getId());
        assertThat(detalleGuardado.getSubtotal()).isEqualByComparingTo("25.00");
        assertThat(entityManager.find(ReclamoEntity.class, reclamo.getId()).getCliente().getId()).isEqualTo(cliente.getId());
        assertThat(entityManager.find(UsuarioEntity.class, usuario.getId()).getPasswordHash()).isNull();

        // El precio histórico se conserva aunque cambie el precio del catálogo.
        detalleGuardado.getProducto().setPrecio(new BigDecimal("50.00"));
        detalleGuardado.setCantidad(3);
        entityManager.flush();
        assertThat(detalleGuardado.getSubtotal()).isEqualByComparingTo("37.50");
        entityManager.clear();
        assertThat(entityManager.find(DetallePedidoEntity.class, detalle.getId()).getSubtotal()).isEqualByComparingTo("37.50");
    }

    @Test
    void admiteClienteSinCuentaYReclamoSinCliente() {
        var cliente = cliente();
        var reclamo = reclamo();
        entityManager.persist(cliente);
        entityManager.persist(reclamo);
        entityManager.flush();
        entityManager.clear();
        assertThat(entityManager.find(ClienteEntity.class, cliente.getId()).getUsuario()).isNull();
        assertThat(entityManager.find(ReclamoEntity.class, reclamo.getId()).getCliente()).isNull();
    }

    @Test
    void validaLaEntidadAntesDeInsertarAunqueNoSeUseUnFormulario() {
        var categoria = categoria();
        entityManager.persist(categoria);
        var producto = producto();
        producto.setCategoria(categoria);
        producto.setPrecio(new BigDecimal("-1.00"));
        assertThatThrownBy(() -> {
            entityManager.persist(producto);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }
}
