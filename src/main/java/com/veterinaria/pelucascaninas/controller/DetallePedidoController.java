package com.veterinaria.pelucascaninas.controller;

import com.veterinaria.pelucascaninas.dto.DetallePedidoFormulario;
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
@RequestMapping("/admin/detalles-pedido")
public class DetallePedidoController {
    private final DetallePedidoService service;
    private final PedidoService pedidos;
    private final ProductoService productos;

    @ModelAttribute
    public void seccion(Model model) {
        model.addAttribute("seccionAdmin", "detalles-pedido");
    }

    @GetMapping
    public String listar(@RequestParam(required = false) Integer pedido, Model model) {
        model.addAttribute("registros", pedido == null ? service.listar() : service.listarPorPedido(pedido));
        model.addAttribute("pedidoFiltro", pedido);
        return "admin/detalles-pedido/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(@RequestParam(required = false) Integer pedido, Model model) {
        var form = new DetallePedidoFormulario();
        if (pedido != null) {
            pedidos.buscar(pedido);
            form.setIdPedido(pedido);
        }
        model.addAttribute("formulario", form);
        return formulario(null, model);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model) {
        model.addAttribute("formulario", service.formulario(id));
        return formulario(id, model);
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("formulario") DetallePedidoFormulario form, BindingResult errors,
            Model model, RedirectAttributes redirect) {
        return guardar(null, form, errors, model, redirect);
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Integer id, @Valid @ModelAttribute("formulario") DetallePedidoFormulario form,
            BindingResult errors, Model model, RedirectAttributes redirect) {
        service.buscar(id);
        return guardar(id, form, errors, model, redirect);
    }

    private String guardar(Integer id, DetallePedidoFormulario form, BindingResult errors,
            Model model, RedirectAttributes redirect) {
        if (!errors.hasErrors()) {
            try {
                service.guardar(id, form);
                redirect.addFlashAttribute("exito", id == null ? "Registro creado." : "Registro actualizado.");
                return "redirect:/admin/detalles-pedido";
            } catch (FormularioInvalidoException ex) {
                errors.rejectValue(ex.getCampo(), "invalido", ex.getMessage());
            } catch (DataIntegrityViolationException ex) {
                errors.reject("integridad", "No se pudo guardar. Revisa los datos duplicados y los registros relacionados.");
            }
        }
        return formulario(id, model);
    }

    private String formulario(Integer id, Model model) {
        model.addAttribute("tituloFormulario", id == null ? "Nuevo detalle" : "Editar detalle");
        model.addAttribute("accion", id == null ? "/admin/detalles-pedido" : "/admin/detalles-pedido/" + id);
        if (id != null) model.addAttribute("registro", service.buscar(id));
        model.addAttribute("pedidos", pedidos.listar());
        model.addAttribute("productos", productos.listar());
        return "admin/detalles-pedido/formulario";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            service.eliminar(id);
            redirect.addFlashAttribute("exito", "Registro eliminado.");
        } catch (DataIntegrityViolationException ex) {
            redirect.addFlashAttribute("error", "No se pudo eliminar el detalle. Inténtalo de nuevo.");
        }
        return "redirect:/admin/detalles-pedido";
    }
}
