package com.veterinaria.pelucas_caninas.Model;

import java.util.List;

public class Planes {
    private int id;
    private String nombre;
    private List<String> componentes;
    private float precio;
    private String imgurl;
    

    public Planes (int id, String nombre,List<String> componentes, float precio, String imgurl){
        this.id=id;
        this.nombre=nombre;
        this.componentes=componentes;
        this.precio=precio;
        this.imgurl= imgurl;
    }

    public int getId() {
        return id;
    }
    public String getNombre() {
        return nombre;
    }
    public List<String> getComponentes() {
        return componentes;
    }
    public float getPrecio() {
        return precio;
    }
    public String getImgurl() {
        return imgurl;
    }
}
