package com.example.afinal

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import java.net.URL
import kotlin.concurrent.thread

class HistorialParosActivity : AppCompatActivity() {
    private lateinit var rvHistorial: RecyclerView
    private val mainHandler = Handler(Looper.getMainLooper())
    private val urlApi = "${ApiConfig.URL_BASE}/gestion_paros.php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_paros)

        // ID de tu RecyclerView en el XML principal
        rvHistorial = findViewById(R.id.rvParos)
        rvHistorial.layoutManager = LinearLayoutManager(this)

        // Botón de atrás que tienes en el CardView superior
        findViewById<ImageView>(R.id.btnBackParos).setOnClickListener {
            finish()
        }
    }

    private val updateTask = object : Runnable {
        override fun run() {
            cargarDatos()
            mainHandler.postDelayed(this, 10000) // Se actualiza solo cada 10 seg
        }
    }

    private fun cargarDatos() {
        thread {
            try {
                // Consultamos al PHP
                val respuesta = URL("$urlApi?accion=historial&t=${System.currentTimeMillis()}").readText()
                val jsonArray = JSONArray(respuesta)
                val listaTemporal = mutableListOf<Map<String, String>>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)

                    // LLAVES EXACTAS DE TU JSON / BASE DE DATOS
                    val mapa = mapOf(
                        "motivo" to obj.getString("motivo"),
                        "numero_orden" to obj.getString("numero_orden"),
                        "fecha_inicio" to obj.getString("fecha_inicio"),
                        "estado" to obj.getString("estado")
                    )
                    listaTemporal.add(mapa)
                }

                runOnUiThread {
                    // Cargamos el adaptador con la lista
                    rvHistorial.adapter = ParosAdapter(listaTemporal)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mainHandler.post(updateTask)
    }

    override fun onPause() {
        super.onPause()
        mainHandler.removeCallbacks(updateTask)
    }
}