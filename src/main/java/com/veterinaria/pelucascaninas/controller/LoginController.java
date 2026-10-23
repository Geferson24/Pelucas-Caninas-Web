package com.veterinaria.pelucascaninas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {
    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("paginaActiva", "login");
        return "login";
    }

    // Provisional hasta incorporar usuarios y autenticación con Spring Security.
    // No se leen ni almacenan las credenciales enviadas por el formulario.
    @PostMapping("/login")
    public String accesoPendiente(RedirectAttributes attributes) {
        attributes.addFlashAttribute("mensajeLogin",
                "El inicio de sesión todavía no está habilitado. Puedes usar el enlace al panel de administración.");
        return "redirect:/login";
    }
}
