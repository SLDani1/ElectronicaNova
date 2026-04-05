package com.example.afinal

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class ValidarFallasActivity : AppCompatActivity() {

    private val urlApi = "${ApiConfig.URL_BASE}/api_ingeniero.php"
    private lateinit var rvFallas: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_validar_fallas)


        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }


        rvFallas = findViewById(R.id.rvFallas)
        rvFallas.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()
        cargarFallas()
    }

    private fun cargarFallas() {
        Thread {
            try {
                val conn = URL(urlApi).openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true

                OutputStreamWriter(conn.outputStream).use {
                    it.write("accion=obtener_fallas")
                }

                val response = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(response)

                runOnUiThread {
                    if (json.getBoolean("exito")) {
                        val datosArray = json.getJSONArray("datos")

                        val listaFallas = mutableListOf<FallaDia>()

                        for (i in 0 until datosArray.length()) {
                            val item = datosArray.getJSONObject(i)

                            listaFallas.add(
                                FallaDia(
                                    id = item.getString("id"),
                                    descripcion = item.getString("descripcion"),
                                    prioridad = item.optString("prioridad", "Media"),
                                    estatus_falla = item.optString("estatus_falla", "Abierta"),
                                    hora = item.optString("hora", "--:--"),
                                    // 'imagen' ahora viene del LEFT JOIN en tu PHP
                                    imagen = item.optString("imagen", "")
                                )
                            )
                        }

                        rvFallas.adapter = FallasAdapter(listaFallas) { falla ->

                            val intent = Intent(this, DetalleFallaActivity::class.java).apply {
                                putExtra("ID", falla.id)
                                putExtra("PRIORIDAD", falla.prioridad)
                                putExtra("ESTATUS", falla.estatus_falla)
                                putExtra("DESCRIPCION", falla.descripcion)
                                putExtra("HORA", falla.hora)
                                putExtra("IMAGEN", falla.imagen)
                            }
                            startActivity(intent)
                        }

                    } else {
                        Toast.makeText(this, "No hay fallas pendientes por validar", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Error de red al cargar fallas", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}