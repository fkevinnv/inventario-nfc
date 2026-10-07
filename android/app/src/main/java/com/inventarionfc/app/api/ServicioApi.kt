package com.inventarionfc.app.api

import com.inventarionfc.app.modelo.Equipo
import com.inventarionfc.app.modelo.Incidencia
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Describe los endpoints del backend. Retrofit genera el código que hace las peticiones.
 */
interface ServicioApi {

    @GET("api/equipos/{codigo}")
    fun obtenerEquipo(@Path("codigo") codigo: String): Call<Equipo>

    @POST("api/equipos")
    fun crearEquipo(@Body equipo: Equipo): Call<Equipo>

    @GET("api/equipos/{codigo}/incidencias")
    fun listarIncidencias(@Path("codigo") codigo: String): Call<List<Incidencia>>

    @POST("api/equipos/{codigo}/incidencias")
    fun crearIncidencia(@Path("codigo") codigo: String, @Body incidencia: Incidencia): Call<Incidencia>

    @PUT("api/incidencias/{id}/resolver")
    fun resolverIncidencia(@Path("id") id: Long): Call<Incidencia>
}
