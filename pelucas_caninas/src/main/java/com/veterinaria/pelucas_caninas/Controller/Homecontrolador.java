package com.veterinaria.pelucas_caninas.Controller;

import com.veterinaria.pelucas_caninas.Model.Mascotas;
import com.veterinaria.pelucas_caninas.Model.Planes;
import com.veterinaria.pelucas_caninas.Model.Productos;
import com.veterinaria.pelucas_caninas.Model.servicios;


import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; 
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;







@Controller 
public class Homecontrolador {
    
    @GetMapping({"/", "/index.html"})
    public String index(Model model) { 
        List<Mascotas> listaMascotas = Arrays.asList(
            new Mascotas("Mascota Bianca", "/img/Bianca.jpg"), // <-- 2. Rutas limpias sin @{}
            new Mascotas("Mascota Candy", "/img/Candy.jpg"),
            new Mascotas("Mascota Goku", "/img/Goku.jpg"),
            new Mascotas("Mascota Linda", "/img/Linda.jpg"),
            new Mascotas("Mascota Loki", "/img/Loki.jpeg"),
            new Mascotas("Mascota Osa", "/img/Osa.jpg"),
            new Mascotas("Mascota Lunita", "/img/Lunita.jpg"),
            new Mascotas("Mascota Max", "/img/Max.jpg"),
            new Mascotas("Mascota Sam", "/img/Sam.jpg"),
            new Mascotas("Mascota Maria", "/img/Maria.jpg"),
            new Mascotas("Mascota Pelusa", "/img/Pelusa.jpg"),
            new Mascotas("Mascota Valentino", "/img/Valentino.jpg")
        );
        
        
        model.addAttribute("mascotas", listaMascotas);
        
        return "index";
    }

    @GetMapping("/servicios.html")
    public String servicios(Model model) {
        List<String> descripcion_Ba = Arrays.asList(
            "2 lavados con shampoo hipoalergénico",
                "Corte higiénico",
                "Corte y limado de uñas",
                "Limpieza y depilación de oídos",
                "Despejado de ojos y almohadillas",
                "Perfume y lazo decorativo");

        List<String> descripcion_BP = Arrays.asList(
            "2 lavados con shampoo hipoalergénico o antipulgas",
                "Aplicación de acondicionador",
                "Corte higiénico",
                "Corte y limado de uñas",
                "Limpieza e hidratación de almohadillas",
                "Retoque de carita",
                "Cepillado de dientes",
                "Limpieza de glándulas perianales",
                "Perfume y lazo decorativo");

        List<String> descripcion_ByC = Arrays.asList(
            "Baño básico o baño premium",
                "Corte de raza o corte adaptadox",
                "Corte según estructura física y pelaje",
                "Corte con máquina, tijera o ambas técnicas",
                "Perfume y lazo decorativo");

        List<Planes> listaPlanes = Arrays.asList(
            new Planes(1, "Baño Basico", descripcion_Ba, 26,"/img/basico.jpg"),
            new Planes(2, "Baño Premium", descripcion_BP, 37,"/img/premium.png"),
            new Planes(3, "Baño y Corte", descripcion_ByC, 22,"/img/corte.jpg")
        );

        List<servicios> listaServicios= Arrays.asList(
            new servicios(1, "/img/cepillado.jpg", "Cepillado de dientes"),
            new servicios(2, "/img/oidos.png", "Limpieza de oídos"),
            new servicios(3, "/img/glandulas.png", "Limpieza de glándulas perianales"),
            new servicios(4, "/img/unas.jpg", "Corte y limado de uñas")
        );


        model.addAttribute("planes",listaPlanes);
        model.addAttribute("servicios",listaServicios);
        return "servicios";

        
    }
    
    @GetMapping("/productos.html")
    public String productos(Model model) {
        List<Productos> listaProductos = Arrays.asList(
            new Productos(1, "Shampoo Bexter", "Shampoo recomendado para perros mayores a 1 año con sensibilidad en la dermis.", 25, "aseo","/img/bexter.jpg"),
            new Productos(2, "Kit de aseo 6 piezas", "Guantes de lavado para perros, cepillo de baño para perros.", 34,"aseo" ,"/img/augwind.jpg"),
            new Productos(3, "Toallitas humedas (400 unidades)", "Hipoalergénicas, limpiadoras y desodorizantes.", 52,"higiene" ,"/img/immhabi.jpg"),
            new Productos(4, "Juguetes para perros", "Juguetes interactivos para perros de tira y afloja.", 41, "juguetes","/img/ALLRIER.jpg"),
            new Productos(5, "Bolsas de basura", "100% a prueba de fugas.", 58, "higiene","/img/Greener.jpg"),
            new Productos(6, "Arnés táctico", "Resistente para perros grandes, ajustable, antitirones y reflectante.", 99, "accesorios","/img/beebiepet.jpg"),
            new Productos(7, "Correa", "Transpirable, diseño cómodo para todo tipo de mascota.", 34, "accesorios","/img/Matilor.jpg"),
            new Productos(8, "Cepillo de aseo 3 en 1", "Cepillo de champú y estropajo con mango ajustable.", 30, "aseo","/img/comotech.jpg"),
            new Productos(9, "Removedor de pelo", "Rodillo reutilizable para retirar pelos de perros y gatos." , 85, "aseo","/img/Chomchom.jpg"),
            new Productos(10, "Pelota chirriante", "Juguete interactivo para perros masticadores fuertes.", 23, "juguetes","/img/dissknic.jpg")
        );

        model.addAttribute("productos", listaProductos);
        return "productos";
    }

    @GetMapping("/detalle-producto")
public String verDetalleProducto(@RequestParam("id") int id, Model model) {
    
    List<Productos> listaProductos = Arrays.asList(
        new Productos(1, "Shampoo Bexter", "Shampoo recomendado para perros mayores a 1 año con sensibilidad en la dermis.", 25, "aseo","/img/bexter.jpg"),
        new Productos(2, "Kit de aseo 6 piezas", "Guantes de lavado para perros, cepillo de baño para perros.", 34,"aseo" ,"/img/augwind.jpg"),
        new Productos(3, "Toallitas humedas (400 unidades)", "Hipoalergénicas, limpiadoras y desodorizantes.", 52,"higiene" ,"/img/immhabi.jpg"),
        new Productos(4, "Juguetes para perros", "Juguetes interactivos para perros de tira y afloja.", 41, "juguetes","/img/ALLRIER.jpg"),
        new Productos(5, "Bolsas de basura", "100% a prueba de fugas y extra gruesas, paquete de 540 bolsas.", 58, "higiene","/img/Greener.jpg"),
        new Productos(6, "Arnés táctico", "Resistente para perros grandes, ajustable, antitirones y reflectante.", 99, "accesorios","/img/beebiepet.jpg"),
        new Productos(7, "Correa", "Transpirable, diseño cómodo para todo tipo de mascota.", 34, "accesorios","/img/Matilor.jpg"),
        new Productos(8, "Cepillo de aseo 3 en 1", "Cepillo de champú y estropajo con mango ajustable.", 30, "aseo","/img/comotech.jpg"),
        new Productos(9, "Removedor de pelo", "Rodillo reutilizable para retirar pelos de perros y gatos." , 85, "aseo","/img/Chomchom.jpg"),
        new Productos(10, "Pelota chirriante", "Juguete interactivo para perros masticadores fuertes.", 23, "juguetes","/img/dissknic.jpg")
    );

    
    Productos productoEncontrado = null;
    for (Productos p : listaProductos) {
        if (p.getId() == id) {
            productoEncontrado = p;
            break;
        }
    }

    
    model.addAttribute("producto", productoEncontrado);
    model.addAttribute("productos", listaProductos);
    
    return "detalle-producto"; 
}
    
    @GetMapping("/index.html#contactanos")
    public String contactanos(Model model) {
        return "contactanos";
    }
    
    @GetMapping("/nosotros.html")
    public String nosotros(Model model) {
        return "nosotros";
    }
    
}