package com.veterinaria.pelucascaninas.fixture;

import com.veterinaria.pelucascaninas.entity.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public final class EntidadesDePrueba {
    private EntidadesDePrueba() {}

    public static UsuarioEntity usuario() {
        var entity = new UsuarioEntity();
        entity.setNombre("Cliente de prueba");
        entity.setCorreo(UUID.randomUUID() + "@example.com");
        return entity;
    }

    public static ClienteEntity cliente() {
        var entity = new ClienteEntity();
        entity.setNombres("Cliente de prueba");
        entity.setTelefono("+51 999 999 999");
        return entity;
    }

    public static MascotaEntity mascota() {
        var entity = new MascotaEntity();
        entity.setNombre("Pelusa");
        entity.setCliente(cliente());
        return entity;
    }

    public static CategoriaProductoEntity categoria() {
        var entity = new CategoriaProductoEntity();
        entity.setNombre("Categoría " + UUID.randomUUID());
        entity.setCodigo("test-" + UUID.randomUUID().toString().substring(0, 8));
        return entity;
    }

    public static ProductoEntity producto() {
        var entity = new ProductoEntity();
        entity.setNombre("Producto de prueba");
        entity.setCategoria(categoria());
        entity.setPrecio(new BigDecimal("12.50"));
        entity.setStock(10);
        entity.setEstado("ACTIVO");
        return entity;
    }

    public static ServicioEntity servicio() {
        var entity = new ServicioEntity();
        entity.setNombre("Servicio " + UUID.randomUUID());
        entity.setTipo("ADICIONAL");
        entity.setEstado("ACTIVO");
        return entity;
    }

    public static CitaEntity cita() {
        var entity = new CitaEntity();
        entity.setMascota(mascota());
        entity.setServicio(servicio());
        // Las citas históricas deben poder leerse y actualizarse.
        entity.setFecha(LocalDateTime.of(2020, 1, 1, 10, 0));
        return entity;
    }

    public static PedidoEntity pedido() {
        var entity = new PedidoEntity();
        entity.setCliente(cliente());
        return entity;
    }

    public static DetallePedidoEntity detalle() {
        var entity = new DetallePedidoEntity();
        entity.setPedido(pedido());
        entity.setProducto(producto());
        entity.setCantidad(2);
        entity.setPrecioUnitario(new BigDecimal("12.50"));
        return entity;
    }

    public static ReclamoEntity reclamo() {
        var entity = new ReclamoEntity();
        entity.setNombres("Persona sin cuenta");
        entity.setDocumento("12345678");
        entity.setCorreo("persona@example.com");
        entity.setTipo("RECLAMO");
        entity.setDescripcion("Descripción del reclamo");
        entity.setSolicitud("Solicito una respuesta");
        return entity;
    }
}
