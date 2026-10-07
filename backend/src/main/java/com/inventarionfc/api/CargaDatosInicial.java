package com.inventarionfc.api;

import com.inventarionfc.api.modelo.Equipo;
import com.inventarionfc.api.repositorio.EquipoRepositorio;
import com.inventarionfc.api.servicio.EquipoServicio;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Si la base de datos está vacía, crea unos equipos de ejemplo para poder
 * probar el panel y la app sin tener que registrarlos a mano.
 */
@Component
public class CargaDatosInicial implements CommandLineRunner {

    private final EquipoRepositorio equipoRepositorio;
    private final EquipoServicio equipoServicio;

    public CargaDatosInicial(EquipoRepositorio equipoRepositorio, EquipoServicio equipoServicio) {
        this.equipoRepositorio = equipoRepositorio;
        this.equipoServicio = equipoServicio;
    }

    @Override
    public void run(String... args) {
        if (equipoRepositorio.count() > 0) {
            return;
        }
        crearEjemplo("Portátil Administración 1", "Portátil", "Lenovo", "ThinkPad E14", "PF3K8821", "Administración");
        crearEjemplo("Impresora Planta 1", "Impresora", "HP", "LaserJet Pro M404", "VNB3R05123", "Pasillo planta 1");
        crearEjemplo("Router Principal", "Red", "TP-Link", "ER605", "2215487310", "Sala de servidores");
    }

    private void crearEjemplo(String nombre, String tipo, String marca, String modelo,
                              String numeroSerie, String ubicacion) {
        Equipo equipo = new Equipo();
        equipo.setNombre(nombre);
        equipo.setTipo(tipo);
        equipo.setMarca(marca);
        equipo.setModelo(modelo);
        equipo.setNumeroSerie(numeroSerie);
        equipo.setUbicacion(ubicacion);
        equipoServicio.crear(equipo);
    }
}
