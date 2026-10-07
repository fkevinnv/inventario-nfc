package com.inventarionfc.app.ui

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.inventarionfc.app.R
import com.inventarionfc.app.api.ClienteApi
import com.inventarionfc.app.api.ejecutar
import com.inventarionfc.app.modelo.Equipo
import com.inventarionfc.app.nfc.GestorNfc

/**
 * Registro de un equipo nuevo en dos pasos:
 * 1. Se rellena el formulario y se guarda en el servidor, que devuelve el código.
 * 2. Se acerca una etiqueta NFC y la app graba ese código en ella.
 */
class RegistrarEquipoActivity : AppCompatActivity() {

    private var adaptadorNfc: NfcAdapter? = null

    // Código del equipo recién creado, a la espera de grabarse en una etiqueta
    private var codigoPendiente: String? = null

    private lateinit var campoNombre: EditText
    private lateinit var campoTipo: EditText
    private lateinit var campoMarca: EditText
    private lateinit var campoModelo: EditText
    private lateinit var campoNumeroSerie: EditText
    private lateinit var campoUbicacion: EditText
    private lateinit var botonGuardar: Button
    private lateinit var textoGrabacion: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_equipo)

        adaptadorNfc = NfcAdapter.getDefaultAdapter(this)

        campoNombre = findViewById(R.id.campoNombre)
        campoTipo = findViewById(R.id.campoTipo)
        campoMarca = findViewById(R.id.campoMarca)
        campoModelo = findViewById(R.id.campoModelo)
        campoNumeroSerie = findViewById(R.id.campoNumeroSerie)
        campoUbicacion = findViewById(R.id.campoUbicacion)
        botonGuardar = findViewById(R.id.botonGuardar)
        textoGrabacion = findViewById(R.id.textoGrabacion)

        botonGuardar.setOnClickListener { guardarEquipo() }
    }

    // El modo lector solo está activo mientras esta pantalla es visible
    override fun onResume() {
        super.onResume()
        adaptadorNfc?.enableReaderMode(
            this,
            NfcAdapter.ReaderCallback { etiqueta -> grabarEnEtiqueta(etiqueta) },
            NfcAdapter.FLAG_READER_NFC_A,
            null
        )
    }

    override fun onPause() {
        super.onPause()
        adaptadorNfc?.disableReaderMode(this)
    }

    private fun guardarEquipo() {
        val nombre = campoNombre.text.toString().trim()
        val tipo = campoTipo.text.toString().trim()
        if (nombre.isEmpty() || tipo.isEmpty()) {
            Toast.makeText(this, "El nombre y el tipo son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val equipo = Equipo(
            nombre = nombre,
            tipo = tipo,
            marca = textoONulo(campoMarca),
            modelo = textoONulo(campoModelo),
            numeroSerie = textoONulo(campoNumeroSerie),
            ubicacion = textoONulo(campoUbicacion)
        )

        botonGuardar.isEnabled = false
        ClienteApi.servicio.crearEquipo(equipo)
            .ejecutar(this, alError = { botonGuardar.isEnabled = true }) { creado ->
                codigoPendiente = creado.codigoNfc
                textoGrabacion.text = getString(R.string.acerca_etiqueta, creado.codigoNfc)
                textoGrabacion.visibility = View.VISIBLE
            }
    }

    private fun textoONulo(campo: EditText): String? = campo.text.toString().trim().ifEmpty { null }

    // Este método lo llama el sistema en un hilo secundario, por eso la parte visual va en runOnUiThread
    private fun grabarEnEtiqueta(etiqueta: Tag) {
        val codigo = codigoPendiente ?: return
        val grabada = GestorNfc.escribirCodigo(etiqueta, codigo)

        runOnUiThread {
            if (grabada) {
                Toast.makeText(this, "Etiqueta grabada con el código $codigo", Toast.LENGTH_LONG).show()
                val intent = Intent(this, DetalleEquipoActivity::class.java)
                intent.putExtra(DetalleEquipoActivity.EXTRA_CODIGO, codigo)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(
                    this,
                    "No se pudo grabar. Acerca la etiqueta de nuevo o prueba con otra (NTAG213/215/216).",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
