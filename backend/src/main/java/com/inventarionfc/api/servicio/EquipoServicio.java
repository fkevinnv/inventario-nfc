package com.inventarionfc.api.servicio;

import com.inventarionfc.api.modelo.Equipo;
import com.inventarionfc.api.modelo.EstadoEquipo;
import com.inventarionfc.api.repositorio.EquipoRepositorio;
import com.inventarionfc.api.repositorio.IncidenciaRepositorio;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Lógica de negocio de los equipos. Los controladores llaman aquí
 * y este servicio se apoya en los repositorios para leer y guardar datos.
 */
@Service
public class EquipoServicio {

    private final EquipoRepositorio equipoRepositorio;
    private final IncidenciaRepositorio incidenciaRepositorio;

    public EquipoServicio(EquipoRepositorio equipoRepositorio, IncidenciaRepositorio incidenciaRepositorio) {
        this.equipoRepositorio = equipoRepositorio;
        this.incidenciaRepositorio = incidenciaRepositorio;
    }

    public List<Equipo> listar(String texto) {
        if (texto == null || texto.isBlank()) {
            return equipoRepositorio.findAll(Sort.by("nombre"));
        }
        return equipoRepositorio.buscar(texto.trim());
    }

    public Equipo buscarPorCodigo(String codigo) {
        return equipoRepositorio.findByCodigoNfc(codigo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe ningún equipo con el código " + codigo));
    }

    public Equipo crear(Equipo equipo) {
        // Se ignoran el id y el código que pueda traer el cliente: los decide el servidor
        equipo.setId(null);
        equipo.setCodigoNfc(generarCodigo());
        equipo.setFechaAlta(LocalDateTime.now());
        if (equipo.getEstado() == null) {
            equipo.setEstado(EstadoEquipo.OPERATIVO);
        }
        return equipoRepositorio.save(equipo);
    }

    public Equipo actualizar(String codigo, Equipo datos) {
        Equipo equipo = buscarPorCodigo(codigo);
        equipo.setNombre(datos.getNombre());
        equipo.setTipo(datos.getTipo());
        equipo.setMarca(datos.getMarca());
        equipo.setModelo(datos.getModelo());
        equipo.setNumeroSerie(datos.getNumeroSerie());
        equipo.setUbicacion(datos.getUbicacion());
        if (datos.getEstado() != null) {
            equipo.setEstado(datos.getEstado());
        }
        return equipoRepositorio.save(equipo);
    }

    public void cambiarEstado(Equipo equipo, EstadoEquipo estado) {
        equipo.setEstado(estado);
        equipoRepositorio.save(equipo);
    }

    @Transactional
    public void eliminar(String codigo) {
        Equipo equipo = buscarPorCodigo(codigo);
        // Primero se borran sus incidencias para no romper la clave foránea
        incidenciaRepositorio.deleteByEquipoId(equipo.getId());
        equipoRepositorio.delete(equipo);
    }

    // Genera códigos cortos y legibles, por ejemplo EQ-3F9A1C7B
    private String generarCodigo() {
        String codigo;
        do {
            codigo = "EQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (equipoRepositorio.existsByCodigoNfc(codigo));
        return codigo;
    }
}
