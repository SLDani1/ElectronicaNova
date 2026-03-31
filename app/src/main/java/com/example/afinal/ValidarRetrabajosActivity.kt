package com.example.afinal

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

class ValidarRetrabajosActivity : AppCompatActivity() {
    private val urlApi = "${ApiConfig.URL_BASE}/api_ingeniero.php"
    private lateinit var rv: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_validar_retrabajos)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

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
                OutputStreamWriter(conn.outputStream).use { it.write("accion=obtener_retrabajos") }

                val resp = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(resp)

                runOnUiThread {
                    if (json.getBoolean("exito")) {
                        rv.adapter = RetrabajosAdapter(json.getJSONArray("datos")) { id, estado ->
                            enviarDecision(id, estado)
                        }
                    } else {
                        Toast.makeText(this, "Sin retrabajos pendientes", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show() }
            }
        }.start()
    }

    private fun enviarDecision(id: String, estado: String) {
        Thread {
            try {
                val conn = URL(urlApi).openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                OutputStreamWriter(conn.outputStream).use {
                    it.write("accion=validar_retrabajo&id_retrabajo=$id&estado=$estado")
                }

                if (conn.responseCode == 200) {
                    runOnUiThread {
                        Toast.makeText(this, "Acción registrada: $estado", Toast.LENGTH_SHORT).show()
                        cargarLista()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this, "Error al enviar", Toast.LENGTH_SHORT).show() }
            }
        }.start()
    }
}