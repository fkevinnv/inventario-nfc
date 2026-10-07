package com.inventarionfc.api.repositorio;

import com.inventarionfc.api.modelo.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidenciaRepositorio extends JpaRepository<Incidencia, Long> {

    List<Incidencia> findByEquipoIdOrderByFechaDesc(Long equipoId);

    // Sirve para saber si al equipo le quedan problemas sin resolver
    long countByEquipoIdAndResueltaFalse(Long equipoId);

    void deleteByEquipoId(Long equipoId);
}
