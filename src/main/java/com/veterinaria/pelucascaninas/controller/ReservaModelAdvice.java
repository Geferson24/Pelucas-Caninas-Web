package com.veterinaria.pelucascaninas.controller;

import com.veterinaria.pelucascaninas.entity.ServicioEntity;
import com.veterinaria.pelucascaninas.service.ServicioService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "com.veterinaria.pelucascaninas.controller")
@RequiredArgsConstructor
public class ReservaModelAdvice {
    private final ServicioService servicios;

    @ModelAttribute("serviciosReserva")
    public List<ServicioEntity> serviciosReserva() {
        return servicios.activos();
    }
}
