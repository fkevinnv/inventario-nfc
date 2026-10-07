package com.inventarionfc.api.servicio;

import com.inventarionfc.api.modelo.Equipo;
import com.inventarionfc.api.modelo.EstadoEquipo;
import com.inventarionfc.api.modelo.Incidencia;
import com.inventarionfc.api.repositorio.IncidenciaRepositorio;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IncidenciaServicio {

    private final IncidenciaRepositorio incidenciaRepositorio;
    private final EquipoServicio equipoServicio;

    public IncidenciaServicio(IncidenciaRepositorio incidenciaRepositorio, EquipoServicio equipoServicio) {
        this.incidenciaRepositorio = incidenciaRepositorio;
        this.equipoServicio = equipoServicio;
    }

    public List<Incidencia> listarPorEquipo(String codigo) {
        Equipo equipo = equipoServicio.buscarPorCodigo(codigo);
        return incidenciaRepositorio.findByEquipoIdOrderByFechaDesc(equipo.getId());
    }

    public Incidencia crear(String codigo, Incidencia datos) {
        Equipo equipo = equipoServicio.buscarPorCodigo(codigo);

        datos.setId(null);
        datos.setEquipo(equipo);
        datos.setFecha(LocalDateTime.now());
        datos.setResuelta(false);
        Incidencia guardada = incidenciaRepositorio.save(datos);

        // Al reportar un problema, el equipo pasa a estar averiado (salvo que esté de baja)
        if (equipo.getEstado() != EstadoEquipo.BAJA) {
            equipoServicio.cambiarEstado(equipo, EstadoEquipo.AVERIADO);
        }
        return guardada;
    }

    @Transactional
    public Incidencia resolver(Long id) {
        Incidencia incidencia = incidenciaRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe la incidencia " + id));

        incidencia.setResuelta(true);
        incidenciaRepositorio.save(incidencia);

        // Si ya no quedan incidencias pendientes, el equipo vuelve a estar operativo
        Equipo equipo = incidencia.getEquipo();
        long pendientes = incidenciaRepositorio.countByEquipoIdAndResueltaFalse(equipo.getId());
        if (pendientes == 0 && equipo.getEstado() != EstadoEquipo.BAJA) {
            equipoServicio.cambiarEstado(equipo, EstadoEquipo.OPERATIVO);
        }
        return incidencia;
    }
}
