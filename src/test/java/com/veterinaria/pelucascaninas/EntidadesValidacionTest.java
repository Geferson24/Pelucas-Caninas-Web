package com.veterinaria.pelucascaninas;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static com.veterinaria.pelucascaninas.fixture.EntidadesDePrueba.*;
import static org.assertj.core.api.Assertions.assertThat;

class EntidadesValidacionTest {
    static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    static final Validator validator = factory.getValidator();

    @AfterAll
    static void cerrar() { factory.close(); }

    static Stream<Object> entidadesValidas() {
        return Stream.of(usuario(), cliente(), mascota(), categoria(), producto(), servicio(),
                cita(), pedido(), detalle(), reclamo());
    }

    @ParameterizedTest @MethodSource("entidadesValidas")
    void admiteDatosValidosYOpcionalesNulos(Object entity) {
        assertThat(validator.validate(entity)).isEmpty();
    }

    private static Arguments invalido(Object entity, String campo, Object valor) {
        try {
            var field = entity.getClass().getDeclaredField(campo);
            field.setAccessible(true);
            field.set(entity, valor);
            return Arguments.of(entity, campo);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }

    static Stream<Arguments> datosInvalidos() {
        return Stream.of(
            invalido(usuario(), "nombre", " "),
            invalido(usuario(), "correo", "correo-invalido"),
            invalido(usuario(), "rol", "SUPERADMIN"),
            invalido(usuario(), "passwordHash", " "),
            invalido(usuario(), "fechaRegistro", LocalDateTime.now().plusDays(1)),
            invalido(cliente(), "nombres", " "),
            invalido(cliente(), "telefono", "no-es-telefono"),
            invalido(cliente(), "apellidos", "a".repeat(81)),
            invalido(cliente(), "correo", "invalido"),
            invalido(mascota(), "cliente", null),
            invalido(mascota(), "especie", " "),
            invalido(mascota(), "fechaNacimiento", LocalDate.now().plusDays(1)),
            invalido(categoria(), "nombre", "a".repeat(81)),
            invalido(categoria(), "codigo", "all"),
            invalido(categoria(), "descripcion", "a".repeat(256)),
            invalido(producto(), "categoria", null),
            invalido(producto(), "precio", new BigDecimal("-0.01")),
            invalido(producto(), "precio", new BigDecimal("100000000.00")),
            invalido(producto(), "precio", new BigDecimal("1.001")),
            invalido(producto(), "stock", -1),
            invalido(producto(), "estado", "OTRO"),
            invalido(producto(), "imagen", "https://example.com/foto.jpg"),
            invalido(servicio(), "duracionMinutos", 0),
            invalido(servicio(), "tipo", "OTRO"),
            invalido(servicio(), "imagen", "/img/../secreto.png"),
            invalido(cita(), "mascota", null),
            invalido(cita(), "servicio", null),
            invalido(cita(), "fecha", null),
            invalido(cita(), "estado", "CONFIRMADO"),
            invalido(cita(), "precioAcordado", new BigDecimal("-1")),
            invalido(pedido(), "cliente", null),
            invalido(pedido(), "total", new BigDecimal("10000000000.00")),
            invalido(pedido(), "canal", "OTRO"),
            invalido(pedido(), "estado", "CONFIRMADA"),
            invalido(detalle(), "pedido", null),
            invalido(detalle(), "producto", null),
            invalido(detalle(), "cantidad", 0),
            invalido(detalle(), "precioUnitario", new BigDecimal("-1")),
            invalido(reclamo(), "nombres", " "),
            invalido(reclamo(), "documento", "a".repeat(21)),
            invalido(reclamo(), "correo", "invalido"),
            invalido(reclamo(), "tipo", "OTRO"),
            invalido(reclamo(), "descripcion", " "),
            invalido(reclamo(), "solicitud", "a".repeat(5001)),
            invalido(reclamo(), "estado", "CERRADO")
        );
    }

    @ParameterizedTest @MethodSource("datosInvalidos")
    void rechazaDatosInvalidos(Object entity, String campo) {
        assertThat(validator.validate(entity)).extracting(v -> v.getPropertyPath().toString()).contains(campo);
    }

    @Test
    void cuentaActivaExigeHashSinImponerSpringSecurity() {
        var usuario = usuario();
        usuario.setActivo(true);
        assertThat(validator.validate(usuario)).extracting(v -> v.getPropertyPath().toString()).contains("accesoValido");
        usuario.setPasswordHash("hash-de-prueba-sin-autenticacion");
        assertThat(validator.validate(usuario)).isEmpty();
    }
}
