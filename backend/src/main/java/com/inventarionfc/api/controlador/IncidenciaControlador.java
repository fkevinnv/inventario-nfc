package com.inventarionfc.api.controlador;

import com.inventarionfc.api.modelo.Incidencia;
import com.inventarionfc.api.servicio.IncidenciaServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class IncidenciaControlador {

    private final IncidenciaServicio incidenciaServicio;

    public IncidenciaControlador(IncidenciaServicio incidenciaServicio) {
        this.incidenciaServicio = incidenciaServicio;
    }

    @GetMapping("/equipos/{codigo}/incidencias")
    public List<Incidencia> listar(@PathVariable String codigo) {
        return incidenciaServicio.listarPorEquipo(codigo);
    }

    @PostMapping("/equipos/{codigo}/incidencias")
    @ResponseStatus(HttpStatus.CREATED)
    public Incidencia crear(@PathVariable String codigo, @Valid @RequestBody Incidencia incidencia) {
        return incidenciaServicio.crear(codigo, incidencia);
    }

    @PutMapping("/incidencias/{id}/resolver")
    public Incidencia resolver(@PathVariable Long id) {
        return incidenciaServicio.resolver(id);
    }
}
