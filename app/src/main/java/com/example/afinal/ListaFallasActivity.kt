package com.example.afinal

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import kotlin.concurrent.thread

class ListaFallasActivity : AppCompatActivity() {

    private lateinit var rvFallas: RecyclerView
    private lateinit var adapter: FallaGerenteAdapter
    private val listaFallas = mutableListOf<JSONObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_historial)

        rvFallas = findViewById(R.id.rvFallasDia)
        rvFallas.layoutManager = LinearLayoutManager(this)

        adapter = FallaGerenteAdapter(listaFallas) { falla ->
            val intent = Intent(this, VistaFallaActivity::class.java).apply {
                putExtra("PRIORIDAD", falla.optString("prioridad"))
                putExtra("ESTATUS", falla.optString("estatus_falla"))
                putExtra("HORA", falla.optString("hora"))
                putExtra("DESCRIPCION", falla.optString("descripcion"))
                putExtra("IMAGEN", falla.optString("imagen"))
            }
            startActivity(intent)
        }

        rvFallas.adapter = adapter
        consultarFallas()
    }

    private fun consultarFallas() {
        thread {
            try {
                val respuesta = URL("${ApiConfig.URL_BASE}/obtener_fallas_gerente.php").readText()
                val array = JSONArray(respuesta)
                val nuevasFallas = mutableListOf<JSONObject>()
                for (i in 0 until array.length()) {
                    nuevasFallas.add(array.getJSONObject(i))
                }
                runOnUiThread {
                    listaFallas.clear()
                    listaFallas.addAll(nuevasFallas)
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Error al cargar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}