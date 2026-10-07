package com.inventarionfc.api;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de la API completa: se levanta Spring con una base de datos H2 en memoria
 * (distinta a la real) y se lanzan peticiones HTTP simuladas.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:pruebas")
@AutoConfigureMockMvc
class EquipoControladorTest {

    @Autowired
    private MockMvc mockMvc;

    // Crea un equipo y devuelve el código NFC que le asigna el servidor
    private String crearEquipoDePrueba() throws Exception {
        String json = "{\"nombre\":\"Portatil de prueba\",\"tipo\":\"Portatil\",\"ubicacion\":\"Aula 1\"}";
        MvcResult resultado = mockMvc.perform(post("/api/equipos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(resultado.getResponse().getContentAsString(), "$.codigoNfc");
    }

    @Test
    void crearEquipoGeneraCodigoYQuedaOperativo() throws Exception {
        String codigo = crearEquipoDePrueba();

        mockMvc.perform(get("/api/equipos/" + codigo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoNfc").value(codigo))
                .andExpect(jsonPath("$.estado").value("OPERATIVO"));
    }

    @Test
    void crearEquipoSinNombreDevuelve400() throws Exception {
        mockMvc.perform(post("/api/equipos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"Router\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscarCodigoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/equipos/EQ-NOEXISTE"))
                .andExpect(status().isNotFound());
    }

    @Test
    void reportarYResolverIncidenciaCambiaElEstadoDelEquipo() throws Exception {
        String codigo = crearEquipoDePrueba();

        // Al reportar una incidencia el equipo pasa a AVERIADO
        mockMvc.perform(post("/api/equipos/" + codigo + "/incidencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descripcion\":\"No enciende\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resuelta").value(false));

        mockMvc.perform(get("/api/equipos/" + codigo))
                .andExpect(jsonPath("$.estado").value("AVERIADO"));

        // Se recupera el id de la incidencia para resolverla
        MvcResult lista = mockMvc.perform(get("/api/equipos/" + codigo + "/incidencias"))
                .andExpect(status().isOk())
                .andReturn();
        Integer idIncidencia = JsonPath.read(lista.getResponse().getContentAsString(), "$[0].id");

        // Al resolver la única incidencia pendiente, el equipo vuelve a OPERATIVO
        mockMvc.perform(put("/api/incidencias/" + idIncidencia + "/resolver"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resuelta").value(true));

        mockMvc.perform(get("/api/equipos/" + codigo))
                .andExpect(jsonPath("$.estado").value("OPERATIVO"));
    }
}
