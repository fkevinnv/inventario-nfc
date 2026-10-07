package com.inventarionfc.api.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

/**
 * Un equipo del inventario (portátil, impresora, router...).
 * Cada equipo tiene una etiqueta NFC pegada que guarda su codigoNfc.
 */
@Entity
@Table(name = "equipos")
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Texto que se graba en la etiqueta NFC. Lo genera el servidor y no se puede cambiar.
    @Column(nullable = false, unique = true, updatable = false)
    private String codigoNfc;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El tipo es obligatorio")
    private String tipo;

    private String marca;
    private String modelo;
    private String numeroSerie;
    private String ubicacion;

    @Enumerated(EnumType.STRING)
    private EstadoEquipo estado = EstadoEquipo.OPERATIVO;

    private LocalDateTime fechaAlta;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoNfc() { return codigoNfc; }
    public void setCodigoNfc(String codigoNfc) { this.codigoNfc = codigoNfc; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(String numeroSerie) { this.numeroSerie = numeroSerie; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public EstadoEquipo getEstado() { return estado; }
    public void setEstado(EstadoEquipo estado) { this.estado = estado; }

    public LocalDateTime getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(LocalDateTime fechaAlta) { this.fechaAlta = fechaAlta; }
}
