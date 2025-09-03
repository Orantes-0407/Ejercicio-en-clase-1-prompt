package com.example.ejercicioenclase.model;

import java.io.Serializable;

/**
 * Modelo para almacenar información del usuario y su nacionalidad
 */
public class User implements Serializable {
    private int id;
    private String nombre;
    private String apellido;
    private String fechaNacimiento;
    private String genero;
    private String nacionalidad;
    private String huellaId;
    private long tiempoEscaneo; // tiempo en milisegundos que tardó el escaneo

    public User() {
    }

    public User(String nombre, String apellido, String fechaNacimiento, String genero) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
        this.genero = genero;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getNacionalidad() {
        return nacionalidad;
    }

    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
    }

    public String getHuellaId() {
        return huellaId;
    }

    public void setHuellaId(String huellaId) {
        this.huellaId = huellaId;
    }

    public long getTiempoEscaneo() {
        return tiempoEscaneo;
    }

    public void setTiempoEscaneo(long tiempoEscaneo) {
        this.tiempoEscaneo = tiempoEscaneo;
    }
}
