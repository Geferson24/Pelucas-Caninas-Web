package com.veterinaria.pelucascaninas.controller;

import com.veterinaria.pelucascaninas.service.CatalogoService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class PaginaController {
    private final CatalogoService catalogo;

    public PaginaController(CatalogoService catalogo) {
        this.catalogo = catalogo;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("paginaActiva", "inicio");
        model.addAttribute("gruposMascotas", catalogo.gruposMascotas());
        return "index";
    }

    @GetMapping("/productos")
    public String productos(Model model) {
        model.addAttribute("paginaActiva", "productos");
        model.addAttribute("productos", catalogo.listarProductos());
        return "productos";
    }

    @GetMapping("/productos/{id:[0-9]+}")
    public String detalle(@PathVariable int id, Model model) {
        var producto = catalogo.buscarProducto(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("paginaActiva", "productos");
        model.addAttribute("producto", producto);
        model.addAttribute("relacionados", catalogo.productosRelacionados(id));
        return "detalle-producto";
    }

    @GetMapping("/servicios")
    public String servicios(Model model) {
        model.addAttribute("paginaActiva", "servicios");
        model.addAttribute("planes", catalogo.listarPlanes());
        model.addAttribute("servicios", catalogo.listarServicios());
        return "servicios";
    }

    @GetMapping("/nosotros")
    public String nosotros(Model model) {
        model.addAttribute("paginaActiva", "nosotros");
        return "nosotros";
    }

    @GetMapping("/contacto")
    public String contacto(Model model) {
        model.addAttribute("paginaActiva", "contacto");
        return "contacto";
    }

    @GetMapping("/libro-reclamaciones")
    public String libro() {
        return "libro";
    }

    @GetMapping("/politica-privacidad")
    public String politica() {
        return "politica-privacidad";
    }

    @GetMapping("/terminos")
    public String terminos() {
        return "terminos";
    }
}
