package com.inventarionfc.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación.
 * Al arrancar, Spring escanea este paquete y sus subpaquetes en busca de
 * controladores, servicios y repositorios.
 */
@SpringBootApplication
public class InventarioNfcAplicacion {

    public static void main(String[] args) {
        SpringApplication.run(InventarioNfcAplicacion.class, args);
    }
}
