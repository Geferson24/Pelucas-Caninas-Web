package com.veterinaria.pelucascaninas.controller;

import com.veterinaria.pelucascaninas.dto.CategoriaFormulario;
import com.veterinaria.pelucascaninas.service.CategoriaProductoService;
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
@RequestMapping("/admin/categorias")
public class CategoriaProductoController {
    private final CategoriaProductoService service;

    @ModelAttribute
    public void seccion(Model model) {
        model.addAttribute("paginaActiva", "admin");
        model.addAttribute("seccionAdmin", "categorias");
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", service.listar());
        return "admin/categorias/lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("categoria", new CategoriaFormulario());
        return formulario(null, model);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model) {
        model.addAttribute("categoria", service.formulario(id));
        return formulario(id, model);
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("categoria") CategoriaFormulario form, BindingResult errors,
            Model model, RedirectAttributes redirect) {
        return guardar(null, form, errors, model, redirect);
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Integer id, @Valid @ModelAttribute("categoria") CategoriaFormulario form,
            BindingResult errors, Model model, RedirectAttributes redirect) {
        service.buscar(id);
        return guardar(id, form, errors, model, redirect);
    }

    private String guardar(Integer id, CategoriaFormulario form, BindingResult errors,
            Model model, RedirectAttributes redirect) {
        if (!errors.hasErrors()) {
            if (service.nombreOcupado(form.getNombre(), id))
                errors.rejectValue("nombre", "duplicado", "Ya existe una categoría con ese nombre.");
            if (service.codigoOcupado(form.getCodigo(), id))
                errors.rejectValue("codigo", "duplicado", "Ya existe una categoría con ese código.");
        }
        if (errors.hasErrors())
            return formulario(id, model);
        try {
            service.guardar(id, form);
        } catch (DataIntegrityViolationException | IllegalArgumentException ex) {
            errors.reject("duplicado", "No se pudo guardar. Revisa que el nombre y el código sean únicos.");
            return formulario(id, model);
        }
        redirect.addFlashAttribute("exito", id == null ? "Categoría registrada." : "Categoría actualizada.");
        return "redirect:/admin/categorias";
    }

    private String formulario(Integer id, Model model) {
        model.addAttribute("tituloFormulario", id == null ? "Nueva categoría" : "Editar categoría");
        model.addAttribute("accion", id == null ? "/admin/categorias" : "/admin/categorias/" + id);
        return "admin/categorias/formulario";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            service.eliminar(id);
            redirect.addFlashAttribute("exito", "Categoría eliminada.");
        } catch (IllegalArgumentException | DataIntegrityViolationException ex) {
            redirect.addFlashAttribute("error", "No se puede eliminar la categoría porque tiene productos asociados.");
        }
        return "redirect:/admin/categorias";
    }
}
