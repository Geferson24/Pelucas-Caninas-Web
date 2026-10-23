package com.veterinaria.pelucascaninas.controller;

import com.veterinaria.pelucascaninas.dto.ServicioFormulario;
import com.veterinaria.pelucascaninas.service.ServicioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/servicios")
public class ServicioController {
    private final ServicioService service;

    @ModelAttribute
    public void seccion(Model model) {
        model.addAttribute("paginaActiva", "admin");
        model.addAttribute("seccionAdmin", "servicios");
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("servicios", service.listar());
        return "admin/servicios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("servicio", new ServicioFormulario());
        return formulario(null, model);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model) {
        model.addAttribute("servicio", service.formulario(id));
        return formulario(id, model);
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("servicio") ServicioFormulario form, BindingResult errors,
            Model model, RedirectAttributes redirect) {
        return guardar(null, form, errors, model, redirect);
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Integer id, @Valid @ModelAttribute("servicio") ServicioFormulario form,
            BindingResult errors, Model model, RedirectAttributes redirect) {
        service.formulario(id);
        return guardar(id, form, errors, model, redirect);
    }

    private String guardar(Integer id, ServicioFormulario form, BindingResult errors,
            Model model, RedirectAttributes redirect) {
        if (!errors.hasFieldErrors("nombre") && service.nombreOcupado(form.getNombre(), id)) {
            errors.rejectValue("nombre", "duplicado", "Ya existe un servicio con ese nombre.");
        }
        if (errors.hasErrors())
            return formulario(id, model);
        try {
            service.guardar(id, form);
        } catch (DataIntegrityViolationException | IllegalArgumentException ex) {
            errors.reject("duplicado", "No se pudo guardar. Comprueba que el nombre sea único.");
            return formulario(id, model);
        }
        redirect.addFlashAttribute("exito", id == null ? "Servicio registrado." : "Servicio actualizado.");
        return "redirect:/admin/servicios";
    }

    private String formulario(Integer id, Model model) {
        model.addAttribute("tituloFormulario", id == null ? "Nuevo servicio" : "Editar servicio");
        model.addAttribute("accion", id == null ? "/admin/servicios" : "/admin/servicios/" + id);
        return "admin/servicios/formulario";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            service.eliminar(id);
            redirect.addFlashAttribute("exito", "Servicio eliminado.");
        } catch (DataIntegrityViolationException ex) {
            redirect.addFlashAttribute("error",
                    "El servicio tiene citas asociadas. Cambia su estado a Inactivo para retirarlo del sitio sin perder el historial.");
        }
        return "redirect:/admin/servicios";
    }
}
