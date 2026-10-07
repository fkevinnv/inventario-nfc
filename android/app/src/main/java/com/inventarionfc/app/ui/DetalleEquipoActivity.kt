package com.inventarionfc.app.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.inventarionfc.app.R
import com.inventarionfc.app.api.ClienteApi
import com.inventarionfc.app.api.ejecutar
import com.inventarionfc.app.modelo.Equipo
import com.inventarionfc.app.modelo.Incidencia
import com.inventarionfc.app.modelo.comoTextoEstado

/**
 * Ficha de un equipo: datos, estado e historial de incidencias.
 * Recibe el código del equipo por el intent (EXTRA_CODIGO).
 */
class DetalleEquipoActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CODIGO = "codigo_equipo"
    }

    private lateinit var codigo: String
    private lateinit var textoNombre: TextView
    private lateinit var textoEstado: TextView
    private lateinit var textoDatos: TextView
    private lateinit var contenedorIncidencias: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_equipo)

        val codigoRecibido = intent.getStringExtra(EXTRA_CODIGO)
        if (codigoRecibido == null) {
            finish()
            return
        }
        codigo = codigoRecibido

        textoNombre = findViewById(R.id.textoNombre)
        textoEstado = findViewById(R.id.textoEstado)
        textoDatos = findViewById(R.id.textoDatos)
        contenedorIncidencias = findViewById(R.id.contenedorIncidencias)

        findViewById<Button>(R.id.botonReportar).setOnClickListener { pedirDescripcionIncidencia() }

        recargar()
    }

    private fun recargar() {
        cargarEquipo()
        cargarIncidencias()
    }

    private fun cargarEquipo() {
        // Si el equipo no existe se cierra la pantalla (el Toast lo muestra ClienteApi)
        ClienteApi.servicio.obtenerEquipo(codigo)
            .ejecutar(this, alError = { finish() }) { equipo -> mostrarEquipo(equipo) }
    }

    private fun cargarIncidencias() {
        ClienteApi.servicio.listarIncidencias(codigo)
            .ejecutar(this) { lista -> mostrarIncidencias(lista) }
    }

    private fun mostrarEquipo(equipo: Equipo) {
        textoNombre.text = equipo.nombre
        textoEstado.text = equipo.estado.comoTextoEstado()

        val datos = listOf(
            "Código" to equipo.codigoNfc,
            "Tipo" to equipo.tipo,
            "Marca" to equipo.marca,
            "Modelo" to equipo.modelo,
            "Número de serie" to equipo.numeroSerie,
            "Ubicación" to equipo.ubicacion
        )
        textoDatos.text = datos.joinToString("\n") { (etiqueta, valor) ->
            "$etiqueta: ${if (valor.isNullOrBlank()) "-" else valor}"
        }
    }

    private fun mostrarIncidencias(incidencias: List<Incidencia>) {
        contenedorIncidencias.removeAllViews()

        if (incidencias.isEmpty()) {
            contenedorIncidencias.addView(crearTexto(getString(R.string.sin_incidencias)))
            return
        }

        for (incidencia in incidencias) {
            val fecha = incidencia.fecha?.take(16)?.replace('T', ' ') ?: ""
            val situacion = if (incidencia.resuelta) "resuelta" else "pendiente"
            val texto = crearTexto("$fecha ($situacion)\n${incidencia.descripcion}")

            // Solo las pendientes se pueden marcar como resueltas
            if (!incidencia.resuelta) {
                texto.setOnClickListener { confirmarResolucion(incidencia) }
            }
            contenedorIncidencias.addView(texto)
        }
    }

    private fun crearTexto(contenido: String): TextView {
        val texto = TextView(this)
        texto.text = contenido
        val margen = (8 * resources.displayMetrics.density).toInt()
        texto.setPadding(0, margen, 0, margen)
        return texto
    }

    private fun pedirDescripcionIncidencia() {
        val campo = EditText(this)
        campo.hint = getString(R.string.hint_incidencia)

        AlertDialog.Builder(this)
            .setTitle(R.string.boton_reportar)
            .setView(campo)
            .setPositiveButton("Enviar") { _, _ ->
                val descripcion = campo.text.toString().trim()
                if (descripcion.isNotEmpty()) crearIncidencia(descripcion)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun crearIncidencia(descripcion: String) {
        ClienteApi.servicio.crearIncidencia(codigo, Incidencia(descripcion = descripcion))
            .ejecutar(this) { recargar() }
    }

    private fun confirmarResolucion(incidencia: Incidencia) {
        val id = incidencia.id ?: return

        AlertDialog.Builder(this)
            .setTitle("Marcar como resuelta")
            .setMessage(incidencia.descripcion)
            .setPositiveButton("Resolver") { _, _ ->
                ClienteApi.servicio.resolverIncidencia(id).ejecutar(this) { recargar() }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
