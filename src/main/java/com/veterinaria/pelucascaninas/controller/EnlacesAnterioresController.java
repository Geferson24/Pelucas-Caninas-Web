package com.veterinaria.pelucascaninas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EnlacesAnterioresController {
    @GetMapping("/index.html")
    public String pagina0() {
        return "redirect:/";
    }

    @GetMapping("/productos.html")
    public String pagina1() {
        return "redirect:/productos";
    }

    @GetMapping("/servicios.html")
    public String pagina2() {
        return "redirect:/servicios";
    }

    @GetMapping("/nosotros.html")
    public String pagina3() {
        return "redirect:/nosotros";
    }

    @GetMapping("/form-contacto.html")
    public String pagina4() {
        return "redirect:/contacto";
    }

    @GetMapping("/libro.html")
    public String pagina5() {
        return "redirect:/libro-reclamaciones";
    }

    @GetMapping("/politica-privacidad.html")
    public String pagina6() {
        return "redirect:/politica-privacidad";
    }

    @GetMapping("/terminos.html")
    public String pagina7() {
        return "redirect:/terminos";
    }

    @GetMapping("/detalle-producto")
    public String detalle(@RequestParam int id) {
        return "redirect:/productos/" + id;
    }
}
