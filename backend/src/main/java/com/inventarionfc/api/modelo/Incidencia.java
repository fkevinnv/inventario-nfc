package com.inventarionfc.api.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

/**
 * Problema reportado sobre un equipo. Un equipo puede tener muchas incidencias.
 */
@Entity
@Table(name = "incidencias")
public class Incidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    private LocalDateTime fecha;

    private boolean resuelta;

    // Relación muchos a uno: varias incidencias pertenecen al mismo equipo.
    // @JsonIgnore evita que el JSON repita todos los datos del equipo en cada incidencia.
    @ManyToOne(optional = false)
    @JoinColumn(name = "equipo_id")
    @JsonIgnore
    private Equipo equipo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public boolean isResuelta() { return resuelta; }
    public void setResuelta(boolean resuelta) { this.resuelta = resuelta; }

    public Equipo getEquipo() { return equipo; }
    public void setEquipo(Equipo equipo) { this.equipo = equipo; }
}
