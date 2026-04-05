package com.example.afinal

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog // <--- IMPORTANTE PARA LA ALERTA
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class DetalleRetrabajoActivity : AppCompatActivity() {
    private var idRetrabajo = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_falla)

        idRetrabajo = intent.getStringExtra("ID") ?: ""

        // Configurar datos
        findViewById<TextView>(R.id.tvLineaDetalle).text = intent.getStringExtra("LINEA")
        findViewById<TextView>(R.id.tvFallaDetalle).text = intent.getStringExtra("RETRABAJO")
        findViewById<TextView>(R.id.tvDescDetalle).text = "Validación técnica requerida para esta unidad."

        // Cargar imagen
        val img = findViewById<ImageView>(R.id.imgEvidencia)
        val urlImg = "${ApiConfig.URL_BASE}/uploads/${intent.getStringExtra("IMAGEN")}"

        if (!intent.getStringExtra("IMAGEN").isNullOrEmpty()) {
            img.visibility = View.VISIBLE
            findViewById<TextView>(R.id.tvSinFoto).visibility = View.GONE
            Glide.with(this).load(urlImg).into(img)
        }

        // Configurar Botones para Ingeniero
        val btnAutorizar = findViewById<Button>(R.id.btnAtendido)
        val btnScrap = findViewById<Button>(R.id.btnCerrar)

        btnAutorizar.text = "AUTORIZAR RETRABAJO"
        btnAutorizar.setBackgroundColor(Color.parseColor("#38A169"))

        btnScrap.text = "MANDAR A SCRAP"
        btnScrap.setTextColor(Color.RED)

        // NUEVA LÓGICA CON ALERTAS DE CONFIRMACIÓN
        btnAutorizar.setOnClickListener {
            mostrarDialogoConfirmacion(
                titulo = "Autorizar Retrabajo",
                mensaje = "¿Estás seguro de que deseas autorizar esta unidad para retrabajo?",
                estado = "aceptar"
            )
        }

        btnScrap.setOnClickListener {
            mostrarDialogoConfirmacion(
                titulo = "Mandar a Scrap",
                mensaje = "¿Estás seguro de que deseas mandar esta pieza a Scrap?",
                estado = "No Viable"
            )
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
    }

    // FUNCIÓN QUE CREA EL CUADRO DE DIÁLOGO
    private fun mostrarDialogoConfirmacion(titulo: String, mensaje: String, estado: String) {
        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage(mensaje)
            .setCancelable(false)
            .setPositiveButton("SÍ, CONFIRMAR") { _, _ ->

                enviarAccion(estado)
            }
            .setNegativeButton("CANCELAR", null)
            .show()
    }

    private fun enviarAccion(estado: String) {
        Thread {
            try {
                val conn = URL("${ApiConfig.URL_BASE}/api_ingeniero.php").openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                val params = "accion=validar_retrabajo&id_retrabajo=$idRetrabajo&estado=$estado"
                conn.outputStream.use { it.write(params.toByteArray()) }

                val resp = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(resp)

                runOnUiThread {
                    if (json.getBoolean("exito")) {
                        Toast.makeText(this, "Unidad marcada como $estado", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }.start()
    }
}