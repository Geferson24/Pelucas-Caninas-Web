package com.veterinaria.pelucascaninas.controller;

import com.veterinaria.pelucascaninas.dto.ProductoFormulario;
import com.veterinaria.pelucascaninas.service.CategoriaProductoService;
import com.veterinaria.pelucascaninas.service.ProductoService;
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
@RequestMapping("/admin/productos")
public class ProductoController {
    private final ProductoService service;
    private final CategoriaProductoService categorias;

    @ModelAttribute
    public void seccion(Model model) {
        model.addAttribute("paginaActiva", "admin");
        model.addAttribute("seccionAdmin", "productos");
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("productos", service.listar());
        return "admin/productos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("producto", new ProductoFormulario());
        return formulario(null, model);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model) {
        model.addAttribute("producto", service.formulario(id));
        return formulario(id, model);
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("producto") ProductoFormulario form, BindingResult errors,
            Model model, RedirectAttributes redirect) {
        return guardar(null, form, errors, model, redirect);
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Integer id, @Valid @ModelAttribute("producto") ProductoFormulario form,
            BindingResult errors, Model model, RedirectAttributes redirect) {
        service.formulario(id);
        return guardar(id, form, errors, model, redirect);
    }

    private String guardar(Integer id, ProductoFormulario form, BindingResult errors,
            Model model, RedirectAttributes redirect) {
        if (!errors.hasFieldErrors("idCategoria") && categorias.listar().stream()
                .noneMatch(c -> c.getId().equals(form.getIdCategoria()))) {
            errors.rejectValue("idCategoria", "inexistente", "La categoría ya no existe. Selecciona otra.");
        }
        if (errors.hasErrors())
            return formulario(id, model);
        try {
            service.guardar(id, form);
        } catch (DataIntegrityViolationException ex) {
            errors.reject("integridad", "No se pudo guardar. Comprueba la categoría e inténtalo de nuevo.");
            return formulario(id, model);
        }
        redirect.addFlashAttribute("exito", id == null ? "Producto registrado." : "Producto actualizado.");
        return "redirect:/admin/productos";
    }

    private String formulario(Integer id, Model model) {
        model.addAttribute("categorias", categorias.listar());
        model.addAttribute("tituloFormulario", id == null ? "Nuevo producto" : "Editar producto");
        model.addAttribute("accion", id == null ? "/admin/productos" : "/admin/productos/" + id);
        return "admin/productos/formulario";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            service.eliminar(id);
            redirect.addFlashAttribute("exito", "Producto eliminado.");
        } catch (DataIntegrityViolationException ex) {
            redirect.addFlashAttribute("error",
                    "El producto tiene pedidos asociados. Puedes cambiar su estado a Inactivo para retirarlo del catálogo.");
        }
        return "redirect:/admin/productos";
    }
}
