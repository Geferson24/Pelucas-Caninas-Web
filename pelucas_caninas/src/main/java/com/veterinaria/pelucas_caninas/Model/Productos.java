package com.veterinaria.pelucas_caninas.Model;

public class Productos {
    private int id;
    private String nombre;
    private String descripcion;
    private double precio;
    private String tipo;
    private String imgurl;

    public Productos(int id, String nombre, String descripcion, double precio, String tipo, String imgurl){
        this.id=id;
        this.nombre=nombre;
        this.descripcion=descripcion;
        this.precio=precio;
        this.tipo=tipo;
        this.imgurl=imgurl;
    }

    public int getId() {
        return id;
    }
    public String getNombre() {
        return nombre;
    }
    public String getDescripcion() {
        return descripcion;
    }
    public double getPrecio() {
        return precio;
    }
    public String getTipo() {
        return tipo;
    }
    public String getImgurl() {
        return imgurl;
    }
}
