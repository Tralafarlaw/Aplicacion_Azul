package com.example.miguel.myapplication;

public class User {
    public String Dueño;
    public Double lat;
    public Double lon;
    public String Nombre;
    public String Placa;
    public int Status;



    public String getDueño() {
        return Dueño;
    }

    public void setDueño(String dueño) {
        Dueño = dueño;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public String getNombre() {
        return Nombre;
    }

    public void setNombre(String nombre) {
        Nombre = nombre;
    }

    public String getPlaca() {
        return Placa;
    }

    public void setPlaca(String placa) {
        Placa = placa;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }
}
