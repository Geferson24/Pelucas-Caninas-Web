package com.veterinaria.pelucas_caninas.Model;

public class Mascotas {
    private String nombre;
    private String imgurl;

    public Mascotas (String nombre, String imgurl){
        this.nombre= nombre;
        this.imgurl = imgurl;
    }

    public String getNombre(){return nombre;};
    public String getImgurl(){return imgurl;};
}
