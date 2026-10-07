package com.inventarionfc.api.repositorio;

import com.inventarionfc.api.modelo.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de los equipos. Spring genera la implementación;
 * los métodos findBy... se convierten en consultas a partir de su nombre.
 */
public interface EquipoRepositorio extends JpaRepository<Equipo, Long> {

    Optional<Equipo> findByCodigoNfc(String codigoNfc);

    boolean existsByCodigoNfc(String codigoNfc);

    // Busca el texto en el nombre, el código o la ubicación (sin distinguir mayúsculas)
    @Query("""
            SELECT e FROM Equipo e
            WHERE LOWER(e.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(e.codigoNfc) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(e.ubicacion) LIKE LOWER(CONCAT('%', :texto, '%'))
            ORDER BY e.nombre
            """)
    List<Equipo> buscar(@Param("texto") String texto);
}
