package com.example.afinal

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ValidarRetrabajosActivity : AppCompatActivity() {
    private val urlApi = "${ApiConfig.URL_BASE}/api_ingeniero.php"
    private lateinit var rv: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_validar_retrabajos)

        // Botón de atrás con protección de nulos
        findViewById<ImageView>(R.id.btnBack)?.setOnClickListener { finish() }

        rv = findViewById(R.id.rvRetrabajos)
        rv.layoutManager = LinearLayoutManager(this)
        cargarLista()
    }

    override fun onResume() {
        super.onResume()
        cargarLista()
    }

    private fun cargarLista() {
        Thread {
            try {
                val conn = URL(urlApi).openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.outputStream.use { it.write("accion=obtener_retrabajos".toByteArray()) }

                val resp = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(resp)

                runOnUiThread {
                    if (json.optBoolean("exito", false)) {
                        val datosArray = json.getJSONArray("datos")
                        if (datosArray.length() > 0) {
                            rv.adapter = RetrabajosAdapter(datosArray)
                        } else {
                            rv.adapter = null
                            Toast.makeText(this, "No hay piezas en Retrabajo", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Error: " + json.optString("error"), Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show() }
            }
        }.start()
    }
}