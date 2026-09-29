package com.veterinaria.pelucas_caninas.Model;

public class servicios {
    private int id;
    private String imgurl;
    private String descripcion;
    
    public servicios(int id, String imgurl, String descripcion){
        this.id=id;
        this.imgurl=imgurl;
        this.descripcion=descripcion;
    }

    public int getId() {
        return id;
    }
    public String getImgurl() {
        return imgurl;
    }
    public String getDescripcion() {
        return descripcion;
    }
    
}
