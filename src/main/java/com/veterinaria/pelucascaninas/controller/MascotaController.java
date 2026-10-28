package com.veterinaria.pelucascaninas.controller;

import com.veterinaria.pelucascaninas.dto.MascotaFormulario;
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
@RequestMapping("/admin/mascotas")
public class MascotaController {
    private final MascotaService service;
    private final ClienteService clientes;

    @ModelAttribute
    public void seccion(Model model) {
        model.addAttribute("seccionAdmin", "mascotas");
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("registros", service.listar());
        return "admin/mascotas/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("formulario", new MascotaFormulario());
        return formulario(null, model);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model) {
        model.addAttribute("formulario", service.formulario(id));
        return formulario(id, model);
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("formulario") MascotaFormulario form, BindingResult errors,
            Model model, RedirectAttributes redirect) {
        return guardar(null, form, errors, model, redirect);
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Integer id, @Valid @ModelAttribute("formulario") MascotaFormulario form,
            BindingResult errors, Model model, RedirectAttributes redirect) {
        service.buscar(id);
        return guardar(id, form, errors, model, redirect);
    }

    private String guardar(Integer id, MascotaFormulario form, BindingResult errors,
            Model model, RedirectAttributes redirect) {
        if (!errors.hasErrors()) {
            try {
                service.guardar(id, form);
                redirect.addFlashAttribute("exito", id == null ? "Registro creado." : "Registro actualizado.");
                return "redirect:/admin/mascotas";
            } catch (FormularioInvalidoException ex) {
                errors.rejectValue(ex.getCampo(), "invalido", ex.getMessage());
            } catch (DataIntegrityViolationException ex) {
                errors.reject("integridad", "No se pudo guardar. Revisa los datos duplicados y los registros relacionados.");
            }
        }
        return formulario(id, model);
    }

    private String formulario(Integer id, Model model) {
        model.addAttribute("tituloFormulario", id == null ? "Nueva mascota" : "Editar mascota");
        model.addAttribute("accion", id == null ? "/admin/mascotas" : "/admin/mascotas/" + id);
        if (id != null) model.addAttribute("registro", service.buscar(id));
        model.addAttribute("clientes", clientes.listar());
        return "admin/mascotas/formulario";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            service.eliminar(id);
            redirect.addFlashAttribute("exito", "Registro eliminado.");
        } catch (DataIntegrityViolationException ex) {
            redirect.addFlashAttribute("error", "La mascota tiene citas asociadas. Elimina o reasigna esas citas antes de eliminarla.");
        }
        return "redirect:/admin/mascotas";
    }
}
