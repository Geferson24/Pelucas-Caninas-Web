package com.veterinaria.pelucascaninas.controller;

import com.veterinaria.pelucascaninas.dto.UsuarioFormulario;
import com.veterinaria.pelucascaninas.service.*;
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
@RequestMapping("/admin/usuarios")
public class UsuarioController {
    private final UsuarioService service;


    @ModelAttribute
    public void seccion(Model model) {
        model.addAttribute("seccionAdmin", "usuarios");
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("registros", service.listar());
        return "admin/usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("formulario", new UsuarioFormulario());
        return formulario(null, model);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model) {
        model.addAttribute("formulario", service.formulario(id));
        return formulario(id, model);
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("formulario") UsuarioFormulario form, BindingResult errors,
            Model model, RedirectAttributes redirect) {
        return guardar(null, form, errors, model, redirect);
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Integer id, @Valid @ModelAttribute("formulario") UsuarioFormulario form,
            BindingResult errors, Model model, RedirectAttributes redirect) {
        service.buscar(id);
        return guardar(id, form, errors, model, redirect);
    }

    private String guardar(Integer id, UsuarioFormulario form, BindingResult errors,
            Model model, RedirectAttributes redirect) {
        if (!errors.hasErrors()) {
            try {
                service.guardar(id, form);
                redirect.addFlashAttribute("exito", id == null ? "Registro creado." : "Registro actualizado.");
                return "redirect:/admin/usuarios";
            } catch (FormularioInvalidoException ex) {
                errors.rejectValue(ex.getCampo(), "invalido", ex.getMessage());
            } catch (DataIntegrityViolationException ex) {
                errors.reject("integridad", "No se pudo guardar. Revisa los datos duplicados y los registros relacionados.");
            }
        }
        return formulario(id, model);
    }

    private String formulario(Integer id, Model model) {
        model.addAttribute("tituloFormulario", id == null ? "Nuevo usuario" : "Editar usuario");
        model.addAttribute("accion", id == null ? "/admin/usuarios" : "/admin/usuarios/" + id);
        if (id != null) model.addAttribute("registro", service.buscar(id));

        return "admin/usuarios/formulario";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            service.eliminar(id);
            redirect.addFlashAttribute("exito", "Registro eliminado.");
        } catch (DataIntegrityViolationException ex) {
            redirect.addFlashAttribute("error", "No se puede eliminar el usuario porque está asociado a un cliente. Desvincula primero la cuenta o desactívala.");
        }
        return "redirect:/admin/usuarios";
    }
}
