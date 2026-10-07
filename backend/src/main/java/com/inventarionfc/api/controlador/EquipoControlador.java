package com.inventarionfc.api.controlador;

import com.inventarionfc.api.modelo.Equipo;
import com.inventarionfc.api.servicio.EquipoServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints REST de los equipos. Los usan tanto la app Android como el panel web.
 */
@RestController
@RequestMapping("/api/equipos")
public class EquipoControlador {

    private final EquipoServicio equipoServicio;

    public EquipoControlador(EquipoServicio equipoServicio) {
        this.equipoServicio = equipoServicio;
    }

    // GET /api/equipos?texto=portatil
    @GetMapping
    public List<Equipo> listar(@RequestParam(required = false) String texto) {
        return equipoServicio.listar(texto);
    }

    // GET /api/equipos/EQ-3F9A1C7B  (es el que usa la app al leer una etiqueta)
    @GetMapping("/{codigo}")
    public Equipo obtener(@PathVariable String codigo) {
        return equipoServicio.buscarPorCodigo(codigo);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Equipo crear(@Valid @RequestBody Equipo equipo) {
        return equipoServicio.crear(equipo);
    }

    @PutMapping("/{codigo}")
    public Equipo actualizar(@PathVariable String codigo, @Valid @RequestBody Equipo equipo) {
        return equipoServicio.actualizar(codigo, equipo);
    }

    @DeleteMapping("/{codigo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String codigo) {
        equipoServicio.eliminar(codigo);
    }
}
