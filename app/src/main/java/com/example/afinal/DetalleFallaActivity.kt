package com.example.afinal

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog // <--- IMPORTANTE PARA LAS ALERTAS
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class DetalleFallaActivity : AppCompatActivity() {

    private val urlApi = "${ApiConfig.URL_BASE}/api_ingeniero.php"
    private var idFalla: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_falla)

        // 1. Referencias de la UI
        val tvLinea = findViewById<TextView>(R.id.tvLineaDetalle)
        val tvFalla = findViewById<TextView>(R.id.tvFallaDetalle)
        val tvHora = findViewById<TextView>(R.id.tvHoraDetalle)
        val tvDesc = findViewById<TextView>(R.id.tvDescDetalle)
        val imgEvidencia = findViewById<ImageView>(R.id.imgEvidencia)
        val tvSinFoto = findViewById<TextView>(R.id.tvSinFoto)
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnAtendido = findViewById<Button>(R.id.btnAtendido)
        val btnCerrar = findViewById<Button>(R.id.btnCerrar)

        // 2. Recuperar datos del Intent
        idFalla = intent.getStringExtra("ID") ?: ""
        val prioridad = intent.getStringExtra("PRIORIDAD") ?: "N/A"
        val estatus = intent.getStringExtra("ESTATUS") ?: "N/A"
        val hora = intent.getStringExtra("HORA") ?: "--:--"
        val descripcion = intent.getStringExtra("DESCRIPCION") ?: "Sin descripción"
        val rutaImagenBD = intent.getStringExtra("IMAGEN") ?: ""

        // 3. Asignar textos
        tvLinea.text = "Prioridad: $prioridad"
        tvFalla.text = "Estatus: $estatus"
        tvHora.text = "Registrado a las: $hora"
        tvDesc.text = descripcion

        // 4. Lógica de Carga de Imagen (Intacta, como la tenías)
        if (rutaImagenBD.isNotEmpty()) {
            val urlFinal = "${ApiConfig.URL_BASE}/$rutaImagenBD"

            Log.d("DEBUG_IMAGE", "Cargando desde: $urlFinal")

            Glide.with(this)
                .load(urlFinal)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.stat_notify_error)
                .into(imgEvidencia)

            imgEvidencia.visibility = View.VISIBLE
            tvSinFoto.visibility = View.GONE
        } else {
            imgEvidencia.visibility = View.GONE
            tvSinFoto.visibility = View.VISIBLE
        }

        // 5. Configurar Botones
        btnBack.setOnClickListener { finish() }

        // NUEVA LÓGICA CON ALERTAS DE CONFIRMACIÓN
        btnAtendido.setOnClickListener {
            mostrarDialogoConfirmacion(
                titulo = "Marcar como Atendido",
                mensaje = "¿Estás seguro de que deseas marcarlo como atendido?",
                nuevoEstado = "Atendida"
            )
        }

        btnCerrar.setOnClickListener {
            mostrarDialogoConfirmacion(
                titulo = "Cerrar Reporte",
                mensaje = " ¿Estás seguro de que deseas cerrar este reporte?",
                nuevoEstado = "Cerrada"
            )
        }
    }

    // FUNCIÓN QUE CREA EL CUADRO DE DIÁLOGO
    private fun mostrarDialogoConfirmacion(titulo: String, mensaje: String, nuevoEstado: String) {
        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage(mensaje)
            .setCancelable(false) // Obliga a elegir una opción
            .setPositiveButton("SÍ, CONFIRMAR") { _, _ ->
                // Solo si acepta, se hace la petición al servidor
                actualizarEstadoFalla(nuevoEstado)
            }
            .setNegativeButton("CANCELAR", null) // Si cancela, no hace nada
            .show()
    }

    private fun actualizarEstadoFalla(nuevoEstado: String) {
        if (idFalla.isEmpty()) return

        Thread {
            try {
                val url = URL(urlApi)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true

                val params = "accion=actualizar_falla&id_falla=$idFalla&estado=$nuevoEstado"

                OutputStreamWriter(conn.outputStream).use { it.write(params) }

                val response = conn.inputStream.bufferedReader().readText()
                Log.d("API_UPDATE", "Respuesta: $response")

                runOnUiThread {
                    Toast.makeText(this, "Estado actualizado a $nuevoEstado", Toast.LENGTH_SHORT).show()
                    finish() // Regresar a la lista
                }
            } catch (e: Exception) {
                Log.e("API_UPDATE", "Error: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this, "Error al conectar con el servidor", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}