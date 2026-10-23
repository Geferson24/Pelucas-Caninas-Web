package com.veterinaria.pelucascaninas.service;

import com.veterinaria.pelucascaninas.model.Producto;
import com.veterinaria.pelucascaninas.model.Plan;
import com.veterinaria.pelucascaninas.model.Servicio;
import com.veterinaria.pelucascaninas.model.Mascota;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CatalogoService {
        private final ProductoService productos;
        private final ServicioService servicios;

        public CatalogoService(ProductoService productos, ServicioService servicios) {
                this.productos = productos;
                this.servicios = servicios;
        }

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
                return productos.catalogo();
        }

        public List<Plan> listarPlanes() {
                return servicios.planes();
        }

        public List<Servicio> listarServicios() {
                return servicios.adicionales();
        }

        public List<List<Mascota>> gruposMascotas() {
                List<List<Mascota>> grupos = new ArrayList<>();
                for (int i = 0; i < mascotas.size(); i += 4) {
                        grupos.add(List.copyOf(mascotas.subList(i, Math.min(i + 4, mascotas.size()))));
                }
                return List.copyOf(grupos);
        }
}
