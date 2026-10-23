package com.veterinaria.pelucascaninas.controller;

import com.veterinaria.pelucascaninas.service.CatalogoService;
import com.veterinaria.pelucascaninas.service.CategoriaProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaginaController {
    private final CatalogoService catalogo;
    private final CategoriaProductoService categorias;

    public PaginaController(CatalogoService catalogo, CategoriaProductoService categorias) {
        this.catalogo = catalogo;
        this.categorias = categorias;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("paginaActiva", "inicio");
        model.addAttribute("gruposMascotas", catalogo.gruposMascotas());
        model.addAttribute("mascotas", catalogo.gruposMascotas().stream().flatMap(java.util.List::stream).toList());
        return "index";
    }

    @GetMapping("/productos")
    public String productos(Model model) {
        model.addAttribute("paginaActiva", "productos");
        model.addAttribute("productos", catalogo.listarProductos());
        model.addAttribute("categorias", categorias.listar());
        return "productos";
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
