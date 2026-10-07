package com.inventarionfc.app.ui

import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.inventarionfc.app.R
import com.inventarionfc.app.nfc.GestorNfc

/**
 * Pantalla de inicio. Se abre sola al acercar una etiqueta NFC (ver el intent-filter del manifest)
 * y también permite buscar un equipo escribiendo su código.
 */
class PrincipalActivity : AppCompatActivity() {

    private lateinit var textoNfc: TextView
    private lateinit var campoCodigo: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)

        textoNfc = findViewById(R.id.textoNfc)
        campoCodigo = findViewById(R.id.campoCodigo)

        findViewById<Button>(R.id.botonBuscar).setOnClickListener { buscarCodigoEscrito() }
        findViewById<Button>(R.id.botonRegistrar).setOnClickListener {
            startActivity(Intent(this, RegistrarEquipoActivity::class.java))
        }

        // Si la app se abrió al acercar una etiqueta, el intent inicial ya trae el código
        procesarIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        mostrarEstadoNfc()
    }

    // Se llama cuando la app ya estaba abierta y llega una etiqueta nueva (launchMode singleTop)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        procesarIntent(intent)
    }

    private fun procesarIntent(intent: Intent) {
        if (intent.action != NfcAdapter.ACTION_NDEF_DISCOVERED) return

        val codigo = GestorNfc.leerCodigoDeIntent(intent)
        // Se sustituye el intent para no volver a abrir la ficha si la pantalla se recrea
        setIntent(Intent())

        if (codigo == null) {
            Toast.makeText(this, "La etiqueta no contiene un código de equipo", Toast.LENGTH_LONG).show()
        } else {
            abrirDetalle(codigo)
        }
    }

    private fun buscarCodigoEscrito() {
        val codigo = campoCodigo.text.toString().trim().uppercase()
        if (codigo.isEmpty()) {
            Toast.makeText(this, "Escribe un código para buscar", Toast.LENGTH_SHORT).show()
            return
        }
        abrirDetalle(codigo)
    }

    private fun abrirDetalle(codigo: String) {
        val intent = Intent(this, DetalleEquipoActivity::class.java)
        intent.putExtra(DetalleEquipoActivity.EXTRA_CODIGO, codigo)
        startActivity(intent)
    }

    private fun mostrarEstadoNfc() {
        val adaptador = NfcAdapter.getDefaultAdapter(this)
        textoNfc.text = when {
            adaptador == null -> getString(R.string.nfc_no_disponible)
            !adaptador.isEnabled -> getString(R.string.nfc_desactivado)
            else -> getString(R.string.nfc_listo)
        }
    }
}
