package com.inventarionfc.app.nfc

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Build
import android.os.Parcelable

/**
 * Agrupa todo lo relacionado con leer y escribir etiquetas NFC.
 * La etiqueta guarda un registro NDEF de tipo texto con el código del equipo (EQ-XXXXXXXX).
 */
object GestorNfc {

    /** Saca el código del primer mensaje NDEF que trae el intent de una etiqueta. */
    fun leerCodigoDeIntent(intent: Intent): String? {
        val mensajes: Array<out Parcelable>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, NdefMessage::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        }
        return leerCodigo(mensajes?.firstOrNull() as? NdefMessage)
    }

    private fun leerCodigo(mensaje: NdefMessage?): String? {
        val registro = mensaje?.records?.firstOrNull() ?: return null
        val esTexto = registro.tnf == NdefRecord.TNF_WELL_KNOWN &&
            registro.type.contentEquals(NdefRecord.RTD_TEXT)
        if (!esTexto) return null

        // En un registro de texto, el primer byte indica la longitud del código de idioma ("es", "en"...)
        val datos = registro.payload
        val longitudIdioma = datos[0].toInt() and 0x3F
        return String(datos, 1 + longitudIdioma, datos.size - 1 - longitudIdioma, Charsets.UTF_8)
    }

    /** Graba el código en la etiqueta. Devuelve true si se pudo escribir. */
    fun escribirCodigo(etiqueta: Tag, codigo: String): Boolean {
        val mensaje = NdefMessage(arrayOf(NdefRecord.createTextRecord("es", codigo)))

        return try {
            val ndef = Ndef.get(etiqueta)
            if (ndef != null) {
                // Etiqueta ya formateada: se escribe directamente
                ndef.connect()
                val sePuedeEscribir = ndef.isWritable
                if (sePuedeEscribir) ndef.writeNdefMessage(mensaje)
                ndef.close()
                sePuedeEscribir
            } else {
                // Etiqueta virgen: primero se formatea y se escribe en un solo paso
                val formateable = NdefFormatable.get(etiqueta) ?: return false
                formateable.connect()
                formateable.format(mensaje)
                formateable.close()
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}
