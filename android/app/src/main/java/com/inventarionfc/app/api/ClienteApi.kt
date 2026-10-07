package com.inventarionfc.app.api

import android.content.Context
import android.widget.Toast
import com.inventarionfc.app.BuildConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Crea una única instancia de Retrofit para toda la app.
 * La dirección del servidor se configura en app/build.gradle.kts (URL_BASE).
 */
object ClienteApi {

    val servicio: ServicioApi by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.URL_BASE)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ServicioApi::class.java)
    }
}

/**
 * Lanza la petición en segundo plano y avisa con un Toast si algo falla.
 * El resultado llega al hilo principal, así que se puede tocar la pantalla directamente.
 *
 * @param alError se ejecuta si la petición falla (por ejemplo, para cerrar la pantalla)
 * @param alExito recibe el cuerpo de la respuesta cuando todo va bien
 */
fun <T> Call<T>.ejecutar(contexto: Context, alError: () -> Unit = {}, alExito: (T) -> Unit) {
    enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, respuesta: Response<T>) {
            val cuerpo = respuesta.body()
            if (respuesta.isSuccessful && cuerpo != null) {
                alExito(cuerpo)
                return
            }
            val mensaje = if (respuesta.code() == 404) {
                "No existe ese equipo en el inventario"
            } else {
                "El servidor respondió con el error ${respuesta.code()}"
            }
            Toast.makeText(contexto, mensaje, Toast.LENGTH_LONG).show()
            alError()
        }

        override fun onFailure(call: Call<T>, error: Throwable) {
            Toast.makeText(contexto, "No se pudo conectar con el servidor", Toast.LENGTH_LONG).show()
            alError()
        }
    })
}
