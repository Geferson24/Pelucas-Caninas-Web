package com.veterinaria.pelucascaninas.service;

import com.veterinaria.pelucascaninas.model.Producto;
import com.veterinaria.pelucascaninas.model.Plan;
import com.veterinaria.pelucascaninas.model.Servicio;
import com.veterinaria.pelucascaninas.model.Mascota;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/*Datos mantenidos en memoria para este avance.*/
@Service
public class CatalogoService {
        private final List<Producto> productos = List.of(
                        new Producto(1, "Shampoo Bexter",
                                        "Shampoo recomendado para perros mayores a 1 año con sensibilidad en la dermis.",
                                        new BigDecimal("25.00"), "aseo", "/img/bexter.jpg"),
                        new Producto(2, "Kit de aseo 6 piezas",
                                        "Guantes de lavado para perros, cepillo de baño para perros.",
                                        new BigDecimal("34.00"), "aseo", "/img/augwind.jpg"),
                        new Producto(3, "Toallitas humedas (400 unidades)",
                                        "Hipoalergénicas, limpiadoras y desodorizantes.",
                                        new BigDecimal("52.00"), "higiene", "/img/immhabi.jpg"),
                        new Producto(4, "Juguetes para perros", "Juguetes interactivos para perros de tira y afloja.",
                                        new BigDecimal("41.00"), "juguetes", "/img/ALLRIER.jpg"),
                        new Producto(5, "Bolsas de basura",
                                        "100% a prueba de fugas y extra gruesas, paquete de 540 bolsas.",
                                        new BigDecimal("58.00"), "higiene", "/img/Greener.jpg"),
                        new Producto(6, "Arnés táctico",
                                        "Resistente para perros grandes, ajustable, antitirones y reflectante.",
                                        new BigDecimal("99.00"), "accesorios", "/img/beebiepet.jpg"),
                        new Producto(7, "Correa", "Transpirable, diseño cómodo para todo tipo de mascota.",
                                        new BigDecimal("34.00"),
                                        "accesorios", "/img/Matilor.jpg"),
                        new Producto(8, "Cepillo de aseo 3 en 1", "Cepillo de champú y estropajo con mango ajustable.",
                                        new BigDecimal("30.00"), "aseo", "/img/comotech.jpg"),
                        new Producto(9, "Removedor de pelo",
                                        "Rodillo reutilizable para retirar pelos de perros y gatos.",
                                        new BigDecimal("85.00"), "aseo", "/img/Chomchom.jpg"),
                        new Producto(10, "Pelota chirriante", "Juguete interactivo para perros masticadores fuertes.",
                                        new BigDecimal("23.00"), "juguetes", "/img/dissknic.jpg"));
        private final List<Plan> planes = List.of(
                        new Plan(1, "Baño Básico",
                                        List.of("2 lavados con shampoo hipoalergénico", "Corte higiénico",
                                                        "Corte y limado de uñas",
                                                        "Limpieza y depilación de oídos",
                                                        "Despejado de ojos y almohadillas",
                                                        "Perfume y lazo decorativo"),
                                        new BigDecimal("26.00"), "/img/basico.jpg", false),
                        new Plan(2, "Baño Premium",
                                        List.of("2 lavados con shampoo hipoalergénico o antipulgas",
                                                        "Aplicación de acondicionador",
                                                        "Corte higiénico", "Corte y limado de uñas",
                                                        "Limpieza e hidratación de almohadillas",
                                                        "Retoque de carita", "Cepillado de dientes",
                                                        "Limpieza de glándulas perianales",
                                                        "Perfume y lazo decorativo"),
                                        new BigDecimal("37.00"), "/img/premium.png", true),
                        new Plan(3, "Baño y Corte",
                                        List.of("Baño básico o baño premium", "Corte de raza o corte adaptado",
                                                        "Corte según estructura física y pelaje",
                                                        "Corte con máquina, tijera o ambas técnicas",
                                                        "Perfume y lazo decorativo"),
                                        new BigDecimal("22.00"), "/img/corte.jpg", false));
        private final List<Servicio> servicios = List.of(
                        new Servicio(1, "Cepillado de dientes", "/img/cepillado.jpg"),
                        new Servicio(2, "Limpieza de oídos", "/img/oidos.png"),
                        new Servicio(3, "Limpieza de glándulas perianales", "/img/glandulas.png"),
                        new Servicio(4, "Corte y limado de uñas", "/img/unas.jpg"));
        private final List<Mascota> mascotas = List.of(
                        new Mascota("Mascota Bianca", "/img/Bianca.jpg"),
                        new Mascota("Mascota Candy", "/img/Candy.jpg"),
                        new Mascota("Mascota Goku", "/img/Goku.jpg"),
                        new Mascota("Mascota Linda", "/img/Linda.jpg"),
                        new Mascota("Mascota Loki", "/img/Loki.jpeg"),
                        new Mascota("Mascota Osa", "/img/Osa.jpg"),
                        new Mascota("Mascota Lunita", "/img/Lunita.jpg"),
                        new Mascota("Mascota Max", "/img/Max.jpg"),
                        new Mascota("Mascota Sam", "/img/Sam.jpg"),
                        new Mascota("Mascota Maria", "/img/Maria.jpg"),
                        new Mascota("Mascota Pelusa", "/img/Pelusa.jpg"),
                        new Mascota("Mascota Valentino", "/img/Valentino.jpg"));

        public List<Producto> listarProductos() {
                return productos;
        }

        public List<Plan> listarPlanes() {
                return planes;
        }

        public List<Servicio> listarServicios() {
                return servicios;
        }

        public Optional<Producto> buscarProducto(int id) {
                return productos.stream().filter(p -> p.getId() == id).findFirst();
        }

        public List<Producto> productosRelacionados(int id) {
                return productos.stream().filter(p -> p.getId() != id).toList();
        }

        public List<List<Mascota>> gruposMascotas() {
                List<List<Mascota>> grupos = new ArrayList<>();
                for (int i = 0; i < mascotas.size(); i += 4) {
                        grupos.add(List.copyOf(mascotas.subList(i, Math.min(i + 4, mascotas.size()))));
                }
                return List.copyOf(grupos);
        }
}
