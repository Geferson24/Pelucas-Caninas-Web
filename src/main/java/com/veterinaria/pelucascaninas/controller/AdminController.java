package com.veterinaria.pelucascaninas.controller;

import com.veterinaria.pelucascaninas.service.CategoriaProductoService;
import com.veterinaria.pelucascaninas.service.ProductoService;
import com.veterinaria.pelucascaninas.service.ServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminController {
    private final CategoriaProductoService categorias;
    private final ProductoService productos;
    private final ServicioService servicios;

    @GetMapping({ "/admin", "/admin/" })
    public String panel(Model model) {
        model.addAttribute("paginaActiva", "admin");
        model.addAttribute("seccionAdmin", "panel");
        model.addAttribute("totalCategorias", categorias.contar());
        model.addAttribute("totalProductos", productos.contar());
        model.addAttribute("totalServicios", servicios.contar());
        return "admin/index";
    }
}
