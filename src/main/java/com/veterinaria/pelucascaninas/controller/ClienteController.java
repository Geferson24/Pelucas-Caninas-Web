package com.veterinaria.pelucascaninas.controller;

import com.veterinaria.pelucascaninas.dto.ClienteFormulario;
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
@RequestMapping("/admin/clientes")
public class ClienteController {
    private final ClienteService service;
    private final UsuarioService usuarios;

    @ModelAttribute
    public void seccion(Model model) {
        model.addAttribute("seccionAdmin", "clientes");
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("registros", service.listar());
        return "admin/clientes/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("formulario", new ClienteFormulario());
        return formulario(null, model);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model) {
        model.addAttribute("formulario", service.formulario(id));
        return formulario(id, model);
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("formulario") ClienteFormulario form, BindingResult errors,
            Model model, RedirectAttributes redirect) {
        return guardar(null, form, errors, model, redirect);
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Integer id, @Valid @ModelAttribute("formulario") ClienteFormulario form,
            BindingResult errors, Model model, RedirectAttributes redirect) {
        service.buscar(id);
        return guardar(id, form, errors, model, redirect);
    }

    private String guardar(Integer id, ClienteFormulario form, BindingResult errors,
            Model model, RedirectAttributes redirect) {
        if (!errors.hasErrors()) {
            try {
                service.guardar(id, form);
                redirect.addFlashAttribute("exito", id == null ? "Registro creado." : "Registro actualizado.");
                return "redirect:/admin/clientes";
            } catch (FormularioInvalidoException ex) {
                errors.rejectValue(ex.getCampo(), "invalido", ex.getMessage());
            } catch (DataIntegrityViolationException ex) {
                errors.reject("integridad", "No se pudo guardar. Revisa los datos duplicados y los registros relacionados.");
            }
        }
        return formulario(id, model);
    }

    private String formulario(Integer id, Model model) {
        model.addAttribute("tituloFormulario", id == null ? "Nuevo cliente" : "Editar cliente");
        model.addAttribute("accion", id == null ? "/admin/clientes" : "/admin/clientes/" + id);
        if (id != null) model.addAttribute("registro", service.buscar(id));
        model.addAttribute("usuarios", usuarios.listar());
        return "admin/clientes/formulario";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            service.eliminar(id);
            redirect.addFlashAttribute("exito", "Registro eliminado.");
        } catch (DataIntegrityViolationException ex) {
            redirect.addFlashAttribute("error", "El cliente tiene mascotas, pedidos o reclamos asociados. Atiende esas relaciones antes de eliminarlo.");
        }
        return "redirect:/admin/clientes";
    }
}
